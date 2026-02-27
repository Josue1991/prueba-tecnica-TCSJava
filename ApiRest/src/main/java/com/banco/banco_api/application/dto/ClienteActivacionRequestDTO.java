package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "DTO para activar cliente con cuentas seleccionadas")
public record ClienteActivacionRequestDTO(
    
    @Schema(description = "Lista de IDs de cuentas a activar", example = "[1, 2]")
    @NotEmpty(message = "Debe seleccionar al menos una cuenta para activar")
    List<Long> cuentasIds
) {}
