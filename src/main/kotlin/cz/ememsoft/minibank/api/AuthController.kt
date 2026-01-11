package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.auth.CreateUserRequestDto
import cz.ememsoft.minibank.dto.auth.CreateUserResponseDto
import cz.ememsoft.minibank.dto.auth.LoginRequestDto
import cz.ememsoft.minibank.dto.auth.RefreshTokenRequestDto
import cz.ememsoft.minibank.dto.auth.TokenResponseDto
import cz.ememsoft.minibank.service.KeycloakAdminService
import cz.ememsoft.minibank.service.KeycloakAuthService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val keycloakAuthService: KeycloakAuthService,
    private val keycloakAdminService: KeycloakAdminService,
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequestDto): Mono<TokenResponseDto> {
        return keycloakAuthService.login(request.username, request.password)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequestDto): Mono<TokenResponseDto> {
        return keycloakAuthService.refresh(request.refreshToken)
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: CreateUserRequestDto): Mono<CreateUserResponseDto> {
        val sanitizedRequest = request.copy(roles = emptySet())
        return keycloakAdminService.createUser(sanitizedRequest, defaultRoles = setOf(DEFAULT_ROLE))
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(@Valid @RequestBody request: CreateUserRequestDto): Mono<CreateUserResponseDto> {
        return keycloakAdminService.createUser(request)
    }

    companion object {
        private const val DEFAULT_ROLE = "CUSTOMER"
    }
}
