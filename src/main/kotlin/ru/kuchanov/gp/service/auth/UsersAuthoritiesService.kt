package ru.kuchanov.gp.service.auth

import org.springframework.transaction.annotation.Transactional
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.UsersAuthorities

interface UsersAuthoritiesService {
    fun findAll(): List<UsersAuthorities>

    fun findAllByUserId(userId: Long): List<UsersAuthorities>
    fun findByUserIdAndAuthority(userId: Long, authorityType: AuthorityType): UsersAuthorities?

    fun save(usersAuthorities: UsersAuthorities): UsersAuthorities?
    fun save(usersAuthorities: List<UsersAuthorities>): List<UsersAuthorities>

    @Transactional
    fun deleteByUserId(userId: Long)
}
