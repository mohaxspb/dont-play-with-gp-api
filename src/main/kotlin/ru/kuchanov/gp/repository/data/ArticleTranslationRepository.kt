package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.data.ArticleTranslation


interface ArticleTranslationRepository : JpaRepository<ArticleTranslation, Long> {
    fun findAllByArticleId(articleId: Long): List<ArticleTranslation>
}
