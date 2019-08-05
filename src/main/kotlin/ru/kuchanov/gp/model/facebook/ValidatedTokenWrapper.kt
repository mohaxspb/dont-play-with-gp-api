package ru.kuchanov.gp.model.facebook

data class ValidatedTokenWrapper(
        val verifiedToken: DebugTokenResponse?,
        val exception: Throwable?
)
