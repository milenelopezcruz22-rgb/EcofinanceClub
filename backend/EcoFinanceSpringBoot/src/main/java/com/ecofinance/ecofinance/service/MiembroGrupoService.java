package com.ecofinance.ecofinance.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecofinance.ecofinance.entity.MiembroGrupo;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.repository.MiembroGrupoRepository;
import com.ecofinance.ecofinance.repository.GastoRepository;
import com.ecofinance.ecofinance.repository.PagoDeudaRepository;

@Service
public class MiembroGrupoService {

    @Autowired
    private MiembroGrupoRepository repository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private PagoDeudaRepository pagoDeudaRepository;

    public List<MiembroGrupo> listar(){
        return repository.findAll();
    }

    public MiembroGrupo guardar(MiembroGrupo miembro){
        return repository.save(miembro);
    }

    public Long totalMiembros(){
        Long total = repository.contarMiembros();
        return total == null ? 0L : total;
    }

    // Antes de borrar, se valida si el miembro todavía tiene Gastos (como
    // pagador) o Pagos de Deuda (como deudor o acreedor) registrados a su
    // nombre. Si tiene, se lanza una excepción con un mensaje claro y
    // específico en vez de dejar que la base de datos rechace el DELETE con
    // una violación de clave foránea genérica.
    public void eliminar(Long id){
        if (gastoRepository.existsByPagadorId(id)) {
            throw new IllegalStateException(
                "No se puede eliminar este miembro: tiene gastos registrados a su nombre. "
                + "Elimina o reasigna esos gastos primero."
            );
        }

        if (pagoDeudaRepository.existsByDeudorIdOrAcreedorId(id, id)) {
            throw new IllegalStateException(
                "No se puede eliminar este miembro: tiene pagos de deuda registrados (como deudor o acreedor). "
                + "Elimina esos pagos primero."
            );
        }

        repository.deleteById(id);
    }

    public List<MiembroGrupo> listarGrupo(Long id){
        return repository.listarGrupo(id);
    }

    // Todas las filas miembro_grupo de esta cuenta, cada una con su
    // grupo + rol DENTRO de ese grupo. Usado por el endpoint "mi-rol".
    public List<MiembroGrupo> misAsignaciones(Usuario usuario){
        return repository.findByUsuarioId(usuario.getId());
    }

    public Long totalGrupo(Long idGrupo){
        return repository.totalMiembros(idGrupo);
    }

    // Agregado (Fase 1 - Tarea 4): necesario para que el REST controller pueda
    // resolver GET/PUT/DELETE por id y devolver 404 cuando no exista, igual
    // que ya hace GrupoService.buscar(id).
    public Optional<MiembroGrupo> buscarPorId(Long id){
        return repository.findById(id);
    }

    // --- Gestión de usuarios y permisos ---
    // Administrador General (ROLE_ADMIN): acceso total, sin restricción.
    // Administrador de Grupo / Miembro: solo si existe una fila de
    // MiembroGrupo que los vincula a ESE grupo puntual (columna usuario_id).
    // La usan todos los controllers que reciben un idGrupo (Miembros,
    // Categorías, Presupuestos, Gastos, Dashboard).
    public boolean tieneAccesoAGrupo(Usuario usuario, Long idGrupo){
        if (usuarioService.esAdminGeneral(usuario)) {
            return true;
        }
        return repository.existsByUsuarioIdAndGrupoId(usuario.getId(), idGrupo);
    }

    // Ids de los grupos a los que una cuenta (Gestor o Miembro) está
    // vinculada. La usa GrupoRestController para filtrar el listado de
    // grupos cuando quien pregunta no es Administrador General.
    public List<Long> idsGruposDeUsuario(Usuario usuario){
        return repository.findByUsuarioId(usuario.getId())
                .stream()
                .map(m -> m.getGrupo().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    // --- Rol DENTRO de un grupo (distinto del rol global del Usuario) ---
    // El Administrador General administra todo, sin importar miembro_grupo.
    // Para cualquier otra cuenta, "ser GESTOR" ya NO depende del rol global
    // guardado en "usuarios" (que puede seguir siendo ROLE_MIEMBRO toda la
    // vida): depende de que exista una fila miembro_grupo para ESE usuario y
    // ESE grupo puntual con rol_grupo = 'GESTOR'. La misma cuenta puede ser
    // GESTOR de un grupo y simple MIEMBRO de otro.
    public boolean esGestorDeGrupo(Usuario usuario, Long idGrupo){
        if (usuarioService.esAdminGeneral(usuario)) {
            return true;
        }
        return repository.existsByUsuarioIdAndGrupoIdAndRolGrupo(usuario.getId(), idGrupo, "GESTOR");
    }

    // Ids de los grupos donde esta cuenta tiene rol_grupo = 'GESTOR'. Lo usa
    // el frontend (vía MiembroGrupoRestController) para decidir si mostrar
    // los módulos de gestión (Presupuestos, Categorías, edición de
    // Gastos/Deudas) sin depender del rol global del JWT.
    public List<Long> idsGruposDondeEsGestor(Usuario usuario){
        return repository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(m -> "GESTOR".equals(m.getRolGrupo()))
                .map(m -> m.getGrupo().getId())
                .distinct()
                .collect(Collectors.toList());
    }

}