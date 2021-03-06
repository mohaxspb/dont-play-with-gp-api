package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.toDto
import ru.kuchanov.gp.bean.data.ArticleTranslation
import ru.kuchanov.gp.bean.data.ArticleTranslationNotFoundException
import ru.kuchanov.gp.bean.data.toDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.repository.data.ArticleRepository
import ru.kuchanov.gp.repository.data.ArticleTranslationRepository
import ru.kuchanov.gp.service.auth.GpUserDetailsService

@Service
class ArticleTranslationServiceImpl @Autowired constructor(
    val articleRepository: ArticleRepository,
    val articleTranslationVersionService: ArticleTranslationVersionService,
    val userService: GpUserDetailsService,
    val articleTranslationRepository: ArticleTranslationRepository
) : ArticleTranslationService {

    override fun getOneById(id: Long): ArticleTranslation? =
        articleTranslationRepository.findByIdOrNull(id)

    override fun getOneByIdAsDtoWithVersions(id: Long): ArticleTranslationDto? =
        getOneById(id)?.toDto()?.withVersions()?.withUsers()

    override fun getCreatedTranslationsBetweenDates(
        startDate: String,
        endDate: String,
        excludedIds: List<Long>
    ): List<ArticleTranslationDto> =
        articleTranslationRepository
            .getCreatedTranslationsBetweenDates(
                startDate,
                endDate,
                //as we are not allowed to pass empty list to JPA query
                excludedIds.ifEmpty { listOf(0L) }
            )
            .map { it.toDto().withVersions().withUsers() }

    override fun findAllByArticleIdAsDtoWithVersions(articleId: Long): List<ArticleTranslationDto> =
        articleTranslationRepository.findAllByArticleId(articleId).map { it.toDto().withVersions().withUsers() }

    override fun findAllByArticleIdAsDto(articleId: Long): List<ArticleTranslationDto> =
        articleTranslationRepository.findAllByArticleId(articleId).map { it.toDto().withUsers() }

    override fun findAllByArticleId(articleId: Long): List<ArticleTranslation> =
        articleTranslationRepository.findAllByArticleId(articleId)

    override fun countOfTranslationsByTranslationId(translationId: Long): Int =
        articleTranslationRepository.countTranslationsByTranslationId(translationId)

    override fun countTranslationsByArticleId(articleId: Long): Int =
        articleTranslationRepository.countByArticleId(articleId)

    override fun isUserIsAuthorOfTranslationOrArticleByTranslationId(translationId: Long, userId: Long): Boolean {
        return if (articleTranslationRepository.existsByIdAndAuthorId(translationId, userId)) {
            true
        } else {
            val articleId = articleTranslationRepository.getArticleIdById(translationId)
                ?: throw ArticleTranslationNotFoundException()
            return articleRepository.existsByIdAndAuthorId(articleId, userId)
        }
    }

    override fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean =
        articleTranslationRepository.existsByIdAndAuthorId(id, authorId)

    override fun save(articleTranslation: ArticleTranslation): ArticleTranslation =
        articleTranslationRepository.save(articleTranslation)

    override fun deleteById(id: Long): Boolean {
        articleTranslationVersionService.deleteAllByArticleTranslationId(id)
        articleTranslationRepository.deleteById(id)
        return true
    }

    override fun deleteAllByArticleId(articleId: Long): Boolean {
        val translationsToDelete = findAllByArticleId(articleId)
        translationsToDelete.forEach {
            articleTranslationVersionService.deleteAllByArticleTranslationId(it.id!!)
        }
        articleTranslationRepository.deleteAll(translationsToDelete)
        return true
    }

    fun ArticleTranslationDto.withVersions() =
        apply {
            versions = articleTranslationVersionService.findAllByArticleTranslationIdAsDto(id)
        }

    fun ArticleTranslationDto.withUsers() =
        apply {
            author = authorId?.let { userService.getById(it)?.toDto() }
            approver = approverId?.let { userService.getById(it)?.toDto() }
            publisher = publisherId?.let { userService.getById(it)?.toDto() }
        }
}
