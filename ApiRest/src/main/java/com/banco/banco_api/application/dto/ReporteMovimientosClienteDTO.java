package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO de reporte de movimientos por cliente")
public record ReporteMovimientosClienteDTO(
    
    @Schema(description = "ID del cliente")
    Long clienteId,
    
    @Schema(description = "Nombre completo del cliente")
    String nombreCliente,
    
    @Schema(description = "Documento de identidad del cliente")
    String documentoIdentidad,
    
    @Schema(description = "Fecha inicial del reporte")
    LocalDate fechaInicio,
    
    @Schema(description = "Fecha final del reporte")
    LocalDate fechaFin,
    
    @Schema(description = "Total de movimientos encontrados")
    int totalMovimientos,
    
    @Schema(description = "Lista de cuentas con sus movimientos")
    List<CuentaConMovimientos> cuentas
) {
    @Schema(description = "Información de cuenta con sus movimientos")
    public record CuentaConMovimientos(
        @Schema(description = "ID de la cuenta")
        Long cuentaId,
        
        @Schema(description = "Número de cuenta")
        String numeroCuenta,
        
        @Schema(description = "Tipo de cuenta")
        String tipoCuenta,
        
        @Schema(description = "Estado de la cuenta")
        boolean estado,
        
        @Schema(description = "Lista de movimientos de la cuenta")
        List<MovimientosResponseDTO> movimientos
    ) {}
}
