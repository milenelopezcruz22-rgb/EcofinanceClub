package com.ecofinance.ecofinance.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

        // Clave secreta robusta de 256 bits. Configurable por variable de entorno
        // (app.jwt.secret / JWT_SECRET en Railway) para producción; si no se
        // define ninguna, se usa el mismo valor que ya estaba hardcodeado, por lo
        // que el comportamiento en desarrollo local no cambia.
        @Value("${app.jwt.secret:ecofinance-seguridad-jwt-2026-super-clave-secreta-cambiada}")
        private String secreto;

        private SecretKey key;

        private SecretKey obtenerKey() {
                if (key == null) {
                        key = Keys.hmacShaKeyFor(secreto.getBytes());
                }
                return key;
        }

        // Corregido: Ahora recibe también el rol para meterlo en el Payload del JWT
        public String generarToken(String usuario, String rol) {
                return Jwts.builder()
                                .subject(usuario)
                                .claims(Map.of("role", rol, "app", "EcoFinance")) // Claims personalizados
                                .issuedAt(new Date())
                                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hora de validez
                                .signWith(obtenerKey())
                                .compact();
        }

        public boolean validarToken(String token) {
                try {
                        Jwts.parser()
                                        .verifyWith(obtenerKey())
                                        .build()
                                        .parseSignedClaims(token);
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        // Agregado: Útil para mostrar en la interfaz quién es el dueño del Token
        public String obtenerUsuarioDeToken(String token) {
                return Jwts.parser()
                                .verifyWith(obtenerKey())
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .getSubject();
        }
}