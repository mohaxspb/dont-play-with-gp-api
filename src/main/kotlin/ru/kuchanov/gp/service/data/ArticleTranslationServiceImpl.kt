package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.ArticleTranslation
import ru.kuchanov.gp.repository.data.ArticleTranslationRepository

@Service
class ArticleTranslationServiceImpl @Autowired constructor(
    val articleTranslationRepository: ArticleTranslationRepository
) : ArticleTranslationService {

    override fun findAllByArticleId(articleId: Long): List<ArticleTranslation> =
        articleTranslationRepository.findAllByArticleId(articleId)

    override fun save(articleTranslation: ArticleTranslation): ArticleTranslation =
        articleTranslationRepository.save(articleTranslation)
}
