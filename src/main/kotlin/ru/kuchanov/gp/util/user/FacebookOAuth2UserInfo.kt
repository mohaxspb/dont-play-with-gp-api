package ru.kuchanov.gp.util.user

class FacebookOAuth2UserInfo(
    override val attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {

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
}
