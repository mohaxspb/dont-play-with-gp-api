package ru.kuchanov.gp.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.model.dto.UserDto
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "users")

//see https://stackoverflow.com/questions/49225739/namednativequery-with-sqlresultsetmapping-for-non-entity
@SqlResultSetMapping(
    name = "UserDtoResult", classes = [
        ConstructorResult(
            targetClass = UserDto::class,
            columns = [
                ColumnResult(name = "id", type = Long::class),
                ColumnResult(name = "fullName"),
                ColumnResult(name = "avatar")
            ]
        )
    ]
)
@NamedNativeQuery(
    name = "GpUser.getOneAsUserDto",
    resultSetMapping = "UserDtoResult",
    query = "SELECT " +
            "id, " +
            "full_name as fullName, " +
            "avatar " +
            "FROM users u " +
            "WHERE u.id = :userId"
)

data class GpUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name_first")
    var nameFirst: String? = null,
    @Column(name = "name_second")
    var nameSecond: String? = null,
    @Column(name = "name_third")
    var nameThird: String? = null,
    @Column(name = "full_name")
    var fullName: String? = null,
    private val username: String,
    private val password: String,
    var avatar: String? = null,
    val enabled: Boolean = true,
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "userId", fetch = FetchType.EAGER)
    var userAuthorities: Set<UsersAuthorities> = setOf(),
    @field:CreationTimestamp
    val created: Timestamp? = null,
    @field:UpdateTimestamp
    val updated: Timestamp? = null,
    //social login fields
    @Column(name = "facebook_id")
    var facebookId: String? = null,
    @Column(name = "google_id")
    var googleId: String? = null,
    @Column(name = "vk_id")
    var vkId: String? = null,
    @Column(name = "github_id")
    var githubId: String? = null,
    @Column(name = "facebook_token")
    var facebookToken: String? = null,
    @Column(name = "google_token")
    var googleToken: String? = null,
    @Column(name = "vk_token")
    var vkToken: String? = null,
    @Column(name = "github_token")
    var githubToken: String? = null
) : UserDetails, OAuth2User {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        userAuthorities.map { SimpleGrantedAuthority(it.authority.name) }.toMutableList()

    override fun isEnabled() = enabled

    override fun getUsername() = username

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = password

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun getAttributes() = mutableMapOf<String, Any>()

    override fun getName() = fullNameToDto()
}

fun GpUser.toDto() = UserDto(
    id = id!!,
    avatar = avatar,
    fullName = fullNameToDto()
)

fun GpUser.fullNameToDto(): String? {
    if (fullName == null) {
        if (nameFirst == null && nameSecond == null && nameThird != null) {
            return nameThird
        }
        if (nameFirst == null && nameSecond != null && nameThird != null) {
            return "$nameSecond $nameThird"
        }
        if (nameFirst == null && nameSecond != null && nameThird == null) {
            return nameSecond
        }
        if (nameFirst != null && nameSecond == null && nameThird == null) {
            return nameFirst
        }
        if (nameFirst != null && nameSecond == null && nameThird != null) {
            return "$nameFirst $nameThird"
        }
        if (nameFirst != null && nameSecond != null && nameThird == null) {
            return "$nameFirst $nameSecond"
        }
        if (nameFirst != null && nameSecond != null && nameThird != null) {
            return "$nameFirst $nameSecond $nameThird"
        } else {
            return null
        }
    } else {
        return fullName
    }
}

fun GpUser.setSocialProviderData(
    provider: GpConstants.SocialProvider,
    idInProvidersSystem: String,
    tokenInProvidersSystem: String
) {
    when (provider) {
        GpConstants.SocialProvider.GOOGLE -> {
            googleId = idInProvidersSystem
            googleToken = tokenInProvidersSystem
        }
        GpConstants.SocialProvider.FACEBOOK -> {
            facebookId = idInProvidersSystem
            facebookToken = tokenInProvidersSystem
        }
        GpConstants.SocialProvider.VK -> {
            vkId = idInProvidersSystem
            vkToken = tokenInProvidersSystem
        }
        GpConstants.SocialProvider.GITHUB -> {
            githubId = idInProvidersSystem
            githubToken = tokenInProvidersSystem
        }
    }
}

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "User with this email already exists")
class UserAlreadyExistsException : RuntimeException()