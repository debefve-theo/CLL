package utils;

/**
 * Holder for storing and retrieving the authentication token.
 * <p>
 * Provides static methods to set, get, clear, and check the presence of
 * a bearer token for API authentication.
 * </p>
 */
public class TokenHolder {
    /** The bearer token used for authenticating API requests. */
    private static String token;

    /**
     * Stores the provided bearer token.
     *
     * @param bearerToken the authentication token to store
     */
    public static void setToken(String bearerToken) {
        token = bearerToken;
    }

    /**
     * Retrieves the currently stored bearer token.
     *
     * @return the stored authentication token, or {@code null} if none set
     */
    public static String getToken() {
        return token;
    }

    /**
     * Clears the stored bearer token.
     */
    public static void clearToken() {
        token = null;
    }

    /**
     * Checks whether a non-empty token is currently stored.
     *
     * @return {@code true} if a token is set and not empty; {@code false} otherwise
     */
    public static boolean hasToken() {
        return token != null && !token.isEmpty();
    }
}