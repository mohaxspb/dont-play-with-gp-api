package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.Article
import ru.kuchanov.gp.bean.data.toDto
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.repository.data.ArticleRepository

@Service
class ArticleServiceImpl @Autowired constructor(
    val articleRepository: ArticleRepository,
    val articleTranslationService: ArticleTranslationService
) : ArticleService {

    override fun getOneById(id: Long): Article? =
        articleRepository.getOne(id)

    override fun getOneByIdAsDtoWithTranslationsAndVersions(id: Long): ArticleDto? =
        articleRepository.getOne(id).toDto().withTranslations()

    override fun findAllByAuthorId(authorId: Long): List<Article> =
        articleRepository.findAllByAuthorId(authorId)

    override fun save(article: Article): Article =
        articleRepository.save(article)

    fun ArticleDto.withTranslations() =
        apply {
            translations = articleTranslationService.findAllByArticleIdAsDtoWithVersions(id)
    }
}
