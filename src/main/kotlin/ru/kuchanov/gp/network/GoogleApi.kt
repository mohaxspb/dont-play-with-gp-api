package ru.kuchanov.gp.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.kuchanov.gp.model.facebook.FacebookSuccessResponse

interface GoogleApi {

    companion object {
        const val BASE_API_URL = "https://accounts.google.com/"
    }

    /**
     * OAuth application owners can revoke a single token for an OAuth application
     *
     * @see [https://developer.github.com/v3/oauth_authorizations/#revoke-a-grant-for-an-application]
     * @param accessToken You must use Basic Authentication for this method, where the username is the OAuth application client_id and the password is its client_secret.
     */
    @GET("o/oauth2/revoke")
    fun logout(
        @Query("token") token: String
    ): Call<FacebookSuccessResponse>
}
