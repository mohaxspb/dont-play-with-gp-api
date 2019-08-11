package ru.kuchanov.gp.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.web.bind.annotation.ResponseStatus
import java.sql.Timestamp
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

//todo refactor it with database table (I mean refactor table field names)
@Entity
@Table(name = "oauth_client_details")
data class OAuthClientDetails(
    @Id
    @Column(name = "client_id")
    private val clientId: String,
    val resource_ids: String,
    val client_secret: String,
    val scope: String,
    val authorized_grant_types: String,
    val web_server_redirect_uri: String,
    val authorities: String,
    /**
     * Token expiration in seconds
     */
    val access_token_validity: Int,
    /**
     * Token expiration in seconds
     */
    val refresh_token_validity: Int,
    val additional_information: String,
    val autoapprove: String,
    @field:CreationTimestamp
    val created: Timestamp? = null,
    @field:UpdateTimestamp
    val updated: Timestamp? = null
) : ClientDetails {
    override fun isSecretRequired() = true

    override fun getAdditionalInformation(): MutableMap<String, Any> = mutableMapOf()

    override fun getAccessTokenValiditySeconds() = access_token_validity

    override fun getResourceIds(): MutableSet<String> = mutableSetOf()

    override fun getClientId() = clientId

    override fun isAutoApprove(scope: String?) = true

    override fun getAuthorities() = authorities
        .split(",")
        .map { SimpleGrantedAuthority(it) }

    override fun getRefreshTokenValiditySeconds() = refresh_token_validity

    override fun getClientSecret() = client_secret

    override fun getRegisteredRedirectUri(): MutableSet<String> = mutableSetOf()

    override fun isScoped() = true

    override fun getScope() = scope.split(",").toMutableSet()

    override fun getAuthorizedGrantTypes() = authorized_grant_types
        .split(",")
        .toMutableSet()
}

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Client not found in db!")
class ClientNotFoundError : RuntimeException()

//create table oauth_client_details (
//client_id VARCHAR(256) PRIMARY KEY,
//resource_ids VARCHAR(256),
//client_secret VARCHAR(256),
//scope VARCHAR(256),
//authorized_grant_types VARCHAR(256),
//web_server_redirect_uri VARCHAR(256),
//authorities VARCHAR(256),
//access_token_validity INTEGER,
//refresh_token_validity INTEGER,
//additional_information VARCHAR(4096),
//autoapprove VARCHAR(256)
//);