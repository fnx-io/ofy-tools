# ofy-tools

## Maven

 - Add dependency:
```
<dependency>
    <groupId>io.fnx.backend</groupId>
    <artifactId>tools</artifactId>
    <version>x.y.z</version>
</dependency>
```
 - Add custom repositories:

```
<repositories>
    <repository>
        <id>fnx-snapshots</id>
        <url>https://fnx-maven-repository.storage.googleapis.com/snapshots</url>
    </repository>
    <repository>
        <id>fnx-releases</id>
        <url>https://fnx-maven-repository.storage.googleapis.com/releases</url>
    </repository>
</repositories>
```

## Howto

TODO

### Unique constraint

Datastore supports transactions and we can use those to manage poor man's unique constraints for properties of our entities (eg. unique `UserEntity.email` property).

The class which manages your unique values is called [UniqueIndexManager](src/main/java/io/fnx/backend/manager/UniqueIndexManager.java).

Example of asserting and claiming unique value:
```
final Key<UserEntity> userKey = ofy().factory().allocateId(UserEntity.class);
final UserEntity registered = ofy().transact(new Work<UserEntity>() {
  @Override
  public UserEntity run() {
    user.setId(userKey.getId());
    try {
      uniqueIndexManager.saveUniqueIndexOwner(UniqueIndexProperty.user_email, email, userKey);
    } catch (UniqueViolationException e) {
      log.info("User with email {} is already registered", email);
      throw e;
    }

    ofy().save().entity(user).now();
    return user;
  }
});
```

The call to `uniqueIndexManager.saveUniqueIndexOwner(UniqueIndexProperty.user_email, email, userKey)` will fail if the value is already *owned* by entity with different key.

Our unique value can be *dropped* by calling  `uniqueIndexManager.deleteUniqueIndexOwner(UniqueIndexProperty.user_email, email);`

### Authentication

Existing users can login via our API and will get back an *authentication token*. This token is expected to be sent with each request in the `Authorization` header. Server can then use this header and verify that the token is valid. Authorization rules might then use the information about the owner of the token to either allow or deny access to protected resources.

From implementation point of view, the token is 22 base64 characters long (132 bits) random (securely generated) string. Each token has an expiration window (21 days) after which it will expire. If it is used before it expired, its expiration time will be reset. The backing entity is [AuthTokenEntity](src/main/java/io/fnx/backend/domain/AuthTokenEntity.java) and is datastore friendly - loading it is small OP (load via key).

`gae-tools` does not prescribe how a user entity should look, but it needs the user to implement simple `Principal` interface and the user should have a notion of some kind of role.

#### Validating authentication tokens

To exchange authentication token for its owner, we provide [AuthTokenManager](src/main/java/io/fnx/backend/auth/AuthTokenManager.java). Applications should use this manager as early in the request's lifecycle as possible (preferably in a servlet filter):

```
public class JerseyAuthFilter implements ContainerRequestFilter {

    // CallContext is @RequestScoped Principal holder
    @Inject
    private Provider<CallContext> callContextProvider;

    @Inject
    private AuthTokenManager<UserEntity> authTokenManager;

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        String token = request.getHeaderValue(HttpHeaders.AUTHORIZATION);
        final UserEntity user = authTokenManager.useToken(token);
        callContextProvider.get().setLoggedUser(user);

        return request;
    }
}
```

### Permissions / Authorization

We provide simple and pluggable mechanism for authorization. The basic idea is that we have an interceptor which validates access to protected resources. The interceptor is called [AuthorizationInterceptor](src/main/java/io/fnx/backend/tools/authorization/AuthorizationInterceptor.java). We usually guard our `Service` instances and use something like this to configure guice injector to intercept them:

```
bindInterceptor(Matchers.annotatedWith(Service.class), Matchers.any(), fnxAuthorizationInterceptor);
```

