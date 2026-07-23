package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecofinance.ecofinance.dto.MiembroGrupoDTO;
import com.ecofinance.ecofinance.dto.MiRolGrupoDTO;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.entity.MiembroGrupo;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.GrupoService;
import com.ecofinance.ecofinance.service.MiembroGrupoService;
import com.ecofinance.ecofinance.service.UsuarioService;

@RestController
@RequestMapping("/api/miembros")
public class MiembroGrupoRestController {

    @Autowired
    private MiembroGrupoService miembroService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

    // --- Gestión de usuarios y permisos ---
    // Administrador General: ve todo. Administrador de Grupo / Miembro: solo
    // los miembros que pertenecen a un grupo donde tienen una fila de
    // MiembroGrupo vinculada a su cuenta (usuario_id). Mismo criterio que
    // GrupoRestController.filtrarAccesibles.
    private List<MiembroGrupo> filtrarAccesibles(List<MiembroGrupo> miembros, Usuario usuario) {
        if (usuarioService.esAdminGeneral(usuario)) {
            return miembros;
        }
        List<Long> idsAccesibles = miembroService.idsGruposDeUsuario(usuario);
        return miembros.stream()
                .filter(m -> m.getGrupo() != null && idsAccesibles.contains(m.getGrupo().getId()))
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<MiembroGrupoDTO> listar(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return filtrarAccesibles(miembroService.listar(), usuario).stream()
                .map(MiembroGrupoDTO::new)
                .collect(Collectors.toList());
    }

    // Rol de la cuenta autenticada EN CADA UNO de sus grupos (MIEMBRO o
    // GESTOR). El frontend usa esto -y no el rol global del JWT- para
    // decidir qué módulos/acciones mostrar por grupo.
    @GetMapping("/mi-rol")
    public List<MiRolGrupoDTO> miRol(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return miembroService.misAsignaciones(usuario).stream()
                .filter(m -> m.getGrupo() != null)
                .map(MiRolGrupoDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MiembroGrupoDTO> buscar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return miembroService.buscarPorId(id)
                .map(miembro -> {
                    if (miembro.getGrupo() != null
                            && !miembroService.tieneAccesoAGrupo(usuario, miembro.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<MiembroGrupoDTO>build();
                    }
                    return ResponseEntity.ok(new MiembroGrupoDTO(miembro));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<List<MiembroGrupoDTO>> listarPorGrupo(@PathVariable Long idGrupo, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroService.tieneAccesoAGrupo(usuario, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<MiembroGrupoDTO> lista = miembroService.listarGrupo(idGrupo).stream()
                .map(MiembroGrupoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody MiembroGrupoDTO miembroDTO, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        Grupo grupo = null;
        if (miembroDTO.getGrupoId() != null) {
            grupo = grupoService.buscar(miembroDTO.getGrupoId()).orElse(null);
            if (grupo == null) {
                return ResponseEntity.badRequest().body("El grupo indicado no existe.");
            }
        } else {
            return ResponseEntity.badRequest().body("Debe seleccionar un grupo.");
        }

        // Un Administrador de Grupo (GESTOR) solo puede asignar dentro de un
        // grupo al que esté vinculado; el Administrador General no tiene
        // esta restricción.
        if (!miembroService.tieneAccesoAGrupo(usuario, grupo.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // --- Flujo correcto: asignar una cuenta YA REGISTRADA a un grupo ---
        // Ya no se escriben nombre/correo a mano: se derivan de la cuenta
        // real en "usuarios". usuarioId ahora es obligatorio.
        if (miembroDTO.getUsuarioId() == null) {
            return ResponseEntity.badRequest().body(
                "Debe seleccionar un usuario registrado para asignarlo al grupo."
            );
        }

        // Solo el Administrador General puede vincular una cuenta a un grupo.
        if (!usuarioService.esAdminGeneral(usuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Usuario cuenta = usuarioService.buscarPorId(miembroDTO.getUsuarioId()).orElse(null);
        if (cuenta == null) {
            return ResponseEntity.badRequest().body("La cuenta de usuario indicada no existe.");
        }

        String rolGrupo = miembroDTO.getRolGrupo();
        if (!"MIEMBRO".equals(rolGrupo) && !"GESTOR".equals(rolGrupo)) {
            return ResponseEntity.badRequest().body("El rol dentro del grupo debe ser MIEMBRO o GESTOR.");
        }

        MiembroGrupo miembro = new MiembroGrupo();
        miembro.setNombre(cuenta.getNombreCompleto() != null ? cuenta.getNombreCompleto() : cuenta.getUsername());
        miembro.setCorreo(cuenta.getEmail());
        miembro.setRolGrupo(rolGrupo);
        miembro.setGrupo(grupo);
        miembro.setUsuario(cuenta);

        MiembroGrupo guardado = miembroService.guardar(miembro);

        // Sincroniza el rol GLOBAL de la cuenta (Usuario.rol / JWT) con el rol
        // que se le acaba de asignar dentro de este grupo. Sin esto, un
        // usuario marcado como GESTOR en miembro_grupo seguiría autenticando
        // y viendo el menú como MIEMBRO hasta que alguien lo cambiara a mano.
        usuarioService.sincronizarRolConAsignacion(cuenta, rolGrupo);

        return ResponseEntity.status(HttpStatus.CREATED).body(new MiembroGrupoDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MiembroGrupoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody MiembroGrupoDTO miembroDTO,
                                                       Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        return miembroService.buscarPorId(id)
                .map(miembroExistente -> {
                    // No puede tocar un miembro de un grupo al que no tiene acceso.
                    if (miembroExistente.getGrupo() != null
                            && !miembroService.tieneAccesoAGrupo(usuario, miembroExistente.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<MiembroGrupoDTO>build();
                    }

                    Grupo grupo = miembroExistente.getGrupo();
                    if (miembroDTO.getGrupoId() != null) {
                        grupo = grupoService.buscar(miembroDTO.getGrupoId()).orElse(grupo);
                    }

                    // Tampoco puede reasignar el miembro a un grupo ajeno.
                    if (grupo != null && !miembroService.tieneAccesoAGrupo(usuario, grupo.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<MiembroGrupoDTO>build();
                    }

                    if (miembroDTO.getRolGrupo() != null) {
                        if (!"MIEMBRO".equals(miembroDTO.getRolGrupo()) && !"GESTOR".equals(miembroDTO.getRolGrupo())) {
                            return ResponseEntity.badRequest().<MiembroGrupoDTO>build();
                        }
                        miembroExistente.setRolGrupo(miembroDTO.getRolGrupo());
                    }
                    miembroExistente.setGrupo(grupo);

                    // --- Gestión de usuarios y permisos ---
                    // Igual que en guardar(): solo el Administrador General puede
                    // asignar o cambiar la cuenta vinculada. Si el DTO no trae
                    // usuarioId, se deja intacto el vínculo que ya tenía. nombre
                    // y correo se derivan siempre de la cuenta real, nunca se
                    // escriben a mano.
                    if (miembroDTO.getUsuarioId() != null) {
                        if (!usuarioService.esAdminGeneral(usuario)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<MiembroGrupoDTO>build();
                        }
                        Usuario cuenta = usuarioService.buscarPorId(miembroDTO.getUsuarioId()).orElse(null);
                        if (cuenta == null) {
                            return ResponseEntity.badRequest().<MiembroGrupoDTO>build();
                        }
                        miembroExistente.setUsuario(cuenta);
                        miembroExistente.setNombre(cuenta.getNombreCompleto() != null ? cuenta.getNombreCompleto() : cuenta.getUsername());
                        miembroExistente.setCorreo(cuenta.getEmail());
                    }

                    MiembroGrupo actualizado = miembroService.guardar(miembroExistente);

                    // Igual que en guardar(): mantiene sincronizado el rol global
                    // de la cuenta vinculada con el rolGrupo que le acaban de
                    // asignar (por si el ADMIN editó a un MIEMBRO existente para
                    // convertirlo en GESTOR de su grupo, o viceversa).
                    if (actualizado.getUsuario() != null) {
                        usuarioService.sincronizarRolConAsignacion(actualizado.getUsuario(), actualizado.getRolGrupo());
                    }

                    return ResponseEntity.ok(new MiembroGrupoDTO(actualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        return miembroService.buscarPorId(id)
                .map(miembro -> {
                    if (miembro.getGrupo() != null
                            && !miembroService.tieneAccesoAGrupo(usuario, miembro.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    miembroService.eliminar(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}