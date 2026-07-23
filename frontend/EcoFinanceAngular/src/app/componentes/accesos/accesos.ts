import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Menu } from '../menu/menu';
import { AuditoriaAccesoService, AuditoriaAccesoDTO } from '../../core/auditoria-acceso.service';

// Pantalla exclusiva de ADMIN: historial de accesos a módulos (Login,
// Dashboard, Grupos, Miembros, Categorías, Presupuestos, Gastos, Perfil,
// Logout), con filtros por usuario, rol y fecha. Se trae la lista completa
// una sola vez y los filtros se aplican en el navegador con un signal
// computed: para el volumen de datos de un proyecto universitario no hace
// falta ida y vuelta al backend por cada filtro.
@Component({
  selector: 'app-accesos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Menu],
  templateUrl: './accesos.html',
  styleUrl: './accesos.scss'
})
export class Accesos implements OnInit {

  listaAccesos = signal<AuditoriaAccesoDTO[]>([]);

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

    return this.listaAccesos().filter(registro => {
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
      next: (registros) => this.listaAccesos.set(registros),
      error: () => this.listaAccesos.set([])
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
