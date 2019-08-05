package ru.kuchanov.gp.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.UsersAuthorities

@Repository
interface UsersAuthoritiesRepository : JpaRepository<UsersAuthorities, Long> {

    fun findAllByUserId(userId: Long): List<UsersAuthorities>

    fun findByUserIdAndAuthority(userId: Long, authorityType: AuthorityType): UsersAuthorities?

    @Transactional
    fun deleteByUserId(userId: Long)
}
