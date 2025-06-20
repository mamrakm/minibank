package cz.ememsoft.minibank.config

import cz.ememsoft.minibank.repository.ClientRepository
import cz.ememsoft.minibank.service.AuthenticationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * JWT authentication filter for WebFlux.
 * 
 * Extracts JWT token from the Authorization header, validates it,
 * and sets up Spring Security context with proper user roles.
 */
@Component
class JwtAuthenticationFilter(
    private val authenticationService: AuthenticationService,
    private val clientRepository: ClientRepository
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            
            authenticationService.validateToken(token)
                .flatMap { clientId ->
                    // Get client from database to retrieve the current role and status
                    clientRepository.findById(clientId)
                        .switchIfEmpty(Mono.error(RuntimeException("Client not found: $clientId")))
                        .filter { client -> client.enabled }
                        .switchIfEmpty(Mono.error(RuntimeException("Client account disabled: $clientId")))
                        .map { client ->
                            logger.debug { "Authenticated client ID: $clientId, role: ${client.role}" }
                            
                            // Create authorities based on an actual user role
                            val authorities = listOf(SimpleGrantedAuthority("ROLE_${client.role.name}"))
                            UsernamePasswordAuthenticationToken(clientId, null, authorities)
                        }
                }
                .flatMap { authentication ->
                    chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                }
                .onErrorResume { error ->
                    logger.warn { "JWT authentication failed: ${error.message}" }
                    // Continue without authentication on JWT validation failure
                    chain.filter(exchange)
                }
        } else {
            chain.filter(exchange)
        }
    }
}