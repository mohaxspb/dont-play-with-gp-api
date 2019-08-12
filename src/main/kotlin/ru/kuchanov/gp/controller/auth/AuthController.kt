package ru.kuchanov.gp.controller.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.ClientRegistrationException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.GpConstants.DEFAULT_LANG_CODE
import ru.kuchanov.gp.bean.auth.*
import ru.kuchanov.gp.bean.data.LanguageNotFoundError
import ru.kuchanov.gp.service.auth.AuthService
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.auth.UsersAuthoritiesService
import ru.kuchanov.gp.service.data.LanguageService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/" + GpConstants.AuthEndpoint.PATH + "/")
class AuthController @Autowired constructor(
    val passwordEncoder: PasswordEncoder,
    val clientDetailsService: ClientDetailsService,
    val authService: AuthService,
    val usersService: GpUserDetailsService,
    val usersAuthoritiesService: UsersAuthoritiesService,
    val languageService: LanguageService
) {

    @PostMapping(GpConstants.AuthEndpoint.Method.REGISTER)
    fun register(
        @RequestParam(value = "email") email: String,
        @RequestParam(value = "password") password: String,
        @RequestParam(value = "fullName") fullName: String,
        @RequestParam(
            value = "primaryLanguage",
            defaultValue = DEFAULT_LANG_CODE
        ) primaryLanguage: String = DEFAULT_LANG_CODE,
        @RequestParam(value = "clientId") clientId: String,
        @RequestParam(value = "targetUrlParameter") targetUrlParameter: String?,
        httpServletResponse: HttpServletResponse,
        httpServletRequest: HttpServletRequest
    ): OAuth2AccessToken? {
        //check client existing
        try {
            clientDetailsService.loadClientByClientId(clientId)
        } catch (e: ClientRegistrationException) {
            throw ClientNotFoundError()
        }
        val language = languageService.findByLangCode(primaryLanguage) ?: throw LanguageNotFoundError()

        //check if user already exists
        val userInDb = usersService.loadUserByUsername(email)
        if (userInDb != null) {
            throw UserAlreadyExistsException()
        }
        val newUserInDb = usersService.save(
            GpUser(
                username = email,
                password = passwordEncoder.encode(password),
                fullName = fullName
            )
        )

        usersAuthoritiesService.save(UsersAuthorities(userId = newUserInDb.id!!, authority = AuthorityType.USER))

        //todo send email with password
//        emailService.sendEmail(email, REGISTRATION_EMAIL_SUBJECT, "Your password is:\n$password")

        val auth = authService.generateAuthenticationForUsernameAndClientId(email, clientId)
//        println("AuthController auth: $auth")
        return if (targetUrlParameter != null) {
            authService.authenticateRequest(auth, httpServletRequest)
            httpServletResponse.sendRedirect(targetUrlParameter)
            null
        } else {
            authService.getAccessTokenFromAuthentication(auth)
        }
    }
}
