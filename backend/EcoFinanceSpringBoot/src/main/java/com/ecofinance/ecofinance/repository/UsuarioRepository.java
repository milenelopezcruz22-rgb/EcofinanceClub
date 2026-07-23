package com.ecofinance.ecofinance.repository;

import com.ecofinance.ecofinance.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Usuarios que todavía no tienen ninguna fila en miembro_grupo (no están
    // asignados a ningún grupo). Se excluye ROLE_ADMIN porque el
    // Administrador General no se asigna a grupos puntuales, ve todo. Se usa
    // para llenar, en el panel de ADMIN -> Miembros, el selector de "cuentas
    // disponibles para asignar a un grupo".
    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.rol.nombre <> 'ROLE_ADMIN'
        AND u.id NOT IN (
            SELECT mg.usuario.id
            FROM MiembroGrupo mg
            WHERE mg.usuario IS NOT NULL
        )
        ORDER BY u.username ASC
        """)
    List<Usuario> buscarSinGrupoAsignado();
}
