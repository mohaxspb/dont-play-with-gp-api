package ru.kuchanov.gp.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.UsersAuthorities
import ru.kuchanov.gp.model.dto.AuthorityDto

@Repository
interface UsersAuthoritiesRepository : JpaRepository<UsersAuthorities, Long> {

    fun findAllByUserId(userId: Long): List<UsersAuthorities>

    @Query("select new ru.kuchanov.gp.model.dto.AuthorityDto(ua.authority) from UsersAuthorities ua where ua.userId = :userId")
    fun findAllByUserIdAsDto(userId: Long): List<AuthorityDto>

    fun findByUserIdAndAuthority(userId: Long, authorityType: AuthorityType): UsersAuthorities?

    @Transactional
    fun deleteByUserId(userId: Long)
}
