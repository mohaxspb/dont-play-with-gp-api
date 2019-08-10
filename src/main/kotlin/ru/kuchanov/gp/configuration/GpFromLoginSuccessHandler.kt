package ru.kuchanov.gp.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.stereotype.Component
import ru.kuchanov.gp.GpConstants
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class GpFromLoginSuccessHandler : AuthenticationSuccessHandler {

    @Value("\${angular.port}")
    lateinit var angularServerPort: String

    @Value("\${angular.href}")
    lateinit var angularServerHref: String

    @Value("\${server.servlet.context-path}")
    lateinit var globalContextPath: String

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        println("formLogin successHandler: ${request.requestURL}")
        println("request.session: ${request.session.id}")
        request.session.attributeNames.toList().forEach {
            println("session.attribute $it: ${request.session.getAttribute(it)}")
        }
        request.headerNames.toList().forEach {
            println("request header $it: ${request.getHeaders(it).toList()}")
        }
        println("formLogin response: $response")
        println("formLogin response headerNames: ${response.headerNames.toList()}")
        response.headerNames.toList().forEach {
            println("response header $it: ${response.getHeaders(it).toList()}")
        }
        println("formLogin successHandler authentication: ${authentication.principal}")
        println("formLogin successHandler authentication: ${authentication.authorities.map { it.authority }}")
        println("formLogin successHandler authentication: ${authentication.credentials}")
        println("formLogin successHandler authentication: ${authentication.details}")

        DefaultRedirectStrategy()
            .sendRedirect(
                request,
                response,
                determineTargetUrl(request, response)
            )
    }

    private fun determineTargetUrl(request: HttpServletRequest, response: HttpServletResponse): String {
        val savedRequest = HttpSessionRequestCache().getRequest(request, response)

        val client = request.session.getAttribute("client") as? String
        val schemeAndDomain = "${request.scheme}://${request.serverName}"
        val defaultRedirectUrl = "$schemeAndDomain:$angularServerPort$angularServerHref"
        val userDetailsUrl =
            "$globalContextPath/${GpConstants.UsersEndpoint.PATH}/${GpConstants.UsersEndpoint.Method.ME}"
        val defaultRedirectUrlAngularLogin = "$schemeAndDomain:${request.serverPort}$userDetailsUrl"
        val redirectUrl = if (client == GpConstants.Client.ANGULAR.name.toLowerCase()) {
            defaultRedirectUrlAngularLogin
        } else {
            defaultRedirectUrl
        }

        println("savedRequest.redirectUrl: ${savedRequest?.redirectUrl}")
        println("redirectUrl: $redirectUrl")
        println("globalContextPath: $globalContextPath")

        return savedRequest?.redirectUrl ?: redirectUrl
    }
}
