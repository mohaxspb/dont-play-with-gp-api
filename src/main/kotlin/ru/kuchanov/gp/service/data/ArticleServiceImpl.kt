package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.Article
import ru.kuchanov.gp.bean.data.toDto
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.repository.data.ArticleRepository
import ru.kuchanov.gp.service.auth.GpUserDetailsService

@Service
class ArticleServiceImpl @Autowired constructor(
    val articleRepository: ArticleRepository,
    val articleTranslationService: ArticleTranslationService,
    val userService: GpUserDetailsService
) : ArticleService {

    override fun getOneById(id: Long): Article? =
        articleRepository.findByIdOrNull(id)

    override fun getOneByIdAsDtoWithTranslationsAndVersions(id: Long): ArticleDto? =
        getOneById(id)?.toDto()?.withTranslations()?.withUsers()

    override fun findAllByAuthorId(authorId: Long): List<Article> =
        articleRepository.findAllByAuthorId(authorId)

    override fun save(article: Article): Article =
        articleRepository.save(article)

    override fun deleteById(id: Long): Boolean {
        //delete all dependent objects
        articleTranslationService.deleteAllByArticleId(id)
        articleRepository.deleteById(id)
        return true
    }

    fun ArticleDto.withTranslations() =
        apply {
            translations = articleTranslationService.findAllByArticleIdAsDtoWithVersions(id)
        }

    fun ArticleDto.withUsers() =
        apply {
            author = authorId?.let { userService.getByIdAsDto(it) }
            approver = approverId?.let { userService.getByIdAsDto(it) }
            publisher = publisherId?.let { userService.getByIdAsDto(it) }
        }
}
