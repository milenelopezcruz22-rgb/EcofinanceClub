package com.ecofinance.ecofinance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ecofinance.ecofinance.entity.Presupuesto;

public interface PresupuestoRepository
        extends JpaRepository<Presupuesto, Long> {
                @Query("""
                        SELECT SUM(p.limiteGasto)
                        FROM Presupuesto p
                        """)
                        Double totalPresupuesto();

                @Query("""
                        SELECT COUNT(p)
                        FROM Presupuesto p
                        """)
                        Long cantidadPresupuestos();

                @Query("""
                        SELECT COUNT(p)
                        FROM Presupuesto p
                        WHERE p.gastoActual > p.limiteGasto
                        """)
                        Long cantidadPresupuestosExcedidos();

                @Query("""
                        SELECT p
                        FROM Presupuesto p
                        WHERE p.grupo.id=:idGrupo
                        """)
                        List<Presupuesto> findByGrupoId(
                                @Param("idGrupo") Long idGrupo
                        );

                // Bugfix: usado por PresupuestoService para impedir que un mismo
                // grupo tenga dos presupuestos para la misma categoría. El id se
                // excluye para que, al editar un presupuesto existente, no se
                // choque consigo mismo.
                boolean existsByGrupoIdAndCategoriaIdAndIdNot(Long grupoId, Long categoriaId, Long id);

}