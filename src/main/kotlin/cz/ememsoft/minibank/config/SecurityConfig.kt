package cz.ememsoft.minibank.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(
                        "/api/auth/**",
                        "/actuator/health",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                    )
                    .permitAll()
                    .anyExchange()
                    .authenticated()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { jwtSpec ->
                    jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .build()
    }

    @Bean
    fun jwtAuthenticationConverter(): ReactiveJwtAuthenticationConverterAdapter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(realmRoleAuthoritiesConverter())
        return ReactiveJwtAuthenticationConverterAdapter(converter)
    }

    private fun realmRoleAuthoritiesConverter(): Converter<Jwt, Collection<GrantedAuthority>> {
        return Converter { jwt ->
            val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
            val roles = realmAccess?.get("roles") as? Collection<*>
            roles
                ?.mapNotNull { role ->
                    (role as? String)?.let { SimpleGrantedAuthority("ROLE_${it}") }
                }
                ?.toSet()
                ?: emptySet()
        }
    }
}
