package ru.kuchanov.gp.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.*
import ru.kuchanov.gp.service.auth.AuthService
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.auth.UsersAuthoritiesService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/" + GpConstants.Path.AUTH + "/")
class AuthController @Autowired constructor(
    val passwordEncoder: PasswordEncoder,
    val clientDetailsService: ClientDetailsService,
    val authService: AuthService,
    val usersService: GpUserDetailsService,
    val usersAuthoritiesService: UsersAuthoritiesService
) {

    @PostMapping("register")
    fun register(
        @RequestParam(value = "email") email: String,
        @RequestParam(value = "password") password: String,
        @RequestParam(value = "fullName") fullName: String,
        //todo enum for language
        @RequestParam(value = "primaryLanguage", defaultValue = "EN") primaryLanguage: String = "EN",
        @RequestParam(value = "clientId") clientId: String,
        @RequestParam(value = "targetUrlParameter") targetUrlParameter: String?,
        httpServletResponse: HttpServletResponse,
        httpServletRequest: HttpServletRequest
    ): OAuth2AccessToken? {
        //check client existing
        try {
            clientDetailsService.loadClientByClientId(clientId)
        } catch (e: ClientNotFoundError) {
            throw e
        }
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

        usersAuthoritiesService.insert(UsersAuthorities(userId = newUserInDb.id!!, authority = AuthorityType.USER))

        //todo send email with password
//        emailService.sendEmail(email, REGISTRATION_EMAIL_SUBJECT, "Your password is:\n$password")

        val auth = authService.generateAuthenticationForUsernameAndClientId(email, clientId)
        return if (targetUrlParameter != null) {
            authService.authenticateRequest(auth, httpServletRequest)
            httpServletResponse.sendRedirect(targetUrlParameter)
            null
        } else{
            authService.getAccessTokenFromAuthentication(auth)
        }
    }
}
