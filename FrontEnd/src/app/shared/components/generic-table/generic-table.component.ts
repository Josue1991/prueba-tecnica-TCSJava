import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableConfig, TableActionEvent } from '../../models/table-config.model';

@Component({
  selector: 'app-generic-table',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './generic-table.component.html',
  styleUrls: ['./generic-table.component.scss']
})
export class GenericTableComponent<T = any> implements OnInit, OnChanges {
  @Input() config!: TableConfig;
  @Input() data: T[] = [];
  @Input() loading: boolean = false;
  @Input() error: string | null = null;
  @Input() noDataMessage: string = 'No hay datos registrados';

  @Output() onCreate = new EventEmitter<void>();
  @Output() onAction = new EventEmitter<TableActionEvent<T>>();

  // Control de búsqueda
  searchTerm: string = '';

  // Control de paginación
  currentPage: number = 1;
  pageSize: number = 10;
  totalPages: number = 1;

  // Datos procesados
  filteredData: T[] = [];
  displayedData: T[] = [];

  handleCreate(): void {
    this.onCreate.emit();
  }

  handleAction(action: string, item: T): void {
    this.onAction.emit({ action, data: item });
  }

  getValue(item: any, key: string): any {
    return key.split('.').reduce((obj, k) => obj?.[k], item);
  }

  formatValue(value: any, column: any): string {
    if (value === null || value === undefined) {
      return 'N/A';
    }

    switch (column.type) {
      case 'date':
        return new Date(value).toLocaleDateString();
      case 'currency':
        return new Intl.NumberFormat('en-US', {
          style: 'currency',
          currency: column.format || 'USD'
        }).format(value);
      case 'boolean':
        return value ? 'Sí' : 'No';
      default:
        return value.toString();
    }
  }

  getBadgeClass(value: any, column: any): string {
    if (column.type === 'badge' && column.badgeConfig) {
      return value ? column.badgeConfig.trueClass : column.badgeConfig.falseClass;
    }
    return '';
  }

  getBadgeLabel(value: any, column: any): string {
    if (column.type === 'badge' && column.badgeConfig) {
      return value ? column.badgeConfig.trueLabel : column.badgeConfig.falseLabel;
    }
    return value.toString();
  }

  ngOnInit(): void {
    this.pageSize = this.config.pageSize || 10;
    this.processData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] || changes['config']) {
      this.currentPage = 1;
      this.processData();
    }
  }

  processData(): void {
    // 1. Filtrar por búsqueda
    this.filteredData = this.filterData(this.data);

    // 2. Calcular paginación
    if (this.config.pageable) {
      this.totalPages = Math.ceil(this.filteredData.length / this.pageSize);
      if (this.currentPage > this.totalPages && this.totalPages > 0) {
        this.currentPage = 1;
      }
      this.displayedData = this.paginateData(this.filteredData);
    } else {
      this.displayedData = this.filteredData;
    }
  }

  filterData(data: T[]): T[] {
    if (!this.config.searchable || !this.searchTerm || this.searchTerm.trim() === '') {
      return data;
    }

    const term = this.searchTerm.toLowerCase().trim();
    const fieldsToSearch = this.config.searchFields && this.config.searchFields.length > 0
      ? this.config.searchFields
      : this.config.columns.map(col => col.key);

    return data.filter(item => {
      return fieldsToSearch.some(field => {
        const value = this.getValue(item, field);
        return value != null && value.toString().toLowerCase().includes(term);
      });
    });
  }

  paginateData(data: T[]): T[] {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    return data.slice(start, end);
  }

  onSearch(term: string): void {
    this.searchTerm = term;
    this.currentPage = 1;
    this.processData();
  }

  onPageChange(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.processData();
    }
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 1;
    this.processData();
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    
    if (this.totalPages <= maxVisible) {
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (this.currentPage <= 3) {
        for (let i = 1; i <= 4; i++) {
          pages.push(i);
        }
        pages.push(-1); // Ellipsis
        pages.push(this.totalPages);
      } else if (this.currentPage >= this.totalPages - 2) {
        pages.push(1);
        pages.push(-1); // Ellipsis
        for (let i = this.totalPages - 3; i <= this.totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(1);
        pages.push(-1); // Ellipsis
        pages.push(this.currentPage - 1);
        pages.push(this.currentPage);
        pages.push(this.currentPage + 1);
        pages.push(-1); // Ellipsis
        pages.push(this.totalPages);
      }
    }
    
    return pages;
  }

  getDisplayRange(): string {
    if (this.filteredData.length === 0) {
      return '0 de 0';
    }
    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(this.currentPage * this.pageSize, this.filteredData.length);
    return `${start}-${end} de ${this.filteredData.length}`;
  }
}
