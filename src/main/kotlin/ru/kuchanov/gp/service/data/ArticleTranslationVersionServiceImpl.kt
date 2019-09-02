package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.ArticleTranslationVersion
import ru.kuchanov.gp.bean.data.toDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import ru.kuchanov.gp.repository.data.ArticleTranslationVersionRepository
import ru.kuchanov.gp.service.auth.GpUserDetailsService

@Service
class ArticleTranslationVersionServiceImpl @Autowired constructor(
    val articleTranslationVersionRepository: ArticleTranslationVersionRepository,
    val userService: GpUserDetailsService
) : ArticleTranslationVersionService {

    override fun getOneById(id: Long): ArticleTranslationVersion? =
        articleTranslationVersionRepository.findByIdOrNull(id)

    override fun getOneByIdAsDto(id: Long): ArticleTranslationVersionDto? =
        getOneById(id)?.toDto()?.withUsers()

    override fun findAllByArticleTranslationIdAsDto(articleTranslationId: Long): List<ArticleTranslationVersionDto> =
        articleTranslationVersionRepository
            .findAllByArticleTranslationId(articleTranslationId)
            .map { it.toDto().withUsers() }

    override fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion> =
        articleTranslationVersionRepository.findAllByArticleTranslationId(articleTranslationId)

    override fun save(articleTranslationVersion: ArticleTranslationVersion): ArticleTranslationVersion =
        articleTranslationVersionRepository.save(articleTranslationVersion)

    override fun deleteAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion> =
        articleTranslationVersionRepository.deleteAllByArticleTranslationId(articleTranslationId)

    fun ArticleTranslationVersionDto.withUsers() =
        apply {
            author = authorId?.let { userService.getByIdAsDto(it) }
            approver = approverId?.let { userService.getByIdAsDto(it) }
            publisher = publisherId?.let { userService.getByIdAsDto(it) }
        }
}
