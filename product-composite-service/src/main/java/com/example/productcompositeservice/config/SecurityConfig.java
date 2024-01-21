package com.example.productcompositeservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
                authorizeRequest -> authorizeRequest
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/openapi/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers(POST, "/productComposite/**")
                        .hasAuthority("SCOPE_product:write")
                        .requestMatchers(DELETE, "/productComposite/**")
                        .hasAuthority("SCOPE_product:write")
                        .requestMatchers(GET, "/productComposite/**")
                        .hasAuthority("SCOPE_product:read")
                        .anyRequest().authenticated()
        ).oauth2ResourceServer(
                oauth2 -> oauth2.jwt(Customizer.withDefaults())
        );
        return http.build();
    }
}

