import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { ReporteService } from '../../../core/services/reporte.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { CuentaService } from '../../../core/services/cuenta.service';
import { Cliente } from '../../../core/models/cliente.model';
import { Cuenta } from '../../../core/models/cuenta.model';
import { GenericTableComponent } from '../../../shared/components/generic-table/generic-table.component';
import { TableConfig } from '../../../shared/models/table-config.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-reporte-main',
  imports: [CommonModule, FormsModule, GenericTableComponent],
  templateUrl: './reporte-main.component.html',
  styleUrl: './reporte-main.component.css'
})
export class ReporteMainComponent implements OnInit {
  // Tipo de reporte
  tipoReporte: 'cliente' | 'cuenta' | 'general' = 'general';

  // Datos para selección
  clientes: Cliente[] = [];
  cuentas: Cuenta[] = [];
  clienteSeleccionado: number | null = null;
  cuentaSeleccionada: number | null = null;

  // Fechas
  fechaInicio: string = '';
  fechaFin: string = '';

  // Email
  emailDestinatario: string = '';
  emailAsunto: string = '';
  emailMensaje: string = '';
  formatoEmail: 'pdf' | 'excel' = 'pdf';
  mostrarFormularioEmail: boolean = false;

  // Preview JSON
  reporteJSON: any = null;
  mostrarPreview: boolean = false;

  // Configuración y datos para tabla genérica
  tableConfig: TableConfig | null = null;
  tableData: any[] = [];

  // Estados
  loading: boolean = false;
  error: string | null = null;
  mensaje: string | null = null;

  constructor(
    private reporteService: ReporteService,
    private clienteService: ClienteService,
    private cuentaService: CuentaService
  ) {}

  ngOnInit(): void {
    this.cargarClientes();
    this.cargarCuentas();
    this.inicializarFechas();
  }

  cargarClientes(): void {
    this.clienteService.obtenerTodosLosClientes().subscribe({
      next: (data) => this.clientes = data,
      error: (err: Error) => console.error('Error al cargar clientes:', err)
    });
  }

  cargarCuentas(): void {
    this.cuentaService.obtenerTodasLasCuentas().subscribe({
      next: (data) => this.cuentas = data,
      error: (err: Error) => console.error('Error al cargar cuentas:', err)
    });
  }

