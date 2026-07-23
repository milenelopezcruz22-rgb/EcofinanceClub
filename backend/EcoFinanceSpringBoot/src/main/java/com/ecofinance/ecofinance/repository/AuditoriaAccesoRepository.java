package com.ecofinance.ecofinance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ecofinance.ecofinance.entity.AuditoriaAcceso;

@Repository
public interface AuditoriaAccesoRepository extends JpaRepository<AuditoriaAcceso, Long> {

    // Historial más reciente primero, tal como se muestra en la pantalla de
    // Accesos. Los filtros (usuario, rol, fecha) se aplican en el frontend
    // sobre esta misma lista, no acá: para el volumen de datos de un
    // proyecto universitario no hace falta una consulta más compleja.
    List<AuditoriaAcceso> findAllByOrderByFechaDesc();
}
