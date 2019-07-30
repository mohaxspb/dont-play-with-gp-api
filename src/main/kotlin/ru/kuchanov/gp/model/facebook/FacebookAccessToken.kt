package ru.kuchanov.gp.model.facebook

import com.fasterxml.jackson.annotation.JsonProperty

data class FacebookAccessToken(
    @JsonProperty("access_token") var accessToken: String? = null,
    @JsonProperty("token_type") var tokenType: String? = null,
    @JsonProperty("expires_in") var expires_in: Int? = null
)
