package com.example.discoveryservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/*
* required for restricting access to the APIs and web pages of the discovery
* server. It will now use HTTP Basic authentication and require
* a user to supply a user-name and password to get access.
* */

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final String username;
    private final String password;


    @Autowired
    public SecurityConfig(
            @Value("${app.eureka-username}") String username,
            @Value("${app.eureka-password}") String password
    ){
        this.username = username;
        this.password = password;
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(){
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username(username)
                        .password(password)
                        .roles("USER")
                        .build();
        return new InMemoryUserDetailsManager(user);

    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize-> authorize
                                .requestMatchers("/actuator/**").permitAll()
                                .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
