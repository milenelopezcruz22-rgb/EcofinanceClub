package com.ecofinance.ecofinance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ecofinance.ecofinance.entity.PagoDeuda;

@Repository
public interface PagoDeudaRepository extends JpaRepository<PagoDeuda, Long> {

        // Usado para validar, ANTES de intentar borrar un MiembroGrupo, si todavía
        // aparece en algún pago de deuda (como deudor o como acreedor).
        boolean existsByDeudorIdOrAcreedorId(Long deudorId, Long acreedorId);

        @Query("""
                SELECT p
                FROM PagoDeuda p
                WHERE p.grupo.id = :idGrupo
                ORDER BY p.fecha DESC, p.id DESC
                """)
                List<PagoDeuda> listarPorGrupo(
                        @Param("idGrupo") Long idGrupo
                );

        // Total pagado por cada miembro (como deudor) dentro del grupo.
        // Se usa en PagoDeudaService para ajustar el saldo neto (RF23) hacia arriba.
        @Query("""
                SELECT p.deudor.id, SUM(p.monto)
                FROM PagoDeuda p
                WHERE p.grupo.id = :idGrupo
                GROUP BY p.deudor.id
                """)
                List<Object[]> totalPagadoPorMiembro(
                        @Param("idGrupo") Long idGrupo
                );

        // Total recibido por cada miembro (como acreedor) dentro del grupo.
        // Se usa en PagoDeudaService para ajustar el saldo neto (RF23) hacia abajo.
        @Query("""
                SELECT p.acreedor.id, SUM(p.monto)
                FROM PagoDeuda p
                WHERE p.grupo.id = :idGrupo
                GROUP BY p.acreedor.id
                """)
                List<Object[]> totalRecibidoPorMiembro(
                        @Param("idGrupo") Long idGrupo
                );

        // Total liquidado en el grupo (para el bloque de Dashboard, RF25).
        @Query("""
                SELECT SUM(p.monto)
                FROM PagoDeuda p
                WHERE p.grupo.id = :idGrupo
                """)
                Double totalLiquidadoGrupo(
                        @Param("idGrupo") Long idGrupo
                );
}