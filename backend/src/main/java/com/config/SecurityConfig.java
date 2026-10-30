package com.config;

import com.users.service.TokenService;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokens)
            throws Exception {
        // Bearer credentials are explicit headers, never ambient cookies.
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/actuator/health")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/users", "/users/login")
                                        .permitAll()
                                        .requestMatchers(
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        e -> e.authenticationEntryPoint((req, res, ex) -> res.sendError(401)))
                .addFilterBefore(
                        new BearerTokenFilter(tokens), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
