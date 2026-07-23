package com.ecofinance.ecofinance.dto;

import java.util.List;

// DTO agregado que arma DashboardService para un grupo puntual. Todos los
// totales y listas ya vienen calculados desde el backend: Angular solo
// necesita pintarlos (tarjetas KPI + gráficos + tablas), sin recalcular nada.
public class DashboardDTO {

    private Long grupoId;
    private String grupoNombre;

    private double gastoTotal;
    private double presupuestoTotal;
    private double presupuestoDisponible;

    private long cantidadMiembros;
    private long cantidadGastos;

    // Nombre de la categoría/miembro con mayor gasto acumulado, y su monto.
    // Quedan en null/0 si el grupo todavía no tiene gastos registrados.
    private String categoriaMayorGastoNombre;
    private double categoriaMayorGastoMonto;

    private String miembroMayorGastoNombre;
    private double miembroMayorGastoMonto;

    // Detalle completo para las tablas y los gráficos (ya vienen ordenados
    // de mayor a menor gasto, tal como los devuelve GastoRepository).
    private List<ItemDashboard> gastoPorCategoria;
    private List<ItemDashboard> gastoPorMiembro;

    // --- Sostenibilidad (Bloque B) ---
    // Totales de gasto agrupados por nivel de impacto ambiental de la
    // categoría (Bajo/Medio/Alto). Solo cuenta gastos cuya categoría ya fue
    // clasificada; las categorías sin clasificar quedan afuera de estos 3
    // números (no del gastoTotal general, que no cambia).
    private double gastoImpactoBajo;
    private double gastoImpactoMedio;
    private double gastoImpactoAlto;

    // % del gastoTotal que corresponde a categorías de impacto Alto. Es el
    // número que va a alimentar la recomendación del Bloque C ("el grupo
    // concentra muchos gastos de alto impacto ambiental").
    private double porcentajeImpactoAlto;

    // Categorías de impacto Alto, ordenadas de mayor a menor gasto.
    private List<ItemDashboard> categoriasMayorImpacto;

    // --- Recomendaciones (Bloque C) ---
    // Motor de reglas simples (sin IA): se arma en DashboardService a partir
    // de datos que este mismo DTO ya calculó (presupuestoDisponible,
    // porcentajeImpactoAlto, categoriasMayorImpacto) más la lista de
    // presupuestos del grupo. El backend solo informa mensaje + nivel; el
    // color/ícono de cada nivel se resuelve en Angular, no acá.
    private List<Recomendacion> recomendaciones;

    // --- Tendencia mensual (RF27) ---
    // Gasto total agrupado por mes (formato "yyyy-MM"), ya ordenado
    // cronológicamente. Reutiliza la misma clase ItemDashboard: "nombre" acá
    // es el mes y "monto" el total gastado ese mes.
    private List<ItemDashboard> tendenciaMensual;

    // --- Liquidación de deudas (RF25) ---
    // Estos 3 campos son puramente aditivos: se calculan a partir de
    // PagoDeudaService, sin tocar ninguno de los campos anteriores.
    private double montoPendienteLiquidar;
    private double montoLiquidado;
    private long cantidadDeudasPendientes;

    public DashboardDTO() {
    }

    // Item genérico (nombre + monto) reutilizado tanto para categorías como
    // para miembros: evita crear dos clases casi idénticas.
    public static class ItemDashboard {
        private String nombre;
        private double monto;

        public ItemDashboard() {
        }

        public ItemDashboard(String nombre, double monto) {
            this.nombre = nombre;
            this.monto = monto;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public double getMonto() {
            return monto;
        }

        public void setMonto(double monto) {
            this.monto = monto;
        }
    }

    // Recomendación puntual del motor de reglas: un mensaje ya redactado en
    // español (listo para mostrar tal cual) y un nivel de severidad como
    // texto plano ("ALTA", "MEDIA" o "INFO"). Angular decide qué color o
    // ícono usar para cada nivel; este DTO no incluye esa información.
    public static class Recomendacion {
        private String mensaje;
        private String nivel;

        public Recomendacion() {
        }

