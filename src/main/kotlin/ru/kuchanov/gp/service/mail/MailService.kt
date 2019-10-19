package ru.kuchanov.gp.service.mail

import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto

interface MailService {

    fun sendMail(vararg to: String, subj: String, text: String)

    fun sendArticleApprovedMail(article: ArticleDto)

    fun sendArticlePublishedMail(article: ArticleDto)

    fun sendTranslationApprovedMail(translation: ArticleTranslationDto)

    fun sendTranslationPublishedMail(translation: ArticleTranslationDto)

    fun sendVersionApprovedMail(version: ArticleTranslationVersionDto)

    fun sendVersionPublishedMail(version: ArticleTranslationVersionDto)

    fun sendArticleCreatedMail(createdArticle: ArticleDto)

    fun sendTranslationCreatedMail(createdTranslation: ArticleTranslationDto)

    fun sendVersionCreatedMail(createdVersion: ArticleTranslationVersionDto)

    fun sendStatisticsEmail()

    fun sendRegistrationEmail(email: String, password: String)
}