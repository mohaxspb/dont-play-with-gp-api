package ru.kuchanov.gp.network

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.kuchanov.gp.model.facebook.DebugTokenResponse
import ru.kuchanov.gp.model.facebook.FacebookAccessToken
import ru.kuchanov.gp.model.facebook.FacebookProfileResponse
import ru.kuchanov.gp.model.facebook.FacebookSuccessResponse

interface FacebookApi {

    companion object {
        const val BASE_API_URL = "https://graph.facebook.com/v4.0/"
        const val GRANT_TYPE = "authorization_code"
    }

    /**
     * @see [https://developers.facebook.com/docs/accountkit/graphapi/]
     *
     * @param grantType use [GRANT_TYPE]
     * @param redirectUri use same as in frontEnd
     */
    @GET("oauth/access_token")
    fun accessTokenFromAuthCode(
        @Query("code") code: String,
        @Query("redirect_uri") redirectUri: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("grant_type") grantType: String
    ): Call<FacebookAccessToken>

    /**
     * @param inputToken token to debug
     * @param accessToken in form "APP_ID|APP_SECRET"
     */
    @GET("debug_token")
    fun debugToken(
        @Query("input_token") inputToken: String,
        @Query("access_token") accessToken: String
    ): Single<DebugTokenResponse>

    @GET("me?fields=email,name,first_name,middle_name,last_name,picture.width(500).height(500){url,height,width}")
    fun profile(@Query("access_token") accessToken: String): Call<FacebookProfileResponse>

    /**
     * @see [https://developers.facebook.com/docs/facebook-login/permissions/requesting-and-revoking#revokelogin]
     * @param accessToken in format "clientId|clientSecret"
     */
    @DELETE("{userId}/permissions")
    fun logout(
        @Path("userId") userId: String,
        @Query("access_token") accessToken: String
    ): Call<FacebookSuccessResponse>
}
