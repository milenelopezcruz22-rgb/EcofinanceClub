package com.ecofinance.ecofinance.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Configuración de seguridad (Fase 2 - Cierre, cambio de arquitectura).
// El frontend definitivo es Angular, que consume únicamente la API REST bajo
// /api/**. La única fuente de usuarios es la tabla "usuarios" (a través de
// UsuarioService), y la única forma de autenticarse es obtener un JWT en
// /api/auth/login y enviarlo en el header Authorization en cada petición.
//
// Nota: ya no se expone un bean AuthenticationManager. El login REST
// (AuthController) verifica la contraseña de forma manual con
// passwordEncoder.matches(), por lo que ese bean dejó de ser necesario.
@Configuration
public class SecurityConfig {

    // Orígenes permitidos por CORS, configurable por variable de entorno
    // (app.cors.allowed-origins / CORS_ALLOWED_ORIGINS en Railway), separados
    // por coma. Si no se define nada, se usan estos por defecto: el dominio
    // de producción (con y sin "www") y localhost:4200 para seguir probando
    // en desarrollo local sin romper nada.
    @Value("${app.cors.allowed-origins:https://ecofinanceclub.com,https://www.ecofinanceclub.com,http://localhost:4200}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // Activa el CORS configurado arriba.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // La API REST es sin estado: no hay formularios ni sesión de navegador
                // que puedan enviar el token CSRF, por lo que se desactiva por completo.
                .csrf(csrf -> csrf.disable())
                // Sin sesión HTTP: cada petición se autentica de forma independiente
                // a partir del JWT que llega en el header Authorization.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Respuestas JSON estándar para peticiones no autenticadas (401) o sin
                // permisos suficientes (403), ya que no existe ninguna página a la cual
                // redirigir.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\":\"No autenticado. Token ausente o inválido.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403);
                            response.getWriter().write("{\"error\":\"No tiene permisos para acceder a este recurso.\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        // Únicamente registro y login quedan públicos: un usuario todavía
                        // no tiene token cuando llama a estos dos endpoints.
                        .requestMatchers("/api/auth/registro", "/api/auth/login").permitAll()

                        // Recursos estáticos (por si Angular se sirve en algún momento desde
                        // este mismo backend); no requieren autenticación.
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()

                        // --- Gestión de usuarios y permisos ---
                        // Reglas específicas por verbo HTTP, evaluadas ANTES del catch-all
                        // de más abajo (Spring Security usa la primera regla que matchea).
                        // Los GET (listar/consultar) quedan cubiertos por el catch-all: los
                        // 3 roles pueden leer, y qué grupos ve cada quien se filtra dentro
                        // de cada controller (no acá, porque esta configuración no puede
                        // consultar la base de datos).

                        // Listado de cuentas (solo lectura): únicamente el Administrador
                        // General puede verlo, para elegir a qué cuenta vincula un
                        // MiembroGrupo. Ni GESTOR ni MIEMBRO deben poder listar usuarios,
                        // por eso esta regla cubre TODOS los verbos (no solo escritura) y
                        // va antes del catch-all de /api/**.
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // Solo el Administrador General crea, edita o elimina Grupos.
                        .requestMatchers(HttpMethod.POST, "/api/grupos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/grupos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/grupos/**").hasRole("ADMIN")

                        // Administrador General y Administrador de Grupo administran
                        // Miembros, Categorías y Presupuestos (siempre de sus grupos
                        // asignados, eso se valida dentro de cada controller).
                        .requestMatchers(HttpMethod.POST, "/api/miembros/**", "/api/categorias/**", "/api/presupuestos/**")
                        .hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/miembros/**", "/api/categorias/**", "/api/presupuestos/**")
                        .hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/miembros/**", "/api/categorias/**", "/api/presupuestos/**")
                        .hasAnyRole("ADMIN", "GESTOR")

                        // Gastos: el Miembro también puede registrar (crear), pero solo
                        // Administrador General/de Grupo pueden editar o eliminar.
                        .requestMatchers(HttpMethod.POST, "/api/gastos/**").hasAnyRole("ADMIN", "GESTOR", "MIEMBRO")
                        .requestMatchers(HttpMethod.PUT, "/api/gastos/**").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/gastos/**").hasAnyRole("ADMIN", "GESTOR")

                        // Cualquier otra petición a /api/** exige un JWT válido (verificado
                        // por JwtAuthFilter) y que el usuario tenga uno de estos roles.
                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "GESTOR", "MIEMBRO")

                        // Todo lo que no sea /api/** o un recurso estático queda cerrado.
                        .anyRequest().authenticated())
                // Registra el filtro JWT antes del filtro estándar de usuario/contraseña:
                // es el único mecanismo que autentica al usuario en cada petición.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}