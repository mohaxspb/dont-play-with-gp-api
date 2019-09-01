package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.data.Article


interface ArticleRepository : JpaRepository<Article, Long> {

    fun findAllByAuthorId(authorId: Long): List<Article>
}
