package ru.kuchanov.gp.configuration

import com.sun.org.apache.xpath.internal.operations.Bool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.network.FacebookApi
import ru.kuchanov.gp.network.GitHubApi
import ru.kuchanov.gp.network.GoogleApi
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class GpLogoutHandler @Autowired constructor(
    val facebookApi: FacebookApi,
    val githubApi: GitHubApi,
    val googleApi: GoogleApi,
    val userDetailsService: GpUserDetailsService
) : LogoutHandler {

    //common
    @Value("\${auth.logout.socialLogout}")
    var logoutFromSocial: Boolean? = null

    //facebook
    @Value("\${spring.security.oauth2.client.registration.facebook.clientId}")
    private lateinit var facebookClientId: String
    @Value("\${spring.security.oauth2.client.registration.facebook.clientSecret}")
    private lateinit var facebookClientSecret: String

    //google
    @Value("\${spring.security.oauth2.client.registration.github.clientId}")
    private lateinit var githubClientId: String
    @Value("\${spring.security.oauth2.client.registration.github.clientSecret}")
    private lateinit var githubClientSecret: String

    override fun logout(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        //logout from providers
        val gpUser = authentication?.principal as? GpUser ?: return

        if (logoutFromSocial == true) {
            logoutFromFacebook(gpUser.facebookToken)
            logoutFromVk(gpUser.vkToken)
            logoutFromGithub(gpUser.githubToken)
            logoutFromGoogle(gpUser.googleToken)
        }

        clearTokensForUser(gpUser.id!!)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun logoutFromVk(vkToken: String?) {

    }

    private fun logoutFromGoogle(googleToken: String?) {
        if (googleToken != null) {
            googleApi
                .logout(googleToken)
                .execute()
        }
    }

    private fun logoutFromGithub(githubToken: String?) {
        if (githubToken != null) {
            val authorization =
                "Basic " + String(Base64Utils.encode("$githubClientId:$githubClientSecret".toByteArray()))
            val githubLogoutResult =
                githubApi
                    .logout(
                        authorization,
                        githubClientId,
                        githubToken
                    )
                    .execute()

            println("githubLogoutResult: $githubLogoutResult")
        }
    }

    private fun logoutFromFacebook(facebookToken: String?) {
        if (facebookToken != null) {
            val facebookLogoutResult =
                facebookApi
                    .logout(
                        facebookToken,
                        "$facebookClientId|$facebookClientSecret"
                    )
                    .execute()
                    .body()

            println("facebookLogoutResult: $facebookLogoutResult")
        }
    }

    /**
     * also clear accessToken in DB
     * user can be deleted already, so check it
     */
    private fun clearTokensForUser(userId: Long) {
        val userInDb = userDetailsService.getById(userId)
        if (userInDb != null) {
            println("GpLogoutHandler. Clearing user social tokens.")
            userDetailsService.save(
                userInDb.apply {
                    facebookToken = null
                    vkToken = null
                    googleToken = null
                    githubToken = null
                })
        }
    }
}