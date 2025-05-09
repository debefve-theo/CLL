package data.api.amazi;

import data.model.ColisResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Retrofit interface for accessing the Amazi API.
 * Provides methods related to package delivery.
 */
public interface IAmaziAPI {

    /**
     * Retrieves information about a specific package by its ID.
     *
     * @param colisId   The unique identifier of the package to fetch.
     * @return A Retrofit Call object containing a ColisResponse if the request is successful.
     */
    @GET("AMAZI/V1/Livraison/colis-detail")
    Call<ColisResponse> getColisParId(@Query("colisId") int colisId);

    /**
     * Updates the delivery status for a specific package within a delivery.
     *
     * @param livraisonId   The identifier of the delivery containing the package
     * @param colisId       The identifier of the package whose status is to be updated
     * @param livreurId     The identifier of the courier performing the update
     * @param newStatus     The new status to apply to the package
     * @return A Call wrapping Void; successful HTTP response indicates the update was applied
     */
    @PUT("AMAZI/V1/Livraison/update-statut")
    Call<Void> updateStatutLivraison(
            @Query("livraisonId") int livraisonId,
            @Query("colisId") int colisId,
            @Query("livreurId") String livreurId,
            @Query("nouveauStatut") int newStatus // 0=Pending/1=Ongoing/2=Delivered/3=Absent
    );

    /**
     * Retrieves the identifier of the current active delivery.
     *
     * @return An Integer representing the current delivery ID
     */
    @GET("AMAZI/V1/Livraison/id")
    Call<Integer> getCurrentLivraisonId();

    /**
     * Supprime un colis de la livraison.
     * @param livraisonId l’ID de la livraison
     * @param colisId     l’ID du colis à supprimer
     * @return Call<Void> (200 = succès)
     */
    @DELETE("AMAZI/V1/Livraison/colis")
    Call<Void> deleteColis(
            @Query("livraisonId") int livraisonId,
            @Query("colisId")     int colisId
    );
}