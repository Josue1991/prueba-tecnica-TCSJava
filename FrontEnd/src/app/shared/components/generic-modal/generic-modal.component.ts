import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-generic-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './generic-modal.component.html',
  styleUrls: ['./generic-modal.component.scss']
})
export class GenericModalComponent {
  @Input() isOpen: boolean = false;
  @Input() title: string = '';
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() showFooter: boolean = true;
  @Input() saveButtonLabel: string = 'Guardar';
  @Input() saveButtonIcon: string = 'fa-solid fa-save';
  @Input() cancelButtonLabel: string = 'Cancelar';
  @Input() cancelButtonIcon: string = 'fa-solid fa-times';
  @Input() disableSave: boolean = false;
  @Input() loading: boolean = false;

  @Output() onSave = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();
  @Output() onClose = new EventEmitter<void>();

  handleSave(): void {
    if (!this.disableSave && !this.loading) {
      this.onSave.emit();
    }
  }

  handleCancel(): void {
    if (!this.loading) {
      this.onCancel.emit();
      this.close();
    }
  }

  handleOverlayClick(event: MouseEvent): void {
    if (event.target === event.currentTarget && !this.loading) {
      this.close();
    }
  }

  close(): void {
    this.isOpen = false;
    this.onClose.emit();
  }
}
