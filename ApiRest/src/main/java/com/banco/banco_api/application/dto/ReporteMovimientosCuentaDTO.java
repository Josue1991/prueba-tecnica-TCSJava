package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO de reporte de movimientos por cuenta")
public record ReporteMovimientosCuentaDTO(
    
    @Schema(description = "ID de la cuenta")
    Long cuentaId,
    
    @Schema(description = "Número de cuenta")
    String numeroCuenta,
    
    @Schema(description = "Tipo de cuenta")
    String tipoCuenta,
    
    @Schema(description = "Nombre del cliente titular")
    String nombreCliente,
    
    @Schema(description = "Saldo inicial al inicio del período")
    BigDecimal saldoInicial,
    
    @Schema(description = "Saldo actual de la cuenta")
    BigDecimal saldoActual,
    
    @Schema(description = "Fecha inicial del reporte")
    LocalDate fechaInicio,
    
    @Schema(description = "Fecha final del reporte")
    LocalDate fechaFin,
    
    @Schema(description = "Total de movimientos encontrados")
    int totalMovimientos,
    
    @Schema(description = "Lista de movimientos de la cuenta")
    List<MovimientosResponseDTO> movimientos
) {}
