package data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the response from the Amazi API for a package (Colis),
 * containing delivery status, preparation and delivery dates,
 * and address and courier information.
 */
public class ColisResponse {

    @SerializedName("dateLivraison")
    private String dateLivraison;

    @SerializedName("adresseRue")
    private String adresseRue;

    @SerializedName("codePostal")
    private String codePostal;

    @SerializedName("ville")
    private String ville;

    @SerializedName("pays")
    private String pays;

    @SerializedName("nomLivreur")
    private String nomLivreur;

    @SerializedName("etat")
    private int statut;

    @SerializedName("datePreparation")
    private String datePreparation;


    /**
     * Returns the delivery status of the package.
     *
     * @return a string representing the current delivery status
     */
    public int getStatut() {
        return statut;
    }

    /**
     * Returns the street address where the package will be delivered.
     *
     * @return the street address as a string
     */
    public String getAdresseRue() {
        return adresseRue;
    }

    /**
     * Returns the postal code for the delivery address.
     *
     * @return the postal code as a string
     */
    public String getCodePostal() {
        return codePostal;
    }

    /**
     * Returns the city of the delivery address.
     *
     * @return the city name as a string
     */
    public String getVille() {
        return ville;
    }


    /**
     * Returns the country of the delivery address.
     *
     * @return the country name as a string
     */
    public String getPays() {
        return pays;
    }

    /**
     * Returns the name of the courier assigned to the delivery.
     *
     * @return the courier's name as a string
     */
    public String getNomLivreur() {
        return nomLivreur;
    }
}
