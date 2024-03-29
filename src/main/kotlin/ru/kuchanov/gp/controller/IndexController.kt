package ru.kuchanov.gp.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
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
    private val articleService: ArticleService,
    private val urlService: UrlService,
    private val passwordEncoder: PasswordEncoder,
) {

    @GetMapping(IndexEndpoint.Method.ROOT)
    fun index() = "Welcome to Don't play with Google Play API!"

    @GetMapping(IndexEndpoint.Method.TEST)
    fun test() = "Test method called!"

    @GetMapping("/encrypt")
    fun encrypt(@RequestParam(value = "target") target: String): String =
        passwordEncoder.encode(target)

    @GetMapping(IndexEndpoint.Method.ARTICLES_LIST)
    fun articlesList(@RequestParam(value = "startArticleId") startArticleId: Long): String {
        val articles = articleService.getPublishedArticlesAfterId(startArticleId)

        val articlesListAsStrings = articles.mapIndexed { index, articleDto ->
            """
                <p>
                ${index + 1}. ${articleDto.translations[0].title}<br/>
                ${articleDto.translations[0].shortDescription?.let { "$it<br/>" } ?: ""}
                ${urlService.createArticleLink(articleId = articleDto.id)}
                </p>
               """.trimIndent()
        }

        return """
            <p>Очередная подборка новостей по теме чата за последнее время:</p>
           
            
            ${articlesListAsStrings.joinToString(separator = "")}
        """.trimIndent()
    }
}
