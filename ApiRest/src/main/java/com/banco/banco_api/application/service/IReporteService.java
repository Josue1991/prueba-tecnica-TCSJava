package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.ReporteMovimientosClienteDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosCuentaDTO;
import com.banco.banco_api.application.dto.ReporteCuentasDTO;

import java.time.LocalDate;

public interface IReporteService {
    
    /**
     * Genera reporte de movimientos de todas las cuentas de un cliente en un rango de fechas
     * 
     * @param clienteId ID del cliente
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Reporte con movimientos agrupados por cuenta
     */
    ReporteMovimientosClienteDTO generarReporteMovimientosPorCliente(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Genera reporte de movimientos de una cuenta específica en un rango de fechas
     * 
     * @param cuentaId ID de la cuenta
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Reporte con movimientos de la cuenta
     */
    ReporteMovimientosCuentaDTO generarReporteMovimientosPorCuenta(Long cuentaId, LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Genera reporte de cuentas con actividad en un rango de fechas
     * 
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Reporte con resumen de cuentas con actividad
     */
    ReporteCuentasDTO generarReporteCuentas(LocalDate fechaInicio, LocalDate fechaFin);
}
