package ru.kuchanov.gp.service.auth

import org.springframework.security.oauth2.provider.ClientDetailsService
import ru.kuchanov.gp.bean.auth.OAuthClientDetails

interface ClientService : ClientDetailsService {
    fun findAll(): List<OAuthClientDetails>
}