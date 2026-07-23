package com.ecofinance.ecofinance.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ecofinance.ecofinance.entity.Gasto;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

        // Usado para validar, ANTES de intentar borrar un MiembroGrupo, si todavía
        // tiene Gastos registrados a su nombre (como pagador). Permite devolver un
        // mensaje 409 específico en vez de esperar a que la base de datos rechace
        // el DELETE por la clave foránea.
        boolean existsByPagadorId(Long pagadorId);
        @Query("""
                SELECT g
                FROM Gasto g
                WHERE g.grupo.id = :idGrupo
                """)
                List<Gasto> listarPorGrupo(
                        @Param("idGrupo") Long idGrupo
                );

        @Query("""
                SELECT SUM(g.monto)
                FROM Gasto g
                WHERE g.grupo.id=:idGrupo
                """)
                Double totalGrupo(
                        @Param("idGrupo") Long idGrupo
                );

        @Query("""
                SELECT SUM(g.monto)
                FROM Gasto g
                WHERE g.grupo.id=:idGrupo
                AND g.categoria.id=:idCategoria
                """)
                Double totalGrupoCategoria(
                        @Param("idGrupo") Long idGrupo,
                        @Param("idCategoria") Long idCategoria
                );

        @Query("""
                SELECT COUNT(g)
                FROM Gasto g
                WHERE g.grupo.id=:idGrupo
                """)
                Long cantidadGrupo(
                        @Param("idGrupo") Long idGrupo
                );

        @Query("""
                SELECT SUM(g.monto)
                FROM Gasto g
                """)
                Double totalGastado();

        @Query("""
                SELECT COUNT(g)
                FROM Gasto g
                """)
                Long cantidadGastos();

        @Query("""
                SELECT g.grupo.id
                FROM Gasto g
                GROUP BY g.grupo.id
                ORDER BY SUM(g.monto) DESC
                """)
                List<Long> gruposPorGastoDesc(Pageable pageable);

        @Query("""
                SELECT AVG(g.monto)
                FROM Gasto g
                """)
                Double promedioGastos();

        @Query("""
                SELECT SUM(g.monto)
                FROM Gasto g
                WHERE g.grupo.id=:idGrupo
                """)
                Double totalGastadoGrupo(
                        @Param("idGrupo") Long idGrupo
                );

        @Query("""
                SELECT AVG(g.monto)
                FROM Gasto g
                WHERE g.grupo.id=:idGrupo
                """)
                Double promedioGrupo(
                        @Param("idGrupo") Long idGrupo
                );

        @Query("""
                SELECT MAX(g.monto)
                FROM Gasto g
                WHERE g.grupo.id=:idGrupo
                """)
                Double gastoMayor(
                        @Param("idGrupo") Long idGrupo
                );

        @Query("""
                SELECT MIN(g.monto)
                FROM Gasto g
                WHERE g.grupo.id=:idGrupo
                """)
                Double gastoMenor(
                        @Param("idGrupo") Long idGrupo
                );

        @Query("""
                SELECT g
                FROM Gasto g
                WHERE g.grupo.id=:idGrupo
                ORDER BY g.monto DESC
                LIMIT 1
                """)
                List<Gasto> gastoMayorGrupo(
                        @Param("idGrupo")
                        Long idGrupo
                );

        @Query("""
                SELECT g.categoria.id, g.categoria.nombre, SUM(g.monto)
                FROM Gasto g
                WHERE g.grupo.id = :idGrupo
                AND g.categoria IS NOT NULL
                GROUP BY g.categoria.id, g.categoria.nombre
                ORDER BY SUM(g.monto) DESC
                """)
                List<Object[]> gastoPorCategoria(
                        @Param("idGrupo") Long idGrupo
                );

        @Query("""
                SELECT g.pagador.id, g.pagador.nombre, SUM(g.monto)
                FROM Gasto g
                WHERE g.grupo.id = :idGrupo
                AND g.pagador IS NOT NULL
                GROUP BY g.pagador.id, g.pagador.nombre
                ORDER BY SUM(g.monto) DESC
                """)
                List<Object[]> gastoPorMiembro(
                        @Param("idGrupo") Long idGrupo
                );

        // Saldo neto por miembro (Bloque 6): cada gasto ya se divide en partes
        // iguales entre todos los miembros del grupo (montoPersona). La suma de
        // montoPersona de todos los gastos del grupo es, entonces, la cuota que
        // le corresponde pagar a CADA miembro (es el mismo valor para todos,
        // porque el reparto es equitativo).
        @Query("""
                SELECT SUM(g.montoPersona)
                FROM Gasto g
                WHERE g.grupo.id = :idGrupo
                """)
                Double sumaMontoPersonaGrupo(
                        @Param("idGrupo") Long idGrupo
                );

        // Nuevo: gasto por categoría, pero incluyendo el nivel de impacto ambiental
        // de esa categoría. Se usa para el bloque de sostenibilidad del Dashboard:
        // de acá se calculan tanto la distribución por nivel (Bajo/Medio/Alto)
        // como las "categorías con mayor impacto", sin necesitar una segunda query.
        // Las categorías con impactoAmbiental = null (creadas antes de este campo,
        // todavía sin clasificar) quedan afuera a propósito.
        @Query("""
                SELECT g.categoria.nombre, g.categoria.impactoAmbiental, SUM(g.monto)
                FROM Gasto g
                WHERE g.grupo.id = :idGrupo
                AND g.categoria IS NOT NULL
                AND g.categoria.impactoAmbiental IS NOT NULL
                GROUP BY g.categoria.id, g.categoria.nombre, g.categoria.impactoAmbiental
                ORDER BY SUM(g.monto) DESC
                """)
                List<Object[]> gastoPorCategoriaConImpacto(
                        @Param("idGrupo") Long idGrupo
                );

        // RF23 (saldo neto por miembro): a diferencia de gastoPorMiembro (que solo
        // suma lo pagado), acá se listan TODOS los miembros del grupo -aunque no
        // hayan pagado ningún gasto todavía- para que el saldo neto se pueda
        // calcular en el service sin perder a nadie de la lista. Se hace con un
        // LEFT JOIN desde MiembroGrupo hacia Gasto (y no al revés) por eso mismo.
        @Query("""
                SELECT m.id, m.nombre, COALESCE(SUM(g.monto), 0)
                FROM MiembroGrupo m
                LEFT JOIN Gasto g ON g.pagador.id = m.id
                WHERE m.grupo.id = :idGrupo
                GROUP BY m.id, m.nombre
                ORDER BY m.nombre ASC
                """)
                List<Object[]> pagadoPorMiembroGrupo(
                        @Param("idGrupo") Long idGrupo
                );

        // RF27 (tendencia mensual): gasto total agrupado por año-mes de la fecha
        // del gasto. FUNCTION('DATE_FORMAT', ...) es la forma estándar de JPQL de
        // usar una función nativa de MySQL sin escribir SQL nativo aparte.
        @Query("""
                SELECT FUNCTION('DATE_FORMAT', g.fecha, '%Y-%m'), SUM(g.monto)
                FROM Gasto g
                WHERE g.grupo.id = :idGrupo
                GROUP BY FUNCTION('DATE_FORMAT', g.fecha, '%Y-%m')
                ORDER BY FUNCTION('DATE_FORMAT', g.fecha, '%Y-%m') ASC
                """)
                List<Object[]> tendenciaMensualGrupo(
                        @Param("idGrupo") Long idGrupo
                );
}