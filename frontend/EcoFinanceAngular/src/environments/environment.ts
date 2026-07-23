// Entorno de desarrollo (ng serve / ng build sin --configuration production).
// Mantiene la misma URL de backend que ya se usaba antes de este cambio.
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api'
};
