package com.ecofinance.ecofinance.config;

import com.ecofinance.ecofinance.repository.UsuarioRepository;
import com.ecofinance.ecofinance.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Crea el usuario Administrador General inicial la primera vez que la app
// arranca contra una base de datos vacía (por ejemplo, un MySQL nuevo en
// Railway). Es idempotente: en cada arranque solo verifica si el username ya
// existe, y si es así no hace nada. Por eso es seguro dejarlo desplegado de
// forma permanente, sin necesidad de quitarlo después del primer deploy.
//
// Reutiliza UsuarioService.registrar(...), el mismo método que ya usa el
// endpoint de registro público: la contraseña queda codificada con el mismo
// PasswordEncoder (BCrypt) que usa el resto de la aplicación, sin depender de
// generar un hash a mano en un script SQL aparte.
@Component
public class AdminInicialSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialSeeder.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    // Configurables por variable de entorno (Railway) por si en algún
    // momento se quiere cambiar el usuario/contraseña/email inicial sin
    // tocar código. Si no se definen, se usan los valores por defecto que ya
    // se venían usando en desarrollo (admin1 / admin123).
    @Value("${app.admin-inicial.username:admin1}")
    private String usernameAdmin;

    @Value("${app.admin-inicial.password:admin123}")
    private String passwordAdmin;

    @Value("${app.admin-inicial.email:admin@ecofinanceclub.com}")
    private String emailAdmin;

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByUsername(usernameAdmin)) {
            // Ya existe (deploy anterior, o alguien lo creó a mano): no se
            // toca nada, ni siquiera la contraseña, para no pisar un cambio
            // que el usuario haya hecho manualmente.
            return;
        }

        usuarioService.registrar(usernameAdmin, passwordAdmin, emailAdmin, "Administrador General", "ROLE_ADMIN");
        log.info("Usuario administrador inicial creado: {}", usernameAdmin);
    }
}