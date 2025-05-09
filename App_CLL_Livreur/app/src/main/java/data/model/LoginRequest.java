package data.model;


/**
 * Represents the informations sent to the MASI ID endpoint
 * when a user attempts to log in with their email and PIN.
 */
public class LoginRequest {
    private final String mail;
    private final String pin;

    /**
     * Creates a new {@code LoginRequest} with the specified email and PIN.
     *
     * @param mail the email address of the user
     * @param pin  the PIN code of the user
     */
    public LoginRequest(String mail, String pin) {
        this.mail = mail;
        this.pin = pin;
    }
}
