package ru.kuchanov.gp.service.auth

import org.springframework.security.core.userdetails.UserDetailsService
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.User
import ru.kuchanov.gp.model.dto.UserDto
import javax.transaction.Transactional

interface GpUserDetailsService : UserDetailsService {
    override fun loadUserByUsername(username: String): User?

    fun findAll(): List<User>
    fun getById(id: Long): User
    fun getByIdDto(id: Long): UserDto
    fun getByProviderId(id: String, provider: GpConstants.SocialProvider): User?

    @Transactional
    fun insert(user: User): User

    fun insert(users: List<User>): List<User>

    fun update(user: User): User

    fun updateAvatarUrl(userId: Long, avatarUrl: String): UserDto

    fun deleteById(id: Long): Boolean
}
