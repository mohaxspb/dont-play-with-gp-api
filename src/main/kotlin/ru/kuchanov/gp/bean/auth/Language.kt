package ru.kuchanov.gp.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

/**
 * @see [https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes]
 */
@Entity
@Table(name = "languages")
data class Language(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    /**
     * ISO 639-1: two-letter codes
     */
    var langCode: String,
    /**
     * ISO language name
     */
    var langName: String,
    /**
     * Native name (endonym)
     */
    var nativeName: String,
    @field:CreationTimestamp
    val created: Timestamp? = null,
    @field:UpdateTimestamp
    val updated: Timestamp? = null
) : Serializable

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Language not found in db!")
class LanguageNotFoundError : RuntimeException()