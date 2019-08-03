package ru.kuchanov.gp.util.user

import ru.kuchanov.gp.GpConstants

class GitHubOAuth2UserInfo(
    override val attributes: Map<String, Any>,
    override val providerToken: String?
) : OAuth2UserInfo(attributes, providerToken) {

    override fun getId(): String =
        attributes["id"].toString()

    override fun getName(): String =
        attributes["name"] as String

    override fun getEmail(): String? =
        attributes["email"] as String?

    override fun getImageUrl(): String? =
        attributes["avatar_url"] as? String

    override fun getProvider() =
        GpConstants.SocialProvider.GITHUB
}
