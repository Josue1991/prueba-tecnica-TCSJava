package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "DTO para solicitar reportes con rango de fechas")
public record ReporteFechasRequestDTO(
    
    @Schema(description = "Fecha inicial del rango (inclusive)", example = "2026-01-01")
    @NotNull(message = "La fecha inicial es obligatoria")
    LocalDate fechaInicio,
    
    @Schema(description = "Fecha final del rango (inclusive)", example = "2026-12-31")
    @NotNull(message = "La fecha final es obligatoria")
    LocalDate fechaFin
) {}
