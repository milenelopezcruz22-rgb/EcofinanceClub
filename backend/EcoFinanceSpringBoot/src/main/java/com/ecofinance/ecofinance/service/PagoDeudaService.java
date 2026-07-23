package com.ecofinance.ecofinance.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecofinance.ecofinance.dto.DeudaDTO;
import com.ecofinance.ecofinance.dto.SaldoMiembroDTO;
import com.ecofinance.ecofinance.entity.PagoDeuda;
import com.ecofinance.ecofinance.repository.PagoDeudaRepository;

@Service
public class PagoDeudaService {

    private static final double EPSILON = 0.01;

    @Autowired
    private PagoDeudaRepository pagoDeudaRepository;

    @Autowired
    private GastoService gastoService;

    public List<PagoDeuda> listarPorGrupo(Long idGrupo) {
        return pagoDeudaRepository.listarPorGrupo(idGrupo);
    }

    public Optional<PagoDeuda> buscarPorId(Long id) {
        return pagoDeudaRepository.findById(id);
    }

    public PagoDeuda registrarPago(PagoDeuda pago) {
        if (pago.getDeudor() == null || pago.getAcreedor() == null) {
            throw new IllegalArgumentException("El deudor y el acreedor son obligatorios");
        }
        if (pago.getDeudor().getId().equals(pago.getAcreedor().getId())) {
            throw new IllegalArgumentException("El deudor y el acreedor no pueden ser el mismo miembro");
        }
        if (pago.getMonto() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        Long idGrupo = pago.getGrupo().getId();
        double saldoDeudor = obtenerSaldoAjustado(idGrupo, pago.getDeudor().getId());

        if (saldoDeudor >= -EPSILON) {
            throw new IllegalArgumentException("El deudor indicado no tiene deuda pendiente en este grupo");
        }
        double deudaPendiente = -saldoDeudor;
        if (pago.getMonto() > deudaPendiente + EPSILON) {
            throw new IllegalArgumentException(
                    "El monto (" + pago.getMonto() + ") supera la deuda pendiente del miembro (" + deudaPendiente + ")"
            );
        }

        return pagoDeudaRepository.save(pago);
    }

    public void eliminarPago(Long id) {
        pagoDeudaRepository.deleteById(id);
    }

    public Double totalLiquidadoGrupo(Long idGrupo) {
        Double total = pagoDeudaRepository.totalLiquidadoGrupo(idGrupo);
        return total == null ? 0.0 : total;
    }

    public List<SaldoMiembroDTO> calcularSaldosAjustados(Long idGrupo) {
        List<SaldoMiembroDTO> saldos = gastoService.calcularSaldos(idGrupo);

        Map<Long, Double> pagadoComoDeudor = new HashMap<>();
        for (Object[] fila : pagoDeudaRepository.totalPagadoPorMiembro(idGrupo)) {
            pagadoComoDeudor.put((Long) fila[0], (Double) fila[1]);
        }

        Map<Long, Double> recibidoComoAcreedor = new HashMap<>();
        for (Object[] fila : pagoDeudaRepository.totalRecibidoPorMiembro(idGrupo)) {
            recibidoComoAcreedor.put((Long) fila[0], (Double) fila[1]);
        }

        List<SaldoMiembroDTO> ajustados = new ArrayList<>();
        for (SaldoMiembroDTO s : saldos) {
            double pagado = pagadoComoDeudor.getOrDefault(s.getMiembroId(), 0.0);
            double recibido = recibidoComoAcreedor.getOrDefault(s.getMiembroId(), 0.0);
            s.setSaldo(s.getSaldo() + pagado - recibido);
            ajustados.add(s);
        }
        return ajustados;
    }

    public List<DeudaDTO> calcularDeudasPendientes(Long idGrupo) {
        return gastoService.simplificarDeudas(calcularSaldosAjustados(idGrupo));
    }

    private double obtenerSaldoAjustado(Long idGrupo, Long miembroId) {
        for (SaldoMiembroDTO s : calcularSaldosAjustados(idGrupo)) {
            if (s.getMiembroId().equals(miembroId)) {
                return s.getSaldo();
            }
        }
        return 0.0;
    }
}