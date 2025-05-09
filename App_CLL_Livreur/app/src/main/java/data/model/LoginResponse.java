package data.model;

/**
 * Represents the response returned by the authentication endpoint
 * after a login attempt. Contains an access token if successful,
 * as well as a status and optional message.
 */
public class LoginResponse {
    private String token;
    private String message;
    private String status;

    /**
     * Returns the authentication token issued by the server.
     * This token is used for authorizing AMAZI API requests.
     *
     * @return the access token string, or {@code null} if not provided
     */
    public String getToken() {
        return token;
    }


    /**
     * Returns a message from the login response.
     * Typically used to convey errors or informational text.
     *
     * @return the response message, or {@code null} if none
     */
    public String getMessage() {
        return message;
    }


    /**
     * Returns the status of the login attempt.
     *
     * @return the status string, or {@code null} if not provided
     */
    public String getStatus() {
        return status;
    }
}
