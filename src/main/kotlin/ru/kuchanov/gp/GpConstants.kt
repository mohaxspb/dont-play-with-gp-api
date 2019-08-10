package ru.kuchanov.gp

object GpConstants {

    const val DEFAULT_FULL_NAME = "N/A"

    const val TARGET_URL_PARAMETER = "targetUrlParameter"

    object Path {
        const val AUTH = "auth"
        const val USERS = "users"
    }

    object UsersEndpoint {
        const val PATH = "users"

        object Method {
            const val ME = "me"
            const val ALL = "all"
        }
    }

    enum class SocialProvider {
        GOOGLE, FACEBOOK, VK, GITHUB
    }

    enum class Client {
        ANGULAR
    }
}
