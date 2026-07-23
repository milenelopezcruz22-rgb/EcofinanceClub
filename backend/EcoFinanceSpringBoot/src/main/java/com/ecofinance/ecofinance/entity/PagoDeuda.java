package com.ecofinance.ecofinance.entity;

import java.time.LocalDate;
import jakarta.persistence.*;

// RF25: pago puntual que un miembro (deudor) le hace a otro (acreedor) para
// saldar parte de su deuda dentro del grupo. No se relaciona con un Gasto
// específico: ajusta el saldo neto agregado de ambos miembros, calculado en
// PagoDeudaService a partir de RF23/RF24 (que no se modifican).
@Entity
@Table(name = "pagos_deuda")
public class PagoDeuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "deudor_id", nullable = false)
    private MiembroGrupo deudor;

    @ManyToOne
    @JoinColumn(name = "acreedor_id", nullable = false)
    private MiembroGrupo acreedor;

    @Column(nullable = false)
    private double monto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 250)
    private String nota;

    public PagoDeuda() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public MiembroGrupo getDeudor() {
        return deudor;
    }

    public void setDeudor(MiembroGrupo deudor) {
        this.deudor = deudor;
    }

    public MiembroGrupo getAcreedor() {
        return acreedor;
    }

    public void setAcreedor(MiembroGrupo acreedor) {
        this.acreedor = acreedor;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}