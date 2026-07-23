package com.ecofinance.ecofinance.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecofinance.ecofinance.dto.DashboardDTO;
import com.ecofinance.ecofinance.dto.DashboardDTO.ItemDashboard;
import com.ecofinance.ecofinance.dto.DashboardDTO.Recomendacion;
import com.ecofinance.ecofinance.dto.DeudaDTO;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.entity.Presupuesto;
import com.ecofinance.ecofinance.repository.GastoRepository;

@Service
public class DashboardService {

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private GastoService gastoService;

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private MiembroGrupoService miembroService;

    @Autowired
    private PagoDeudaService pagoDeudaService;

    // Arma el resumen financiero completo de un grupo, reutilizando por completo
    // los servicios/repositorios ya existentes (no se agrega ninguna regla de
    // negocio nueva: solo se consulta y se junta todo en un único DTO).
    public Optional<DashboardDTO> obtenerDashboard(Long idGrupo) {
        Optional<Grupo> grupoOpt = grupoService.buscar(idGrupo);
        if (grupoOpt.isEmpty()) {
            return Optional.empty();
        }
        Grupo grupo = grupoOpt.get();

        DashboardDTO dto = new DashboardDTO();
        dto.setGrupoId(grupo.getId());
        dto.setGrupoNombre(grupo.getNombre());

        double gastoTotal = gastoService.totalGrupo(idGrupo);
        double presupuestoTotal = presupuestoService.limiteTotalGrupo(idGrupo);

        dto.setGastoTotal(gastoTotal);
        dto.setPresupuestoTotal(presupuestoTotal);
        dto.setPresupuestoDisponible(presupuestoTotal - gastoTotal);

        Long miembros = miembroService.totalGrupo(idGrupo);
        dto.setCantidadMiembros(miembros == null ? 0 : miembros);

        Long cantidadGastos = gastoService.cantidadGrupo(idGrupo);
        dto.setCantidadGastos(cantidadGastos == null ? 0 : cantidadGastos);

        List<ItemDashboard> gastoPorCategoria = mapearItems(gastoRepository.gastoPorCategoria(idGrupo));
        dto.setGastoPorCategoria(gastoPorCategoria);

        List<ItemDashboard> gastoPorMiembro = mapearItems(gastoRepository.gastoPorMiembro(idGrupo));
        dto.setGastoPorMiembro(gastoPorMiembro);

        // Ambas listas ya vienen ordenadas de mayor a menor gasto (ORDER BY en
        // el repository), así que el primer elemento es siempre el mayor.
        if (!gastoPorCategoria.isEmpty()) {
            dto.setCategoriaMayorGastoNombre(gastoPorCategoria.get(0).getNombre());
            dto.setCategoriaMayorGastoMonto(gastoPorCategoria.get(0).getMonto());
        }

        if (!gastoPorMiembro.isEmpty()) {
            dto.setMiembroMayorGastoNombre(gastoPorMiembro.get(0).getNombre());
            dto.setMiembroMayorGastoMonto(gastoPorMiembro.get(0).getMonto());
        }

        aplicarSostenibilidad(dto, idGrupo, gastoTotal);
        aplicarRecomendaciones(dto, idGrupo);

        // RF27: tendencia mensual. Misma query pattern (Object[]) que el resto
        // del dashboard, reutilizando mapearItems (mes en "nombre", total en
        // "monto"). No agrega ninguna regla de negocio nueva.
        dto.setTendenciaMensual(mapearItemsMes(gastoRepository.tendenciaMensualGrupo(idGrupo)));

        // RF25: bloque de liquidación de deudas. Puramente aditivo, no
        // modifica ningún campo calculado arriba.
        aplicarLiquidacion(dto, idGrupo);

        return Optional.of(dto);
    }

    // RF25: KPIs de liquidación, reutilizando PagoDeudaService (que a su vez
    // reutiliza GastoService sin modificarlo). No se agrega ninguna consulta
    // nueva fuera de PagoDeudaService.
    private void aplicarLiquidacion(DashboardDTO dto, Long idGrupo) {
        List<DeudaDTO> deudasPendientes = pagoDeudaService.calcularDeudasPendientes(idGrupo);

        double pendiente = 0.0;
        for (DeudaDTO d : deudasPendientes) {
            pendiente += d.getMonto();
        }

        dto.setMontoPendienteLiquidar(pendiente);
        dto.setCantidadDeudasPendientes(deudasPendientes.size());
        dto.setMontoLiquidado(pagoDeudaService.totalLiquidadoGrupo(idGrupo));
    }

