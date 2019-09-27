package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.data.Tag


interface TagRepository : JpaRepository<Tag, Long> {
    fun findByTitle(title: String): Tag?
}
