package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.toDto
import ru.kuchanov.gp.bean.data.ArticleTranslationNotFoundException
import ru.kuchanov.gp.bean.data.ArticleTranslationVersion
import ru.kuchanov.gp.bean.data.ArticleTranslationVersionNotFoundException
import ru.kuchanov.gp.bean.data.toDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import ru.kuchanov.gp.repository.data.ArticleRepository
import ru.kuchanov.gp.repository.data.ArticleTranslationRepository
import ru.kuchanov.gp.repository.data.ArticleTranslationVersionRepository
import ru.kuchanov.gp.service.auth.GpUserDetailsService

@Service
class ArticleTranslationVersionServiceImpl @Autowired constructor(
    val articleRepository: ArticleRepository,
    val translationRepository: ArticleTranslationRepository,
    val userService: GpUserDetailsService,
    val articleTranslationVersionRepository: ArticleTranslationVersionRepository
) : ArticleTranslationVersionService {

    override fun getOneById(id: Long): ArticleTranslationVersion? =
        articleTranslationVersionRepository.findByIdOrNull(id)

    override fun getOneByIdAsDto(id: Long): ArticleTranslationVersionDto? =
        getOneById(id)?.toDto()?.withUsers()

    override fun getCreatedVersionsBetweenDates(
        startDate: String,
        endDate: String,
        excludedIds: List<Long>
    ): List<ArticleTranslationVersionDto> =
        articleTranslationVersionRepository
            .getCreatedVersionsBetweenDates(
                startDate,
                endDate,
                //as we are not allowed to pass empty list to JPA query
                excludedIds.ifEmpty { listOf(0) }
            )
            .map { it.toDto().withUsers() }

    override fun getPublishedByTranslationId(translationId: Long): ArticleTranslationVersion? =
        articleTranslationVersionRepository.findOneByArticleTranslationIdAndPublished(translationId)

    override fun findAllByArticleTranslationIdAsDto(articleTranslationId: Long): List<ArticleTranslationVersionDto> =
        articleTranslationVersionRepository
            .findAllByArticleTranslationId(articleTranslationId)
            .map { it.toDto().withUsers() }

    override fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion> =
        articleTranslationVersionRepository.findAllByArticleTranslationId(articleTranslationId)

    override fun findAllByAuthorId(authorId: Long): List<ArticleTranslationVersion> =
        articleTranslationVersionRepository.findAllByAuthorId(authorId)

    override fun countOfVersionsByVersionId(versionId: Long): Int =
        articleTranslationVersionRepository.countVersionsByVersionId(versionId)

    override fun countByArticleId(articleId: Long): Int =
        articleTranslationVersionRepository.countByArticleId(articleId)

    override fun isUserIsAuthorOfVersionOrTranslationOrArticleByVersionId(versionId: Long, userId: Long): Boolean {
        return if (articleTranslationVersionRepository.existsByIdAndAuthorId(userId, versionId)) {
            true
        } else {
            val translationId = articleTranslationVersionRepository.getTranslationIdById(versionId)
                ?: throw ArticleTranslationVersionNotFoundException()
            if (translationRepository.existsByIdAndAuthorId(translationId, userId)) {
                true
            } else {
                val articleId =
                    translationRepository.getArticleIdById(translationId) ?: throw ArticleTranslationNotFoundException()
                articleRepository.existsByIdAndAuthorId(articleId, userId)
            }
        }
    }

    override fun save(articleTranslationVersion: ArticleTranslationVersion): ArticleTranslationVersion =
        articleTranslationVersionRepository.save(articleTranslationVersion)

    override fun deleteById(id: Long): Boolean {
        articleTranslationVersionRepository.deleteById(id)
        return true
    }

    override fun deleteAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion> =
        articleTranslationVersionRepository.deleteAllByArticleTranslationId(articleTranslationId)

    fun ArticleTranslationVersionDto.withUsers() =
        apply {
            author = authorId?.let { userService.getById(it)?.toDto() }
            approver = approverId?.let { userService.getById(it)?.toDto() }
            publisher = publisherId?.let { userService.getById(it)?.toDto() }
        }
}
