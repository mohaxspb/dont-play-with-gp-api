package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.data.GpLanguage


interface LanguageRepository : JpaRepository<GpLanguage, Long> {
    fun findByLangCode(langCode: String): GpLanguage?
}
