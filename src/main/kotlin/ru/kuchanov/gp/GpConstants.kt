package ru.kuchanov.gp

object GpConstants {

    const val DEFAULT_LANG_CODE = "en"

    const val TARGET_URL_PARAMETER = "targetUrlParameter"

    object UsersEndpoint {
        const val PATH = "users"

        object Method {
            const val ME = "me"
        }
    }

    object AuthEndpoint {
        const val PATH = "auth"

        object Method {
            const val REGISTER = "register"
        }
    }

    object LanguageEndpoint {
        const val PATH = "language"

        object Method {
            const val ALL = "all"
            const val ADD = "add"
        }
    }

    object ArticleEndpoint {
        const val PATH = "article"

        object Method {
            const val CREATE = "create"
            const val ALL_BY_AUTHOR_ID = "allByAuthorId"
        }
    }

    enum class SocialProvider {
        GOOGLE, FACEBOOK, VK, GITHUB
    }
}
