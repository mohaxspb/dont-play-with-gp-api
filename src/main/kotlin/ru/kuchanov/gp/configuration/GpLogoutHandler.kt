package ru.kuchanov.gp.configuration

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
): LogoutHandler {

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

    override fun logout(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        //logout from providers
        val gpUser = authentication?.principal as? GpUser ?: return

        gpUser.facebookId?.let {
            val facebookLogoutResult =
                facebookApi
                    .logout(
                        it,
                        "$facebookClientId|$facebookClientSecret"
                    )
                    .execute()
                    .body()

            println("facebookLogoutResult: $facebookLogoutResult")
        }
        // nothing to do for vk
        gpUser.githubToken?.let {
            val authorization =
                "Basic " + String(Base64Utils.encode("$githubClientId:$githubClientSecret".toByteArray()))
            val githubLogoutResult =
                githubApi
                    .logout(
                        authorization,
                        githubClientId,
                        it
                    )
                    .execute()

            println("githubLogoutResult: $githubLogoutResult")
        }
        gpUser.googleToken?.let {
            googleApi
                .logout(it)
                .execute()
        }

        //also clear accessToken in DB
        userDetailsService.save(
            gpUser.apply {
                facebookToken = null
                vkToken = null
                googleToken = null
                githubToken = null
            })
    }
}