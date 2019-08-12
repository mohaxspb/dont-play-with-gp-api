package ru.kuchanov.gp.network

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query
import ru.kuchanov.gp.model.facebook.DefaultSuccessResponse

interface FacebookApi {

    companion object {
        const val BASE_API_URL = "https://graph.facebook.com/v4.0/"
    }

    /**
     * @see [https://developers.facebook.com/docs/facebook-login/permissions/requesting-and-revoking#revokelogin]
     * @param accessToken in format "clientId|clientSecret"
     */
    @DELETE("{userId}/permissions")
    fun logout(
        @Path("userId") userId: String,
        @Query("access_token") accessToken: String
    ): Call<DefaultSuccessResponse>
}