`AuthorizationInterceptor` doesn't authorize calls on its own, but delegates to [AuthorizationGuards](src/main/java/io/fnx/backend/tools/authorization/AuthorizationGuard.java). Each guard should declare annotations it is interested in and then implement the validation for the call. Lets see how we implemented simple `AuthorizationGuard` which requires that the request is made by logged in user:

The `getAnnotationClasses` method returns list of single annotation `@AllowedForAuthenticated`, meaning that every service method with this annotation might be checked by this guard:

```
@Service
class ProjectServiceImpl implements ProjectService {

  @AllowedForAuthenticated
  public List<Project> listProjects() {...}

}
```

Each guard can be either definitive or not. All definitive guards must succeed for the invocation of the guarded method to proceed. If any of the definitive guards for given method fails, the invocation will be aborted.

On the other hand, if method is annotated by annotations for all non-definitive guards, one successful check will suffice for the invocation to proceed.

Lets say that we have service method annotated with all non-definitive guarded annotations:

```
@AllowedOnMonday
@AllowedOnFriday
public String greet() {...}
```
If all guards are non-definitive, the invocation will be successful on Mondays and on Fridays.

If `MondayAuthorizationGuard` was definitive, the invocation could proceed only on Mondays.

And finally this is what the implementation of the guarding rule might look. We just check that the user is present and has some meaningful role.

```
    @Override
    public AuthorizationResult guardInvocation(final MethodInvocation invocation,
                                                    final Annotation annotation,
                                                    final PrincipalRole callingRole,
                                                    final Key<? extends Principal> callingPrincipal) {
        if (callingPrincipal == null || callingRole == null || callingRole.isAnonymous()) {
            return AuthorizationResult.failure("Must authenticate.");
        } else {
            return AuthorizationResult.SUCCESS;
        }

    }
```

We have implemented guards for these annotations:

 - `@AllowedForAdmins`
 - `@AllowedForAuthenticated`
 - `@AllowedForOwner` - this one is trickier since the guard will scan method's parameters to find those annotated with:
   - `@OwnedEntity` - will succeed if `ownedEntity.getOwnerKey()` returns the key of current user
   - `@IdHasOwner` - will succeed if the id used as a key would load an entity which is returns owner key same as current user. Example of usage:
   ```
      @AllowedForOwner
      public void updateProject(@IdHasOwner(ProjectEntity.class) Long prjId, ProjectChange change) {...}
   ```
   - `@KeyHasOwner` - same as `@IdHasOwner` but we have key instead of only `id`
 - `@AllowedForRole` - this has only partial implementation is needed to be defined with concrete types for the enumeration used for user's roles. Example implementation:

  ```
    @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD})
    public @interface AllowedForRoles {

        Role[] value();
    }


    public class AllowedForRoleAuthorizationGuard extends AllowedForRolesAuthorizationGuard<AllowedForRoles> {

        public AllowedForRoleAuthorizationGuard() {
            super(AllowedForRoles.class);
        }

        @Override
        public Collection<PrincipalRole> getRoles(AllowedForRoles annotation) {
            if (annotation == null) return Lists.newArrayList();
            final ArrayList<PrincipalRole> ret = Lists.newArrayListWithCapacity(annotation.value().length);
            Collections.addAll(ret, annotation.value());
            return ret;
        }
    }

  ```

### Hydration

Hydration is process of bundling more information for a class than it previously had.
When writing APIs we want usually to bundle more information for clients to save them work. When you write HydrationRecipes for your entities, our hydrator will then fetch all entities requested by the recipe and set them appropriately. It batches entity keys and loads them at once, so the overhead is quiet small even if you are hydrating a lot of entities.

Lets say that client is calling our endpoint to list projects `/projects` and our entity looks like this:

```
@Entity
public class ProjectEntity {

  @Id
  Long id;
  String title;
  Ref<UserEntity> owner;
}
```
The client will receive list of projects and will have to fetch all the owners on its own. What we would like to do is bundle the user somewhat with the projects. For this we use hydration.

