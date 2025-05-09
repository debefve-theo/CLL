package ui.login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.app_cll_livreur.R;

/**
 * Fragment that presents a login screen for entering the user's email.
 * <p>
 * Upon entering a valid email, transitions to {@link AuthFragment} to
 * handle PIN-based authentication.
 * </p>
 */
public class LoginFragment extends Fragment {

    /** Default empty constructor required for fragment instantiation. */
    public LoginFragment() {}

    /**
     * Inflates the login layout, initializes UI widgets, and sets up
     * the login button click handler.
     * <p>
     * Validates that the email field is not empty, then creates a new
     * {@link AuthFragment}, passing the entered email as an argument,
     * and replaces this fragment in the container.
     * </p>
     *
     * @param inflater           the LayoutInflater object to inflate views
     * @param container          the parent view that the fragment's UI should attach to
     * @param savedInstanceState if non-null, this fragment is being re-created
     * @return the root {@link View} for this fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize UI elements
        Button loginButton = view.findViewById(R.id.btn_login);
        EditText emailInput = view.findViewById(R.id.input_email);

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            // Validate email field
            if (email.isEmpty()) {
                emailInput.setError("Email requis");
                return;
            }

            // Create AuthFragment and pass the email as an argument
            Fragment newFragment = new AuthFragment();
            Bundle bundle = new Bundle();
            bundle.putString("email", email);
            newFragment.setArguments(bundle);

            // Replace this fragment with AuthFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}