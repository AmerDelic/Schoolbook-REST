package com.amerd.schoolbook.security.provider;

import com.amerd.schoolbook.security.user.UserPrincipal;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.amerd.schoolbook.common.constant.SecurityConstant.AUTHORITIES;
import static com.amerd.schoolbook.common.constant.SecurityConstant.EXPIRATION_TIME;
import static com.amerd.schoolbook.common.constant.SecurityConstant.ISSUER_INFO;
import static com.amerd.schoolbook.common.constant.SecurityConstant.TOKEN_CANNOT_BE_VERIFIED;
import static com.amerd.schoolbook.common.constant.SecurityConstant.USER_ADMINISTRATION;

@Component
public class JWTProvider {

    @Value("${jwt.provider.secret:}")
    private String secret;

    public String generateJwtToken(UserPrincipal userPrincipal) {
        String[] claims = getClaimsFromUser(userPrincipal);
        Date now = new Date();
        return JWT.create()
                .withIssuer(ISSUER_INFO)
                .withAudience(USER_ADMINISTRATION)
                .withIssuedAt(now)
                .withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(now.getTime() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes(StandardCharsets.UTF_8)));
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }

    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return Arrays.stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null, authorities);

        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return token;
    }

    public boolean isTokenValid(String username, String token) {
        JWTVerifier verifier = getVerifier();
        return StringUtils.hasText(username) && !isTokenExpired(verifier, token);

    }

    public String getSubject(String token) {
        JWTVerifier verifier = getVerifier();
        return verifier.verify(token).getSubject();
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(secret.getBytes());
            verifier = JWT.require(algorithm).withIssuer(ISSUER_INFO).build();
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }
}
