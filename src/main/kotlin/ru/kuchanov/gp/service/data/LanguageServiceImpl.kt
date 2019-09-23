package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.data.GpLanguage
import ru.kuchanov.gp.repository.data.LanguageRepository

@Service
class LanguageServiceImpl @Autowired constructor(
    val languageRepository: LanguageRepository
) : LanguageService {

    override fun getOneById(langId: Long): GpLanguage? =
        languageRepository.findByIdOrNull(langId)

    override fun findAll(): List<GpLanguage> =
        languageRepository.findAll()

    override fun findByLangCode(langCode: String): GpLanguage? =
        languageRepository.findByLangCode(langCode)

    override fun save(language: GpLanguage): GpLanguage =
        languageRepository.save(language)
}
