import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CuentaService } from '../../../core/services/cuenta.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { Cuenta, CuentaRequest, TipoCuenta } from '../../../core/models/cuenta.model';
import { Cliente } from '../../../core/models/cliente.model';
import { GenericTableComponent } from '../../../shared/components/generic-table/generic-table.component';
import { GenericModalComponent } from '../../../shared/components/generic-modal/generic-modal.component';
import { CuentaFormComponent } from '../cuenta-form/cuenta-form.component';
import { TableConfig, TableActionEvent } from '../../../shared/models/table-config.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-cuenta-list',
  standalone: true,
  imports: [CommonModule, FormsModule, GenericTableComponent, GenericModalComponent, CuentaFormComponent],
  templateUrl: './cuenta-list.component.html',
  styleUrls: ['./cuenta-list.component.scss']
})
export class CuentaListComponent implements OnInit {
  @ViewChild(CuentaFormComponent) cuentaFormComponent!: CuentaFormComponent;

  cuentas: Cuenta[] = [];
  clientes: Cliente[] = [];
  loading = false;
  error: string | null = null;

  // Modal
  modalAbierto = false;
  modoEdicion = false;
  guardando = false;
  cuentaForm: CuentaRequest = this.crearFormularioVacio();
  cuentaEditandoId: number | null = null;
  formularioValido_: boolean = false;

  tableConfig: TableConfig = {
    title: 'Gestión de Cuentas',
    showCreateButton: true,
    createButtonLabel: 'Nueva Cuenta',
    
    // Configuración de búsqueda
    searchable: true,
    searchFields: ['numeroCuenta', 'nombreCliente', 'tipoCuenta'],
    searchPlaceholder: 'Buscar por número de cuenta, cliente o tipo...',
    
    // Configuración de paginación
    pageable: true,
    pageSize: 10,
    pageSizeOptions: [5, 10, 25, 50],
    
    columns: [
      { key: 'id', label: 'ID', type: 'text' },
      { key: 'numeroCuenta', label: 'Número de Cuenta', type: 'text' },
      { key: 'tipoCuenta', label: 'Tipo', type: 'text' },
      { key: 'saldoActual', label: 'Saldo', type: 'currency', format: 'USD' },
      { key: 'nombreCliente', label: 'Cliente', type: 'text' },
      { 
        key: 'estado', 
        label: 'Estado', 
        type: 'badge',
        badgeConfig: {
          trueClass: 'badge-active',
          falseClass: 'badge-inactive',
          trueLabel: 'Activa',
          falseLabel: 'Inactiva'
        }
      }
    ],
    actions: [
      { label: 'Ver', action: 'view', class: 'btn-view', icon: 'fa-solid fa-eye' },
      { label: 'Activar/Desactivar', action: 'toggle', class: 'btn-toggle', icon: 'fa-solid fa-toggle-on' }
    ]
  };

  constructor(
    private cuentaService: CuentaService,
    private clienteService: ClienteService
  ) { }

  ngOnInit(): void {
    this.cargarCuentas();
    this.cargarClientes();
  }

