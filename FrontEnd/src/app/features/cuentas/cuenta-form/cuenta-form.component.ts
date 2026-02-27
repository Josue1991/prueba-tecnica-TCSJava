import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CuentaRequest, TipoCuenta } from '../../../core/models/cuenta.model';
import { Cliente } from '../../../core/models/cliente.model';

@Component({
  selector: 'app-cuenta-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cuenta-form.component.html',
  styleUrls: ['./cuenta-form.component.scss']
})
export class CuentaFormComponent implements OnChanges {
  @Input() cuentaData: CuentaRequest | null = null;
  @Input() clientes: Cliente[] = [];
  @Input() modoEdicion = false;
  
  @Output() onFormChange = new EventEmitter<CuentaRequest>();
  @Output() onValidChange = new EventEmitter<boolean>();

  formData: CuentaRequest = {
    numeroCuenta: '',
    tipoCuenta: TipoCuenta.AHORRO,
    clienteId: 0
  };

  tiposCuenta = Object.values(TipoCuenta);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['cuentaData'] && this.cuentaData) {
      this.formData = { ...this.cuentaData };
    } else if (changes['cuentaData'] && !this.cuentaData) {
      this.resetForm();
    }
    this.emitChanges();
  }

  onInputChange(): void {
    this.emitChanges();
  }

  private emitChanges(): void {
    this.onFormChange.emit(this.formData);
    this.onValidChange.emit(this.isFormValid());
  }

  isFormValid(): boolean {
    return !!(
      this.formData.numeroCuenta?.trim() &&
      this.formData.tipoCuenta &&
      this.formData.clienteId > 0
    );
  }

  getFormData(): CuentaRequest {
    return this.formData;
  }

  resetForm(): void {
    this.formData = {
      numeroCuenta: '',
      tipoCuenta: TipoCuenta.AHORRO,
      clienteId: 0
    };
    this.emitChanges();
  }

  getClienteNombre(clienteId: number): string {
    const cliente = this.clientes.find(c => c.id === clienteId);
    return cliente ? `${cliente.nombre}` : '';
  }
}
