package ru.kuchanov.gp.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.service.auth.GpUserDetailsService

@RestController
@RequestMapping("/" + GpConstants.Path.USERS + "/")
class UsersController {

    @Autowired
    lateinit var gpUserDetailsService: GpUserDetailsService

    @GetMapping("")
    fun index() = "Welcome to Don't play with Google Play API! Users endpoint"

    @GetMapping("test")
    fun test() = "Test method called!"

    @GetMapping("all")
    fun all() = gpUserDetailsService.findAll()
}
