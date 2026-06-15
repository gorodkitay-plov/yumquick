package com.yumquick.security.filter;

import com.yumquick.security.jwt.JwtTokenProvider;
import com.yumquick.security.jwt.TokenBlacklistService;
import com.yumquick.user.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)
                && jwtTokenProvider.validateToken(token)
                && !tokenBlacklistService.isBlacklisted(token)) {

            Claims claims = jwtTokenProvider.parseClaims(token);
            String type = claims.get("type", String.class);

            // Только access token даёт аутентификацию
            if ("access".equals(type)) {
                UUID userId = UUID.fromString(claims.getSubject());
                String role = claims.get("role", String.class);

                var authority = new SimpleGrantedAuthority("ROLE_" + role);
                var auth = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(authority)
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
