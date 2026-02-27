import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteRequest } from '../../../core/models/cliente.model';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cliente-form.component.html',
  styleUrls: ['./cliente-form.component.scss']
})
export class ClienteFormComponent implements OnChanges {
  @Input() clienteData: ClienteRequest | null = null;
  @Input() modoEdicion = false;
  
  @Output() onFormChange = new EventEmitter<ClienteRequest>();
  @Output() onValidChange = new EventEmitter<boolean>();

  formData: ClienteRequest = {
    nombre: '',
    genero: '',
    edad: 0,
    identificacion: '',
    direccion: '',
    telefono: '',
    password: ''
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['clienteData'] && this.clienteData) {
      this.formData = { ...this.clienteData };
    } else if (changes['clienteData'] && !this.clienteData) {
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
      this.formData.nombre?.trim() &&
      this.formData.genero?.trim() &&
      this.formData.identificacion?.trim() &&
      this.formData.direccion?.trim() &&
      this.formData.edad > 0 &&
      this.formData.password?.trim() &&
      this.formData.password.length >= 6
    );
  }

  isValidDireccion(direccion: string): boolean {
    return direccion.trim().length > 0;
  }

  getFormData(): ClienteRequest {
    return this.formData;
  }

  resetForm(): void {
    this.formData = {
      nombre: '',
      genero: '',
      edad: 0,
      identificacion: '',
      direccion: '',
      telefono: '',
      password: ''
    };
    this.emitChanges();
  }
}
