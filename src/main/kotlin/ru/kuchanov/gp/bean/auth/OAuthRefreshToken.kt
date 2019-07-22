package ru.kuchanov.gp.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "oauth_refresh_token")
data class OAuthRefreshToken(
        @Id
        val token_id: String,
        val token:ByteArray,
        val authentication:ByteArray,
        @field:CreationTimestamp
        val created: Timestamp,
        @field:UpdateTimestamp
        @Column(insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
        val updated: Timestamp
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as OAuthRefreshToken

                if (token_id != other.token_id) return false

                return true
        }

        override fun hashCode(): Int {
                return token_id.hashCode()
        }
}
//create table oauth_refresh_token (
//token_id VARCHAR(256),
//token bytea,
//authentication bytea
//);