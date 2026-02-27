export interface TableColumn {
  key: string;
  label: string;
  type?: 'text' | 'date' | 'currency' | 'badge' | 'boolean';
  format?: string;
  badgeConfig?: {
    trueClass: string;
    falseClass: string;
    trueLabel: string;
    falseLabel: string;
  };
}

export interface TableAction {
  icon?: string;
  label: string;
  class?: string;
  action: 'edit' | 'delete' | 'view' | 'toggle' | 'custom';
  customAction?: string;
}

export interface TableConfig {
  columns: TableColumn[];
  actions?: TableAction[];
  showCreateButton?: boolean;
  createButtonLabel?: string;
  title?: string;
  searchable?: boolean;           // Si muestra el campo de búsqueda
  searchFields?: string[];        // Campos por los que se puede buscar (si está vacío, busca en todos)
  searchPlaceholder?: string;     // Placeholder del campo de búsqueda
  pageable?: boolean;             // Si muestra paginación
  pageSize?: number;              // Tamaño de página por defecto
  pageSizeOptions?: number[];     // Opciones de tamaño de página
}

export interface TableActionEvent<T = any> {
  action: string;
  data: T;
}
