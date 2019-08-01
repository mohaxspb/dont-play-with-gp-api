package ru.kuchanov.gp.util.user

abstract class OAuth2UserInfo(
    open val attributes: Map<String, Any>
) {

    abstract fun getId(): String

    abstract fun getName(): String

    abstract fun getEmail(): String

    abstract fun getImageUrl(): String?
}