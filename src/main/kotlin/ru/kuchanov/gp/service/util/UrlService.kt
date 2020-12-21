package ru.kuchanov.gp.service.util

interface UrlService {

    fun createArticleLink(articleId: Long, translationLangId: Long? = null): String
}