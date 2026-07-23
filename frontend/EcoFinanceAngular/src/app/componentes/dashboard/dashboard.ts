import { Component, ElementRef, OnInit, ViewChild, Injector, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Chart, registerables } from 'chart.js';
import { Menu } from '../menu/menu';
import { DashboardService, DashboardData } from '../../core/dashboard.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Menu
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {

  protected readonly grupoContextoService = inject(GrupoContextoService);
  private readonly injector = inject(Injector);

  formulario!: FormGroup;

  readonly listaGrupos = this.grupoContextoService.grupos;
  readonly grupoSeleccionadoId = this.grupoContextoService.grupoSeleccionadoId;
  datos = signal<DashboardData | null>(null);
  cargando = signal<boolean>(false);

  @ViewChild('chartCategoria') chartCategoriaRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartMiembro') chartMiembroRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartDistribucion') chartDistribucionRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartImpacto') chartImpactoRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartTendencia') chartTendenciaRef!: ElementRef<HTMLCanvasElement>;

  private chartCategoria?: Chart;
  private chartMiembro?: Chart;
  private chartDistribucion?: Chart;
  private chartImpacto?: Chart;
  private chartTendencia?: Chart;

  private readonly paleta = [
    '#2E7D57', '#4E9F76', '#7BC29A', '#1F5C40', '#A7D8BE',
    '#F4A259', '#E07A5F', '#3D405B', '#81B29A', '#F2CC8F'
  ];

  private readonly coloresImpacto = ['#2E7D57', '#E9A83C', '#C0392B'];

  constructor(
    private dashboardService: DashboardService
  ) {}

  ngOnInit(): void {
    this.formulario = new FormGroup({
      grupo: new FormControl('', Validators.required)
    });

    this.grupoContextoService.cargarGrupos();

    effect(() => {
      const idGrupo = this.grupoContextoService.grupoSeleccionadoId();
      this.formulario.patchValue({ grupo: idGrupo ?? '' }, { emitEvent: false });

      if (!idGrupo) {
        this.datos.set(null);
        this.actualizarGraficos(null);
        return;
      }

      this.cargando.set(true);
      this.dashboardService.obtenerPorGrupo(idGrupo).subscribe({
        next: (data) => {
          this.datos.set(data);
          this.actualizarGraficos(data);
          this.cargando.set(false);
        },
        error: (err) => {
          this.cargando.set(false);
          alert(err.error?.mensaje ?? 'Error al cargar el dashboard del grupo');
        }
      });
    }, { injector: this.injector });

    this.formulario.get('grupo')!.valueChanges.subscribe((valor) => {
      const idGrupo = valor ? Number(valor) : null;
      this.grupoContextoService.seleccionarGrupo(idGrupo);
    });
  }

  private actualizarGraficos(data: DashboardData | null): void {
    this.chartCategoria?.destroy();
    this.chartMiembro?.destroy();
    this.chartDistribucion?.destroy();
    this.chartImpacto?.destroy();
    this.chartTendencia?.destroy();

    if (!data) {
      return;
    }

    const etiquetasCategoria = data.gastoPorCategoria.map(i => i.nombre);
    const montosCategoria = data.gastoPorCategoria.map(i => i.monto);

    const etiquetasMiembro = data.gastoPorMiembro.map(i => i.nombre);
    const montosMiembro = data.gastoPorMiembro.map(i => i.monto);

    this.chartCategoria = new Chart(this.chartCategoriaRef.nativeElement, {
      type: 'bar',
      data: {
        labels: etiquetasCategoria,
        datasets: [{
          label: 'Gasto por categoría (S/.)',
          data: montosCategoria,
          backgroundColor: this.paleta
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } }
      }
    });

    this.chartMiembro = new Chart(this.chartMiembroRef.nativeElement, {
      type: 'bar',
      data: {
        labels: etiquetasMiembro,
        datasets: [{
          label: 'Gasto por miembro (S/.)',
          data: montosMiembro,
          backgroundColor: this.paleta
        }]
      },
      options: {
        responsive: true,
        indexAxis: 'y',
        plugins: { legend: { display: false } }
      }
    });

    this.chartDistribucion = new Chart(this.chartDistribucionRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: etiquetasCategoria,
        datasets: [{
          data: montosCategoria,
          backgroundColor: this.paleta
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'bottom' } }
      }
    });

    this.chartImpacto = new Chart(this.chartImpactoRef.nativeElement, {
      type: 'bar',
      data: {
        labels: ['Bajo', 'Medio', 'Alto'],
        datasets: [{
          label: 'Gasto por nivel de impacto (S/.)',
          data: [data.gastoImpactoBajo, data.gastoImpactoMedio, data.gastoImpactoAlto],
          backgroundColor: this.coloresImpacto
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } }
      }
    });

    this.chartTendencia = new Chart(this.chartTendenciaRef.nativeElement, {
      type: 'line',
      data: {
        labels: data.tendenciaMensual.map(i => i.nombre),
        datasets: [{
          label: 'Gasto mensual (S/.)',
          data: data.tendenciaMensual.map(i => i.monto),
          borderColor: '#2E7D57',
          backgroundColor: 'rgba(46, 125, 87, 0.15)',
          fill: true,
          tension: 0.3
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } }
      }
    });
  }

  claseRecomendacion(nivel: string): string {
    if (nivel === 'ALTA') return 'recomendacion-alta';
    if (nivel === 'MEDIA') return 'recomendacion-media';
    return 'recomendacion-info';
  }

  iconoRecomendacion(nivel: string): string {
    if (nivel === 'ALTA') return '🔴';
    if (nivel === 'MEDIA') return '🟡';
    return '🟢';
  }
}