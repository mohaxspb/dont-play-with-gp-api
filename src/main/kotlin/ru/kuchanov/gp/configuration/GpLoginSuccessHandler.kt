package ru.kuchanov.gp.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import ru.kuchanov.gp.GpConstants.TARGET_URL_PARAMETER
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class GpLoginSuccessHandler : SavedRequestAwareAuthenticationSuccessHandler() {

    @Value("\${angular.port}")
    lateinit var angularServerPort: String

    @Value("\${angular.href}")
    lateinit var angularServerHref: String

    /**
     * We want to redirect to angular after login via spring formLogin if we do not have saved request.
     */
    override fun determineTargetUrl(request: HttpServletRequest, response: HttpServletResponse): String {
        println("targetUrl: ${request.getParameter(TARGET_URL_PARAMETER)}")
        val targetUrl = super.determineTargetUrl(request, response)
        println("targetUrl: $targetUrl")
        if (targetUrl == "/") {
            val schemeAndDomain = "${request.scheme}://${request.serverName}"
            val defaultRedirectUrl = "$schemeAndDomain:$angularServerPort$angularServerHref"
            println("defaultRedirectUrl: $defaultRedirectUrl")
            return defaultRedirectUrl
        } else {
            return targetUrl
        }
    }
}
