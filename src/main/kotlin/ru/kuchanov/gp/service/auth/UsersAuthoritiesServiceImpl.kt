package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.UsersAuthorities
import ru.kuchanov.gp.model.dto.AuthorityDto
import ru.kuchanov.gp.repository.auth.UsersAuthoritiesRepository

@Service
class UsersAuthoritiesServiceImpl @Autowired constructor(
    val usersAuthoritiesRepository: UsersAuthoritiesRepository
) : UsersAuthoritiesService {
    override fun findAll(): List<UsersAuthorities> =
        usersAuthoritiesRepository.findAll()

    override fun findAllByUserId(userId: Long): List<UsersAuthorities> =
        usersAuthoritiesRepository.findAllByUserId(userId)

    override fun findAllByUserIdAsDto(userId: Long): List<AuthorityDto> =
        usersAuthoritiesRepository.findAllByUserIdAsDto(userId)

    override fun findByUserIdAndAuthority(userId: Long, authorityType: AuthorityType): UsersAuthorities? =
        usersAuthoritiesRepository.findByUserIdAndAuthority(userId, authorityType)

    override fun save(usersAuthorities: UsersAuthorities): UsersAuthorities? =
        usersAuthoritiesRepository.save(usersAuthorities)

    override fun save(usersAuthorities: List<UsersAuthorities>): List<UsersAuthorities> =
        usersAuthoritiesRepository.saveAll(usersAuthorities)

    override fun deleteByUserId(userId: Long) =
        usersAuthoritiesRepository.deleteByUserId(userId)
}
