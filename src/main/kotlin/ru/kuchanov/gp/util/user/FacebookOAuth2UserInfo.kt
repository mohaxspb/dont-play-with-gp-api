package ru.kuchanov.gp.util.user

import ru.kuchanov.gp.GpConstants

class FacebookOAuth2UserInfo(
    override val attributes: Map<String, Any>,
    override val providerToken: String?
) : OAuth2UserInfo(attributes, providerToken) {

    override fun getId(): String =
        attributes["id"] as String

    override fun getName(): String =
        attributes["name"] as String

    override fun getEmail(): String =
        attributes["email"] as String

    override fun getImageUrl(): String? {
        if (attributes.containsKey("picture")) {
            val pictureObj = attributes["picture"] as Map<String, Any>
            if (pictureObj.containsKey("data")) {
                val dataObj = pictureObj["data"] as Map<String, Any>
                if (dataObj.containsKey("url")) {
                    return dataObj["url"] as String
                }
            }
        }
        return null
    }

    override fun getProvider() =
        GpConstants.SocialProvider.FACEBOOK
}
