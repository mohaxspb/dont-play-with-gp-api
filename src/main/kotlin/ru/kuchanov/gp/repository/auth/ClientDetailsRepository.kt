package ru.kuchanov.gp.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.auth.OAuthClientDetails

interface ClientDetailsRepository : JpaRepository<OAuthClientDetails, String> {
    fun findOneByClientId(clientId: String): OAuthClientDetails?
}