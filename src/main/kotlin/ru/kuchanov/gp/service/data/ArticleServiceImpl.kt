package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.Article
import ru.kuchanov.gp.repository.data.ArticleRepository

@Service
class ArticleServiceImpl @Autowired constructor(
    val articleRepository: ArticleRepository
) : ArticleService {

    override fun findAllByAuthorId(authorId: Long): List<Article> =
        articleRepository.findAllByAuthorId(authorId)

    override fun save(article: Article): Article =
        articleRepository.save(article)

    override fun getOneById(id: Long): Article? =
        articleRepository.getOne(id)
}
