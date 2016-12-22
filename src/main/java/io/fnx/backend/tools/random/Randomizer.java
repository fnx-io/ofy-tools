package io.fnx.backend.tools.random;

/**
 * @author Jiri Zuna (jiri@zunovi.cz)
 */
public interface Randomizer {

    /**
     * Returns random string encoded by base64 without padding
     * @param length the length of the string to produce
     * @return random String of given length
     */
    String randomBase64(int length);

    /**
     * @return next random double precision number from range <0.0, 1.0)
     */
    double nextRand();
}
