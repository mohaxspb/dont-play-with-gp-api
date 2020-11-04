package ru.kuchanov.gp.exception

import org.springframework.security.core.AuthenticationException

class OAuth2AuthenticationProcessingException(
    override val message: String?
) : AuthenticationException(message)