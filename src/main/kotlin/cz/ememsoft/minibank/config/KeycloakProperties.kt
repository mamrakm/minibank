package cz.ememsoft.minibank.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak")
data class KeycloakProperties(
    val baseUrl: String,
    val realm: String,
    val clientId: String,
    val clientSecret: String,
    val adminRealm: String = "master",
)
