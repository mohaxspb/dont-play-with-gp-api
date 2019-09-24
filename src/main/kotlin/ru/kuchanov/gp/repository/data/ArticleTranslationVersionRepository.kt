package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.data.ArticleTranslationVersion
import javax.transaction.Transactional


interface ArticleTranslationVersionRepository : JpaRepository<ArticleTranslationVersion, Long> {
    fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun findOneByArticleTranslationIdAndPublished(
        articleTranslationId: Long,
        published: Boolean = true
    ): ArticleTranslationVersion?

    fun getTranslationIdById(versionId: Long): Long

    @Query(
        """
            select count(*) from article_translation_versions as ats 
            where ats.article_translation_id = (select article_translation_id from ats where id=:versionId)
        """,
        nativeQuery = true
    )
    fun countVersionsByVersionId(versionId: Long): Int

    fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean

    @Transactional
    fun deleteAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun findAllByAuthorId(authorId: Long): List<ArticleTranslationVersion>
}
