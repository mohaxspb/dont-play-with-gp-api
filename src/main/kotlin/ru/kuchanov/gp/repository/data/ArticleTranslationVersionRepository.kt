package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.data.ArticleTranslationVersion


interface ArticleTranslationVersionRepository : JpaRepository<ArticleTranslationVersion, Long> {
    fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun deleteAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>
}
