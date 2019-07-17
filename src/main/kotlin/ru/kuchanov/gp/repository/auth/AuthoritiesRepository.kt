package ru.kuchanov.gp.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.kuchanov.gp.bean.auth.Authority

@Repository
interface AuthoritiesRepository : JpaRepository<Authority, Long> {

    fun deleteByUserId(userId: Long)
}