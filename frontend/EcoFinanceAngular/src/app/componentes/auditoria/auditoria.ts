import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Menu } from '../menu/menu';
import { AuditoriaAccesoService, AuditoriaAccesoDTO } from '../../core/auditoria-acceso.service';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Menu],
  templateUrl: './auditoria.html',
  styleUrl: './auditoria.scss'
})
export class Auditoria implements OnInit {

  listaAuditoria = signal<AuditoriaAccesoDTO[]>([]);

  formularioFiltros = new FormGroup({
    usuario: new FormControl(''),
    rol: new FormControl(''),
    fechaDesde: new FormControl(''),
    fechaHasta: new FormControl('')
  });

  private readonly filtros = signal({
    usuario: '',
    rol: '',
    fechaDesde: '',
    fechaHasta: ''
  });

  readonly listaFiltrada = computed(() => {
    const { usuario, rol, fechaDesde, fechaHasta } = this.filtros();

    return this.listaAuditoria().filter(registro => {
      const coincideUsuario = !usuario
        || registro.usuario.toLowerCase().includes(usuario.toLowerCase());

      const coincideRol = !rol || registro.rol === rol;

      const fechaRegistro = registro.fecha.substring(0, 10);
      const coincideDesde = !fechaDesde || fechaRegistro >= fechaDesde;
      const coincideHasta = !fechaHasta || fechaRegistro <= fechaHasta;

      return coincideUsuario && coincideRol && coincideDesde && coincideHasta;
    });
  });

  constructor(private auditoriaAccesoService: AuditoriaAccesoService) {}

  ngOnInit(): void {
    this.listar();

    this.formularioFiltros.valueChanges.subscribe(valor => {
      this.filtros.set({
        usuario: valor.usuario ?? '',
        rol: valor.rol ?? '',
        fechaDesde: valor.fechaDesde ?? '',
        fechaHasta: valor.fechaHasta ?? ''
      });
    });
  }

  listar(): void {
    this.auditoriaAccesoService.listar().subscribe({
      next: (registros) => this.listaAuditoria.set(registros),
      error: () => this.listaAuditoria.set([])
    });
  }

  limpiarFiltros(): void {
    this.formularioFiltros.reset({
      usuario: '',
      rol: '',
      fechaDesde: '',
      fechaHasta: ''
    });
  }
}
