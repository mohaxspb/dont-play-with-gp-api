package ru.kuchanov.gp.util.user

class GoogleOAuth2UserInfo(
    override val attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {

    override fun getId(): String =
        attributes["sub"] as String

    override fun getName(): String =
        attributes["name"] as String

    override fun getEmail(): String =
        attributes["email"] as String

    override fun getImageUrl(): String? =
        attributes["picture"] as? String
}
