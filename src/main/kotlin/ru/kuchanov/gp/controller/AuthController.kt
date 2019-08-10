package ru.kuchanov.gp.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.UserAlreadyExistsException
import ru.kuchanov.gp.bean.auth.UsersAuthorities
import ru.kuchanov.gp.service.auth.AuthService
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.auth.UsersAuthoritiesService

@RestController
@RequestMapping("/" + GpConstants.Path.AUTH + "/")
class AuthController @Autowired constructor(
    val passwordEncoder: PasswordEncoder,
    val authService: AuthService,
    val usersService: GpUserDetailsService,
    val usersAuthoritiesService: UsersAuthoritiesService
) {

    @PostMapping("register")
    fun register(
        @RequestParam(value = "email") email: String,
        @RequestParam(value = "password") password: String,
        @RequestParam(value = "fullName") fullName: String,
        @RequestParam(value = "avatarUrl") avatarUrl: String? = null,
        @RequestParam(value = "clientId") clientId: String
    ): OAuth2AccessToken? {
        //check if user already exists
        val userInDb = usersService.loadUserByUsername(email)
        if (userInDb != null) {
            throw UserAlreadyExistsException()
        }
        val newUserInDb = usersService.save(
            GpUser(
                username = email,
                password = passwordEncoder.encode(password),
                avatar = avatarUrl,
                userAuthorities = setOf(),
                fullName = fullName
            )
        )

        usersAuthoritiesService.insert(UsersAuthorities(userId = newUserInDb.id!!, authority = AuthorityType.USER))

        //todo send email with password
//        emailService.sendEmail(email, REGISTRATION_EMAIL_SUBJECT, "Your password is:\n$password")

        return authService.getAccessTokenForUsernameAndClientId(email, clientId)
    }
}
