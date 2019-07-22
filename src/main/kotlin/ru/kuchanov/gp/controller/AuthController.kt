package ru.kuchanov.gp.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.UserAlreadyExistsException
import ru.kuchanov.gp.bean.auth.UsersAuthorities
import ru.kuchanov.gp.repository.auth.UserNotFoundException
import ru.kuchanov.gp.service.auth.GpClientDetailsService
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.auth.UsersAuthoritiesService
import java.io.Serializable
import java.util.*

@RestController
@RequestMapping("/" + GpConstants.Path.AUTH + "/")
class AuthController @Autowired constructor(
    val passwordEncoder: PasswordEncoder,
    val tokenServices: DefaultTokenServices,
    val gpClientDetailsService: GpClientDetailsService,
    val usersService: GpUserDetailsService,
    val usersAuthoritiesService: UsersAuthoritiesService
) {

    @PostMapping("register")
    fun register(
        @RequestParam(value = "email") email: String,
        @RequestParam(value = "password") password: String,
        @RequestParam(value = "fullName") fullName: String,
        @RequestParam(value = "avatarUrl") avatarUrl: String? = null,
        @RequestParam(value = "clientId") clientId: String,
        @RequestParam(value = "clientSecret") clientSecret: String
    ): OAuth2AccessToken {
        //check if user already exists
        val userInDb = usersService.loadUserByUsername(email)
        if (userInDb != null) {
            throw UserAlreadyExistsException()
        }
        val newUserInDb = usersService.insert(
            GpUser(
                username = email,
                password = passwordEncoder.encode(password),
                avatar = avatarUrl,
                userAuthorities = setOf(),
                fullName = fullName
            )
        )

        usersAuthoritiesService.insert(UsersAuthorities(userId = newUserInDb.id!!, authority = AuthorityType.USER))

        //todo
//        emailService.sendEmail(email, REGISTRATION_EMAIL_SUBJECT, "Your password is:\n$password")

        return getAccessToken(email, clientId)
    }

    fun getAccessToken(email: String, clientId: String): OAuth2AccessToken {
        val clientDetails: ClientDetails = gpClientDetailsService.loadClientByClientId(clientId)

        val requestParameters = mapOf<String, String>()
        val authorities: MutableCollection<GrantedAuthority> = clientDetails.authorities
        val approved = true
        val scope: MutableSet<String> = clientDetails.scope
        val resourceIds: MutableSet<String> = clientDetails.resourceIds
        val redirectUri = null
        val responseTypes = setOf("code")
        val extensionProperties = HashMap<String, Serializable>()

        val oAuth2Request = OAuth2Request(
            requestParameters,
            clientId,
            authorities,
            approved,
            scope,
            resourceIds,
            redirectUri,
            responseTypes,
            extensionProperties
        )

        val user = usersService.loadUserByUsername(email) ?: throw UserNotFoundException()
        val authenticationToken = UsernamePasswordAuthenticationToken(
            user,
            user.password,
            authorities
        )

        val auth = OAuth2Authentication(oAuth2Request, authenticationToken)

        return tokenServices.createAccessToken(auth)
    }
}
