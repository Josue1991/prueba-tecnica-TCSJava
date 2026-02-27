package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO de respuesta con información del movimiento registrado")
public record MovimientosResponseDTO(
        @Schema(description = "ID único del movimiento", example = "1")
        Long id,
        
        @Schema(description = "ID de la cuenta asociada", example = "1")
        Long cuentaId,
        
        @Schema(description = "Número de la cuenta", example = "123456")
        String numeroCuenta,
        
        @Schema(description = "Nombre del cliente", example = "Juan Pérez")
        String nombreCliente,
        
        @Schema(description = "Tipo de movimiento realizado", example = "DEPOSITO", allowableValues = {"RETIRO", "DEPOSITO", "ACTIVAR", "DESACTIVAR"})
        String tipo,
        
        @Schema(description = "Valor del movimiento (negativo para débitos)", example = "100.00")
        BigDecimal valor,
        
        @Schema(description = "Saldo anterior de la cuenta", example = "1000.00")
        BigDecimal saldoAnterior,
        
        @Schema(description = "Saldo de la cuenta después del movimiento", example = "1100.00")
        BigDecimal saldoNuevo,
        
        @Schema(description = "Fecha y hora del movimiento", example = "2026-02-26T13:15:00")
        LocalDateTime fechaMovimiento,
        
        @Schema(description = "Fecha y hora de creación del registro", example = "2026-02-26T13:15:00")
        LocalDateTime createdAt
    ) {}
