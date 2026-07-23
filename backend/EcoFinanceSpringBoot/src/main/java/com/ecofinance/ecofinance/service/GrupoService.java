package com.ecofinance.ecofinance.service;

import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.repository.GrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GrupoService {
    
    @Autowired
    private GrupoRepository grupoRepository;
    
    public List<Grupo> listar(){
        List<Grupo> grupos = grupoRepository.findAll();
        for(Grupo grupo : grupos){
            aplicarMetricasGrupo(grupo);
        }
        return grupos;
    }
        
    public Grupo guardar(Grupo grupo){
        return grupoRepository.save(grupo);
    }

    public Optional<Grupo> buscar(Long id){
        return grupoRepository.findById(id);
    }

    public void eliminar(Long id){
        grupoRepository.deleteById(id);
    }

    public List<Grupo> buscarNombre(String nombre){
        List<Grupo> grupos = grupoRepository.buscarPorNombre(nombre);
        for (Grupo grupo : grupos) {
            aplicarMetricasGrupo(grupo);
        }
        return grupos;
    }

    public Long totalGrupos(){
        return grupoRepository.contarGrupos();
    }

    public List<Grupo> ordenar(){
        return grupoRepository.ordenarPorNombre();
    }
    
    @Autowired
    private GastoService gastoService;

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private MiembroGrupoService miembroService;

    public Double disponible(Long id){
        double limiteTotal = presupuestoService.limiteTotalGrupo(id);
        Double gasto = gastoService.totalGrupo(id);
        return limiteTotal - gasto;
    }

    public boolean presupuestoExcedido(Long id){
        double limiteTotal = presupuestoService.limiteTotalGrupo(id);
        Double gasto = gastoService.totalGrupo(id);
        if (limiteTotal == 0.0) {
            return false;
        }
        return gasto > limiteTotal;
    }

    private void aplicarMetricasGrupo(Grupo grupo) {
        if (grupo == null || grupo.getId() == null) {
            return;
        }

        Double totalGastado = gastoService.totalGrupo(grupo.getId());
        Long totalMiembros = miembroService.totalGrupo(grupo.getId());
        Double mayorGasto = gastoService.gastoMayor(grupo.getId());

        grupo.setTotalGastado(totalGastado);
        grupo.setMayorGasto(mayorGasto);
        grupo.setDisponible(disponible(grupo.getId()));
        grupo.setExcedido(presupuestoExcedido(grupo.getId()));

        if (totalMiembros != null && totalMiembros > 0) {
            grupo.setPromedioGastoPorMiembro(
                totalGastado / totalMiembros
            );
        } else {
            grupo.setPromedioGastoPorMiembro(0.0);
        }
    }

}