package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.data.ArticleTranslationVersion
import javax.transaction.Transactional


interface ArticleTranslationVersionRepository : JpaRepository<ArticleTranslationVersion, Long> {
    fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun findOneByArticleTranslationIdAndPublished(
        articleTranslationId: Long,
        published: Boolean = true
    ): ArticleTranslationVersion?

    @Transactional
    fun deleteAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun findAllByAuthorId(authorId: Long): List<ArticleTranslationVersion>
}
