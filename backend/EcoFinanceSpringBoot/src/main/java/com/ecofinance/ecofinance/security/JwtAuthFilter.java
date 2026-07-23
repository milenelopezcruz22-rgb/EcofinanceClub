package com.ecofinance.ecofinance.security;

import com.ecofinance.ecofinance.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Filtro JWT (Fase 2 - Tarea 3). Se ejecuta una vez por cada petición HTTP.
// Reutiliza JwtService (validación/lectura del token) y UsuarioService
// (carga del usuario real desde la tabla "usuarios"). No implementa lógica
// JWT propia: solo orquesta las clases ya existentes.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    // Excluye explícitamente los endpoints públicos de autenticación: no tiene
    // sentido que el filtro intente leer/validar un token en /api/auth/registro
    // o /api/auth/login, ya que ahí un usuario todavía no posee uno.
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/registro") || path.equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String headerAutorizacion = request.getHeader("Authorization");

        // Si no viene el header o no tiene el formato "Bearer <token>", se continúa
        // sin autenticar. La decisión de si la ruta requiere autenticación la toma
        // SecurityConfig, no este filtro.
        if (headerAutorizacion == null || !headerAutorizacion.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = headerAutorizacion.substring(7);

        // Si el token no es válido (expirado, mal firmado, corrupto), tampoco se
        // autentica; se deja que la petición continúe sin usuario en el contexto.
        if (!jwtService.validarToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Solo se autentica si aún no hay una autenticación registrada en el
        // contexto de esta petición (evita sobrescribir una autenticación previa).
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtService.obtenerUsuarioDeToken(token);

                if (username != null) {
                    UserDetails userDetails = usuarioService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token válido en firma/expiración, pero apunta a una cuenta que ya
                // no existe o cambió (username renombrado/eliminado). En vez de
                // dejar que la excepción se propague sin control (lo que puede
                // devolver un error inconsistente), simplemente no autenticamos:
                // la petición sigue y SecurityConfig/el authenticationEntryPoint
                // responde 401 de forma predecible, igual que si no hubiera token.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}