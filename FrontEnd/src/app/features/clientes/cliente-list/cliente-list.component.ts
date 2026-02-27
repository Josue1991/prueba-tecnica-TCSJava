import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../../core/services/cliente.service';
import { Cliente, ClienteRequest } from '../../../core/models/cliente.model';
import { GenericTableComponent } from '../../../shared/components/generic-table/generic-table.component';
import { GenericModalComponent } from '../../../shared/components/generic-modal/generic-modal.component';
import { ClienteFormComponent } from '../cliente-form/cliente-form.component';
import { TableConfig, TableActionEvent } from '../../../shared/models/table-config.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [CommonModule, FormsModule, GenericTableComponent, GenericModalComponent, ClienteFormComponent],
  templateUrl: './cliente-list.component.html',
  styleUrls: ['./cliente-list.component.scss']
})
export class ClienteListComponent implements OnInit {
  @ViewChild(ClienteFormComponent) clienteFormComponent!: ClienteFormComponent;

  clientes: Cliente[] = [];
  loading = false;
  error: string | null = null;

  // Modal
  modalAbierto = false;
  modoEdicion = false;
  guardando = false;
  clienteForm: ClienteRequest = this.crearFormularioVacio();
  clienteEditandoId: number | null = null;
  formularioValido_: boolean = false;

  tableConfig: TableConfig = {
    title: 'Gestión de Clientes',
    showCreateButton: true,
    createButtonLabel: 'Nuevo Cliente',
    
    // Configuración de búsqueda
    searchable: true,
    searchFields: ['nombre', 'genero', 'identificacion', 'telefono'],
    searchPlaceholder: 'Buscar por nombre, género, documento o teléfono...',
    
    // Configuración de paginación
    pageable: true,
    pageSize: 10,
    pageSizeOptions: [5, 10, 25, 50],
    
    columns: [
      { key: 'id', label: 'ID', type: 'text' },
      { key: 'nombre', label: 'Nombre', type: 'text' },
      { key: 'genero', label: 'Género', type: 'text' },
      { key: 'edad', label: 'Edad', type: 'text' },
      { key: 'identificacion', label: 'Documento', type: 'text' },
      { key: 'direccion', label: 'Dirección', type: 'text' },
      { key: 'telefono', label: 'Teléfono', type: 'text' },
      { 
        key: 'estado', 
        label: 'Estado', 
        type: 'badge',
        badgeConfig: {
          trueClass: 'badge-active',
          falseClass: 'badge-inactive',
          trueLabel: 'Activo',
          falseLabel: 'Inactivo'
        }
      }
    ],
    actions: [
      { label: 'Editar', action: 'edit', class: 'btn-edit', icon: 'fa-solid fa-edit' },
      { label: 'Activar/Desactivar', action: 'toggle', class: 'btn-toggle', icon: 'fa-solid fa-toggle-on' }
    ]
  };

  constructor(private clienteService: ClienteService) { }

  ngOnInit(): void {
    this.cargarClientes();
  }

