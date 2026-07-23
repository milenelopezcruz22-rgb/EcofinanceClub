import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';
import { AuditoriaAccesoService } from '../../core/auditoria-acceso.service';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './menu.html',
  styleUrl: './menu.scss'
})
export class Menu {
  private router = inject(Router);
  readonly authService = inject(AuthService);
  private readonly grupoContextoService = inject(GrupoContextoService);
  private readonly auditoriaAccesoService = inject(AuditoriaAccesoService);

  // ADMIN: administra usuarios, grupos y asignaciones -- NO ve los módulos
  // operativos (Dashboard/Gastos/Deudas/Presupuestos/Categorías).
  mostrarGrupos(): boolean {
    return this.authService.esAdmin();
  }

  mostrarMiembros(): boolean {
    return this.authService.esAdmin();
  }

  mostrarAuditoria(): boolean {
    return this.authService.esAdmin();
  }

  mostrarAccesos(): boolean {
    return this.authService.esAdmin();
  }

  // Mi Perfil: visible para cualquier rol con sesión iniciada.
  mostrarPerfil(): boolean {
    return this.authService.isLoggedIn();
  }

  // GESTOR: administra la información financiera de su grupo. MIEMBRO solo
  // consulta Dashboard/Gastos/Deudas (sin Presupuestos/Categorías, que son
  // exclusivos de GESTOR).
  mostrarDashboard(): boolean {
    return this.authService.esGestor() || this.authService.esMiembro();
  }

  mostrarGastos(): boolean {
    return this.authService.esGestor() || this.authService.esMiembro();
  }

  mostrarDeudas(): boolean {
    return this.authService.esGestor() || this.authService.esMiembro();
  }

  mostrarPresupuestos(): boolean {
    return this.authService.esGestor();
  }

  mostrarCategorias(): boolean {
    return this.authService.esGestor();
  }

  cerrarSesion() {
    // Se registra el LOGOUT antes de borrar el token: si se llamara después
    // de authService.logout(), la petición ya saldría sin Authorization y
    // el backend la rechazaría con 401 sin llegar a guardar el registro.
    this.auditoriaAccesoService.registrar('LOGOUT');
    this.authService.logout();
    this.grupoContextoService.limpiarContexto();
    this.router.navigate(['/home']);
  }
}
