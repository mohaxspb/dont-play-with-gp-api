package ru.kuchanov.gp.service.auth

import ru.kuchanov.gp.bean.auth.Authority

interface AuthorityService {
    fun findAll(): List<Authority>

    fun insert(authority: Authority): Authority?
    fun insert(authorities: List<Authority>): List<Authority>
}
