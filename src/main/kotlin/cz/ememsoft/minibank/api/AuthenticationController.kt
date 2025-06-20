package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.auth.AuthResponseDto
import cz.ememsoft.minibank.dto.auth.LoginRequestDto
import cz.ememsoft.minibank.dto.auth.RegisterRequestDto
import cz.ememsoft.minibank.service.AuthenticationService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * REST controller for authentication operations.
 * 
 * Handles client registration and login operations.
 */
@RestController
@RequestMapping("/auth")
@Validated
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun register(@Valid @RequestBody registerRequest: RegisterRequestDto): Mono<AuthResponseDto> {
        logger.info { "Registration request received for email: ${registerRequest.email}" }
        return authenticationService.register(registerRequest)
            .doOnSuccess { logger.info { "Registration successful for email: ${registerRequest.email}" } }
            .doOnError { logger.error(it) { "Registration failed for email: ${registerRequest.email}" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun login(@Valid @RequestBody loginRequest: LoginRequestDto): Mono<AuthResponseDto> {
        logger.info { "Login request received for email: ${loginRequest.email}" }
        return authenticationService.login(loginRequest)
            .doOnSuccess { logger.info { "Login successful for email: ${loginRequest.email}" } }
            .doOnError { logger.error(it) { "Login failed for email: ${loginRequest.email}" } }
    }
}