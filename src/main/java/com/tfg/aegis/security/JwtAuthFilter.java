package com.tfg.aegis.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

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

/**
 * Este filtro se encarga de interceptar las peticiones entrantes,
 * extraer el token del header Authorization y validar el token.
 * Rechazará con 401 si el token no es válido.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Value("${clerk.jwks-url}")
    private String jwksUrl;

    @Value("${clerk.issuer:https://clerk.clerk.dev}")
    private String expectedIssuer;

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @PostConstruct
    public void init() {
        try {
            // Es mejor usar RemoteJWKSet para actualizaciones automáticas de claves
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUrl));
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource));
            logger.info("JwtAuthFilter initialized successfully with JWKS URL: {}", jwksUrl);
        } catch (Exception e) {
            logger.error("Failed to initialize JwtAuthFilter", e);
            // No lanzamos la excepción para permitir que la aplicación continúe inicializándose
            // El filtro manejará adecuadamente las solicitudes si no está inicializado
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Si no se pudo inicializar el procesador, dejamos pasar la solicitud
        // pero no establecemos la autenticación
        if (jwtProcessor == null) {
            logger.error("JWT processor not initialized, authentication disabled");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // Si no hay header de autorización, continuamos sin autenticación
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found in request");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Procesar y validar el token
            JWTClaimsSet claims = jwtProcessor.process(token, null);

            // Validar que no esté expirado
            Date now = new Date();
            if (claims.getExpirationTime() != null && claims.getExpirationTime().before(now)) {
                logger.warn("Expired token detected");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            }

            // Validar el emisor si está configurado
            if (expectedIssuer != null && !expectedIssuer.equals(claims.getIssuer())) {
                logger.warn("Invalid issuer: {}, expected: {}", claims.getIssuer(), expectedIssuer);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token issuer");
                return;
            }

            // Extraer el ID del usuario
            String userId = claims.getSubject();
            if (userId == null) {
                logger.warn("Token doesn't contain a subject claim");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: missing subject");
                return;
            }

            logger.debug("Valid token for user: {}", userId);

            // Establecer la autenticación en el contexto de seguridad
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Continuar con la cadena de filtros
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