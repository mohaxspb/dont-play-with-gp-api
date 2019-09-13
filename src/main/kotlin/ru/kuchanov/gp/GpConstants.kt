package ru.kuchanov.gp

object GpConstants {

    const val DEFAULT_LANG_CODE = "en"

    const val TARGET_URL_PARAMETER = "targetUrlParameter"

    object UsersEndpoint {
        const val PATH = "users"

        object Method {
            const val ME = "me"
            const val DELETE = "delete"
            const val UPDATE = "update"
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
            const val APPROVE = "approve"
            const val PUBLISH = "publish"
            const val ALL = "all"
            const val ALL_BY_AUTHOR_ID = "allByAuthorId"
        }
    }

    object ArticleTranslationEndpoint {
        const val PATH = "article/translation"

        object Method {
            const val CREATE = "create"
            const val APPROVE = "approve"
            const val PUBLISH = "publish"
            const val ALL_BY_AUTHOR_ID = "allByAuthorId"
        }
    }

    object ArticleTranslationVersionEndpoint {
        const val PATH = "article/translation/version"

        object Method {
            const val CREATE = "create"
            const val APPROVE = "approve"
            const val PUBLISH = "publish"
            const val ALL_BY_AUTHOR_ID = "allByAuthorId"
        }
    }

    object ImageEndpoint {
        const val PATH = "image"

        object Method {
            const val ADD = "add"
        }
    }

    object FilesPaths {
        const val IMAGE = "image"
    }

    enum class SocialProvider {
        GOOGLE, FACEBOOK, VK, GITHUB
    }
}
