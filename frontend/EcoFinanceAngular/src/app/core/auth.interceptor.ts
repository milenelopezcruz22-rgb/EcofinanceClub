import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

const RUTAS_PUBLICAS = ['/api/auth/login', '/api/auth/registro'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const esRutaPublica = RUTAS_PUBLICAS.some(ruta => req.url.includes(ruta));

  if (esRutaPublica) {
    return next(req);
  }

  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    const reqConToken = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(reqConToken);
  }

  return next(req);
};