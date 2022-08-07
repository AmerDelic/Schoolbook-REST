package com.amerd.schoolbook.security.filter;

import com.amerd.schoolbook.security.provider.JWTProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.amerd.schoolbook.common.constant.SecurityConstant.TOKEN_PREFIX;

@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JWTProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            response.setStatus(HttpStatus.OK.value());
        } else {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = authHeader.substring(TOKEN_PREFIX.length());
            String username = jwtProvider.getSubject(token);
            SecurityContext securityContext = SecurityContextHolder.getContext();

            if (jwtProvider.isTokenValid(username, token) && securityContext.getAuthentication() == null) {
                List<GrantedAuthority> authorities = jwtProvider.getAuthorities(token);
                Authentication authentication = jwtProvider.getAuthentication(username, authorities, request);
                securityContext.setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
