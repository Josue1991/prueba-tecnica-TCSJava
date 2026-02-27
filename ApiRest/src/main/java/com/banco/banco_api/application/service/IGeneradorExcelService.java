package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.ReporteMovimientosClienteDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosCuentaDTO;
import com.banco.banco_api.application.dto.ReporteCuentasDTO;

public interface IGeneradorExcelService {
    
    /**
     * Genera Excel de reporte de movimientos por cliente
     * 
     * @param reporte Datos del reporte
     * @return Contenido Excel como array de bytes
     */
    byte[] generarExcelMovimientosCliente(ReporteMovimientosClienteDTO reporte);
    
    /**
     * Genera Excel de reporte de movimientos por cuenta
     * 
     * @param reporte Datos del reporte
     * @return Contenido Excel como array de bytes
     */
    byte[] generarExcelMovimientosCuenta(ReporteMovimientosCuentaDTO reporte);
    
    /**
     * Genera Excel de reporte de cuentas
     * 
     * @param reporte Datos del reporte
     * @return Contenido Excel como array de bytes
     */
    byte[] generarExcelCuentas(ReporteCuentasDTO reporte);
}
