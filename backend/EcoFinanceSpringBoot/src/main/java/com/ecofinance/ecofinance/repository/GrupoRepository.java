package com.ecofinance.ecofinance.repository;

import com.ecofinance.ecofinance.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GrupoRepository
        extends JpaRepository<Grupo,Long>{
            @Query("""
                SELECT g
                FROM Grupo g
                WHERE g.nombre
                LIKE %:nombre%
            """)
            List<Grupo> buscarPorNombre(
                    @Param("nombre") String nombre
            );

            @Query("""
                SELECT COUNT(g)
                FROM Grupo g
                """)
                Long contarGrupos();

            @Query("""
                SELECT g
                FROM Grupo g
                ORDER BY g.nombre
                """)
                List<Grupo> ordenarPorNombre();

}