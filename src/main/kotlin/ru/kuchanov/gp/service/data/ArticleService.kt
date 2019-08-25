package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.Article

interface ArticleService {

    fun getOneById(id: Long): Article?

    fun findAllByAuthorId(authorId: Long): List<Article>

    fun save(article: Article): Article
}
