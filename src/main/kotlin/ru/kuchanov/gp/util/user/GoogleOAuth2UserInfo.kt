package ru.kuchanov.gp.util.user

import ru.kuchanov.gp.GpConstants

class GoogleOAuth2UserInfo(
    override val attributes: Map<String, Any>,
    override val providerToken: String?
) : OAuth2UserInfo(attributes, providerToken) {

    override fun getId(): String =
        attributes["sub"] as String

    override fun getName(): String =
        attributes["name"] as String

    override fun getEmail(): String? =
        attributes["email"] as String?

    override fun getImageUrl(): String? =
        attributes["picture"] as? String

    override fun getProvider() =
        GpConstants.SocialProvider.GOOGLE
}
