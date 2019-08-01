package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.model.dto.UserDto
import ru.kuchanov.gp.repository.auth.UserNotFoundException
import ru.kuchanov.gp.repository.auth.UsersRepository


@Service
class GpUserDetailsServiceImpl @Autowired constructor(
    val repository: UsersRepository
) : GpUserDetailsService {

    override fun findAll(): List<GpUser> =
        repository.findAll()

    override fun getById(id: Long) =
        repository.getOne(id) ?: throw UserNotFoundException()

    override fun getByIdDto(id: Long): UserDto =
        repository.getOneAsUserDto(id) ?: throw UserNotFoundException()

    override fun insert(user: GpUser): GpUser =
        repository.save(user)

    override fun insert(users: List<GpUser>): List<GpUser> =
        repository.saveAll(users)

    override fun update(user: GpUser): GpUser =
        repository.save(user)

    override fun updateAvatarUrl(userId: Long, avatarUrl: String): UserDto {
        repository.updateAvatarUrl(userId, avatarUrl)
        return repository.getOneAsUserDto(userId)!!
    }

    override fun loadUserByUsername(username: String) =
        repository.findOneByUsername(username)

    override fun getByProviderId(id: String, provider: GpConstants.SocialProvider) =
        when (provider) {
            GpConstants.SocialProvider.GOOGLE -> repository.findOneByGoogleId(id)
            GpConstants.SocialProvider.FACEBOOK -> repository.findOneByFacebookId(id)
            GpConstants.SocialProvider.VK -> repository.findOneByVkId(id)
            else -> throw NotImplementedError()
        }

    override fun deleteById(id: Long): Boolean {
        repository.deleteById(id)
        return true
    }

    override fun deleteByUsername(username: String): Boolean {
        repository.deleteByUsername(username)
        return true
    }
}
