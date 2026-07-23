package com.ecofinance.ecofinance.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ecofinance.ecofinance.entity.Auditoria;
import com.ecofinance.ecofinance.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    // Registra una acción sobre Gasto o Presupuesto. Se llama directamente
    // desde GastoService/PresupuestoService en crear/editar/eliminar, sin
    // aspectos ni eventos: un INSERT simple más, como el resto del proyecto.
    public void registrar(String entidad, Long entidadId, String accion, String detalle) {
        Auditoria auditoria = new Auditoria(entidad, entidadId, accion, obtenerUsuarioActual(), detalle);
        auditoriaRepository.save(auditoria);
    }

    public List<Auditoria> listar() {
        return auditoriaRepository.findAllByOrderByFechaDesc();
    }

    // El username autenticado se toma del contexto de seguridad de Spring
    // (el mismo JWT que ya validan los filtros existentes), no de un
    // parámetro nuevo que habría que hacer viajar por todos los services.
    private String obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "sistema";
        }
        return authentication.getName();
    }
}