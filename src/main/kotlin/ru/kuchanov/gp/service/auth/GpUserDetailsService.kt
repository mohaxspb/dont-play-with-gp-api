package ru.kuchanov.gp.service.auth

import org.springframework.security.core.userdetails.UserDetailsService
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.model.dto.UserDto
import javax.transaction.Transactional

interface GpUserDetailsService : UserDetailsService {

    override fun loadUserByUsername(username: String): GpUser?

    fun getById(id: Long): GpUser?
    fun getByIdDto(id: Long): UserDto
    fun getByProviderId(id: String, provider: GpConstants.SocialProvider): GpUser?

    @Transactional
    fun save(user: GpUser): GpUser

    fun update(user: GpUser): GpUser

    fun updateAvatarUrl(userId: Long, avatarUrl: String): UserDto

    fun deleteById(id: Long): Boolean

    fun deleteByUsername(username: String): Boolean
}
