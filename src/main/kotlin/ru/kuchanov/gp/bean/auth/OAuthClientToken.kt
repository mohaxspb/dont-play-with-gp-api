package ru.kuchanov.gp.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "oauth_client_token")
data class OAuthClientToken(
        @Id
        val token_id: String,
        val token:ByteArray,
        val authentication_id:String,
        val user_name:String,
        val client_id:String,
        @field:CreationTimestamp
        val created: Timestamp,
        @field:UpdateTimestamp
        val updated: Timestamp
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as OAuthClientToken

                if (token_id != other.token_id) return false

                return true
        }

        override fun hashCode(): Int {
                return token_id.hashCode()
        }
}
//create table oauth_client_token (
//token_id VARCHAR(256),
//token bytea,
//authentication_id VARCHAR(256),
//user_name VARCHAR(256),
//client_id VARCHAR(256)
//);