import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface MenuItem {
  icon: string;
  label: string;
  route: string;
  expanded?: boolean;
  submenu?: MenuItem[];
}

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  isCollapsed = false;
  
  menuItems: MenuItem[] = [
    { icon: 'fa-solid fa-house', label: 'Inicio', route: '/' },
    { icon: 'fa-solid fa-users', label: 'Clientes', route: '/clientes' },
    { icon: 'fa-solid fa-credit-card', label: 'Cuentas', route: '/cuentas' },
    { icon: 'fa-solid fa-money-bill-transfer', label: 'Movimientos', route: '/movimientos' },
    { icon: 'fa-solid fa-chart-line', label: 'Reportes', route: '/reportes' }
  ];

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  toggleSubmenu(item: MenuItem): void {
    if (item.submenu) {
      item.expanded = !item.expanded;
    }
  }
}
