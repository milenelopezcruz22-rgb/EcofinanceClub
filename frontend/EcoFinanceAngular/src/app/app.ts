import { Component, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from './core/auth.service';
import { AuditoriaAccesoService } from './core/auditoria-acceso.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>'
})
export class App {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly auditoriaAccesoService = inject(AuditoriaAccesoService);

  constructor() {
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event) => {
      const modulo = this.obtenerModuloDesdeRuta(event.urlAfterRedirects);
      if (modulo && this.authService.isLoggedIn()) {
        this.auditoriaAccesoService.registrar(modulo);
      }
    });
  }

  private obtenerModuloDesdeRuta(url: string): string | null {
    const ruta = url.split('?')[0];

    switch (ruta) {
      case '/dashboard':
        return 'DASHBOARD';
      case '/grupos':
        return 'GRUPOS';
      case '/miembros':
        return 'MIEMBROS';
      case '/categorias':
        return 'CATEGORIAS';
      case '/presupuestos':
        return 'PRESUPUESTOS';
      case '/gastos':
        return 'GASTOS';
      case '/perfil':
        return 'PERFIL';
      case '/login':
        return 'LOGIN';
      default:
        return null;
    }
  }
}