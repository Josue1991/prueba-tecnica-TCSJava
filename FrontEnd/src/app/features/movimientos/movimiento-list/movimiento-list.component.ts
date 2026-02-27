import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovimientoService, ClienteService, CuentaService } from '../../../core/services';
import { Movimiento, Cliente, Cuenta } from '../../../core/models';
import { GenericTableComponent } from '../../../shared/components/generic-table/generic-table.component';
import { TableConfig } from '../../../shared/models/table-config.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-movimiento-list',
  standalone: true,
  imports: [CommonModule, FormsModule, GenericTableComponent],
  templateUrl: './movimiento-list.component.html',
  styleUrl: './movimiento-list.component.css'
})
export class MovimientoListComponent implements OnInit {
  movimientos: Movimiento[] = [];
  clientes: Cliente[] = [];
  cuentas: Cuenta[] = [];
  loading = false;
  error: string | null = null;

  // Filtros
  filtroTipo: 'todos' | 'cliente' | 'cuenta' = 'todos';
  clienteSeleccionado: number | null = null;
  cuentaSeleccionada: number | null = null;
  fechaInicio: string = '';
  fechaFin: string = '';

  tableConfig: TableConfig = {
    title: 'Listado de Movimientos',
    showCreateButton: false,
    searchable: true,
    searchFields: ['numeroCuenta', 'nombreCliente', 'tipo'],
    searchPlaceholder: 'Buscar por cuenta, cliente o tipo...',
    pageable: true,
    pageSize: 10,
    pageSizeOptions: [5, 10, 25, 50],
    columns: [
      { key: 'id', label: 'ID', type: 'text' },
      {
        key: 'tipo',
        label: 'Tipo',
        type: 'text'
      },
      { key: 'numeroCuenta', label: 'Cuenta', type: 'text' },
      { key: 'nombreCliente', label: 'Cliente', type: 'text' },
      { key: 'valor', label: 'Valor', type: 'currency', format: 'USD' },
      { key: 'saldoAnterior', label: 'Saldo Anterior', type: 'currency', format: 'USD' },
      { key: 'saldoNuevo', label: 'Saldo Nuevo', type: 'currency', format: 'USD' },
      { key: 'fechaMovimiento', label: 'Fecha', type: 'date' }
    ],
    actions: []
  };

  constructor(
    private movimientoService: MovimientoService,
    private clienteService: ClienteService,
    private cuentaService: CuentaService
  ) {}

  ngOnInit(): void {
    this.cargarClientes();
    this.cargarCuentas();
    this.cargarTodosLosMovimientos();
  }

  cargarClientes(): void {
    this.clienteService.obtenerTodosLosClientes().subscribe({
      next: (data) => {
        this.clientes = data;
      },
      error: (err: Error) => {
        console.error('Error al cargar clientes:', err);
      }
    });
  }

  cargarCuentas(): void {
    this.cuentaService.obtenerTodasLasCuentas().subscribe({
      next: (data) => {
        this.cuentas = data;
      },
      error: (err: Error) => {
        console.error('Error al cargar cuentas:', err);
      }
    });
  }

  cargarTodosLosMovimientos(): void {
    this.loading = true;
    this.error = null;

    this.movimientoService.obtenerTodosLosMovimientos().subscribe({
      next: (data) => {
        this.movimientos = data;
        this.loading = false;
      },
      error: (err: Error) => {
        this.error = err.message;
        this.loading = false;
        Swal.fire({ icon: 'error', title: 'Error', text: err.message });
      }
    });
  }

  aplicarFiltros(): void {
    this.loading = true;
    this.error = null;

    // Validación previa antes de llamar al servicio
    if (this.filtroTipo === 'cliente' && !this.clienteSeleccionado) {
      this.loading = false;
      Swal.fire({ icon: 'warning', title: 'Atención', text: 'Por favor seleccione un cliente.' });
      return;
    }
    if (this.filtroTipo === 'cuenta' && !this.cuentaSeleccionada) {
      this.loading = false;
      Swal.fire({ icon: 'warning', title: 'Atención', text: 'Por favor seleccione una cuenta.' });
      return;
    }

    const onSuccess = (data: Movimiento[]) => {
      this.movimientos = data;
      this.loading = false;
      if (data.length === 0) {
        Swal.fire({
          icon: 'info',
          title: 'Sin resultados',
          text: 'No se encontraron movimientos para los filtros aplicados.',
          timer: 2500,
          showConfirmButton: false
        });
      } else {
        Swal.fire({
          icon: 'success',
          title: '¡Listo!',
          text: `Se encontraron ${data.length} movimiento(s).`,
          timer: 2000,
          showConfirmButton: false
        });
      }
    };

    const onError = (err: Error) => {
      this.error = err.message;
      this.loading = false;
      Swal.fire({ icon: 'error', title: 'Error al buscar', text: err.message });
    };

    if (this.filtroTipo === 'todos') {
      this.cargarTodosLosMovimientos();
    } else if (this.filtroTipo === 'cliente' && this.clienteSeleccionado) {
      if (this.fechaInicio && this.fechaFin) {
        this.movimientoService.obtenerMovimientosPorClienteYFechas(
          this.clienteSeleccionado, this.fechaInicio, this.fechaFin
        ).subscribe({ next: onSuccess, error: onError });
      } else {
        this.movimientoService.obtenerMovimientosPorCliente(this.clienteSeleccionado)
          .subscribe({ next: onSuccess, error: onError });
      }
    } else if (this.filtroTipo === 'cuenta' && this.cuentaSeleccionada) {
      if (this.fechaInicio && this.fechaFin) {
        this.movimientoService.obtenerMovimientosPorCuentaYFechas(
          this.cuentaSeleccionada, this.fechaInicio, this.fechaFin
        ).subscribe({ next: onSuccess, error: onError });
      } else {
        this.movimientoService.obtenerMovimientosPorCuenta(this.cuentaSeleccionada)
          .subscribe({ next: onSuccess, error: onError });
      }
    }
  }

  limpiarFiltros(): void {
    this.filtroTipo = 'todos';
    this.clienteSeleccionado = null;
    this.cuentaSeleccionada = null;
    this.fechaInicio = '';
    this.fechaFin = '';
    this.cargarTodosLosMovimientos();
  }
}
