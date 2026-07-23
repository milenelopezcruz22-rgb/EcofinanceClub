import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth.service';

// Guard de rol: se agrega DESPUÉS de authGuard en cada ruta que lo necesite.
// Lee route.data['roles'] (ej. ['ROLE_GESTOR', 'ROLE_MIEMBRO']) y compara
// contra el rol real guardado por AuthService al hacer login. Si la ruta no
// declara 'roles', se deja pasar (no todas las rutas necesitan restricción).
export const rolGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const rolesPermitidos = (route.data['roles'] as string[] | undefined) ?? [];

  if (rolesPermitidos.length === 0) {
    return true;
  }

  const rolActual = authService.getRol();

  if (rolActual && rolesPermitidos.includes(rolActual)) {
    return true;
  }

  // Si el rol no tiene permiso para esta ruta, lo mandamos a UN módulo que sí
  // pueda ver -- ya no es el mismo destino para todos, porque ADMIN y
  // GESTOR/MIEMBRO tienen conjuntos de módulos completamente separados:
  // ADMIN administra (Grupos/Miembros/Auditoría), GESTOR/MIEMBRO operan
  // (Dashboard/Gastos/Deudas/...). Redirigir siempre a '/dashboard' dejaría a
  // ADMIN en un bucle, porque ya no tiene acceso a esa ruta.
  const destino = authService.esAdmin() ? '/grupos' : '/dashboard';
  router.navigate([destino]);
  return false;
};
