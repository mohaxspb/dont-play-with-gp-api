package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.ArticleTranslationVersion
import ru.kuchanov.gp.repository.data.ArticleTranslationVersionRepository

@Service
class ArticleTranslationVersionServiceImpl @Autowired constructor(
    val articleTranslationVersionRepository: ArticleTranslationVersionRepository
) : ArticleTranslationVersionService {

    override fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion> =
        articleTranslationVersionRepository.findAllByArticleTranslationId(articleTranslationId)

    override fun save(articleTranslationVersion: ArticleTranslationVersion): ArticleTranslationVersion =
        articleTranslationVersionRepository.save(articleTranslationVersion)
}
