package ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.app_cll_livreur.R;

import data.api.masiid.MasiIdServices;
import data.model.LoginResponse;
import ui.main.MainActivity;
import utils.TokenHolder;

/**
 * Fragment responsible for authenticating the user via a 4-digit PIN.
 * <p>
 * Expects an email argument to be provided via bundle under key "email".
 * Displays an input field for the PIN and a button to submit.
 * On successful authentication, saves the returned token and navigates to MainActivity.
 * </p>
 */
public class AuthFragment extends Fragment {

    /** Default empty constructor required for fragment instantiation. */
    public AuthFragment() {}

    /**
     * Inflates the authentication layout, initializes UI widgets,
     * and sets up the authentication button click handler.
     * <p>
     * Validates that the PIN is exactly 4 digits and that an email argument
     * was provided, then calls {@link MasiIdServices#login(String, String, MasiIdServices.LoginCallback)}.
     * On success with status "OK", stores the token in {@link TokenHolder} and
     * launches {@link MainActivity}, finishing the current Activity.
     * On failure or error, displays an error on the PIN input field.
     * </p>
     *
     * @param inflater           the LayoutInflater object to inflate views
     * @param container          the parent view for the fragment's UI (may be null)
     * @param savedInstanceState if non-null, this fragment is being re-created
     * @return the root View of the inflated authentication layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);

        EditText inputCode = view.findViewById(R.id.input_auth);
        Button authButton = view.findViewById(R.id.btn_auth);

        authButton.setOnClickListener(v -> {
            String pin = inputCode.getText().toString().trim();
            String email = getArguments() != null ? getArguments().getString("email") : null;

            // Validate PIN format
            if (pin.length() != 4) {
                inputCode.setError("Code à 4 chiffres requis");
                return;
            }

            // Ensure email argument is present
            if (email == null || email.isEmpty()) {
                Log.e("AuthFragment", "Email manquant");
                return;
            }

            // Perform login via MasiIdServices
            new MasiIdServices().login(email, pin, new MasiIdServices.LoginCallback() {
                @Override
                public void onSuccess(LoginResponse response) {
                    if ("OK".equals(response.getStatus())) {
                        // Store token and navigate to main screen
                        TokenHolder.setToken(response.getToken());
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        // Invalid PIN
                        inputCode.setError("Code invalide");
                    }
                }

                @Override
                public void onError(String message) {
                    inputCode.setError("Erreur : " + message);
                    Log.e("AuthFragment", "Erreur : " + message);
                }
            });
        });


        return view;
    }
}