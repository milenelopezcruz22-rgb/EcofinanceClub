package com.ecofinance.ecofinance.repository;

import com.ecofinance.ecofinance.entity.MiembroGrupo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MiembroGrupoRepository
        extends JpaRepository<MiembroGrupo,Long> {
            @Query("""
                SELECT m
                FROM MiembroGrupo m
                WHERE m.grupo.id=:idGrupo
                """)
                List<MiembroGrupo> listarGrupo(@Param("idGrupo") Long idGrupo);

            @Query("""
                SELECT COUNT(m)
                FROM MiembroGrupo m
                WHERE m.grupo.id=:idGrupo
                """)
                Long totalMiembros(@Param("idGrupo") Long idGrupo);

            @Query("""
                SELECT COUNT(m)
                FROM MiembroGrupo m
                """)
                Long contarMiembros();

            @Query("""
                SELECT m
                FROM MiembroGrupo m
                WHERE m.rolGrupo='ADMIN'
                """)
                List<MiembroGrupo> administradores();

            @Query("""
                SELECT m
                FROM MiembroGrupo m
                WHERE m.grupo.id=:grupo
                """)
                List<MiembroGrupo> miembrosGrupo(
                        @Param("grupo")
                        Long grupo
                );

            // --- Gestión de usuarios y permisos ---
            // ¿Esta cuenta (usuarioId) tiene acceso a este grupo puntual?
            // Spring Data arma la consulta solo a partir del nombre del
            // método (misma idea que existsByUsername en UsuarioRepository).
            boolean existsByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);

            // Todos los grupos a los que una cuenta (Gestor o Miembro) está
            // vinculada, usado para filtrar qué ve cada quien.
            List<MiembroGrupo> findByUsuarioId(Long usuarioId);

            // --- Rol DENTRO de un grupo (distinto del rol global del Usuario) ---
            // ¿Esta cuenta es GESTOR (administrador de grupo) en ESTE grupo
            // puntual? Una misma cuenta puede ser GESTOR de un grupo y MIEMBRO
            // de otro; por eso esto se consulta por grupo_id, no por el rol
            // global guardado en "usuarios".
            boolean existsByUsuarioIdAndGrupoIdAndRolGrupo(Long usuarioId, Long grupoId, String rolGrupo);

}