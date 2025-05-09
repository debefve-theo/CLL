package ui.delivery;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.app_cll_livreur.R;

import java.util.ArrayList;

import data.model.Colis;
import ui.ColisViewModel;

/**
 * Activity responsible for displaying the delivery interface.
 * <p>
 * Initialize the layout, sets up the {@link ColisViewModel},
 * ingests a list of {@link Colis} passed via Intent, and hosts the
 * {@link DeliveryFragment} in its container.
 * </p>
 */
public class DeliveryActivity extends AppCompatActivity {

    /**
     * ViewModel for storing and managing the list of Colis during this Activity's lifecycle.
     */
    ColisViewModel colisViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Set the layout for the delivery screen
        setContentView(R.layout.activity_delivery);

        // Obtain the ViewModel instance
        colisViewModel = new ViewModelProvider(this).get(ColisViewModel.class);
        // Retrieve the list of Colis passed via Intent extras
        ArrayList<Colis> colisList = getIntent().getParcelableArrayListExtra("colis_list");

        if (colisList != null) {
            for (Colis colis : colisList) {
                colisViewModel.addColis(colis);
            }
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DeliveryFragment())
                    .commit();
        }
    }
}