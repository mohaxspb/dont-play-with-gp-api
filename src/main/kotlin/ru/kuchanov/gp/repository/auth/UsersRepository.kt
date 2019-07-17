package ru.kuchanov.gp.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.gp.bean.auth.User
import ru.kuchanov.gp.model.dto.UserDto

interface UsersRepository : JpaRepository<User, Long> {
    fun findOneByMyUsername(username: String): User?
    fun findOneById(id: Long): User?
    fun findOneByGoogleId(id: String): User?
    fun findOneByFacebookId(id: String): User?
    fun findOneByVkId(id: String): User?

    @Modifying
    @Query("UPDATE User u SET u.avatar = ?2 WHERE u.id = ?1")
    @Transactional
    fun updateAvatarUrl(userId: Long, avatarUrl: String): Int

    //see https://stackoverflow.com/a/50968131/3212712
    @Query(nativeQuery = true)
    fun getOneAsUserDto(userId: Long): UserDto?
}

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such user")
class UserNotFoundException : RuntimeException()