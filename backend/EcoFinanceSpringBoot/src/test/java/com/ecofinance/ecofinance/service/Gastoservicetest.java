package com.ecofinance.ecofinance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecofinance.ecofinance.dto.DeudaDTO;
import com.ecofinance.ecofinance.dto.SaldoMiembroDTO;
import com.ecofinance.ecofinance.entity.Gasto;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.repository.GastoRepository;

// Pruebas TDD sobre la lógica financiera de GastoService: reparto equitativo
// (montoPorPersona), saldo neto por miembro (RF23) y simplificación de
// deudas (RF24). Se mockean los repositorios/services externos para probar
// SOLO la lógica de cálculo, sin base de datos real.
@ExtendWith(MockitoExtension.class)
class GastoServiceTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private MiembroGrupoService miembroService;

    @Mock
    private PresupuestoService presupuestoService;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private GastoService gastoService;

    private Grupo grupo;

    @BeforeEach
    void setUp() {
        grupo = new Grupo();
        grupo.setId(1L);
        grupo.setNombre("Viaje a Máncora");
    }

    @Test
    void guardarGasto_debeCalcularMontoPorPersonaSegunCantidadDeMiembros() {
        Gasto gasto = new Gasto();
        gasto.setMonto(120.0);
        gasto.setGrupo(grupo);

        when(miembroService.totalGrupo(1L)).thenReturn(4L);
        when(gastoRepository.save(gasto)).thenReturn(gasto);

        Gasto guardado = gastoService.guardarGasto(gasto);

        assertThat(guardado.getCantidadMiembros()).isEqualTo(4);
        assertThat(guardado.getMontoPorPersona()).isEqualTo(30.0);
    }

    @Test
    void guardarGasto_debeAsignarMontoCompletoSiElGrupoNoTieneMiembros() {
        Gasto gasto = new Gasto();
        gasto.setMonto(80.0);
        gasto.setGrupo(grupo);

        when(miembroService.totalGrupo(1L)).thenReturn(0L);
        when(gastoRepository.save(gasto)).thenReturn(gasto);

        Gasto guardado = gastoService.guardarGasto(gasto);

        assertThat(guardado.getCantidadMiembros()).isEqualTo(0);
        assertThat(guardado.getMontoPorPersona()).isEqualTo(80.0);
    }

    @Test
    void calcularSaldos_debeReflejarQuienPagoDeMasYDeMenos() {
        // Cuota por persona: S/ 50 (viene de sumaMontoPersonaGrupo).
        // Ana pagó 100 (le deben 50), Luis pagó 0 (debe 50).
        Object[] filaAna = {1L, "Ana", 100.0};
        Object[] filaLuis = {2L, "Luis", 0.0};

        when(gastoRepository.pagadoPorMiembroGrupo(1L)).thenReturn(List.of(filaAna, filaLuis));
        when(gastoRepository.sumaMontoPersonaGrupo(1L)).thenReturn(50.0);

        List<SaldoMiembroDTO> saldos = gastoService.calcularSaldos(1L);

        assertThat(saldos).hasSize(2);
        SaldoMiembroDTO ana = saldos.stream().filter(s -> s.getMiembroId().equals(1L)).findFirst().orElseThrow();
        SaldoMiembroDTO luis = saldos.stream().filter(s -> s.getMiembroId().equals(2L)).findFirst().orElseThrow();

        assertThat(ana.getSaldo()).isEqualTo(50.0);
        assertThat(luis.getSaldo()).isEqualTo(-50.0);
    }

    @Test
    void calcularDeudas_debeAsignarAlDeudorConSuAcreedorPorElMontoJusto() {
        // Misma situación que el test anterior: Luis le debe 50 a Ana.
        Object[] filaAna = {1L, "Ana", 100.0};
        Object[] filaLuis = {2L, "Luis", 0.0};

        when(gastoRepository.pagadoPorMiembroGrupo(1L)).thenReturn(List.of(filaAna, filaLuis));
        when(gastoRepository.sumaMontoPersonaGrupo(1L)).thenReturn(50.0);

        List<DeudaDTO> deudas = gastoService.calcularDeudas(1L);

        assertThat(deudas).hasSize(1);
        DeudaDTO deuda = deudas.get(0);
        assertThat(deuda.getDeudorNombre()).isEqualTo("Luis");
        assertThat(deuda.getAcreedorNombre()).isEqualTo("Ana");
        assertThat(deuda.getMonto()).isEqualTo(50.0);
    }

    @Test
    void calcularDeudas_noDebeGenerarMovimientosSiTodosPagaronLoMismo() {
        // Los 2 miembros pagaron exactamente su cuota: nadie le debe a nadie.
        Object[] filaAna = {1L, "Ana", 50.0};
        Object[] filaLuis = {2L, "Luis", 50.0};

        when(gastoRepository.pagadoPorMiembroGrupo(1L)).thenReturn(List.of(filaAna, filaLuis));
        when(gastoRepository.sumaMontoPersonaGrupo(1L)).thenReturn(50.0);

        List<DeudaDTO> deudas = gastoService.calcularDeudas(1L);

        assertThat(deudas).isEmpty();
    }
}