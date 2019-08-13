package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.data.GpLanguage
import ru.kuchanov.gp.bean.data.LanguageAlreadyExistsError
import ru.kuchanov.gp.service.data.LanguageService

@RestController
@RequestMapping("/" + GpConstants.LanguageEndpoint.PATH + "/")
class LanguageController @Autowired constructor(
    val languageService: LanguageService
) {

    @GetMapping
    fun index() =
        "Language endpoint"

    @GetMapping(GpConstants.LanguageEndpoint.Method.ALL)
    fun getAll(): List<GpLanguage> =
        languageService.findAll()

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(GpConstants.LanguageEndpoint.Method.ADD)
    fun addLanguage(
        @RequestParam(value = "langCode") langCode: String,
        @RequestParam(value = "langName") langName: String,
        @RequestParam(value = "nativeName") nativeName: String
    ): GpLanguage {
        if (languageService.findByLangCode(langCode) != null) {
            throw LanguageAlreadyExistsError()
        }
        return languageService.save(
            GpLanguage(
                langCode = langCode,
                langName = langName,
                nativeName = nativeName
            )
        )
    }
}
