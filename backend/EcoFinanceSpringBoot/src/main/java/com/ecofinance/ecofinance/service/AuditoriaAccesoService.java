package com.ecofinance.ecofinance.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ecofinance.ecofinance.entity.AuditoriaAcceso;
import com.ecofinance.ecofinance.repository.AuditoriaAccesoRepository;

@Service
public class AuditoriaAccesoService {

    @Autowired
    private AuditoriaAccesoRepository auditoriaAccesoRepository;

    // Registra que el usuario autenticado entró a "modulo". El usuario y el
    // rol se toman del contexto de seguridad (el mismo JWT que ya validan
    // los filtros existentes); el grupo es el que informa el frontend
    // (contexto de grupo seleccionado en pantalla), y puede venir vacío.
    public void registrar(String modulo, String grupo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuario = obtenerUsuarioActual(authentication);
        String rol = obtenerRolActual(authentication);
        String grupoNormalizado = (grupo == null || grupo.isBlank()) ? null : grupo;

        AuditoriaAcceso acceso = new AuditoriaAcceso(usuario, rol, grupoNormalizado, modulo);
        auditoriaAccesoRepository.save(acceso);
    }

    public List<AuditoriaAcceso> listar() {
        return auditoriaAccesoRepository.findAllByOrderByFechaDesc();
    }

    private String obtenerUsuarioActual(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "sistema";
        }
        return authentication.getName();
    }

    private String obtenerRolActual(Authentication authentication) {
        if (authentication == null) {
            return "-";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("-");
    }
}
