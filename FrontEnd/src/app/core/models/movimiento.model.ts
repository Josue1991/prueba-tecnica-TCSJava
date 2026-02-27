export enum TipoMovimiento {
  DEPOSITO = 'DEPOSITO',
  RETIRO = 'RETIRO',
  ACTIVAR = 'ACTIVAR',
  DESACTIVAR = 'DESACTIVAR'
}

export interface Movimiento {
  id?: number;
  tipo: TipoMovimiento;
  valor: number;
  saldoAnterior: number;
  saldoNuevo: number;
  fechaMovimiento: Date;
  cuentaId: number;
  numeroCuenta?: string;
  clienteId?: number;
  nombreCliente?: string;
}

export interface MovimientoDTO {
  id: number;
  tipo: TipoMovimiento;
  valor: number;
  saldoAnterior: number;
  saldoNuevo: number;
  fechaMovimiento: string; // ISO format
}
