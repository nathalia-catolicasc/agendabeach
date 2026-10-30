package com.config;

import com.users.service.TokenService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class BearerTokenFilter extends OncePerRequestFilter {
    private final TokenService tokens;

    public BearerTokenFilter(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            tokens.authenticate(header.substring(7))
                    .ifPresent(
                            user -> {
                                var auth =
                                        new UsernamePasswordAuthenticationToken(
                                                user.getId(),
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_"
                                                                        + user.getProfile()
                                                                                .name())));
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            });
        }
        chain.doFilter(request, response);
    }
}
