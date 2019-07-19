package ru.kuchanov.gp.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "oauth_client_details")
data class OAuthClientDetails(
    @Id
    val client_id: String,
    val resource_ids: String,
    val client_secret: String,
    val scope: String,
    val authorized_grant_types: String,
    val web_server_redirect_uri: String,
    val authorities: String,
    val access_token_validity: Int,
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

    override fun getClientId() = client_id

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