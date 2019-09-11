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

    override fun getPublishedArticles(
        offset: Int,
        limit: Int,
        published: Boolean,
        approved: Boolean,
        withTranslations: Boolean,
        withVersions: Boolean
    ): List<ArticleDto> =
        articleRepository.getPublishedArticles(offset, limit, published, approved)
            .map {
                if (withTranslations) {
                    it.toDto()
                        .withTranslations(withVersions)
                        .withUsers()
                        .apply {
                            translations = translations
                                .map { translation ->
                                    translation.apply {
                                        versions = versions
                                            .filter { version ->
                                                version.published == published
                                            }
                                            .filter { version ->
                                                version.approved == approved
                                            }
                                    }
                                }
                                .filter { translation ->
                                    translation.published == published
                                }
                                .filter { translation ->
                                    translation.approved == approved
                                }
                        }
                } else {
                    it.toDto().withUsers()
                }
            }

    override fun save(article: Article): Article =
        articleRepository.save(article)

    override fun deleteById(id: Long): Boolean {
        //delete all dependent objects
        articleTranslationService.deleteAllByArticleId(id)
        articleRepository.deleteById(id)
        return true
    }

    fun ArticleDto.withTranslations(withVersions: Boolean = true) =
        apply {
            translations = if (withVersions) {
                articleTranslationService.findAllByArticleIdAsDtoWithVersions(id)
            } else {
                articleTranslationService.findAllByArticleIdAsDto(id)
            }
        }

    fun ArticleDto.withUsers() =
        apply {
            author = authorId?.let { userService.getByIdAsDto(it) }
            approver = approverId?.let { userService.getByIdAsDto(it) }
            publisher = publisherId?.let { userService.getByIdAsDto(it) }
        }
}
