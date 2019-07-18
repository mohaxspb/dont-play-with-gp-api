package ru.kuchanov.gp.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants

@RestController
@RequestMapping("/${GpConstants.Path.USERS}/")
class UsersController {

    @GetMapping("")
    fun index() = "Welcome to Don't play with Google Play API! Users endpoint"

    @GetMapping("test")
    fun test() = "Test method called!"
}