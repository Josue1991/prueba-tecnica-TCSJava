import { TipoMovimiento } from './movimiento.model';

export interface ReporteMovimientoDTO {
  movimientoId: number;
  tipo: TipoMovimiento;
  valor: number;
  saldoAnterior: number;
  saldoNuevo: number;
  fechaMovimiento: string;
}

export interface CuentaConMovimientos {
  cuentaId: number;
  numeroCuenta: string;
  tipoCuenta: string;
  estado: boolean;
  movimientos: ReporteMovimientoDTO[];
}

export interface ReporteMovimientosCliente {
  clienteId: number;
  nombreCliente: string;
  documentoIdentidad: string;
  fechaInicio: string;
  fechaFin: string;
  totalMovimientos: number;
  cuentas: CuentaConMovimientos[];
}

export interface ReporteMovimientosCuenta {
  cuentaId: number;
  numeroCuenta: string;
  tipoCuenta: string;
  nombreCliente: string;
  saldoInicial: number;
  saldoActual: number;
  fechaInicio: string;
  fechaFin: string;
  totalMovimientos: number;
  movimientos: ReporteMovimientoDTO[];
}

export interface CuentaResumen {
  cuentaId: number;
  numeroCuenta: string;
  tipoCuenta: string;
  nombreCliente: string;
  saldoInicial: number;
  saldoActual: number;
  estado: boolean;
  cantidadMovimientos: number;
  fechaCreacion: string;
}

export interface ReporteCuentas {
  fechaInicio: string;
  fechaFin: string;
  totalCuentas: number;
  cuentas: CuentaResumen[];
}

export interface ArchivoResponse {
  contenidoBase64: string;
  nombreArchivo: string;
  tipoMime: string;
  tamanioBytes: number;
}

export interface EnviarReporteEmailRequest {
  emailDestinatario: string;
  formato: 'PDF' | 'EXCEL';
  asunto?: string;
  mensajeAdicional?: string;
}
