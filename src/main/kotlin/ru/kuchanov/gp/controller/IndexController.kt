package ru.kuchanov.gp.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class IndexController {

    @GetMapping("")
    fun index() = "Welcome to Don't play with Google Play API!"

    @GetMapping("test")
    fun test() = "Test method called!"
}