    // Sostenibilidad (Bloque B): a partir de una sola query (categoría + nivel
    // de impacto + monto), calcula los 3 totales por nivel, el % de impacto
    // alto sobre el gasto total, y la lista de categorías de impacto alto.
    private void aplicarSostenibilidad(DashboardDTO dto, Long idGrupo, double gastoTotal) {
        List<Object[]> filas = gastoRepository.gastoPorCategoriaConImpacto(idGrupo);

        double bajo = 0.0;
        double medio = 0.0;
        double alto = 0.0;
        List<ItemDashboard> categoriasAlto = new ArrayList<>();

        for (Object[] fila : filas) {
            String nombreCategoria = (String) fila[0];
            String nivel = (String) fila[1];
            Double monto = (Double) fila[2];
            double montoSeguro = monto == null ? 0.0 : monto;

            switch (nivel) {
                case "Bajo" -> bajo += montoSeguro;
                case "Medio" -> medio += montoSeguro;
                case "Alto" -> {
                    alto += montoSeguro;
                    // La query ya devuelve las filas ordenadas de mayor a menor
                    // gasto, así que esta lista queda ordenada "gratis".
                    categoriasAlto.add(new ItemDashboard(nombreCategoria, montoSeguro));
                }
                default -> {
                    // Nivel desconocido/mal cargado: no se contabiliza en ningún
                    // total para no distorsionar las cifras de sostenibilidad.
                }
            }
        }

        dto.setGastoImpactoBajo(bajo);
        dto.setGastoImpactoMedio(medio);
        dto.setGastoImpactoAlto(alto);
        dto.setPorcentajeImpactoAlto(gastoTotal > 0 ? (alto / gastoTotal) * 100 : 0.0);
        dto.setCategoriasMayorImpacto(categoriasAlto);
    }

    // Recomendaciones (Bloque C): motor de reglas simples, sin IA. No agrega
    // ninguna consulta nueva: reutiliza la lista de presupuestos del grupo
    // (ya usada por PresupuestoService para presupuestoTotal) y los campos
    // que este mismo método ya dejó calculados en el dto (sostenibilidad).
    // El backend solo arma mensaje + nivel ("ALTA"/"MEDIA"/"INFO"); el color
    // o ícono de cada nivel se resuelve en Angular.
    private void aplicarRecomendaciones(DashboardDTO dto, Long idGrupo) {
        List<Recomendacion> recomendaciones = new ArrayList<>();

        // Regla 1 (ALTA): presupuesto excedido en una categoría puntual.
        List<Presupuesto> presupuestos = presupuestoService.listarPorGrupo(idGrupo);
        for (Presupuesto presupuesto : presupuestos) {
            boolean excedido = presupuesto.getGastoActual() > presupuesto.getLimiteGasto();
            if (excedido && presupuesto.getCategoria() != null) {
                double exceso = presupuesto.getGastoActual() - presupuesto.getLimiteGasto();
                recomendaciones.add(new Recomendacion(
                        "La categoría \"" + presupuesto.getCategoria().getNombre()
                                + "\" superó su presupuesto por S/ " + String.format("%.2f", exceso) + ".",
                        "ALTA"
                ));
            }
        }

        // Regla 2 (ALTA): el grupo en conjunto (suma de todos sus
        // presupuestos) gastó más de lo presupuestado.
        if (dto.getPresupuestoDisponible() < 0) {
            recomendaciones.add(new Recomendacion(
                    "El grupo gastó S/ " + String.format("%.2f", Math.abs(dto.getPresupuestoDisponible()))
                            + " por encima del presupuesto total.",
                    "ALTA"
            ));
        }

        // Regla 3 (MEDIA): más del 40% del gasto está en categorías de alto
        // impacto ambiental (umbral fijo, sin necesidad de configurarlo).
        if (dto.getPorcentajeImpactoAlto() > 40.0 && !dto.getCategoriasMayorImpacto().isEmpty()) {
            String categorias = dto.getCategoriasMayorImpacto().stream()
                    .map(ItemDashboard::getNombre)
                    .collect(Collectors.joining(", "));
            recomendaciones.add(new Recomendacion(
                    "El grupo concentra el " + String.format("%.0f", dto.getPorcentajeImpactoAlto())
                            + "% de su gasto en categorías de alto impacto ambiental (" + categorias
                            + "). Considera reducir el gasto en ellas.",
                    "MEDIA"
            ));
        }

        // Regla 4 (INFO): si ninguna alerta anterior se disparó, mensaje
        // positivo para que la sección nunca quede vacía.
        if (recomendaciones.isEmpty()) {
            recomendaciones.add(new Recomendacion(
                    "El grupo mantiene sus gastos dentro de lo esperado, sin presupuestos excedidos "
                            + "ni concentración relevante en categorías de alto impacto ambiental.",
                    "INFO"
            ));
        }

        dto.setRecomendaciones(recomendaciones);
    }

    // Los repositorios devuelven Object[] {id, nombre, sumaMonto} porque la
    // query hace SELECT de campos sueltos (no de una entidad completa).
    private List<ItemDashboard> mapearItems(List<Object[]> filas) {
        List<ItemDashboard> items = new ArrayList<>();
        for (Object[] fila : filas) {
            String nombre = (String) fila[1];
            Double monto = (Double) fila[2];
            items.add(new ItemDashboard(nombre, monto == null ? 0.0 : monto));
        }
        return items;
    }

    // RF27: variante de mapearItems para queries de solo 2 columnas
    // {mes, sumaMonto}, como tendenciaMensualGrupo (no tiene id de por medio).
    private List<ItemDashboard> mapearItemsMes(List<Object[]> filas) {
        List<ItemDashboard> items = new ArrayList<>();
        for (Object[] fila : filas) {
            String mes = (String) fila[0];
            Double monto = (Double) fila[1];
            items.add(new ItemDashboard(mes, monto == null ? 0.0 : monto));
        }
        return items;
    }
}