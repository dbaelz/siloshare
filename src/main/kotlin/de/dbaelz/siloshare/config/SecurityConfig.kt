package de.dbaelz.siloshare.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Value("\${basic.auth.username}")
    lateinit var basicAuthUsername: String

    @Value("\${basic.auth.password}")
    lateinit var basicAuthPassword: String

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/notes/**").authenticated()
                    .anyRequest().permitAll()
            }
            .httpBasic { }
        return http.build()
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun users(passwordEncoder: PasswordEncoder): UserDetailsService {
        val user = User.builder()
            .username(basicAuthUsername)
            .password(passwordEncoder.encode(basicAuthPassword))
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(user)
    }
}
