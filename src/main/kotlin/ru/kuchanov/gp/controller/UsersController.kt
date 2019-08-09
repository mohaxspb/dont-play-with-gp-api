package ru.kuchanov.gp.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.model.dto.UserDto
import ru.kuchanov.gp.service.auth.AuthService
import ru.kuchanov.gp.service.auth.GpUserDetailsService

@RestController
@RequestMapping("/" + GpConstants.Path.USERS + "/")
class UsersController {

    @Autowired
    lateinit var gpUserDetailsService: GpUserDetailsService

    @Autowired
    lateinit var authService: AuthService

    @GetMapping("")
    fun index() = "Welcome to Don't play with Google Play API! Users endpoint"

    @GetMapping("test")
    fun test() = "Test method called!"

    @GetMapping("testAccessToken")
    fun testAccessToken() =
        authService.getAccessTokenForUsernameAndClientId(
            "test@test.ru",
            "client_id"
        )

    @GetMapping("all")
    fun all() = gpUserDetailsService.findAll()

    @GetMapping("me")
    fun showMeClient(
        @AuthenticationPrincipal user: GpUser
    ): UserDto = gpUserDetailsService.getByIdDto(user.id!!)
}
