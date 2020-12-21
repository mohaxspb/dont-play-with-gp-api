package ru.kuchanov.gp.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants.IndexEndpoint
import ru.kuchanov.gp.service.data.ArticleService
import ru.kuchanov.gp.service.util.UrlService

@RestController
@RequestMapping(IndexEndpoint.PATH)
class IndexController @Autowired constructor(
    val articleService: ArticleService,
    val urlService: UrlService
) {

    @GetMapping(IndexEndpoint.Method.ROOT)
    fun index() = "Welcome to Don't play with Google Play API!"

    @GetMapping(IndexEndpoint.Method.TEST)
    fun test() = "Test method called!"
}
