package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.Authority
import ru.kuchanov.gp.repository.auth.AuthoritiesRepository


@Service
class AuthorityServiceImpl @Autowired constructor(
    val repository: AuthoritiesRepository
) : AuthorityService {

    override fun findAll(): List<Authority> =
        repository.findAll().toList()

    override fun save(authority: Authority): Authority =
        repository.save(authority)

    override fun save(authorities: List<Authority>): List<Authority> =
        repository.saveAll(authorities)
}
