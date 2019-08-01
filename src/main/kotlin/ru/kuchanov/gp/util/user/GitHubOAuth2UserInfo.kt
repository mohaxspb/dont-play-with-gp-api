package ru.kuchanov.gp.util.user

class GitHubOAuth2UserInfo(
    override val attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {

    override fun getId(): String =
        attributes["id"] as String

    override fun getName(): String =
        attributes["name"] as String

    override fun getEmail(): String =
        attributes["email"] as String

    override fun getImageUrl(): String? =
        attributes["avatar_url"] as? String
}
