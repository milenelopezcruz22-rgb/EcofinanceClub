package com.ecofinance.ecofinance.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import jakarta.servlet.http.HttpServletRequest;

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

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    // Orígenes permitidos por CORS, configurable por variable de entorno
    // (app.cors.allowed-origins / APP_CORS_ALLOWED_ORIGINS en Railway, Spring
    // Boot hace el "relaxed binding" entre ambos formatos automáticamente),
    // separados por coma. Si no se define nada, se usan estos por defecto: el
    // dominio de producción (con y sin "www") y localhost:4200 para seguir
    // probando en desarrollo local sin romper nada.
    @Value("${app.cors.allowed-origins:https://ecofinanceclub.com,https://www.ecofinanceclub.com,http://localhost:4200}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Se recorta cada origen (espacios en blanco y comillas sueltas que
        // suelen quedar pegadas al copiar/pegar el valor en el panel de
        // variables de Railway) para que un espacio de más o una comilla
        // accidental no rompa la comparación exacta del origen.
        List<String> origenes = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .map(o -> o.replaceAll("^[\"']+|[\"']+$", ""))
                .filter(o -> !o.isEmpty())
                .collect(Collectors.toList());

        // Log al arrancar: aparece en los logs de deploy de Railway y permite
        // confirmar, sin adivinar, qué orígenes quedaron cargados realmente
        // en este despliegue.
        log.info("CORS - orígenes permitidos cargados: {}", origenes);

        CorsConfiguration configuration = new CorsConfiguration();
        // setAllowedOriginPatterns en vez de setAllowedOrigins: admite los
        // mismos valores exactos que ya usábamos, pero además permite usar
        // patrones con "*" si en algún momento hiciera falta (por ejemplo
        // subdominios), sin tener que volver a tocar este archivo.
        configuration.setAllowedOriginPatterns(origenes);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Registrado para "/**" (no solo "/api/**") como red de seguridad: si
        // el navegador manda un preflight OPTIONS a una ruta sin el prefijo
        // "/api" (por una URL mal armada en el frontend, por ejemplo), el
        // preflight igual recibe los headers CORS y no queda bloqueado en el
        // navegador. Esto NO afecta autorización ni roles: el bloqueo real de
        // acceso lo sigue haciendo authorizeHttpRequests(), que no se toca.
        source.registerCorsConfiguration("/**", configuration);

        // Envolvemos el source para loguear, EN CADA PETICIÓN REAL (no solo al
        // arrancar), qué header "Origin" llegó y si matchea contra la lista
        // configurada. Esto permite confirmar en los logs de Railway, request
        // por request, si el problema es el origen recibido o algo de
        // infraestructura (réplica vieja, caché de build, etc.).
        return (HttpServletRequest request) -> {
            String origenRecibido = request.getHeader("Origin");
            CorsConfiguration resultado = source.getCorsConfiguration(request);
            boolean permitido = resultado != null && origenRecibido != null
                    && resultado.checkOrigin(origenRecibido) != null;
            log.info("CORS - petición {} {} | Origin recibido: {} | ¿permitido?: {} | orígenes configurados: {}",
                    request.getMethod(), request.getRequestURI(), origenRecibido, permitido, origenes);
            return resultado;
        };
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
                        // El navegador nunca manda credenciales/JWT en un preflight OPTIONS,
                        // así que debe dejarse pasar sin autenticación para TODAS las rutas,
                        // antes que cualquier regla de rol. Esto no cambia ningún permiso:
                        // las peticiones reales (GET/POST/PUT/DELETE) que vengan después del
                        // preflight se siguen validando exactamente igual, con las mismas
                        // reglas de más abajo.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

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