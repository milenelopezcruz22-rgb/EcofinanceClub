import { Component, OnInit, Injector, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Menu } from '../menu/menu';
import { GastoService, SaldoMiembro, Deuda } from '../../core/gasto.service';
import { PagoDeudaService, PagoDeuda, PagoDeudaDTO } from '../../core/pago-deuda.service';
import { AuthService } from '../../core/auth.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';

@Component({
  selector: 'app-deudas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Menu],
  templateUrl: './deudas.html',
  styleUrl: './deudas.scss'
})
export class Deudas implements OnInit {
  protected readonly grupoContextoService = inject(GrupoContextoService);
  private readonly injector = inject(Injector);

  formulario!: FormGroup;
  formPago!: FormGroup;

  readonly listaGrupos = this.grupoContextoService.grupos;
  readonly grupoSeleccionadoId = this.grupoContextoService.grupoSeleccionadoId;
  saldos = signal<SaldoMiembro[]>([]);
  deudas = signal<Deuda[]>([]);
  pagos = signal<PagoDeuda[]>([]);
  mostrarFormPago = signal<boolean>(false);

  private idGrupoActual: number | null = null;

  constructor(
    private gastoService: GastoService,
    private pagoDeudaService: PagoDeudaService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.formulario = new FormGroup({
      grupo: new FormControl('', Validators.required)
    });

    this.formPago = new FormGroup({
      deudorId: new FormControl('', Validators.required),
      deudorNombre: new FormControl(''),
      acreedorId: new FormControl('', Validators.required),
      acreedorNombre: new FormControl(''),
      monto: new FormControl('', [Validators.required, Validators.min(0.01)]),
      fecha: new FormControl(this.hoyISO(), Validators.required),
      nota: new FormControl('')
    });

    this.grupoContextoService.cargarGrupos();

    effect(() => {
      const idGrupo = this.grupoContextoService.grupoSeleccionadoId();
      this.formulario.patchValue({ grupo: idGrupo ?? '' }, { emitEvent: false });
      this.idGrupoActual = idGrupo;
      this.mostrarFormPago.set(false);

      if (!idGrupo) {
        this.saldos.set([]);
        this.deudas.set([]);
        this.pagos.set([]);
        return;
      }

      this.cargarSaldosYDeudas(idGrupo);
      this.cargarPagos(idGrupo);
    }, { injector: this.injector });

    this.formulario.get('grupo')!.valueChanges.subscribe((valor) => {
      const idGrupo = valor ? Number(valor) : null;
      this.grupoContextoService.seleccionarGrupo(idGrupo);
    });
  }

  private cargarSaldosYDeudas(idGrupo: number): void {
    this.gastoService.obtenerSaldos(idGrupo).subscribe({
      next: (saldos) => this.saldos.set(saldos),
      error: () => this.saldos.set([])
    });

    this.gastoService.obtenerDeudas(idGrupo).subscribe({
      next: (deudas) => this.deudas.set(deudas),
      error: () => this.deudas.set([])
    });
  }

  private cargarPagos(idGrupo: number): void {
    this.pagoDeudaService.listarPorGrupo(idGrupo).subscribe({
      next: (pagos) => this.pagos.set(pagos),
      error: () => this.pagos.set([])
    });
  }

  private hoyISO(): string {
    return new Date().toISOString().substring(0, 10);
  }

  seleccionarDeuda(d: Deuda): void {
    if (this.authService.esMiembro()) {
      return;
    }

    this.formPago.setValue({
      deudorId: d.deudorId,
      deudorNombre: d.deudorNombre,
      acreedorId: d.acreedorId,
      acreedorNombre: d.acreedorNombre,
      monto: d.monto,
      fecha: this.hoyISO(),
      nota: ''
    });

    this.mostrarFormPago.set(true);
  }

  cancelarPago(): void {
    this.mostrarFormPago.set(false);
    this.formPago.reset();
  }

  registrarPago(): void {
    if (this.authService.esMiembro()) {
      return;
    }

    if (this.formPago.invalid || !this.idGrupoActual) {
      this.formPago.markAllAsTouched();
      return;
    }

    const idGrupo = this.idGrupoActual;
    const valores = this.formPago.value;

    const dto: PagoDeudaDTO = {
      grupoId: idGrupo,
      deudorId: Number(valores.deudorId),
      acreedorId: Number(valores.acreedorId),
      monto: Number(valores.monto),
      fecha: valores.fecha,
      nota: valores.nota || undefined
    };

    this.pagoDeudaService.registrar(dto).subscribe({
      next: () => {
        this.mostrarFormPago.set(false);
        this.formPago.reset();

        this.cargarSaldosYDeudas(idGrupo);
        this.cargarPagos(idGrupo);
      },
      error: (err) => alert(err.error?.mensaje ?? 'Error al registrar el pago')
    });
  }
}
