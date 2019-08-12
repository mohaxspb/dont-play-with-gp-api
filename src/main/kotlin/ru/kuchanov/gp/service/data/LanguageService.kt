package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.GpLanguage

interface LanguageService {

    fun findAll(): List<GpLanguage>

    fun findByLangCode(langCode: String): GpLanguage?

    fun save(language: GpLanguage): GpLanguage
}
