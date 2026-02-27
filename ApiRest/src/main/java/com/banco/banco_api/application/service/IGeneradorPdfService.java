package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.ReporteMovimientosClienteDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosCuentaDTO;
import com.banco.banco_api.application.dto.ReporteCuentasDTO;

public interface IGeneradorPdfService {
    
    /**
     * Genera PDF de reporte de movimientos por cliente
     * 
     * @param reporte Datos del reporte
     * @return Contenido PDF como array de bytes
     */
    byte[] generarPdfMovimientosCliente(ReporteMovimientosClienteDTO reporte);
    
    /**
     * Genera PDF de reporte de movimientos por cuenta
     * 
     * @param reporte Datos del reporte
     * @return Contenido PDF como array de bytes
     */
    byte[] generarPdfMovimientosCuenta(ReporteMovimientosCuentaDTO reporte);
    
    /**
     * Genera PDF de reporte de cuentas
     * 
     * @param reporte Datos del reporte
     * @return Contenido PDF como array de bytes
     */
    byte[] generarPdfCuentas(ReporteCuentasDTO reporte);
}
