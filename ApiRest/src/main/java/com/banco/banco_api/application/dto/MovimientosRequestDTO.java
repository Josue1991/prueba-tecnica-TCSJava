package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import com.banco.banco_api.domain.model.enums.TipoMovimiento;

@Schema(description = "DTO para registrar un nuevo movimiento bancario")
public record MovimientosRequestDTO(

        @Schema(description = "ID de la cuenta donde se realizará el movimiento", example = "1")
        @NotNull(message = "El ID de la cuenta es obligatorio")
        Long cuentaId,

        @Schema(description = "Tipo de movimiento", example = "DEPOSITO", allowableValues = {"RETIRO", "DEPOSITO", "ACTIVAR", "DESACTIVAR"})
        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipoMovimiento,

        @Schema(description = "Valor del movimiento (requerido para RETIRO y DEPOSITO, opcional para ACTIVAR/DESACTIVAR)", example = "100.00")
        BigDecimal valor
) {}
