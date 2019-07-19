package ru.kuchanov.gp.service.auth

import org.springframework.security.oauth2.provider.ClientDetailsService
import ru.kuchanov.gp.bean.auth.OAuthClientDetails

interface GpClientDetailsService : ClientDetailsService {
    fun findAll(): List<OAuthClientDetails>

    fun insert(oAuthClientDetails: OAuthClientDetails): OAuthClientDetails
}