package data.api.amazi;

import android.util.Log;

import androidx.annotation.NonNull;

import data.model.ColisResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A service class for the Amazi API.
 * Provides utilities to fetch package data by ID.
 */
public class AmaziServices {
    private static final String BASE_URL = "http://192.168.8.55:30080/";
    private final IAmaziAPI apiService;

    /**
     * Initializes the Amazi service with an authentication token.
     *
     * @param bearerToken   The bearer token used for authentication.
     */
    public AmaziServices(String bearerToken) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request requestWithAuth = original.newBuilder()
                            .header("Authorization", "Bearer " + bearerToken)
                            .build();
                    return chain.proceed(requestWithAuth);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(IAmaziAPI.class);
    }

    /**
     * Fetches a delivery package (colis) by its unique identifier.
     *
     * @param colisId   The ID of the package to fetch.
     * @param callback  The callback interface for handling success or failure.
     */
    public void fetchColisParId(int colisId, ColisCallback callback) {
        Log.d("Amazi-Request", "Récupération colis : colisId=" + colisId);
        Call<ColisResponse> call = apiService.getColisParId(colisId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ColisResponse> call, @NonNull Response<ColisResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatut() == 0) {
                    callback.onSuccess(response.body());
                } else if (!response.isSuccessful()) {
                    callback.onError("Erreur HTTP : " + response.code());
                } else if (response.body().getStatut() != 0) {
                    callback.onError("Colis déjà présent dans une autre tournée !");
                } else {
                    callback.onError("Réponse vide ou invalide");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ColisResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface ColisCallback {
        void onSuccess(ColisResponse colis);
        void onError(String message);
    }

    /**
     * Updates the status of a delivery for a given package.
     *
     * @param livraisonId   The ID of the delivery to update
     * @param colisId       The ID of the package concerned
     * @param livreurId     The ID of the courier (livreur) performing the update
     * @param nouveauStatut The new status to set for the delivery
     * @param callback      The callback interface for handling success or failure.
     */
    public void updateDeliveryStatus(int livraisonId, int colisId, String livreurId, int nouveauStatut, StatutCallback callback) {
        Log.d("Amazi-Request", "Mise à jour statut : livraisonId=" + livraisonId + ", statut=" + nouveauStatut);
        Call<Void> call = apiService.updateStatutLivraison(livraisonId, colisId, livreurId, nouveauStatut);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                    Log.d("Amazi", "Mise à jour statut : livraisonId=" + livraisonId + ", statut=" + nouveauStatut);
                } else {
                    callback.onError("Erreur HTTP : " + response.code());
                    Log.d("Amazi", "Mise à jour statut ratée : livraisonId=" + livraisonId + ", statut=" + nouveauStatut);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface StatutCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Retrieves the current delivery ID.
     *
     * @param callback The callback to receive the ID or handle an error
     */
    public void getLivraisonId(CallbackId callback) {
        Log.d("Amazi-Request", "Récupération ID");
        Call<Integer> call = apiService.getCurrentLivraisonId();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erreur HTTP : " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface CallbackId {
        void onSuccess(int id);
        void onError(String message);
    }

    /**
     * Supprime un colis de la livraison.
     *
     * @param livraisonId l’ID de la livraison
     * @param colisId     l’ID du colis à supprimer
     * @param callback    le callback à invoquer
     */
    public void deleteColis(int livraisonId, int colisId, DeleteCallback callback) {
        Log.d("Amazi-Request", "Suppression colis : livraisonId=" + livraisonId + ", colisId=" + colisId);
        Call<Void> call = apiService.deleteColis(livraisonId, colisId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                    Log.d("Amazi", "Colis supprimé avec succès");
                } else {
                    String err = "Erreur HTTP : " + response.code();
                    callback.onError(err);
                    Log.e("Amazi", err);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
                Log.e("Amazi", "Suppression échouée : " + t.getMessage());
            }
        });
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String message);
    }
}
