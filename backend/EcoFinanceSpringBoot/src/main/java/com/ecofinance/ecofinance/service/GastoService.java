package com.ecofinance.ecofinance.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecofinance.ecofinance.entity.Gasto;
import com.ecofinance.ecofinance.repository.GastoRepository;

@Service
public class GastoService {

    @Autowired
    private GastoRepository gastoRepository;

    public List<Gasto> listarGastos() {
                List<Gasto> lista = gastoRepository.findAll();
                for (Gasto gasto : lista) {
                    if (gasto.getGrupo() == null) {
                        gasto.setCantidadMiembros(0);
                        gasto.setMontoPorPersona(gasto.getMonto());
                        continue;
                    }

                    Long miembros = miembroService.totalGrupo(
                        gasto.getGrupo().getId()
                    );

                    int cantidad = (miembros == null) ? 0 : miembros.intValue();
                    gasto.setCantidadMiembros(cantidad);

                    if (cantidad > 0) {
                        gasto.setMontoPorPersona(
                            gasto.getMonto() / cantidad
                        );
                    } else {
                        gasto.setMontoPorPersona(
                            gasto.getMonto()
                        );
                    }
                }
                return lista;
            }

    public Optional<Gasto> buscarGasto(Long id) {
        return gastoRepository.findById(id);
    }

    public void eliminarGasto(Long id) {
        Gasto gasto = gastoRepository.findById(id).orElse(null);
        if (gasto != null && gasto.getGrupo() != null) {
            Long idGrupo = gasto.getGrupo().getId();
            gastoRepository.deleteById(id);
            presupuestoService.actualizarGastoActualPorGrupo(idGrupo);
        } else {
            gastoRepository.deleteById(id);
        }
    }

    public List<Gasto> listarGrupo(Long id){
        return gastoRepository.listarPorGrupo(id);
    }

    public Long cantidadGrupo(Long id){
        return gastoRepository.cantidadGrupo(id);
    }

    @Autowired
    private MiembroGrupoService miembroService;

    @Autowired
    private PresupuestoService presupuestoService;

    public Gasto guardarGasto(Gasto gasto) {
        // Bugfix: si este gasto ya existía y se está editando, se guarda el
        // grupo al que pertenecía ANTES de guardar. Si el grupo cambia (el
        // DTO lo permite), los presupuestos del grupo anterior quedaban con
        // el gasto contado de más y nunca se recalculaban hasta que otro
        // gasto de ese grupo disparara un recálculo.
        boolean esNuevo = gasto.getId() == null;
        Long idGrupoAnterior = null;
        if (gasto.getId() != null) {
            Gasto anterior = gastoRepository.findById(gasto.getId()).orElse(null);
            if (anterior != null && anterior.getGrupo() != null) {
                idGrupoAnterior = anterior.getGrupo().getId();
            }
        }

        // La cantidad de miembros y el monto por persona ya NO los manda el
        // cliente: siempre se recalculan aquí a partir de los miembros
        // reales del grupo del gasto.
        Long miembros = null;
        if (gasto.getGrupo() != null && gasto.getGrupo().getId() != null) {
            miembros = miembroService.totalGrupo(
                gasto.getGrupo().getId()
            );
        }

        int cantidad = (miembros == null) ? 0 : miembros.intValue();
        gasto.setCantidadMiembros(cantidad);

        if (cantidad > 0) {
            gasto.setMontoPorPersona(
                gasto.getMonto() / cantidad
            );
        } else {
            gasto.setMontoPorPersona(
                gasto.getMonto()
            );
        }

        Gasto guardado = gastoRepository.save(gasto);
        if (gasto.getGrupo() != null && gasto.getGrupo().getId() != null) {
            presupuestoService.actualizarGastoActualPorGrupo(
                gasto.getGrupo().getId()
            );
        }

        // Bugfix (continuación): si el grupo cambió, el grupo anterior
        // también necesita recalcular sus presupuestos para dejar de
        // contar este gasto.
        boolean cambioDeGrupo = idGrupoAnterior != null
                && (gasto.getGrupo() == null || !idGrupoAnterior.equals(gasto.getGrupo().getId()));
        if (cambioDeGrupo) {
            presupuestoService.actualizarGastoActualPorGrupo(idGrupoAnterior);
        }

        return guardado;
    }


    public Double totalGastado(){
        Double total =
        gastoRepository.totalGastado();
        return total==null?0:total;
    }

    public Long cantidad(){
        Long total = gastoRepository.cantidadGastos();
        return total == null ? 0L : total;
    }