#### Hydration Context

A class which you implement and which holds useful information for your hydration process, simple example might be this:

```
public class MyHydrationContext extends HydrationContext {

    // concrete Principal holder type
    private final CallContext cc;

    public MyHydrationContext(CallContext ctx) {
        super(ctx.getLoggedUser());
        this.cc = ctx;
    }

    public Long getUserId() {
        if (principal == null) return null;
        return cc.getLoggedUserId();
    }

    public boolean canSeePrivateInfo(OwnedEntity<UserEntity> entity) {
        if (!isLogged()) {
            return false;
        } else if (cc.getLoggedUser().isAdmin()) {
            return true;
        } else {
            return cc.getLoggedUser().owns(entity);
        }
    }
}
```

Then you might want to export simplified interface for your particular context:

```
public interface MyHydrationRecipe<D> extends HydrationRecipe<D, MyHydrationContext> {
}
```

#### Hydration Recipe

Given a hydration context, it shold return collection of properties to hydrate.

Example:

```
public class ProjectHydrationRecipe implements MyHydrationRecipe<ProjectEntity> {
    private final ProjectEntity project;

    public ProjectHydrationRecipe(ProjectEntity project) {
        this.project = project;
    }

    @Override
    public ProjectEntity transformForApi(MyHydrationContext ctx) {
        if (project != null && !ctx.canSeePrivateInfo(project)) {
            project.clearPrivateInfo();
        }
        return project;
    }

    @Override
    public Collection<HydratedProperty<ProjectEntity, ?>> propsToHydrate(MyHydrationContext ctx) {
        final List<HydratedProperty<ProjectEntity, ?>> props = Lists.newArrayList();
        boolean canSeePrivateInfo = ctx.canSeePrivateInfo(project);
        props.add(new ProjectOwnerProperty(canSeePrivateInfo));
        return props;
    }
}
```

#### Hydrated property

First the hydrator will go through all hydration properties and call the `getKey` function. Then it will batch all keys and load them at once, and after that it will go and call the appropriate setter for each hydrated property, giving you the chance to set the resulting entity into your hydrated object.

```
public class ProjectOwnerProperty implements SingleValueHydratedProperty<ProjectEntity, UserEntity> {

    private final boolean canSeePrivateInfo;

    public ProjectOwnerProperty(boolean canSeePrivateInfo) {
        // we can use additional information when setting
        // the property later
        this.canSeePrivateInfo = canSeePrivateInfo;
    }

    // first hydrator will ask us to return the keys we want it to fetch
    @Override
    public Key<UserEntity> getKey(ProjectEntity project) {
        if (project == null) return null;
        return project.getOwnerKey();
    }

    // then it will give us corresponding entity
    @Override
    public void setProperty(ProjectEntity object, UserEntity entity) {
        if (object == null || entity == null) return;

        final UserDetailsResponse details;
        if (canSeePrivateInfo) {
         details = UserDetailsResponse.from(entity);
        } else {
          details = UserDetailsResponse.publicFrom(entity);
        }
        // and we set it
        object.setHydratedOwner(details);
    }
}
```

We use it often to enrich the resulting json with more information which might be useful for the clients

```
{'id': 1, 'owner': 12}

// becomes

{
  'id': 1,
  'owner': 12,
  '_owner': {
    'id': 1,
    'email': 'me@there.com',
    'name': 'Toutates'
  }
}
```

### Random

Often we need some kind of random values, for that we have simple interface [Randomizer](src/main/java/io/fnx/backend/tools/random/Randomizer.java) which has single implementation in `SecureRandomizer.java`. We use this to be able to provide predictable randomizer in our tests.

The most interesting function it has, is `String randomBase64(int length);` which will give you back URL safe base64 string of desired length. You can use it to generate random Entity names (text Ids) for example.

## Development and deployment
