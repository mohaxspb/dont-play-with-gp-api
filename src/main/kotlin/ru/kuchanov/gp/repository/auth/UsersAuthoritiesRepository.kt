package ru.kuchanov.gp.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.UsersAuthorities

@Repository
interface UsersAuthoritiesRepository : JpaRepository<UsersAuthorities, Long> {

    fun findAllByUserIdAndAuthority(userId: Long, authorityType: AuthorityType): List<UsersAuthorities>

    fun deleteByUserId(userId: Long)
}