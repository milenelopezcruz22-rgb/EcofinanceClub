package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecofinance.ecofinance.dto.PagoDeudaDTO;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.entity.MiembroGrupo;
import com.ecofinance.ecofinance.entity.PagoDeuda;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.GrupoService;
import com.ecofinance.ecofinance.service.MiembroGrupoService;
import com.ecofinance.ecofinance.service.PagoDeudaService;
import com.ecofinance.ecofinance.service.UsuarioService;

@RestController
@RequestMapping("/api/pagos-deuda")
public class PagoDeudaRestController {

    @Autowired
    private PagoDeudaService pagoDeudaService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private MiembroGrupoService miembroService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<List<PagoDeudaDTO>> listarPorGrupo(@PathVariable Long idGrupo, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroService.tieneAccesoAGrupo(usuario, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<PagoDeudaDTO> lista = pagoDeudaService.listarPorGrupo(idGrupo).stream()
                .map(PagoDeudaDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<PagoDeudaDTO> registrar(@Valid @RequestBody PagoDeudaDTO dto, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroService.esGestorDeGrupo(usuario, dto.getGrupoId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Grupo grupo = grupoService.buscar(dto.getGrupoId()).orElse(null);
        if (grupo == null) {
            return ResponseEntity.badRequest().build();
        }

        MiembroGrupo deudor = miembroService.buscarPorId(dto.getDeudorId()).orElse(null);
        if (deudor == null || deudor.getGrupo() == null
                || !deudor.getGrupo().getId().equals(grupo.getId())) {
            return ResponseEntity.badRequest().build();
        }

        MiembroGrupo acreedor = miembroService.buscarPorId(dto.getAcreedorId()).orElse(null);
        if (acreedor == null || acreedor.getGrupo() == null
                || !acreedor.getGrupo().getId().equals(grupo.getId())) {
            return ResponseEntity.badRequest().build();
        }

        PagoDeuda pago = new PagoDeuda();
        pago.setGrupo(grupo);
        pago.setDeudor(deudor);
        pago.setAcreedor(acreedor);
        pago.setMonto(dto.getMonto());
        pago.setFecha(dto.getFecha());
        pago.setNota(dto.getNota());

        PagoDeuda guardado = pagoDeudaService.registrarPago(pago);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PagoDeudaDTO(guardado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        PagoDeuda pago = pagoDeudaService.buscarPorId(id).orElse(null);
        if (pago == null) {
            return ResponseEntity.notFound().build();
        }
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroService.esGestorDeGrupo(usuario, pago.getGrupo().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        pagoDeudaService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }
}