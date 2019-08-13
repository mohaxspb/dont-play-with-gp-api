package ru.kuchanov.gp.service.auth

import ru.kuchanov.gp.bean.auth.Authority

interface AuthorityService {
    fun findAll(): List<Authority>

    fun save(authority: Authority): Authority?
    fun save(authorities: List<Authority>): List<Authority>
}
