package utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.function.Consumer;

/**
 * Utility class for fetching the user's current location.
 * <p>
 * Provides a method to request the last known location via the
 * {@link FusedLocationProviderClient}, handling permission checks
 * and dispatching results through callbacks.
 * </p>
 */
public class LocationUtils {

    /**
     * Fetches the user's last known location.
     * <p>
     * If the {@link android.Manifest.permission#ACCESS_FINE_LOCATION} permission
     * is not granted, this method will request it and return immediately.
     * Otherwise, it attempts to retrieve the last known location:
     * <ul>
     *   <li>On success and non-null location, invokes {@code onSuccess} with
     *       a {@link LatLng} containing the latitude and longitude.</li>
     *   <li>If the location is null, invokes {@code onError} with an appropriate
     *       message.</li>
     *   <li>On failure, invokes {@code onError} with the exception message.</li>
     * </ul>
     *
     * @param fragment   the {@link Fragment} from which to derive the context and
     *                   request permissions if needed
     * @param provider   the {@link FusedLocationProviderClient} used to request
     *                   the last known location
     * @param onSuccess  callback to receive the {@link LatLng} on successful retrieval
     * @param onError    callback to receive an error message if retrieval fails
     */
    public static void fetchUserLocation(
            Fragment fragment,
            FusedLocationProviderClient provider,
            Consumer<LatLng> onSuccess,
            Consumer<String> onError
    ) {
        Context context = fragment.requireContext();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(fragment.requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        provider.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                onSuccess.accept(new LatLng(location.getLatitude(), location.getLongitude()));
                Log.d("CLL-LocationUtils", "User location: " + location.getLatitude() + "-" + location.getLongitude());

            } else {
                onError.accept("Location is null.");
                Log.e("CLL-LocationUtils", "Error fetching user location: Location is null.");
            }
        }).addOnFailureListener(e -> {
            String msg = "Location error : " + e.getMessage();
            onError.accept(msg);
            Log.e("CLL-LocationUtils", msg);
        });
    }
}

