package ru.kuchanov.gp.controller.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.model.dto.UserDto
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.auth.GpUserDetailsService

@RestController
@RequestMapping("/" + GpConstants.UsersEndpoint.PATH + "/")
class UsersController @Autowired constructor(
    val gpUserDetailsService: GpUserDetailsService
) {

    @GetMapping("")
    fun index() = "Welcome to Don't play with Google Play API! Users endpoint"

    @GetMapping("test")
    fun test() = "Test method called!"

    @GetMapping(GpConstants.UsersEndpoint.Method.ME)
    fun showMe(
        @AuthenticationPrincipal user: GpUser
    ): UserDto = gpUserDetailsService.getByIdAsDto(user.id!!)

    @DeleteMapping("delete/{id}")
    fun deleteUserById(
        @AuthenticationPrincipal user: GpUser,
        @PathVariable(value = "id") id: Long
    ): Boolean {
        if (user.isAdmin() || user.id == id) {
            return gpUserDetailsService.deleteById(id)
        } else {
            throw GpAccessDeniedException("You not admin and given ID is not your ID");
        }
    }
}
