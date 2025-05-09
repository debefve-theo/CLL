package data.controller;

import android.content.Context;
import android.util.Log;

import data.api.amazi.AmaziServices;
import data.api.maps.GoogleMapsServices;
import data.model.Colis;
import data.model.ColisResponse;
import utils.TokenHolder;


/**
 * Controller responsible for the retrieval and processing of
 * package (Colis) data using the Amazi API and Google Maps API.
 */
public class ColisController {

    private final Context context;
    private final AmaziServices livraisonServices;
    private final GoogleMapsServices googleMapsServices;

    public interface ColisCallback {
        void onColisReady(Colis colis);
        void onError(String message);
    }


    /**
     * Constructs a new ColisController.
     *
     * @param context           the Android context used for service initialization
     * @param googleMapsApiKey  the API key for Google Maps geocoding requests
     */
    public ColisController(Context context, String googleMapsApiKey) {
        this.context = context;
        this.livraisonServices = new AmaziServices(TokenHolder.getToken());
        this.googleMapsServices = new GoogleMapsServices(googleMapsApiKey);
    }

    /**
     * Processes a package based on its QR code ID by:
     * <ol>
     *     <li>Fetching package details from the Amazi API</li>
     *     <li>Building the full address string</li>
     *     <li>Geocoding the address via the Google Maps API</li>
     *     <li>Constructing a {@link Colis} object with the retrieved data</li>
     *     <li>Returning the result through the provided callback</li>
     * </ol>
     *
     * @param colisId  the identifier of the package obtained from the QR code
     * @param callback the {@link ColisCallback} to receive the processed Colis or an error
     */
    public void traiterColisDepuisQRCode(int colisId, ColisCallback callback) {
        Log.d("ColisController", "Appel API Amazi pour ID : " + colisId);

        livraisonServices.fetchColisParId(colisId, new AmaziServices.ColisCallback() {
            @Override
            public void onSuccess(ColisResponse response) {
                Log.d("ColisController", "Réponse ColisResponse reçue : " + response.getNomLivreur());

                String adresseComplete = response.getAdresseRue() + ", " +
                        response.getCodePostal() + " " + response.getVille() + ", " +
                        response.getPays();

                googleMapsServices.fetchCoordinates(adresseComplete, new GoogleMapsServices.CoordinateCallback() {
                    @Override
                    public void onCoordinatesReceived(double lat, double lng) {
                        Log.d("ColisController", "📍 Coordonnées : " + lat + ", " + lng);

                        Colis colis = new Colis(
                                response.getNomLivreur(),
                                adresseComplete,
                                colisId,
                                lat,
                                lng
                        );

                        Log.d("ColisController", "Colis prêt à être retourné au fragment : " + colis.getName());

                        callback.onColisReady(colis);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("ColisController", "Erreur géocoding : " + message);
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e("ColisController", "Erreur API Amazi : " + message);
                callback.onError(message);
            }
        });
    }
}
