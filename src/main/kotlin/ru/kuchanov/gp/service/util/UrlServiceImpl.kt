package ru.kuchanov.gp.service.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.kuchanov.gp.GpConstants

@Service
class UrlServiceImpl @Autowired constructor(
    @Value("\${angular.port}") val angularServerPort: String,
    @Value("\${angular.href}") val angularServerHref: String,
    @Value("\${my.site.domain}") val domain: String
) : UrlService {

    override fun createArticleLink(articleId: Long, translationLangId: Long?): String {
        val serverAddress = "https://$domain$angularServerPort$angularServerHref#"
        val articlePage = GpConstants.ArticleEndpoint.PATH
        var link = "$serverAddress/$articlePage/$articleId"
        translationLangId?.let { link += "?langId=$it" }
        return link
    }
}