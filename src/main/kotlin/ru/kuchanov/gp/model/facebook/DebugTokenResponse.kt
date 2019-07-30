package ru.kuchanov.gp.model.facebook

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DebugTokenResponse(var data: Data? = null) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Data(
        @JsonProperty("app_id") var appId: Long? = null,
        @JsonProperty("is_valid") var isValid: Boolean? = null
    )
}
