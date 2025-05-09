package data.api.masiid;

import android.util.Log;

import androidx.annotation.NonNull;

import data.api.UnsafeOkHttpClient;
import data.model.LoginRequest;
import data.model.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Service class for communicating with the Masi-ID authentication API.
 * Provides a method to log in and obtain authentication tokens.
 */
public class MasiIdServices {
    private static final String BASE_URL = "https://192.168.8.55:30444/";
    private final IMasiIdAPI api;

    /**
     * Constructs a new MasiIdServices instance, configured with an unsafe HTTP client
     * to allow communication over HTTPS with self-signed certificates.
     */
    public MasiIdServices() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.api = retrofit.create(IMasiIdAPI.class);
    }

    /**
     * Attempts to log in a user with the specified email and PIN.
     * On success, {@link LoginCallback#onSuccess(LoginResponse)} is invoked with the
     * authentication response. On failure, {@link LoginCallback#onError(String)} is invoked
     * with an error message.
     *
     * @param mail     the user's email address
     * @param pin      the user's PIN code
     * @param callback the callback to handle success or error results
     */
    public void login(String mail, String pin, LoginCallback callback) {
        LoginRequest request = new LoginRequest(mail, pin);
        Call<LoginResponse> call = api.login(request);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erreur HTTP : " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
                Log.e("MasiIdServices", "Échec de login", t);
            }
        });
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String message);
    }
}