  cargarCuentas(): void {
    this.loading = true;
    this.error = null;
    
    this.cuentaService.obtenerTodasLasCuentas().subscribe({
      next: (data) => {
        this.cuentas = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  cargarClientes(): void {
    this.clienteService.obtenerClientesActivos().subscribe({
      next: (data) => {
        this.clientes = data;
      },
      error: (err: Error) => {
        console.error('Error al cargar clientes:', err);
      }
    });
  }

  handleCreate(): void {
    this.modoEdicion = false;
    this.cuentaForm = this.crearFormularioVacio();
    this.cuentaEditandoId = null;
    this.modalAbierto = true;
  }

  handleAction(event: TableActionEvent<Cuenta>): void {
    switch (event.action) {
      case 'view':
        this.verCuenta(event.data);
        break;
      case 'toggle':
        this.toggleEstadoCuenta(event.data);
        break;
    }
  }

  verCuenta(cuenta: Cuenta): void {
    Swal.fire({
      title: `Cuenta ${cuenta.numeroCuenta}`,
      icon: 'info',
      html: `
        <table style="width:100%;text-align:left;border-collapse:collapse">
          <tr><td style="padding:6px 8px;font-weight:600">Número:</td><td style="padding:6px 8px">${cuenta.numeroCuenta}</td></tr>
          <tr><td style="padding:6px 8px;font-weight:600">Tipo:</td><td style="padding:6px 8px">${cuenta.tipoCuenta}</td></tr>
          <tr><td style="padding:6px 8px;font-weight:600">Saldo:</td><td style="padding:6px 8px">$${cuenta.saldoActual?.toFixed(2) ?? '0.00'}</td></tr>
          <tr><td style="padding:6px 8px;font-weight:600">Cliente:</td><td style="padding:6px 8px">${cuenta.nombreCliente}</td></tr>
          <tr><td style="padding:6px 8px;font-weight:600">Estado:</td><td style="padding:6px 8px">${cuenta.estado ? 'Activa' : 'Inactiva'}</td></tr>
        </table>`,
      confirmButtonText: 'Cerrar'
    });
  }

  toggleEstadoCuenta(cuenta: Cuenta): void {
    if (cuenta.estado) {
      // DESACTIVAR: confirmación simple
      Swal.fire({
        title: '¿Desactivar cuenta?',
        text: `¿Está seguro de desactivar la cuenta ${cuenta.numeroCuenta}?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#6b7280',
        confirmButtonText: 'Sí, desactivar',
        cancelButtonText: 'Cancelar'
      }).then((result) => {
        if (result.isConfirmed) {
          this.ejecutarDesactivarCuenta(cuenta);
        }
      });
    } else {
      // ACTIVAR: verificar primero el estado del cliente
      this.clienteService.obtenerClientePorId(cuenta.clienteId).subscribe({
        next: (cliente) => {
          if (!cliente.estado) {
            // Cliente Y cuenta están inactivos → activar ambos o cancelar
            Swal.fire({
              icon: 'info',
              title: 'Cliente también inactivo',
              html: `El cliente <b>${cuenta.nombreCliente}</b> también está inactivo.<br><br>
                     Para activar esta cuenta es necesario activar también al cliente.`,
              showCancelButton: true,
              confirmButtonColor: '#22c55e',
              cancelButtonColor: '#6b7280',
              confirmButtonText: 'Activar cliente y cuenta',
              cancelButtonText: 'Cancelar'
            }).then((result) => {
              if (result.isConfirmed) {
                this.ejecutarActivarClienteConCuenta(cliente.id!, cuenta);
              }
            });
          } else {
            // Cliente activo → confirmación normal para activar cuenta
            Swal.fire({
              title: '¿Activar cuenta?',
              text: `¿Está seguro de activar la cuenta ${cuenta.numeroCuenta}?`,
              icon: 'question',
              showCancelButton: true,
              confirmButtonColor: '#22c55e',
              cancelButtonColor: '#6b7280',
              confirmButtonText: 'Sí, activar',
              cancelButtonText: 'Cancelar'
            }).then((result) => {
              if (result.isConfirmed) {
                this.ejecutarActivarCuenta(cuenta);
              }
            });
          }
        },
        error: () => {
          // Fallback: intentar activar directamente
          Swal.fire({
            title: '¿Activar cuenta?',
            text: `¿Está seguro de activar la cuenta ${cuenta.numeroCuenta}?`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#22c55e',
            cancelButtonColor: '#6b7280',
            confirmButtonText: 'Sí, activar',
            cancelButtonText: 'Cancelar'
          }).then((result) => {
            if (result.isConfirmed) {
              this.ejecutarActivarCuenta(cuenta);
            }
          });
        }
      });
    }
  }

  private ejecutarActivarCuenta(cuenta: Cuenta): void {
    this.cuentaService.activarCuenta(cuenta.id!).subscribe({
      next: () => {
        this.cargarCuentas();
        Swal.fire({
          icon: 'success',
          title: '¡Listo!',
          text: `Cuenta ${cuenta.numeroCuenta} activada exitosamente`,
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err: Error) => {
        console.error('Error al activar cuenta:', err);
        Swal.fire({ icon: 'error', title: 'Error', text: err.message });
      }
    });
  }

  private ejecutarDesactivarCuenta(cuenta: Cuenta): void {
    this.cuentaService.desactivarCuenta(cuenta.id!).subscribe({
      next: () => {
        this.cargarCuentas();
        Swal.fire({
          icon: 'success',
          title: '¡Listo!',
          text: `Cuenta ${cuenta.numeroCuenta} desactivada exitosamente`,
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err: Error) => {
        console.error('Error al desactivar cuenta:', err);
        Swal.fire({ icon: 'error', title: 'Error', text: err.message });
      }
    });
  }

  private ejecutarActivarClienteConCuenta(clienteId: number, cuenta: Cuenta): void {
    this.clienteService.activarClienteConCuentas(clienteId, { cuentasIds: [cuenta.id!] }).subscribe({
      next: () => {
        this.cargarCuentas();
        Swal.fire({
          icon: 'success',
          title: '¡Listo!',
          text: `Cliente y cuenta ${cuenta.numeroCuenta} activados exitosamente`,
          timer: 2500,
          showConfirmButton: false
        });
      },
      error: (err: Error) => {
        console.error('Error al activar cliente con cuenta:', err);
        Swal.fire({ icon: 'error', title: 'Error', text: err.message });
      }
    });
  }

  // Manejadores de eventos del formulario
  onFormChange(formData: CuentaRequest): void {
    this.cuentaForm = formData;
  }

  onValidChange(isValid: boolean): void {
    this.formularioValido_ = isValid;
  }

  // Modal methods
  guardarCuenta(): void {
    if (!this.formularioValido_) {
      return;
    }

    this.guardando = true;

    this.cuentaService.crearCuenta(this.cuentaForm).subscribe({
      next: () => {
        this.guardando = false;
        this.cerrarModal();
        this.cargarCuentas();
        Swal.fire({
          icon: 'success',
          title: '¡Éxito!',
          text: 'Cuenta creada exitosamente',
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err: Error) => {
        this.guardando = false;
        console.error('Error al crear cuenta:', err);
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: `Error al crear la cuenta: ${err.message}`
        });
      }
    });
  }

  cerrarModal(): void {
    this.modalAbierto = false;
    this.cuentaForm = this.crearFormularioVacio();
    this.cuentaEditandoId = null;
    this.formularioValido_ = false;
  }

  crearFormularioVacio(): CuentaRequest {
    return {
      numeroCuenta: '',
      tipoCuenta: TipoCuenta.AHORRO,
      clienteId: 0
    };
  }

  getNombreCliente(clienteId: number): string {
    const cliente = this.clientes.find(c => c.id === clienteId);
    return cliente ? `${cliente.nombre}` : 'Desconocido';
  }
}
