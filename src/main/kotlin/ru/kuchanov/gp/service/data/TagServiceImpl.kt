package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.Tag
import ru.kuchanov.gp.repository.data.TagRepository

@Service
class TagServiceImpl @Autowired constructor(val tagRepository: TagRepository) : TagService {

    override fun getOneById(id: Long): Tag? =
        tagRepository.findByIdOrNull(id)

    override fun findByTitle(title: String): Tag? =
        tagRepository.findByTitle(title)

    override fun findAll(): List<Tag> =
        tagRepository.findAll()

    override fun save(tag: Tag): Tag =
        tagRepository.save(tag)

    override fun delete(tag: Tag): Boolean {
        tagRepository.delete(tag)
        return true
    }
}
