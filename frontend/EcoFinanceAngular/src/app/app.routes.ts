import { Routes } from '@angular/router';

import { Home } from './componentes/home/home';
import { Login } from './componentes/login/login';
import { Registro } from './componentes/registro/registro';
import { Grupos } from './componentes/grupos/grupos';
import { Gastos } from './componentes/gastos/gastos';
import { Miembros } from './componentes/miembros/miembros';
import { Presupuestos } from './componentes/presupuestos/presupuestos';
import { Categorias } from './componentes/categorias/categorias';
import { Dashboard } from './componentes/dashboard/dashboard';
import { Deudas } from './componentes/deudas/deudas';
import { Auditoria } from './componentes/auditoria/auditoria';
import { Perfil } from './componentes/perfil/perfil';
import { Accesos } from './componentes/accesos/accesos';
import { authGuard } from './core/auth.guard';
import { rolGuard } from './core/guards/rol.guard';

export const routes: Routes = [

{
path:'home',
component:Home
},

{
path:'login',
component:Login
},

{
path:'registro',
component:Registro
},

// --- ADMIN: administra usuarios, grupos y asignaciones ---
{
path:'grupos',
component:Grupos,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_ADMIN'] }
},

{
path:'miembros',
component:Miembros,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_ADMIN'] }
},

{
path:'auditoria',
component:Auditoria,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_ADMIN'] }
},

{
path:'accesos',
component:Accesos,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_ADMIN'] }
},

// --- Cualquier rol logueado: pantalla propia de la cuenta ---
{
path:'perfil',
component:Perfil,
canActivate:[authGuard]
},

// --- GESTOR (+ MIEMBRO donde corresponde): módulos operativos del grupo ---
{
path:'dashboard',
component:Dashboard,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_GESTOR', 'ROLE_MIEMBRO'] }
},

{
path:'gastos',
component:Gastos,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_GESTOR', 'ROLE_MIEMBRO'] }
},

{
path:'deudas',
component:Deudas,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_GESTOR', 'ROLE_MIEMBRO'] }
},

{
path:'presupuestos',
component:Presupuestos,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_GESTOR'] }
},

{
path:'categorias',
component:Categorias,
canActivate:[authGuard, rolGuard],
data: { roles: ['ROLE_GESTOR'] }
},

{
path:'',
redirectTo:'home',
pathMatch:'full'
},

{
path:'**',
redirectTo:'home'
}

];
