package cz.ememsoft.minibank.service

import com.fasterxml.jackson.annotation.JsonProperty
import cz.ememsoft.minibank.config.KeycloakProperties
import cz.ememsoft.minibank.dto.auth.TokenResponseDto
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class KeycloakAuthService(
    private val keycloakProperties: KeycloakProperties,
    private val webClientBuilder: WebClient.Builder,
) {

    fun login(username: String, password: String): Mono<TokenResponseDto> {
        return tokenRequest(
            grantType = "password",
            formCustomizer = BodyInserters.fromFormData("username", username)
                .with("password", password),
        )
    }

    fun refresh(refreshToken: String): Mono<TokenResponseDto> {
        return tokenRequest(
            grantType = "refresh_token",
            formCustomizer = BodyInserters.fromFormData("refresh_token", refreshToken),
        )
    }

    private fun tokenRequest(
        grantType: String,
        formCustomizer: BodyInserters.FormInserter<String>,
    ): Mono<TokenResponseDto> {
        return webClientBuilder.build()
            .post()
            .uri("${keycloakProperties.baseUrl}/realms/${keycloakProperties.realm}/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                formCustomizer
                    .with("grant_type", grantType)
                    .with("client_id", keycloakProperties.clientId)
                    .with("client_secret", keycloakProperties.clientSecret),
            )
            .retrieve()
            .bodyToMono(KeycloakTokenResponse::class.java)
            .map { response ->
                TokenResponseDto(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    expiresIn = response.expiresIn,
                    refreshExpiresIn = response.refreshExpiresIn,
                    tokenType = response.tokenType,
                    scope = response.scope,
                )
            }
    }

    private data class KeycloakTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("refresh_token")
        val refreshToken: String?,
        @JsonProperty("expires_in")
        val expiresIn: Long,
        @JsonProperty("refresh_expires_in")
        val refreshExpiresIn: Long?,
        @JsonProperty("token_type")
        val tokenType: String,
        @JsonProperty("scope")
        val scope: String?,
    )
}
