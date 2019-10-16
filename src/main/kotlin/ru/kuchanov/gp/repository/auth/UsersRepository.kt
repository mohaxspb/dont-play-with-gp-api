package ru.kuchanov.gp.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.model.dto.UserDto

interface UsersRepository : JpaRepository<GpUser, Long> {
    fun findOneByUsername(username: String): GpUser?

    @Query(
        """
            SELECT count(created) FROM users 
            WHERE created >= CAST( :startDate AS timestamp) 
            AND created <= CAST( :endDate AS timestamp)
        """,
        nativeQuery = true
    )
    fun countUsersCreatedBetweenDates(startDate: String, endDate: String): Int

    fun findOneById(id: Long): GpUser?
    fun findOneByGoogleId(id: String): GpUser?
    fun findOneByFacebookId(id: String): GpUser?
    fun findOneByVkId(id: String): GpUser?
    fun findOneByGithubId(id: String): GpUser?

    //see https://stackoverflow.com/a/50968131/3212712
    @Query(nativeQuery = true)
    fun getOneAsUserDto(userId: Long): UserDto?
}
