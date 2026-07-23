package com.ecofinance.ecofinance.service;

import com.ecofinance.ecofinance.entity.Rol;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.repository.RolRepository;
import com.ecofinance.ecofinance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Usado por Spring Security en el proceso de autenticación (login con formLogin
    // y también para la autenticación manual que hará el login REST en la Tarea 2).
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(usuario.getRol().getNombre())))
                .disabled(!usuario.getHabilitado())
                .build();
    }

    // Registro de un nuevo usuario. Por defecto asigna el rol MIEMBRO si no se indica
    // otro. La contraseña se recibe en texto plano y se codifica aquí antes de guardarla.
    public Usuario registrar(String username, String passwordPlano, String email, String nombreCompleto, String nombreRol) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        String rolBuscado = (nombreRol == null || nombreRol.isBlank()) ? "ROLE_MIEMBRO" : nombreRol;

        Rol rol = rolRepository.findByNombre(rolBuscado)
                .orElseGet(() -> rolRepository.save(new Rol(rolBuscado)));

        Usuario usuario = new Usuario(
                username,
                passwordEncoder.encode(passwordPlano),
                email,
                nombreCompleto,
                rol
        );

        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    // --- Gestión de usuarios y permisos ---
    // Reutilizado por todos los controllers que necesitan saber "quién está
    // haciendo esta petición": Spring ya resuelve el Authentication solo con
    // declararlo como parámetro del método del controller (no hace falta
    // configuración adicional), y authentication.getName() es el username
    // que ya viaja dentro del JWT.
    public Usuario obtenerPorAuthentication(Authentication authentication) {
        return buscarPorUsername(authentication.getName());
    }

    // El Administrador General no pertenece a ningún grupo puntual: ve y
    // administra todo sin pasar por el chequeo de acceso por grupo.
    public boolean esAdminGeneral(Usuario usuario) {
        return "ROLE_ADMIN".equals(usuario.getRol().getNombre());
    }

    // --- Sincronización de rol global con la asignación dentro de un grupo ---
    // El sistema tiene DOS roles con nombres parecidos que antes nunca se
    // conectaban: Usuario.rol (global, va en el JWT, lo valida Spring
    // Security y lo lee el menú de Angular) y MiembroGrupo.rolGrupo (solo
    // dentro de esa asignación puntual a un grupo). Al asignar/editar una
    // fila de miembro_grupo con rolGrupo="GESTOR", promovemos también el rol
    // global de esa cuenta a ROLE_GESTOR (creando la fila en "roles" si
    // todavía no existe, igual que ya hace registrar()). Si se reasigna como
    // "MIEMBRO", se revierte el rol global -- salvo que la cuenta sea
    // ROLE_ADMIN, a la que nunca se le toca el rol por esta vía.
    //
    // Limitación conocida (heredada del diseño de un solo rol global por
    // cuenta): si una misma persona fuera GESTOR de un grupo y MIEMBRO de
    // otro, el rol global refleja SIEMPRE la última asignación que se le
    // hizo, no un rol distinto por grupo. Es la limitación explícita del
    // modelo actual, no un efecto colateral de este método.
    public void sincronizarRolConAsignacion(Usuario usuario, String rolGrupo) {
        if (esAdminGeneral(usuario)) {
            return;
        }

        String rolDestino = "GESTOR".equals(rolGrupo) ? "ROLE_GESTOR" : "ROLE_MIEMBRO";

        if (rolDestino.equals(usuario.getRol().getNombre())) {
            return;
        }

        Rol rol = rolRepository.findByNombre(rolDestino)
                .orElseGet(() -> rolRepository.save(new Rol(rolDestino)));

        usuario.setRol(rol);
        usuarioRepository.save(usuario);
    }

    // Agregado: listado de solo lectura de todas las cuentas, usado
    // únicamente por UsuarioRestController para que el Administrador General
    // pueda elegir a cuál vincular un MiembroGrupo.
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    // Usuarios (no ADMIN) sin ninguna fila en miembro_grupo todavía. Ver
    // UsuarioRepository.buscarSinGrupoAsignado() para el detalle de la query.
    public List<Usuario> listarSinGrupo() {
        return usuarioRepository.buscarSinGrupoAsignado();
    }

    // Agregado: búsqueda por id, usada por MiembroGrupoRestController para
    // resolver el usuarioId que llega en MiembroGrupoDTO al crear/editar un
    // miembro.
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}