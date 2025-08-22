// File: JwtAuthFilter.java
package com.tfg.aegis.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.util.DefaultResourceRetriever;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Value("${clerk.jwks-url}")
    private String jwksUrl;

    @Value("${clerk.issuer:https://clerk.clerk.dev}")
    private String expectedIssuer;

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @PostConstruct
    public void init() {
        try {
            var resourceRetriever = new DefaultResourceRetriever(2000, 2000);
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUrl), resourceRetriever);
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource));
            log.info("JwtAuthFilter initialized successfully with JWKS URL: {}", jwksUrl);
        } catch (Exception e) {
            log.error("Failed to initialize JwtAuthFilter", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (jwtProcessor == null) {
            logger.error("JWT processor not initialized, authentication disabled");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found in request");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            JWTClaimsSet claims = jwtProcessor.process(token, null);

            Date now = new Date();
            if (claims.getExpirationTime() != null && claims.getExpirationTime().before(now)) {
                logger.warn("Expired token detected");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            }

            if (expectedIssuer != null && !expectedIssuer.equals(claims.getIssuer())) {
                log.warn("Invalid issuer: {}, expected: {}", claims.getIssuer(), expectedIssuer);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token issuer");
                return;
            }

            String userId = claims.getSubject();
            if (userId == null) {
                log.warn("Token doesn't contain a subject claim");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: missing subject");
                return;
            }

            log.debug("Valid token for user: {}", userId);

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (ParseException e) {
            logger.warn("Error parsing JWT token", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
        } catch (Exception e) {
            logger.warn("JWT validation error", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }
}
