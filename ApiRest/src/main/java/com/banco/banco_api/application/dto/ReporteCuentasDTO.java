package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "DTO de reporte de cuentas con actividad en un período")
public record ReporteCuentasDTO(
    
    @Schema(description = "Fecha inicial del reporte")
    LocalDate fechaInicio,
    
    @Schema(description = "Fecha final del reporte")
    LocalDate fechaFin,
    
    @Schema(description = "Total de cuentas con actividad")
    int totalCuentas,
    
    @Schema(description = "Lista de cuentas con información resumida")
    List<CuentaResumen> cuentas
) {
    @Schema(description = "Resumen de cuenta con estadísticas")
    public record CuentaResumen(
        @Schema(description = "ID de la cuenta")
        Long cuentaId,
        
        @Schema(description = "Número de cuenta")
        String numeroCuenta,
        
        @Schema(description = "Tipo de cuenta")
        String tipoCuenta,
        
        @Schema(description = "Nombre del cliente titular")
        String nombreCliente,
        
        @Schema(description = "Saldo inicial de la cuenta")
        BigDecimal saldoInicial,
        
        @Schema(description = "Saldo actual de la cuenta")
        BigDecimal saldoActual,
        
        @Schema(description = "Estado de la cuenta")
        boolean estado,
        
        @Schema(description = "Cantidad de movimientos en el período")
        int cantidadMovimientos,
        
        @Schema(description = "Fecha de creación de la cuenta")
        LocalDateTime fechaCreacion
    ) {}
}