        public Recomendacion(String mensaje, String nivel) {
            this.mensaje = mensaje;
            this.nivel = nivel;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getNivel() {
            return nivel;
        }

        public void setNivel(String nivel) {
            this.nivel = nivel;
        }
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public String getGrupoNombre() {
        return grupoNombre;
    }

    public void setGrupoNombre(String grupoNombre) {
        this.grupoNombre = grupoNombre;
    }

    public double getGastoTotal() {
        return gastoTotal;
    }

    public void setGastoTotal(double gastoTotal) {
        this.gastoTotal = gastoTotal;
    }

    public double getPresupuestoTotal() {
        return presupuestoTotal;
    }

    public void setPresupuestoTotal(double presupuestoTotal) {
        this.presupuestoTotal = presupuestoTotal;
    }

    public double getPresupuestoDisponible() {
        return presupuestoDisponible;
    }

    public void setPresupuestoDisponible(double presupuestoDisponible) {
        this.presupuestoDisponible = presupuestoDisponible;
    }

    public long getCantidadMiembros() {
        return cantidadMiembros;
    }

    public void setCantidadMiembros(long cantidadMiembros) {
        this.cantidadMiembros = cantidadMiembros;
    }

    public long getCantidadGastos() {
        return cantidadGastos;
    }

    public void setCantidadGastos(long cantidadGastos) {
        this.cantidadGastos = cantidadGastos;
    }

    public String getCategoriaMayorGastoNombre() {
        return categoriaMayorGastoNombre;
    }

    public void setCategoriaMayorGastoNombre(String categoriaMayorGastoNombre) {
        this.categoriaMayorGastoNombre = categoriaMayorGastoNombre;
    }

    public double getCategoriaMayorGastoMonto() {
        return categoriaMayorGastoMonto;
    }

    public void setCategoriaMayorGastoMonto(double categoriaMayorGastoMonto) {
        this.categoriaMayorGastoMonto = categoriaMayorGastoMonto;
    }

    public String getMiembroMayorGastoNombre() {
        return miembroMayorGastoNombre;
    }

    public void setMiembroMayorGastoNombre(String miembroMayorGastoNombre) {
        this.miembroMayorGastoNombre = miembroMayorGastoNombre;
    }

    public double getMiembroMayorGastoMonto() {
        return miembroMayorGastoMonto;
    }

    public void setMiembroMayorGastoMonto(double miembroMayorGastoMonto) {
        this.miembroMayorGastoMonto = miembroMayorGastoMonto;
    }

    public List<ItemDashboard> getGastoPorCategoria() {
        return gastoPorCategoria;
    }

    public void setGastoPorCategoria(List<ItemDashboard> gastoPorCategoria) {
        this.gastoPorCategoria = gastoPorCategoria;
    }

    public List<ItemDashboard> getGastoPorMiembro() {
        return gastoPorMiembro;
    }

    public void setGastoPorMiembro(List<ItemDashboard> gastoPorMiembro) {
        this.gastoPorMiembro = gastoPorMiembro;
    }

    public double getGastoImpactoBajo() {
        return gastoImpactoBajo;
    }

    public void setGastoImpactoBajo(double gastoImpactoBajo) {
        this.gastoImpactoBajo = gastoImpactoBajo;
    }

    public double getGastoImpactoMedio() {
        return gastoImpactoMedio;
    }

    public void setGastoImpactoMedio(double gastoImpactoMedio) {
        this.gastoImpactoMedio = gastoImpactoMedio;
    }

    public double getGastoImpactoAlto() {
        return gastoImpactoAlto;
    }

    public void setGastoImpactoAlto(double gastoImpactoAlto) {
        this.gastoImpactoAlto = gastoImpactoAlto;
    }

    public double getPorcentajeImpactoAlto() {
        return porcentajeImpactoAlto;
    }

    public void setPorcentajeImpactoAlto(double porcentajeImpactoAlto) {
        this.porcentajeImpactoAlto = porcentajeImpactoAlto;
    }

    public List<ItemDashboard> getCategoriasMayorImpacto() {
        return categoriasMayorImpacto;
    }

    public void setCategoriasMayorImpacto(List<ItemDashboard> categoriasMayorImpacto) {
        this.categoriasMayorImpacto = categoriasMayorImpacto;
    }

    public List<Recomendacion> getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(List<Recomendacion> recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public List<ItemDashboard> getTendenciaMensual() {
        return tendenciaMensual;
    }

    public void setTendenciaMensual(List<ItemDashboard> tendenciaMensual) {
        this.tendenciaMensual = tendenciaMensual;
    }

    public double getMontoPendienteLiquidar() {
        return montoPendienteLiquidar;
    }

    public void setMontoPendienteLiquidar(double montoPendienteLiquidar) {
        this.montoPendienteLiquidar = montoPendienteLiquidar;
    }

    public double getMontoLiquidado() {
        return montoLiquidado;
    }

    public void setMontoLiquidado(double montoLiquidado) {
        this.montoLiquidado = montoLiquidado;
    }

    public long getCantidadDeudasPendientes() {
        return cantidadDeudasPendientes;
    }

    public void setCantidadDeudasPendientes(long cantidadDeudasPendientes) {
        this.cantidadDeudasPendientes = cantidadDeudasPendientes;
    }
}