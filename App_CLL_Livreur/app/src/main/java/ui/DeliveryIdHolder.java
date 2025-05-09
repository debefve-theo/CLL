package ui;

import android.util.Log;

import data.api.amazi.AmaziServices;
import utils.TokenHolder;

public class DeliveryIdHolder {
    private static int deliveryId;

    public static void setDeliveryId() {
        Log.d("ColisVM", "🔔 setLivraisonId() appelé", new Throwable());
        AmaziServices service = new AmaziServices(TokenHolder.getToken());
        service.getLivraisonId(new AmaziServices.CallbackId() {
            @Override
            public void onSuccess(int id) {
                deliveryId = id;
                Log.d("Amazi-Response", "ID actuel = " + id);
            }

            @Override
            public void onError(String message) {
                Log.e("Amazi-Response", "Erreur : " + message);
            }
        });
    }

    public static int getDeliveryId() {
        return deliveryId;
    }

    public static void clearDeliveryId() {
        deliveryId = -1;
    }
}