  cargarClientes(): void {
    this.loading = true;
    this.error = null;
    
    this.clienteService.obtenerTodosLosClientes().subscribe({
      next: (data) => {
        this.clientes = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  handleCreate(): void {
    this.modoEdicion = false;
    this.clienteForm = this.crearFormularioVacio();
    this.clienteEditandoId = null;
    this.modalAbierto = true;
  }

  handleAction(event: TableActionEvent<Cliente>): void {
    switch (event.action) {
      case 'edit':
        this.editarCliente(event.data);
        break;
      case 'toggle':
        this.toggleEstadoCliente(event.data);
        break;
    }
  }

  editarCliente(cliente: Cliente): void {
    this.modoEdicion = true;
    this.clienteEditandoId = cliente.id!;
    this.clienteForm = {
      nombre: cliente.nombre,
      genero: cliente.genero,
      identificacion: cliente.identificacion,
      direccion: cliente.direccion,
      telefono: cliente.telefono,
      edad: cliente.edad,
      password: ''
    };
    this.modalAbierto = true;
  }

  toggleEstadoCliente(cliente: Cliente): void {
    if (cliente.estado) {
      // DESACTIVAR: advertir que se desactivarán también todas sus cuentas activas
      Swal.fire({
        title: '¿Desactivar cliente?',
        html: `¿Está seguro de desactivar a <b>${cliente.nombre}</b>?<br><br>
               <small style="color:#ef4444">⚠️ Se desactivarán también <b>todas sus cuentas activas</b>.</small>`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#6b7280',
        confirmButtonText: 'Sí, desactivar',
        cancelButtonText: 'Cancelar'
      }).then((result) => {
        if (result.isConfirmed) {
          this.ejecutarDesactivarCliente(cliente);
        }
      });
    } else {
      // ACTIVAR: consultar estado del cliente y sus cuentas
      Swal.fire({ title: 'Consultando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

      this.clienteService.validarActivacion(cliente.id!).subscribe({
        next: (validacion) => {
          Swal.close();
          const cuentasInactivas = validacion.cuentas.filter(c => !c.estado);

          if (cuentasInactivas.length === 0) {
            // Sin cuentas inactivas → activar solo el cliente
            Swal.fire({
              title: `Activar cliente: ${cliente.nombre}`,
              html: `<p>${validacion.mensaje}</p><p style="margin-top:8px">El cliente no tiene cuentas inactivas.</p>`,
              icon: 'question',
              showCancelButton: true,
              confirmButtonColor: '#22c55e',
              cancelButtonColor: '#6b7280',
              confirmButtonText: 'Sí, activar',
              cancelButtonText: 'Cancelar'
            }).then((result) => {
              if (result.isConfirmed) {
                this.ejecutarActivarCliente(cliente);
              }
            });
          } else {
            // Con cuentas inactivas → mostrar checkboxes para seleccionar cuáles activar
            const checkboxesHtml = cuentasInactivas.map(c =>
              `<label style="display:flex;align-items:center;gap:10px;padding:6px 0;cursor:pointer">
                 <input type="checkbox" class="cuenta-check" value="${c.id}" style="width:16px;height:16px;cursor:pointer">
                 <span><b>${c.numeroCuenta}</b> &mdash; ${c.tipoCuenta}</span>
               </label>`
            ).join('');

            Swal.fire({
              title: `Activar cliente: ${cliente.nombre}`,
              html: `<p style="margin-bottom:12px">${validacion.mensaje}</p>
                     <p style="margin-bottom:8px;font-weight:600">Seleccione las cuentas a activar (opcional):</p>
                     <div style="text-align:left;max-height:200px;overflow-y:auto;border:1px solid #e2e8f0;border-radius:8px;padding:8px 12px">
                       ${checkboxesHtml}
                     </div>`,
              icon: 'info',
              showCancelButton: true,
              confirmButtonColor: '#22c55e',
              cancelButtonColor: '#6b7280',
              confirmButtonText: 'Activar',
              cancelButtonText: 'Cancelar',
              preConfirm: () => {
                const checks = Swal.getPopup()!.querySelectorAll<HTMLInputElement>('.cuenta-check:checked');
                return Array.from(checks).map(cb => Number(cb.value));
              }
            }).then((result) => {
              if (result.isConfirmed) {
                const selectedIds: number[] = result.value;
                if (selectedIds.length > 0) {
                  this.ejecutarActivarClienteConCuentas(cliente, selectedIds);
                } else {
                  this.ejecutarActivarCliente(cliente);
                }
              }
            });
          }
        },
        error: () => {
          Swal.close();
          // Fallback: activar solo el cliente
          Swal.fire({
            title: '¿Activar cliente?',
            text: `¿Está seguro de activar a ${cliente.nombre}?`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#22c55e',
            cancelButtonColor: '#6b7280',
            confirmButtonText: 'Sí, activar',
            cancelButtonText: 'Cancelar'
          }).then((result) => {
            if (result.isConfirmed) {
              this.ejecutarActivarCliente(cliente);
            }
          });
        }
      });
    }
  }

  private ejecutarActivarCliente(cliente: Cliente): void {
    this.clienteService.activarCliente(cliente.id!).subscribe({
      next: () => {
        this.cargarClientes();
        Swal.fire({
          icon: 'success',
          title: '¡Listo!',
          text: `Cliente ${cliente.nombre} activado exitosamente`,
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err: Error) => Swal.fire({ icon: 'error', title: 'Error', text: err.message })
    });
  }

  private ejecutarDesactivarCliente(cliente: Cliente): void {
    this.clienteService.desactivarCliente(cliente.id!).subscribe({
      next: () => {
        this.cargarClientes();
        Swal.fire({
          icon: 'success',
          title: '¡Listo!',
          text: `Cliente ${cliente.nombre} y sus cuentas activas desactivados exitosamente`,
          timer: 2500,
          showConfirmButton: false
        });
      },
      error: (err: Error) => Swal.fire({ icon: 'error', title: 'Error', text: err.message })
    });
  }

  private ejecutarActivarClienteConCuentas(cliente: Cliente, cuentasIds: number[]): void {
    this.clienteService.activarClienteConCuentas(cliente.id!, { cuentasIds }).subscribe({
      next: () => {
        this.cargarClientes();
        Swal.fire({
          icon: 'success',
          title: '¡Listo!',
          text: `Cliente ${cliente.nombre} y ${cuentasIds.length} cuenta(s) activadas exitosamente`,
          timer: 2500,
          showConfirmButton: false
        });
      },
      error: (err: Error) => Swal.fire({ icon: 'error', title: 'Error', text: err.message })
    });
  }

  // Manejadores de eventos del formulario
  onFormChange(formData: ClienteRequest): void {
    this.clienteForm = formData;
  }

  onValidChange(isValid: boolean): void {
    this.formularioValido_ = isValid;
  }

  // Modal methods
  guardarCliente(): void {
    if (!this.formularioValido_) {
      return;
    }

    this.guardando = true;

    const operacion = this.modoEdicion
      ? this.clienteService.actualizarCliente(this.clienteEditandoId!, this.clienteForm)
      : this.clienteService.crearCliente(this.clienteForm);

    operacion.subscribe({
      next: () => {
        this.guardando = false;
        this.cerrarModal();
        this.cargarClientes();
        Swal.fire({
          icon: 'success',
          title: '¡Éxito!',
          text: `Cliente ${this.modoEdicion ? 'actualizado' : 'creado'} exitosamente`,
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err: Error) => {
        this.guardando = false;
        console.error('Error al guardar cliente:', err);
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: `Error al guardar el cliente: ${err.message}`
        });
      }
    });
  }

  cerrarModal(): void {
    this.modalAbierto = false;
    this.clienteForm = this.crearFormularioVacio();
    this.clienteEditandoId = null;
    this.formularioValido_ = false;
  }

  crearFormularioVacio(): ClienteRequest {
    return {
      nombre: '',
      genero: '',
      identificacion: '',
      direccion: '',
      telefono: '',
      edad: 0,
      password: ''
    };
  }
}
