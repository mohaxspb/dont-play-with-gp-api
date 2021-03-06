package ru.kuchanov.gp.service.auth

import org.springframework.security.core.userdetails.UserDetailsService
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.model.dto.UserDto
import javax.transaction.Transactional

interface GpUserDetailsService : UserDetailsService {

    override fun loadUserByUsername(username: String): GpUser?

    fun getAllById(ids: List<Long>): List<GpUser>

    fun countUsersCreatedBetweenDates(startDate: String, endDate: String): Int

    fun getById(id: Long): GpUser?
    fun getByIdAsDto(id: Long): UserDto?
    fun getByProviderId(id: String, provider: GpConstants.SocialProvider): GpUser?

    @Transactional
    fun save(user: GpUser): GpUser

    fun update(user: GpUser): GpUser

    @Transactional
    fun deleteById(id: Long): Boolean

    @Transactional
    fun deleteByUsername(username: String): Boolean
}
