package data.api.masiid;

import data.model.LoginRequest;
import data.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


/**
 * Retrofit interface for the Masi-ID authentication API.
 * Provides methods for user login and retrieval of authentication tokens.
 */
public interface IMasiIdAPI {
    /**
     * Sends user credentials to the login endpoint and returns authentication details.
     *
     * @param request a {@link LoginRequest} containing the user's login credentials
     * @return A {@link LoginResponse} with authentication tokens and user info
     */
    @POST("AMAZI/v1/mazi-id/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}