// URL base de la API REST del backend Spring Boot.
// La usan AuthService y los demás servicios (GrupoService, GastoService, etc.)
// Antes estaba hardcodeada a localhost; ahora toma el valor de
// src/environments/environment.ts (desarrollo) o environment.prod.ts
// (producción, reemplazado automáticamente por Angular en "ng build" según
// el fileReplacements configurado en angular.json).
import { environment } from '../../environments/environment';

export const API_BASE_URL = environment.apiBaseUrl;
