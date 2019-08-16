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
    val usersRepository: UsersRepository,
    val usersAuthoritiesService: UsersAuthoritiesService
) : GpUserDetailsService {

    override fun getById(id: Long) =
        usersRepository.findOneById(id)?.withAuthorities() ?: throw UserNotFoundException()

    override fun getByIdDto(id: Long): UserDto =
        usersRepository
            .getOneAsUserDto(id)
            ?.withAuthorities()
            ?: throw UserNotFoundException()

    override fun save(user: GpUser): GpUser =
        usersRepository.save(user).withAuthorities()

    override fun update(user: GpUser): GpUser =
        usersRepository.save(user).withAuthorities()

    override fun updateAvatarUrl(userId: Long, avatarUrl: String): UserDto {
        usersRepository.updateAvatarUrl(userId, avatarUrl)
        return usersRepository.getOneAsUserDto(userId)!!
    }

    override fun loadUserByUsername(username: String) =
        usersRepository.findOneByUsername(username)?.withAuthorities()

    override fun getByProviderId(id: String, provider: GpConstants.SocialProvider) =
        when (provider) {
            GpConstants.SocialProvider.GOOGLE -> usersRepository.findOneByGoogleId(id)
            GpConstants.SocialProvider.FACEBOOK -> usersRepository.findOneByFacebookId(id)
            GpConstants.SocialProvider.VK -> usersRepository.findOneByVkId(id)
            GpConstants.SocialProvider.GITHUB -> usersRepository.findOneByGithubId(id)
        }?.withAuthorities()

    override fun deleteById(id: Long): Boolean {
        usersRepository.deleteById(id)
        return true
    }

    override fun deleteByUsername(username: String): Boolean {
        usersRepository.deleteByUsername(username)
        return true
    }

    fun GpUser.withAuthorities() = this.apply {
        userAuthorities = usersAuthoritiesService.findAllByUserId(id!!).toSet()
    }

    fun UserDto.withAuthorities() = this.apply {
        authorities = usersAuthoritiesService.findAllByUserIdAsDto(id)
    }
}
