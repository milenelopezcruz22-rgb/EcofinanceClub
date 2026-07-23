package com.ecofinance.ecofinance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecofinance.ecofinance.entity.Categoria;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.entity.Presupuesto;
import com.ecofinance.ecofinance.repository.GastoRepository;
import com.ecofinance.ecofinance.repository.PresupuestoRepository;

// Pruebas TDD sobre la lógica financiera de PresupuestoService: la
// validación anti-duplicados por categoría/grupo y el cálculo del
// gastoActual a partir de los gastos reales de esa categoría.
@ExtendWith(MockitoExtension.class)
class PresupuestoServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PresupuestoService presupuestoService;

    @Test
    void guardarPresupuesto_debeRechazarSegundoPresupuestoParaLaMismaCategoriaEnElMismoGrupo() {
        Grupo grupo = new Grupo();
        grupo.setId(1L);

        Categoria categoria = new Categoria();
        categoria.setId(2L);
        categoria.setNombre("Transporte");

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setGrupo(grupo);
        presupuesto.setCategoria(categoria);
        presupuesto.setLimiteGasto(300.0);

        when(presupuestoRepository.existsByGrupoIdAndCategoriaIdAndIdNot(1L, 2L, -1L)).thenReturn(true);

        assertThatThrownBy(() -> presupuestoService.guardarPresupuesto(presupuesto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un presupuesto");
    }

    @Test
    void guardarPresupuesto_debeCalcularGastoActualConElTotalRealDeLaCategoria() {
        Grupo grupo = new Grupo();
        grupo.setId(1L);

        Categoria categoria = new Categoria();
        categoria.setId(2L);
        categoria.setNombre("Transporte");

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setGrupo(grupo);
        presupuesto.setCategoria(categoria);
        presupuesto.setLimiteGasto(300.0);

        when(presupuestoRepository.existsByGrupoIdAndCategoriaIdAndIdNot(1L, 2L, -1L)).thenReturn(false);
        when(gastoRepository.totalGrupoCategoria(1L, 2L)).thenReturn(180.0);
        when(presupuestoRepository.save(presupuesto)).thenReturn(presupuesto);

        Presupuesto guardado = presupuestoService.guardarPresupuesto(presupuesto);

        assertThat(guardado.getGastoActual()).isEqualTo(180.0);
    }
}