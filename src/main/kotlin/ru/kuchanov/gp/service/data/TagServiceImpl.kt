package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.ArticlesTags
import ru.kuchanov.gp.bean.data.Tag
import ru.kuchanov.gp.repository.data.ArticlesTagsRepository
import ru.kuchanov.gp.repository.data.TagRepository

@Service
class TagServiceImpl @Autowired constructor(
    val tagRepository: TagRepository,
    val articlesTagsRepository: ArticlesTagsRepository
) : TagService {

    override fun getOneById(id: Long): Tag? =
        tagRepository.findByIdOrNull(id)

    override fun findByTitle(title: String): Tag? =
        tagRepository.findByTitle(title)

    override fun findAll(): List<Tag> =
        tagRepository.findAll()

    override fun findAllForArticle(articleId: Long): List<Tag> {
        val tagIds = articlesTagsRepository.findAllByArticleId(articleId).map { it.tagId }
        return tagRepository.findAllById(tagIds)
    }

    override fun save(tag: Tag): Tag =
        tagRepository.save(tag)

    override fun saveTagsForArticle(tags: List<Tag>, articleId: Long, authorId: Long) {
        tags.forEach {
            if (articlesTagsRepository.findByArticleIdAndTagId(articleId, it.id!!) == null) {
                articlesTagsRepository.save(ArticlesTags(articleId = articleId, tagId = it.id!!, authorId = authorId))
            }
        }
    }

    override fun delete(tag: Tag): Boolean {
        tagRepository.delete(tag)
        return true
    }

    override fun deleteAllByArticleId(articleId: Long) =
        articlesTagsRepository.deleteAllByArticleId(articleId)
}
