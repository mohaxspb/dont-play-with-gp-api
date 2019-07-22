package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.ClientNotFoundError
import ru.kuchanov.gp.bean.auth.OAuthClientDetails
import ru.kuchanov.gp.repository.auth.ClientDetailsRepository


@Service
class GpClientDetailsServiceImpl @Autowired constructor(
    val repository: ClientDetailsRepository
) : GpClientDetailsService {

    override fun loadClientByClientId(clientId: String): ClientDetails =
        repository.findOneByClientId(clientId) ?: throw ClientNotFoundError()

    override fun findAll(): List<OAuthClientDetails> =
        repository.findAll()

    override fun insert(oAuthClientDetails: OAuthClientDetails): OAuthClientDetails =
        repository.save(oAuthClientDetails)
}
