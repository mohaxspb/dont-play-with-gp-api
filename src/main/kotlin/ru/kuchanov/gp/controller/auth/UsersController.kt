package ru.kuchanov.gp.controller.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.*
import ru.kuchanov.gp.bean.data.LanguageNotFoundError
import ru.kuchanov.gp.model.dto.UserDto
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.data.LanguageService

@RestController
@RequestMapping("/" + GpConstants.UsersEndpoint.PATH + "/")
class UsersController @Autowired constructor(
    val gpUserDetailsService: GpUserDetailsService,
    val languageService: LanguageService
) {

    @GetMapping("")
    fun index() = "Welcome to Don't play with Google Play API! Users endpoint"

    @GetMapping("test")
    fun test() = "Test method called!"

    @GetMapping(GpConstants.UsersEndpoint.Method.ME)
    fun showMe(
        @AuthenticationPrincipal user: GpUser
    ): UserDto = gpUserDetailsService.getByIdAsDto(user.id!!) ?: throw UserNotFoundException()

    @DeleteMapping(GpConstants.UsersEndpoint.Method.DELETE + "/{id}")
    fun deleteUserById(
        @AuthenticationPrincipal user: GpUser,
        @PathVariable(value = "id") id: Long
    ): Boolean {
        if (user.isAdmin() || user.id == id) {
            return gpUserDetailsService.deleteById(id)
        } else {
            throw GpAccessDeniedException("You not admin and given ID is not your ID")
        }
    }

    @PostMapping(GpConstants.UsersEndpoint.Method.UPDATE)
    fun update(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(value = "userId") userId: Long,
        @RequestParam(value = "name") name: String,
        @RequestParam(value = "langCode") langCode: String
    ): UserDto {
        val userToUpdate = gpUserDetailsService.getById(userId) ?: throw UserNotFoundException()
        val languageToSet = languageService.findByLangCode(langCode) ?: throw LanguageNotFoundError()
        if (name.isBlank()) {
            throw UserNameIsBlankException()
        }

        if (user.isAdmin() || user.id == userId) {
            gpUserDetailsService.update(
                userToUpdate.apply {
                    fullName = name
                    primaryLanguageId = languageToSet.id!!
                }
            )

            return gpUserDetailsService.getById(userId)!!.toDto(true)
        } else {
            throw GpAccessDeniedException("You are not admin or this user!")
        }
    }
}
