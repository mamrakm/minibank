package cz.ememsoft.minibank.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KeycloakProperties::class)
class KeycloakConfig