  inicializarFechas(): void {
    const hoy = new Date();
    const hace30Dias = new Date();
    hace30Dias.setDate(hoy.getDate() - 30);
    
    this.fechaFin = this.formatDate(hoy);
    this.fechaInicio = this.formatDate(hace30Dias);
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  cambiarTipoReporte(tipo: 'cliente' | 'cuenta' | 'general'): void {
    this.tipoReporte = tipo;
    this.limpiarMensajes();
    this.cerrarPreview();
  }

  // Generar reporte JSON
  generarReporteJSON(): void {
    this.limpiarMensajes();
    this.loading = true;

    const fechaInicioFormateada = this.reporteService.formatearFecha(new Date(this.fechaInicio));
    const fechaFinFormateada = this.reporteService.formatearFecha(new Date(this.fechaFin));

    let observable: Observable<any>;

    if (this.tipoReporte === 'cliente' && this.clienteSeleccionado) {
      observable = this.reporteService.obtenerReporteMovimientosCliente(
        this.clienteSeleccionado,
        fechaInicioFormateada,
        fechaFinFormateada
      );
    } else if (this.tipoReporte === 'cuenta' && this.cuentaSeleccionada) {
      observable = this.reporteService.obtenerReporteMovimientosCuenta(
        this.cuentaSeleccionada,
        fechaInicioFormateada,
        fechaFinFormateada
      );
    } else if (this.tipoReporte === 'general') {
      observable = this.reporteService.obtenerReporteCuentas(
        fechaInicioFormateada,
        fechaFinFormateada
      );
    } else {
      this.error = 'Por favor seleccione un ' + this.tipoReporte;
      this.loading = false;
      Swal.fire({ icon: 'warning', title: 'Selección requerida', text: `Por favor seleccione un ${this.tipoReporte} antes de generar el reporte.` });
      return;
    }

    observable.subscribe({
      next: (data) => {
        this.reporteJSON = data;
        this.prepararDatosTabla();
        this.mostrarPreview = true;
        this.loading = false;
        this.mensaje = 'Reporte generado correctamente';
        const total = this.tableData.length;
        Swal.fire({
          icon: 'success',
          title: '¡Reporte generado!',
          text: total > 0
            ? `Se encontraron ${total} registro(s) en el período seleccionado.`
            : 'No se encontraron registros en el período seleccionado.',
          timer: 2500,
          showConfirmButton: false
        });
      },
      error: (err: Error) => {
        this.error = 'Error al generar reporte: ' + err.message;
        this.loading = false;
        Swal.fire({ icon: 'error', title: 'Error al generar reporte', text: err.message });
      }
    });
  }

  // Descargar PDF
  descargarPDF(): void {
    this.limpiarMensajes();
    this.loading = true;

    const fechaInicioFormateada = this.reporteService.formatearFecha(new Date(this.fechaInicio));
    const fechaFinFormateada = this.reporteService.formatearFecha(new Date(this.fechaFin));

    let observable: Observable<any>;

    if (this.tipoReporte === 'cliente' && this.clienteSeleccionado) {
      observable = this.reporteService.descargarReporteClientePdf(
        this.clienteSeleccionado,
        fechaInicioFormateada,
        fechaFinFormateada
      );
    } else if (this.tipoReporte === 'cuenta' && this.cuentaSeleccionada) {
      observable = this.reporteService.descargarReporteCuentaPdf(
        this.cuentaSeleccionada,
        fechaInicioFormateada,
        fechaFinFormateada
      );
    } else {
      this.error = 'Por favor seleccione un ' + this.tipoReporte;
      this.loading = false;
      Swal.fire({ icon: 'warning', title: 'Selección requerida', text: `Por favor seleccione un ${this.tipoReporte} para descargar el PDF.` });
      return;
    }

    observable.subscribe({
      next: (archivoResponse) => {
        this.reporteService.descargarArchivo(archivoResponse);
        this.loading = false;
        this.mensaje = 'PDF descargado correctamente';
        Swal.fire({
          icon: 'success',
          title: '¡PDF descargado!',
          text: 'El archivo PDF se ha descargado correctamente.',
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err: Error) => {
        this.error = 'Error al descargar PDF: ' + err.message;
        this.loading = false;
        Swal.fire({ icon: 'error', title: 'Error al descargar PDF', text: err.message });
      }
    });
  }

  // Descargar Excel
  descargarExcel(): void {
    this.limpiarMensajes();
    this.loading = true;

    const fechaInicioFormateada = this.reporteService.formatearFecha(new Date(this.fechaInicio));
    const fechaFinFormateada = this.reporteService.formatearFecha(new Date(this.fechaFin));

    let observable: Observable<any>;

    if (this.tipoReporte === 'cliente' && this.clienteSeleccionado) {
      observable = this.reporteService.descargarReporteClienteExcel(
        this.clienteSeleccionado,
        fechaInicioFormateada,
        fechaFinFormateada
      );
    } else if (this.tipoReporte === 'cuenta' && this.cuentaSeleccionada) {
      observable = this.reporteService.descargarReporteCuentaExcel(
        this.cuentaSeleccionada,
        fechaInicioFormateada,
        fechaFinFormateada
      );
    } else if (this.tipoReporte === 'general') {
      observable = this.reporteService.descargarReporteCuentasExcel(
        fechaInicioFormateada,
        fechaFinFormateada
      );
    } else {
      this.error = 'Por favor seleccione un ' + this.tipoReporte;
      this.loading = false;
      Swal.fire({ icon: 'warning', title: 'Selección requerida', text: `Por favor seleccione un ${this.tipoReporte} para descargar el Excel.` });
      return;
    }

    observable.subscribe({
      next: (archivoResponse) => {
        this.reporteService.descargarArchivo(archivoResponse);
        this.loading = false;
        this.mensaje = 'Excel descargado correctamente';
        Swal.fire({
          icon: 'success',
          title: '¡Excel descargado!',
          text: 'El archivo Excel se ha descargado correctamente.',
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err: Error) => {
        this.error = 'Error al descargar Excel: ' + err.message;
        this.loading = false;
        Swal.fire({ icon: 'error', title: 'Error al descargar Excel', text: err.message });
      }
    });
  }

  // Mostrar formulario de email
  abrirFormularioEmail(): void {
    this.mostrarFormularioEmail = true;
    this.limpiarMensajes();
    // Pre-rellenar asunto
    if (!this.emailAsunto) {
      this.emailAsunto = `Reporte de ${this.tipoReporte} - ${new Date().toLocaleDateString('es-ES')}`;
    }
  }

  cerrarFormularioEmail(): void {
    this.mostrarFormularioEmail = false;
  }

  // Enviar por email
  enviarEmail(): void {
    if (!this.emailDestinatario || !this.emailAsunto) {
      Swal.fire({ icon: 'warning', title: 'Campos requeridos', text: 'Por favor complete el email y el asunto.' });
      return;
    }
    if (this.tipoReporte !== 'general' && !this.clienteSeleccionado && !this.cuentaSeleccionada) {
      Swal.fire({ icon: 'warning', title: 'Selección requerida', text: `Por favor seleccione un ${this.tipoReporte} para enviar el reporte.` });
      return;
    }

    this.limpiarMensajes();
    this.loading = true;

    const fechaInicioFormateada = this.reporteService.formatearFecha(new Date(this.fechaInicio));
    const fechaFinFormateada = this.reporteService.formatearFecha(new Date(this.fechaFin));

    let observable: Observable<any>;

    const formatoMayusculas = this.formatoEmail.toUpperCase() as 'PDF' | 'EXCEL';

    if (this.tipoReporte === 'cliente' && this.clienteSeleccionado) {
      observable = this.reporteService.enviarReporteClientePorEmail(
        this.clienteSeleccionado,
        fechaInicioFormateada,
        fechaFinFormateada,
        this.emailDestinatario,
        formatoMayusculas,
        this.emailAsunto,
        this.emailMensaje
      );
    } else if (this.tipoReporte === 'cuenta' && this.cuentaSeleccionada) {
      observable = this.reporteService.enviarReporteCuentaPorEmail(
        this.cuentaSeleccionada,
        fechaInicioFormateada,
        fechaFinFormateada,
        this.emailDestinatario,
        formatoMayusculas,
        this.emailAsunto,
        this.emailMensaje
      );
    } else if (this.tipoReporte === 'general') {
      observable = this.reporteService.enviarReporteCuentasPorEmail(
        fechaInicioFormateada,
        fechaFinFormateada,
        this.emailDestinatario,
        formatoMayusculas,
        this.emailAsunto,
        this.emailMensaje
      );
    } else {
      this.error = 'Por favor seleccione un ' + this.tipoReporte;
      this.loading = false;
      Swal.fire({ icon: 'warning', title: 'Selección requerida', text: `Por favor seleccione un ${this.tipoReporte} para enviar el reporte.` });
      return;
    }

    observable.subscribe({
      next: (response) => {
        this.loading = false;
        this.mensaje = 'Email enviado correctamente a ' + this.emailDestinatario;
        this.cerrarFormularioEmail();
        Swal.fire({
          icon: 'success',
          title: '¡Email enviado!',
          html: `El reporte fue enviado correctamente a<br><b>${this.emailDestinatario}</b>`,
          timer: 3000,
          showConfirmButton: false
        });
        this.emailDestinatario = '';
        this.emailMensaje = '';
      },
      error: (err: Error) => {
        this.error = 'Error al enviar email: ' + err.message;
        this.loading = false;
        Swal.fire({ icon: 'error', title: 'Error al enviar email', text: err.message });
      }
    });
  }

  cerrarPreview(): void {
    this.mostrarPreview = false;
    this.reporteJSON = null;
    this.tableConfig = null;
    this.tableData = [];
  }

  limpiarMensajes(): void {
    this.error = null;
    this.mensaje = null;
  }

  prepararDatosTabla(): void {
    if (!this.reporteJSON) return;

    if (this.tipoReporte === 'general') {
      this.tableConfig = {
        title: 'Reporte General de Cuentas',
        columns: [
          { key: 'numeroCuenta', label: 'Número Cuenta', type: 'text' },
          { key: 'tipoCuenta', label: 'Tipo', type: 'text' },
          { key: 'nombreCliente', label: 'Cliente', type: 'text' },
          { key: 'saldoInicial', label: 'Saldo Inicial', type: 'currency', format: 'USD' },
          { key: 'saldoActual', label: 'Saldo Actual', type: 'currency', format: 'USD' },
          { key: 'estado', label: 'Estado', type: 'badge', 
            badgeConfig: { 
              trueClass: 'badge-active', 
              falseClass: 'badge-inactive',
              trueLabel: 'Activa',
              falseLabel: 'Inactiva'
            }
          },
          { key: 'cantidadMovimientos', label: 'Movimientos', type: 'text' },
          { key: 'fechaCreacion', label: 'Fecha Creación', type: 'date' }
        ],
        searchable: true,
        searchPlaceholder: 'Buscar en cuentas...',
        pageable: true,
        pageSize: 10,
        pageSizeOptions: [5, 10, 25, 50]
      };
      this.tableData = this.reporteJSON.cuentas || [];
    } 
    else if (this.tipoReporte === 'cuenta') {
      this.tableConfig = {
        title: `Movimientos - Cuenta ${this.reporteJSON.numeroCuenta}`,
        columns: [
          { key: 'id', label: 'ID', type: 'text' },
          { key: 'fechaMovimiento', label: 'Fecha', type: 'date' },
          { key: 'tipo', label: 'Tipo', type: 'text' },
          { key: 'valor', label: 'Valor', type: 'currency', format: 'USD' },
          { key: 'saldoAnterior', label: 'Saldo Anterior', type: 'currency', format: 'USD' },
          { key: 'saldoActual', label: 'Saldo Actual', type: 'currency', format: 'USD' }
        ],
        searchable: true,
        searchPlaceholder: 'Buscar en movimientos...',
        pageable: true,
        pageSize: 10,
        pageSizeOptions: [5, 10, 25, 50]
      };
      this.tableData = this.reporteJSON.movimientos || [];
    }
    else if (this.tipoReporte === 'cliente') {
      // Aplanar movimientos de todas las cuentas incluyendo info de cuenta
      const movimientosAplanados: any[] = [];
      if (this.reporteJSON.cuentas) {
        this.reporteJSON.cuentas.forEach((cuenta: any) => {
          if (cuenta.movimientos) {
            cuenta.movimientos.forEach((mov: any) => {
              movimientosAplanados.push({
                ...mov,
                numeroCuenta: cuenta.numeroCuenta,
                tipoCuenta: cuenta.tipoCuenta,
                estadoCuenta: cuenta.estado
              });
            });
          }
        });
      }
      
      this.tableConfig = {
        title: `Movimientos del Cliente: ${this.reporteJSON.nombreCliente}`,
        columns: [
          { key: 'id', label: '#', type: 'text' },
          { key: 'numeroCuenta', label: 'Cuenta', type: 'text' },
          { key: 'tipoCuenta', label: 'Tipo Cuenta', type: 'text' },
          { key: 'fechaMovimiento', label: 'Fecha', type: 'date' },
          { key: 'tipo', label: 'Tipo Movimiento', type: 'text' },
          { key: 'valor', label: 'Valor', type: 'currency', format: 'USD' },
          { key: 'saldoAnterior', label: 'Saldo Anterior', type: 'currency', format: 'USD' },
          { key: 'saldoActual', label: 'Saldo Actual', type: 'currency', format: 'USD' }
        ],
        searchable: true,
        searchPlaceholder: 'Buscar en movimientos...',
        pageable: true,
        pageSize: 10,
        pageSizeOptions: [5, 10, 25, 50]
      };
      this.tableData = movimientosAplanados;
    }
  }
}
