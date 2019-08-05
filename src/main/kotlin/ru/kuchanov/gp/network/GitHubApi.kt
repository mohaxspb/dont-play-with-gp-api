package ru.kuchanov.gp.network

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Path
import ru.kuchanov.gp.model.facebook.FacebookSuccessResponse

interface GitHubApi {

    companion object {
        const val BASE_API_URL = "https://api.github.com/"
    }

    /**
     * OAuth application owners can revoke a single token for an OAuth application
     *
     * @see [https://developer.github.com/v3/oauth_authorizations/#revoke-a-grant-for-an-application]
     * @param accessToken You must use Basic Authentication for this method, where the username is the OAuth application client_id and the password is its client_secret.
     */
    @DELETE("applications/{clientId}/grants/{accessToken}")
    fun logout(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: String,
        @Path("accessToken") accessToken: String
    ): Call<FacebookSuccessResponse>
}
