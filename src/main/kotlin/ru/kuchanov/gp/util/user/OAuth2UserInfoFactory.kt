package ru.kuchanov.gp.util.user

import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.exception.OAuth2AuthenticationProcessingException

object OAuth2UserInfoFactory {

    fun getOAuth2UserInfo(
        registrationId: String,
        attributes: Map<String, Any>,
        providerToken: String?
    ): OAuth2UserInfo {
        return when {
            registrationId.equals(
                GpConstants.SocialProvider.GOOGLE.toString(),
                ignoreCase = true
            ) -> GoogleOAuth2UserInfo(attributes, providerToken)
            registrationId.equals(
                GpConstants.SocialProvider.FACEBOOK.toString(),
                ignoreCase = true
            ) -> FacebookOAuth2UserInfo(attributes, providerToken)
            registrationId.equals(
                GpConstants.SocialProvider.GITHUB.toString(),
                ignoreCase = true
            ) -> GitHubOAuth2UserInfo(attributes, providerToken)
            registrationId.equals(
                GpConstants.SocialProvider.VK.toString(),
                ignoreCase = true
            ) -> VkOAuth2UserInfo(attributes, providerToken)
            else -> throw OAuth2AuthenticationProcessingException("Sorry! Login with $registrationId is not supported yet.")
        }
    }
}
