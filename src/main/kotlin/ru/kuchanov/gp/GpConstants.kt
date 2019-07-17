package ru.kuchanov.gp

object GpConstants {

    const val DEFAULT_FULL_NAME = "N/A"

    object Path {
        const val AUTH = "auth"
        const val USER = "user"
        const val TRANSLATIONS = "translations"
    }

    enum class SocialProvider {
        GOOGLE, FACEBOOK, VK
    }
}
