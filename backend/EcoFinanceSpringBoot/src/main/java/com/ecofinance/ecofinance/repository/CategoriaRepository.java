package com.ecofinance.ecofinance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ecofinance.ecofinance.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("""
            SELECT c
            FROM Categoria c
            WHERE c.grupo.id=:idGrupo
            """)
    List<Categoria> listarGrupo(@Param("idGrupo") Long idGrupo);
}