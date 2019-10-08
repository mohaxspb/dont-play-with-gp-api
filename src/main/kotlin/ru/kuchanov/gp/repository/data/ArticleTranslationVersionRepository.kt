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

    @Query("select articleTranslationId from ArticleTranslationVersion where id=:versionId")
    fun getTranslationIdById(versionId: Long): Long?

    @Query(
        """
            select count(*) from article_translation_versions 
            where article_translation_id = (select article_translation_id from article_translation_versions where id=:versionId)
        """,
        nativeQuery = true
    )
    fun countVersionsByVersionId(versionId: Long): Int

    @Query(
        """
            select count(*) from article_translation_versions 
            where article_translation_id = (select id from article_translations where article_id=:articleId)
        """,
        nativeQuery = true
    )
    fun countByArticleId(articleId: Long): Int

    fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean

    @Transactional
    fun deleteAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun findAllByAuthorId(authorId: Long): List<ArticleTranslationVersion>
}
