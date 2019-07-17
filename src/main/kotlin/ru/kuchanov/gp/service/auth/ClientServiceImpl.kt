package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.ClientNotFoundError
import ru.kuchanov.gp.bean.auth.OAuthClientDetails
import ru.kuchanov.gp.repository.auth.ClientDetailsRepository


@Service
class ClientServiceImpl @Autowired constructor(
    val repository: ClientDetailsRepository
) : ClientService {

    override fun loadClientByClientId(clientId: String): ClientDetails {
        return repository.getOne(clientId) ?: throw ClientNotFoundError()
    }

    override fun findAll(): List<OAuthClientDetails> = repository.findAll()
}
