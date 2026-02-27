export enum TipoCuenta {
  AHORRO = 'AHORRO',
  CORRIENTE = 'CORRIENTE',
  NOMINA = 'NOMINA'
}

export interface Cuenta {
  id?: number;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial?: number;
  saldoActual: number;
  clienteId: number;
  nombreCliente?: string;
  estado?: boolean;
  createdAt?: Date;
}

export interface CuentaRequest {
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  clienteId: number;
}
