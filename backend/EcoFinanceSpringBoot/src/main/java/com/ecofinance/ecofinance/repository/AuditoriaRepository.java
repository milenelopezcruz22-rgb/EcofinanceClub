package com.ecofinance.ecofinance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ecofinance.ecofinance.entity.Auditoria;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    // Historial más reciente primero, tal como se va a mostrar en pantalla.
    List<Auditoria> findAllByOrderByFechaDesc();
}