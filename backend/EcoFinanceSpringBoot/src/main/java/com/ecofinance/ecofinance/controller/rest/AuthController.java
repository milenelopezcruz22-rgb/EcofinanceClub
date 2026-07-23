package com.ecofinance.ecofinance.controller.rest;

import com.ecofinance.ecofinance.dto.AuthResponse;
import com.ecofinance.ecofinance.dto.LoginRequest;
import com.ecofinance.ecofinance.dto.RegistroRequest;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.security.JwtService;
import com.ecofinance.ecofinance.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

// Controlador REST de autenticación. Es el único punto de entrada para obtener
// un token JWT (registro o login). Reutiliza UsuarioService (persistencia de
// usuarios) y JwtService (generación de token).
//
// El login verifica la contraseña de forma manual y explícita con
// passwordEncoder.matches(), en lugar de delegarlo en un AuthenticationManager.
// Para un login tan simple, es una comparación directa y fácil de explicar:
// se busca el usuario, se compara el hash guardado contra la contraseña
// recibida, y si coincide se genera el token.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // POST /api/auth/registro -> crea un nuevo usuario y ya le entrega su token,
    // para que pueda usar la API inmediatamente después de registrarse.
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest request) {
        try {
            Usuario usuario = usuarioService.registrar(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getNombreCompleto(),
                    request.getRol()
            );

            String token = jwtService.generarToken(usuario.getUsername(), usuario.getRol().getNombre());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token, usuario.getUsername(), usuario.getRol().getNombre()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // POST /api/auth/login -> busca el usuario, compara la contraseña recibida
    // contra el hash guardado con BCrypt, y si coincide genera y devuelve el JWT.
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario;

        try {
            usuario = usuarioService.buscarPorUsername(request.getUsername());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos");
        }

        if (!usuario.getHabilitado()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El usuario está deshabilitado");
        }

        boolean passwordCorrecta = passwordEncoder.matches(request.getPassword(), usuario.getPassword());

        if (!passwordCorrecta) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos");
        }

        String rol = usuario.getRol().getNombre();
        String token = jwtService.generarToken(usuario.getUsername(), rol);

        return ResponseEntity.ok(new AuthResponse(token, usuario.getUsername(), rol));
    }
}