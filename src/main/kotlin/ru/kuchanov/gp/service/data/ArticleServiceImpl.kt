package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.toDto
import ru.kuchanov.gp.bean.data.Article
import ru.kuchanov.gp.bean.data.toDto
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.repository.data.ArticleRepository
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import java.sql.Timestamp
import java.util.*

@Service
class ArticleServiceImpl @Autowired constructor(
    val articleRepository: ArticleRepository,
    val articleTranslationService: ArticleTranslationService,
    val userService: GpUserDetailsService,
    val tagService: TagService,
    val commentService: CommentService
) : ArticleService {

    override fun getOneById(id: Long): Article? =
        articleRepository.findByIdOrNull(id)

    override fun getOneByIdAsDtoWithTranslationsAndVersions(id: Long): ArticleDto? =
        getOneById(id)?.toDto()?.withTranslations()?.withUsers()?.withTags()?.withCommentsCount()

    override fun findAllByIdsAsDtoWithTranslations(ids: List<Long>): List<ArticleDto> =
        articleRepository.findAllById(ids).map { it.toDto().withTranslations() }

    override fun findAllByAuthorId(authorId: Long): List<Article> =
        articleRepository.findAllByAuthorId(authorId)

    override fun findAllByAuthorIdWithTranslationsAsDto(authorId: Long, published: Boolean): List<ArticleDto> =
        articleRepository
            .findAllContentAuthorId(authorId)
            .filter { if (published) it.published else true }
            .map { it.toDto().withUsers().withTranslations(false).withTags() }

    override fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean =
        articleRepository.existsByIdAndAuthorId(id, authorId)

    override fun getPublishedArticles(
        offset: Int,
        limit: Int,
        published: Boolean,
        approved: Boolean,
        withTranslations: Boolean,
        withVersions: Boolean,
        onlyForCurrentDate: Boolean
    ): List<ArticleDto> =
        articleRepository.getPublishedArticles(
            offset,
            limit,
            published,
            approved,
            if (onlyForCurrentDate)
                Timestamp(System.currentTimeMillis()).toString()
            else
                Timestamp(Calendar.getInstance().apply { add(Calendar.YEAR, 100) }.timeInMillis).toString()
        )
            .map {
                if (withTranslations) {
                    it.toDto()
                        .withTranslations(withVersions)
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
                    it.toDto()
                }
                    .withTags()
                    .withUsers()
                    .withCommentsCount()
            }

    override fun getPublishedArticlesCount(
        published: Boolean,
        approved: Boolean,
        onlyForCurrentDate: Boolean
    ): Int =
        articleRepository.getPublishedArticlesCount(
            published,
            approved,
            if (onlyForCurrentDate)
                Timestamp(System.currentTimeMillis()).toString()
            else
                Timestamp(Calendar.getInstance().apply { add(Calendar.YEAR, 100) }.timeInMillis).toString()
        )

    override fun getCreatedArticlesBetweenDates(startDate: String, endDate: String): List<ArticleDto> =
        articleRepository.getCreatedArticlesBetweenDates(startDate, endDate)
            .map { it.toDto().withTranslations().withUsers() }

    override fun save(article: Article): Article =
        articleRepository.save(article)

    override fun deleteById(id: Long): Boolean {
        //delete all dependent objects
        articleTranslationService.deleteAllByArticleId(id)
        tagService.deleteAllByArticleId(id)
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

    fun ArticleDto.withTags() =
        apply {
            tags = tagService.findAllForArticle(id)
        }

    fun ArticleDto.withCommentsCount() =
        apply {
            commentsCount = commentService.countCommentsForArticle(id)
        }

    fun ArticleDto.withUsers() =
        apply {
            author = authorId?.let { userService.getById(it)?.toDto() }
            approver = approverId?.let { userService.getById(it)?.toDto() }
            publisher = publisherId?.let { userService.getById(it)?.toDto() }
        }
}
