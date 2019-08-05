package ru.kuchanov.gp.model.facebook

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class FacebookProfileResponse(
    var email: String? = null,
    var name: String? = null,
    @JsonProperty("first_name") var firstName: String? = null,
    @JsonProperty("last_name") var lastName: String? = null,
    var id: Long? = null,
    var picture: Picture? = null
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Picture(var data: Data? = null) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Data(
            var url: String? = null,
            var height: Int? = null,
            var width: Int? = null
        )
    }
}
