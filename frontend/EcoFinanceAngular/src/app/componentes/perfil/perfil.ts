import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Menu } from '../menu/menu';
import { AuthService } from '../../core/auth.service';

// Pantalla mínima de "Mi Perfil": muestra los datos de la sesión actual
// (usuario y rol, ya disponibles en AuthService desde el login). No agrega
// edición de datos ni llamadas nuevas al backend más allá del registro de
// auditoría: no formaba parte del alcance pedido, solo el módulo en sí para
// poder registrar el acceso a "Perfil".
@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, Menu],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss'
})
export class Perfil implements OnInit {

  constructor(
    public authService: AuthService
  ) {}

  ngOnInit(): void {
  }
}
