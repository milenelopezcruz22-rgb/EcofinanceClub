package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecofinance.ecofinance.dto.DeudaDTO;
import com.ecofinance.ecofinance.dto.GastoDTO;
import com.ecofinance.ecofinance.dto.SaldoMiembroDTO;
import com.ecofinance.ecofinance.entity.Categoria;
import com.ecofinance.ecofinance.entity.Gasto;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.entity.MiembroGrupo;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.CategoriaService;
import com.ecofinance.ecofinance.service.GastoService;
import com.ecofinance.ecofinance.service.GrupoService;
import com.ecofinance.ecofinance.service.MiembroGrupoService;
import com.ecofinance.ecofinance.service.PagoDeudaService;
import com.ecofinance.ecofinance.service.UsuarioService;

@RestController
@RequestMapping("/api/gastos")
public class GastoRestController {

    @Autowired
    private GastoService gastoService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private MiembroGrupoService miembroService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PagoDeudaService pagoDeudaService;

    // --- Gestión de usuarios y permisos ---
    // Antes este controller no validaba nada: cualquier cuenta autenticada
    // podía leer/escribir los gastos de CUALQUIER grupo. Se agrega el mismo
    // criterio que ya usan Categorías/Presupuestos/Miembros: lectura para
    // cualquiera que pertenezca al grupo (tieneAccesoAGrupo), escritura solo
    // para quien sea GESTOR de ESE grupo puntual (esGestorDeGrupo) o ADMIN.
    @GetMapping
    public List<GastoDTO> listar(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        List<Long> idsAccesibles = miembroService.idsGruposDeUsuario(usuario);
        boolean esAdmin = usuarioService.esAdminGeneral(usuario);
        return gastoService.listarGastos().stream()
                .filter(g -> esAdmin || (g.getGrupo() != null && idsAccesibles.contains(g.getGrupo().getId())))
                .map(GastoDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoDTO> buscar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return gastoService.buscarGasto(id)
                .map(gasto -> {
                    if (gasto.getGrupo() != null && !miembroService.tieneAccesoAGrupo(usuario, gasto.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<GastoDTO>build();
                    }
                    return ResponseEntity.ok(new GastoDTO(gasto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<List<GastoDTO>> listarPorGrupo(@PathVariable Long idGrupo, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroService.tieneAccesoAGrupo(usuario, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<GastoDTO> lista = gastoService.listarGrupo(idGrupo).stream()
                .map(GastoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // Registrar un gasto: según SecurityConfig, ADMIN, GESTOR y MIEMBRO
    // pueden crear gastos (ver comentario en SecurityConfig#filterChain).
    // Aquí solo se exige que el usuario tenga acceso al grupo (igual que en
    // listarPorGrupo/buscar), no que sea su gestor. Editar y eliminar siguen
    // exigiendo esGestorDeGrupo (ver actualizar()/eliminar() más abajo), que
    // no se modifican.
    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody GastoDTO gastoDTO, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        Grupo grupo = grupoService.buscar(gastoDTO.getGrupoId()).orElse(null);
        if (grupo == null) {
            return ResponseEntity.badRequest().body("El grupo indicado no existe.");
        }

        if (!miembroService.tieneAccesoAGrupo(usuario, grupo.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Categoria categoria = categoriaService.buscarPorId(gastoDTO.getCategoriaId()).orElse(null);
        if (categoria == null || categoria.getGrupo() == null
                || !categoria.getGrupo().getId().equals(grupo.getId())) {
            return ResponseEntity.badRequest().build();
        }

        MiembroGrupo pagador = miembroService.buscarPorId(gastoDTO.getPagadorId()).orElse(null);
        if (pagador == null || pagador.getGrupo() == null
                || !pagador.getGrupo().getId().equals(grupo.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Gasto gasto = new Gasto();
        gasto.setDescripcion(gastoDTO.getDescripcion());
        gasto.setMonto(gastoDTO.getMonto());
        gasto.setCategoria(categoria);
        gasto.setPagador(pagador);
        gasto.setImpacto(gastoDTO.getImpacto());
        gasto.setFecha(gastoDTO.getFecha());
        gasto.setGrupo(grupo);

        Gasto guardado = gastoService.guardarGasto(gasto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GastoDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody GastoDTO gastoDTO,
                                                Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        return gastoService.buscarGasto(id)
                .map(gastoExistente -> {
                    if (gastoExistente.getGrupo() != null
                            && !miembroService.esGestorDeGrupo(usuario, gastoExistente.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<GastoDTO>build();
                    }

                    Grupo grupo = grupoService.buscar(gastoDTO.getGrupoId()).orElse(gastoExistente.getGrupo());

                    if (grupo != null && !miembroService.esGestorDeGrupo(usuario, grupo.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<GastoDTO>build();
                    }

                    Categoria categoria = categoriaService.buscarPorId(gastoDTO.getCategoriaId()).orElse(null);
                    if (categoria == null || categoria.getGrupo() == null
                            || !categoria.getGrupo().getId().equals(grupo.getId())) {
                        return ResponseEntity.badRequest().<GastoDTO>build();
                    }

                    MiembroGrupo pagador = miembroService.buscarPorId(gastoDTO.getPagadorId()).orElse(null);
                    if (pagador == null || pagador.getGrupo() == null
                            || !pagador.getGrupo().getId().equals(grupo.getId())) {
                        return ResponseEntity.badRequest().<GastoDTO>build();
                    }

                    gastoExistente.setDescripcion(gastoDTO.getDescripcion());
                    gastoExistente.setMonto(gastoDTO.getMonto());
                    gastoExistente.setCategoria(categoria);
                    gastoExistente.setPagador(pagador);
                    gastoExistente.setImpacto(gastoDTO.getImpacto());
                    gastoExistente.setFecha(gastoDTO.getFecha());
                    gastoExistente.setGrupo(grupo);

                    Gasto actualizado = gastoService.guardarGasto(gastoExistente);
                    return ResponseEntity.ok(new GastoDTO(actualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        return gastoService.buscarGasto(id)
                .map(gasto -> {
                    if (gasto.getGrupo() != null
                            && !miembroService.esGestorDeGrupo(usuario, gasto.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    gastoService.eliminarGasto(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // RF23 + RF25: saldo neto por miembro, ya ajustado con los pagos de
    // deuda registrados (PagoDeudaService.calcularSaldosAjustados). La
    // fórmula original de GastoService.calcularSaldos no se modifica; este
    // endpoint solo cambia de dónde toma el resultado final.
    @GetMapping("/grupo/{idGrupo}/saldos")
    public ResponseEntity<List<SaldoMiembroDTO>> saldosPorGrupo(@PathVariable Long idGrupo, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroService.tieneAccesoAGrupo(usuario, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(pagoDeudaService.calcularSaldosAjustados(idGrupo));
    }

    // RF24 + RF25: "quién le debe a quién", ya descontando los pagos de
    // deuda registrados (PagoDeudaService.calcularDeudasPendientes). El
    // algoritmo greedy de GastoService.simplificarDeudas no se modifica;
    // este endpoint solo cambia de dónde toma el resultado final.
    @GetMapping("/grupo/{idGrupo}/deudas")
    public ResponseEntity<List<DeudaDTO>> deudasPorGrupo(@PathVariable Long idGrupo, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroService.tieneAccesoAGrupo(usuario, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(pagoDeudaService.calcularDeudasPendientes(idGrupo));
    }
}