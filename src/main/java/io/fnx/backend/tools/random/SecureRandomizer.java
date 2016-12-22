package io.fnx.backend.tools.random;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import java.security.SecureRandom;

public class SecureRandomizer implements Randomizer {
    private SecureRandom random = new SecureRandom();

    @Override
    public String randomBase64(final int length) {
        Preconditions.checkArgument(length > 0, "Invalid length. Must be > 0, was " + length);
        final byte[] buf = new byte[length];
        random.nextBytes(buf);
        // base64 will make string bigger by approx 33%, so we need to substr to make the string requested length.
        return BaseEncoding.base64Url().omitPadding().encode(buf).substring(0, length);
    }

    @Override
    public double nextRand() {
        return random.nextDouble();
    }
}
