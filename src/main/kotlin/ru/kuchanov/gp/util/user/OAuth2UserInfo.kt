package ru.kuchanov.gp.util.user

import ru.kuchanov.gp.GpConstants

abstract class OAuth2UserInfo(
    open val attributes: Map<String, Any>,
    open val providerToken: String?
) {

    abstract fun getId(): String

    abstract fun getName(): String

    abstract fun getEmail(): String

    abstract fun getImageUrl(): String?

    abstract fun getProvider(): GpConstants.SocialProvider
}