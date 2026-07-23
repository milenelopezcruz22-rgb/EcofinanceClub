package com.ecofinance.ecofinance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecofinance.ecofinance.entity.Presupuesto;
import com.ecofinance.ecofinance.repository.GastoRepository;
import com.ecofinance.ecofinance.repository.PresupuestoRepository;

@Service
public class PresupuestoService {

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private GastoRepository gastoRepository;

    public List<Presupuesto> listarPresupuestos(){
        return presupuestoRepository.findAll();
    }

    public Presupuesto guardarPresupuesto(Presupuesto presupuesto){
        // Bugfix: sin esta validación se podían crear dos presupuestos para
        // la misma categoría dentro del mismo grupo, lo que duplicaba el
        // límite total del grupo y las alertas del Dashboard. El id se
        // reemplaza por -1L cuando el presupuesto es nuevo (todavía no tiene
        // id), para que la comparación "distinto de sí mismo" no falle.
        boolean esNuevo = presupuesto.getId() == null;
        if (presupuesto.getGrupo() != null && presupuesto.getGrupo().getId() != null
                && presupuesto.getCategoria() != null && presupuesto.getCategoria().getId() != null) {
            Long idActual = presupuesto.getId() != null ? presupuesto.getId() : -1L;
            boolean yaExiste = presupuestoRepository.existsByGrupoIdAndCategoriaIdAndIdNot(
                    presupuesto.getGrupo().getId(),
                    presupuesto.getCategoria().getId(),
                    idActual
            );
            if (yaExiste) {
                throw new IllegalArgumentException(
                        "Ya existe un presupuesto para esta categoría en este grupo");
            }
        }

        presupuesto.setGastoActual(calcularGastoActualCategoria(presupuesto));
        return presupuestoRepository.save(presupuesto);
    }

    public void eliminarPresupuesto(Long id){
        presupuestoRepository.deleteById(id);
    }

    public Double totalPresupuesto(){
        Double total =
        presupuestoRepository.totalPresupuesto();
        return total==null?0:total;
    }

    public Long cantidad(){
        return presupuestoRepository.cantidadPresupuestos();
    }

    public Long cantidadExcedidos(){
        Long total = presupuestoRepository.cantidadPresupuestosExcedidos();
        return total == null ? 0L : total;
    }

    // Vista agregada del grupo: como un grupo puede tener varios presupuestos
    // (uno por categoría), "disponible"/"excedido" a nivel de grupo se calculan
    // sumando el límite y el gasto actual de todos sus presupuestos.
    public double limiteTotalGrupo(Long idGrupo) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByGrupoId(idGrupo);
        return presupuestos.stream().mapToDouble(Presupuesto::getLimiteGasto).sum();
    }

    public double gastoActualTotalGrupo(Long idGrupo) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByGrupoId(idGrupo);
        return presupuestos.stream().mapToDouble(Presupuesto::getGastoActual).sum();
    }

    // Se llama después de guardar/eliminar un Gasto. Antes recalculaba el
    // mismo gastoActual (el total del grupo) para TODOS los presupuestos del
    // grupo por igual. Ahora que cada Presupuesto es por Categoria, cada uno
    // se recalcula con SU PROPIA categoría, no con el total del grupo.
    public void actualizarGastoActualPorGrupo(Long idGrupo) {
        if (idGrupo == null) {
            return;
        }
        List<Presupuesto> presupuestos = presupuestoRepository.findByGrupoId(idGrupo);
        for (Presupuesto presupuesto : presupuestos) {
            presupuesto.setGastoActual(calcularGastoActualCategoria(presupuesto));
        }
        presupuestoRepository.saveAll(presupuestos);
    }

    // Gasto real de la categoría del presupuesto, dentro del grupo del
    // presupuesto. Si el presupuesto todavía no tiene grupo o categoría
    // asignados (caso borde), el gasto actual queda en 0.
    private double calcularGastoActualCategoria(Presupuesto presupuesto) {
        if (presupuesto.getGrupo() == null || presupuesto.getGrupo().getId() == null
                || presupuesto.getCategoria() == null || presupuesto.getCategoria().getId() == null) {
            return 0.0;
        }
        Double total = gastoRepository.totalGrupoCategoria(
                presupuesto.getGrupo().getId(),
                presupuesto.getCategoria().getId()
        );
        return total == null ? 0.0 : total;
    }

    // Agregado (Fase 1 - Tarea 4): necesario para que el REST controller pueda
    // resolver GET/PUT/DELETE por id y devolver 404 cuando no exista.
    public Optional<Presupuesto> buscarPorId(Long id){
        return presupuestoRepository.findById(id);
    }

    // Agregado (Fase 1 - Tarea 4): expone la lista completa de presupuestos de
    // un grupo (un grupo puede tener más de un presupuesto por categoría).
    // Reutiliza el mismo repositorio ya usado internamente por
    // actualizarGastoActualPorGrupo, no se agregó ninguna query nueva.
    public List<Presupuesto> listarPorGrupo(Long idGrupo){
        return presupuestoRepository.findByGrupoId(idGrupo);
    }

}