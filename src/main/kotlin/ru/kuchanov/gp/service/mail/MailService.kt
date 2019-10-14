package ru.kuchanov.gp.service.mail

import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto

interface MailService {

    fun sendMail(vararg to: String, subj: String, text: String)

    fun sendArticleApprovedMail(articleId: Long)

    fun sendArticlePublishedMail(articleId: Long)

    fun sendTranslationApprovedMail(translationId: Long)

    fun sendTranslationPublishedMail(translationId: Long)

    fun sendVersionApprovedMail(versionId: Long)

    fun sendVersionPublishedMail(versionId: Long)

    fun sendArticleCreatedMail(createdArticle: ArticleDto)

    fun sendTranslationCreatedMail(createdTranslation: ArticleTranslationDto)

    fun sendVersionCreatedMail(createdVersion: ArticleTranslationVersionDto)
}