    public Long grupoConMayorGastoId(){
        List<Long> resultados = gastoRepository.gruposPorGastoDesc(
                org.springframework.data.domain.PageRequest.of(0, 1)
        );
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    public Double promedio(){
        Double promedio =
        gastoRepository.promedioGastos();
        return promedio==null?0:promedio;
    }

    public Double totalGrupo(Long id){
        Double total =
        gastoRepository.totalGastadoGrupo(id);
        return total==null?0:total;
    }
    
    public Double promedioGrupo(Long id){
        Double promedio =
        gastoRepository.promedioGrupo(id);
        return promedio==null?0:promedio;
    }

    public Double gastoMayor(Long id){
        Double mayor =
        gastoRepository.gastoMayor(id);
        return mayor==null?0:mayor;
    }

    public Double gastoMenor(Long id){
        Double menor =
        gastoRepository.gastoMenor(id);
        return menor==null?0:menor;
    }

    // RF23: saldo neto por miembro. Reutiliza pagadoPorMiembroGrupo (lo que
    // pagó cada uno) y sumaMontoPersonaGrupo (la cuota, igual para todos por
    // el reparto equitativo). saldo > 0 = le deben, saldo < 0 = debe.
    public List<com.ecofinance.ecofinance.dto.SaldoMiembroDTO> calcularSaldos(Long idGrupo) {
        List<Object[]> filas = gastoRepository.pagadoPorMiembroGrupo(idGrupo);
        Double cuotaObj = gastoRepository.sumaMontoPersonaGrupo(idGrupo);
        double cuota = cuotaObj == null ? 0.0 : cuotaObj;

        List<com.ecofinance.ecofinance.dto.SaldoMiembroDTO> saldos = new java.util.ArrayList<>();
        for (Object[] fila : filas) {
            Long miembroId = (Long) fila[0];
            String nombre = (String) fila[1];
            Double pagado = (Double) fila[2];
            saldos.add(new com.ecofinance.ecofinance.dto.SaldoMiembroDTO(
                    miembroId, nombre, pagado == null ? 0.0 : pagado, cuota
            ));
        }
        return saldos;
    }

    // Algoritmo greedy de simplificación de deudas (RF24), extraído como
    // método reutilizable: recibe cualquier lista de saldos netos (brutos o
    // ajustados por pagos) y devuelve el emparejamiento deudor-acreedor.
    // El cuerpo del algoritmo es EXACTAMENTE el mismo que tenía calcularDeudas
    // antes de este refactor; no se modificó ninguna regla de negocio.
    public List<com.ecofinance.ecofinance.dto.DeudaDTO> simplificarDeudas(
            List<com.ecofinance.ecofinance.dto.SaldoMiembroDTO> saldos) {

        List<com.ecofinance.ecofinance.dto.SaldoMiembroDTO> deudores = new java.util.ArrayList<>();
        List<com.ecofinance.ecofinance.dto.SaldoMiembroDTO> acreedores = new java.util.ArrayList<>();
        double epsilon = 0.01;

        for (com.ecofinance.ecofinance.dto.SaldoMiembroDTO s : saldos) {
            if (s.getSaldo() < -epsilon) {
                deudores.add(s);
            } else if (s.getSaldo() > epsilon) {
                acreedores.add(s);
            }
        }

        // Copias mutables de los montos pendientes, para no alterar los DTO
        // de saldos originales mientras se van saldando cuentas.
        List<Double> montoDeudores = new java.util.ArrayList<>();
        for (com.ecofinance.ecofinance.dto.SaldoMiembroDTO d : deudores) {
            montoDeudores.add(-d.getSaldo());
        }
        List<Double> montoAcreedores = new java.util.ArrayList<>();
        for (com.ecofinance.ecofinance.dto.SaldoMiembroDTO a : acreedores) {
            montoAcreedores.add(a.getSaldo());
        }

        List<com.ecofinance.ecofinance.dto.DeudaDTO> deudas = new java.util.ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < deudores.size() && j < acreedores.size()) {
            double debe = montoDeudores.get(i);
            double leDeben = montoAcreedores.get(j);
            double pago = Math.min(debe, leDeben);

            if (pago > epsilon) {
                deudas.add(new com.ecofinance.ecofinance.dto.DeudaDTO(
                        deudores.get(i).getMiembroId(), deudores.get(i).getNombre(),
                        acreedores.get(j).getMiembroId(), acreedores.get(j).getNombre(),
                        Math.round(pago * 100.0) / 100.0
                ));
            }

            montoDeudores.set(i, debe - pago);
            montoAcreedores.set(j, leDeben - pago);

            if (montoDeudores.get(i) <= epsilon) {
                i++;
            }
            if (montoAcreedores.get(j) <= epsilon) {
                j++;
            }
        }

        return deudas;
    }

    // RF24: "quién le debe a quién". Ahora es un método de dos líneas que
    // reutiliza calcularSaldos (sin cambios) y simplificarDeudas (mismo
    // algoritmo de siempre, solo extraído a método aparte). El resultado es
    // idéntico al que devolvía esta función antes del refactor.
    public List<com.ecofinance.ecofinance.dto.DeudaDTO> calcularDeudas(Long idGrupo) {
        return simplificarDeudas(calcularSaldos(idGrupo));
    }

}