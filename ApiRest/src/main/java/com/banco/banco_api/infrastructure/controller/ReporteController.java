package com.banco.banco_api.infrastructure.controller;

import com.banco.banco_api.application.dto.ArchivoResponseDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosClienteDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosCuentaDTO;
import com.banco.banco_api.application.dto.ReporteCuentasDTO;
import com.banco.banco_api.application.service.IReporteService;
import com.banco.banco_api.application.service.IGeneradorPdfService;
import com.banco.banco_api.application.service.IGeneradorExcelService;
import com.banco.banco_api.application.service.IEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Generación de reportes de movimientos y cuentas en JSON, PDF y Excel con opción de envío por correo")
public class ReporteController {

    private final IReporteService reporteService;
    private final IGeneradorPdfService generadorPdfService;
    private final IGeneradorExcelService generadorExcelService;
    private final IEmailService emailService;

    public ReporteController(IReporteService reporteService,
                            IGeneradorPdfService generadorPdfService,
                            IGeneradorExcelService generadorExcelService,
                            IEmailService emailService) {
        this.reporteService = reporteService;
        this.generadorPdfService = generadorPdfService;
        this.generadorExcelService = generadorExcelService;
        this.emailService = emailService;
    }

    @Operation(
        summary = "Reporte de movimientos por cliente",
        description = "Genera un reporte detallado de todos los movimientos de las cuentas de un cliente en un rango de fechas específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Rango de fechas inválido"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/movimientos/cliente/{clienteId}")
    public ResponseEntity<ReporteMovimientosClienteDTO> obtenerReporteMovimientosPorCliente(
            @Parameter(description = "ID del cliente", example = "1")
            @PathVariable Long clienteId,
            
            @Parameter(description = "Fecha de inicio del período (formato: dd/MM/yyyy)", example = "01/01/2026")
            @RequestParam 
            @DateTimeFormat(pattern = "dd/MM/yyyy") 
            LocalDate fechaInicio,
            
            @Parameter(description = "Fecha de fin del período (formato: dd/MM/yyyy)", example = "31/12/2026")
            @RequestParam 
            @DateTimeFormat(pattern = "dd/MM/yyyy") 
            LocalDate fechaFin) {
        
        ReporteMovimientosClienteDTO reporte = reporteService
            .generarReporteMovimientosPorCliente(clienteId, fechaInicio, fechaFin);
        
        return ResponseEntity.ok(reporte);
    }

    @Operation(
        summary = "Reporte de movimientos por cuenta",
        description = "Genera un reporte detallado de todos los movimientos de una cuenta específica en un rango de fechas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Rango de fechas inválido"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/movimientos/cuenta/{cuentaId}")
    public ResponseEntity<ReporteMovimientosCuentaDTO> obtenerReporteMovimientosPorCuenta(
            @Parameter(description = "ID de la cuenta", example = "1")
            @PathVariable Long cuentaId,
            
            @Parameter(description = "Fecha de inicio del período (formato: dd/MM/yyyy)", example = "01/01/2026")
            @RequestParam 
            @DateTimeFormat(pattern = "dd/MM/yyyy") 
            LocalDate fechaInicio,
            
            @Parameter(description = "Fecha de fin del período (formato: dd/MM/yyyy)", example = "31/12/2026")
            @RequestParam 
            @DateTimeFormat(pattern = "dd/MM/yyyy") 
            LocalDate fechaFin) {
        
        ReporteMovimientosCuentaDTO reporte = reporteService
            .generarReporteMovimientosPorCuenta(cuentaId, fechaInicio, fechaFin);
        
        return ResponseEntity.ok(reporte);
    }

    @Operation(
        summary = "Reporte de cuentas",
        description = "Genera un reporte de todas las cuentas con su actividad en un rango de fechas. " +
                     "Incluye cuentas sin movimientos en el período especificado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Rango de fechas inválido")
    })
    @GetMapping("/cuentas")
    public ResponseEntity<ReporteCuentasDTO> obtenerReporteCuentas(
            @Parameter(description = "Fecha de inicio del período (formato: dd/MM/yyyy)", example = "01/01/2026")
            @RequestParam 
            @DateTimeFormat(pattern = "dd/MM/yyyy") 
            LocalDate fechaInicio,
            
            @Parameter(description = "Fecha de fin del período (formato: dd/MM/yyyy)", example = "31/12/2026")
            @RequestParam 
            @DateTimeFormat(pattern = "dd/MM/yyyy") 
            LocalDate fechaFin) {
        
        ReporteCuentasDTO reporte = reporteService.generarReporteCuentas(fechaInicio, fechaFin);
        
        return ResponseEntity.ok(reporte);
    }
    
    // ===== ENDPOINTS PARA DESCARGA DE ARCHIVOS =====
    
    @Operation(
        summary = "Descargar reporte de movimientos por cliente en PDF",
        description = "Genera y retorna un archivo PDF en base64 con los movimientos del cliente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/movimientos/cliente/{clienteId}/pdf")
    public ResponseEntity<ArchivoResponseDTO> descargarReporteClientePdf(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin) {
        
        ReporteMovimientosClienteDTO reporte = reporteService
            .generarReporteMovimientosPorCliente(clienteId, fechaInicio, fechaFin);
        
        byte[] pdfBytes = generadorPdfService.generarPdfMovimientosCliente(reporte);
        String base64 = Base64.getEncoder().encodeToString(pdfBytes);
        String nombreArchivo = generarNombreArchivo("reporte_cliente_" + clienteId, fechaInicio, fechaFin, "pdf");
        
        return ResponseEntity.ok(new ArchivoResponseDTO(
            base64, nombreArchivo, "application/pdf", pdfBytes.length
        ));
    }
    
    @Operation(
        summary = "Descargar reporte de movimientos por cliente en Excel",
        description = "Genera y retorna un archivo Excel en base64 con los movimientos del cliente"
    )
    @GetMapping("/movimientos/cliente/{clienteId}/excel")
    public ResponseEntity<ArchivoResponseDTO> descargarReporteClienteExcel(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin) {
        
        ReporteMovimientosClienteDTO reporte = reporteService
            .generarReporteMovimientosPorCliente(clienteId, fechaInicio, fechaFin);
        
        byte[] excelBytes = generadorExcelService.generarExcelMovimientosCliente(reporte);
        String base64 = Base64.getEncoder().encodeToString(excelBytes);
        String nombreArchivo = generarNombreArchivo("reporte_cliente_" + clienteId, fechaInicio, fechaFin, "xlsx");
        
        return ResponseEntity.ok(new ArchivoResponseDTO(
            base64, nombreArchivo, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes.length
        ));
    }
    
    @Operation(
        summary = "Descargar reporte de movimientos por cuenta en PDF",
        description = "Genera y retorna un archivo PDF en base64 con los movimientos de la cuenta"
    )
    @GetMapping("/movimientos/cuenta/{cuentaId}/pdf")
    public ResponseEntity<ArchivoResponseDTO> descargarReporteCuentaPdf(
            @PathVariable Long cuentaId,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin) {
        
        ReporteMovimientosCuentaDTO reporte = reporteService
            .generarReporteMovimientosPorCuenta(cuentaId, fechaInicio, fechaFin);
        
        byte[] pdfBytes = generadorPdfService.generarPdfMovimientosCuenta(reporte);
        String base64 = Base64.getEncoder().encodeToString(pdfBytes);
        String nombreArchivo = generarNombreArchivo("reporte_cuenta_" + cuentaId, fechaInicio, fechaFin, "pdf");
        
        return ResponseEntity.ok(new ArchivoResponseDTO(
            base64, nombreArchivo, "application/pdf", pdfBytes.length
        ));
    }
    
    @Operation(
        summary = "Descargar reporte de movimientos por cuenta en Excel",
        description = "Genera y retorna un archivo Excel en base64 con los movimientos de la cuenta"
    )
    @GetMapping("/movimientos/cuenta/{cuentaId}/excel")
    public ResponseEntity<ArchivoResponseDTO> descargarReporteCuentaExcel(
            @PathVariable Long cuentaId,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin) {
        
        ReporteMovimientosCuentaDTO reporte = reporteService
            .generarReporteMovimientosPorCuenta(cuentaId, fechaInicio, fechaFin);
        
        byte[] excelBytes = generadorExcelService.generarExcelMovimientosCuenta(reporte);
        String base64 = Base64.getEncoder().encodeToString(excelBytes);
        String nombreArchivo = generarNombreArchivo("reporte_cuenta_" + cuentaId, fechaInicio, fechaFin, "xlsx");
        
        return ResponseEntity.ok(new ArchivoResponseDTO(
            base64, nombreArchivo, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes.length
        ));
    }
    
    @Operation(
        summary = "Descargar reporte de cuentas en PDF",
        description = "Genera y retorna un archivo PDF en base64 con el reporte de todas las cuentas"
    )
    @GetMapping("/cuentas/pdf")
    public ResponseEntity<ArchivoResponseDTO> descargarReporteCuentasPdf(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin) {
        
        ReporteCuentasDTO reporte = reporteService.generarReporteCuentas(fechaInicio, fechaFin);
        
        byte[] pdfBytes = generadorPdfService.generarPdfCuentas(reporte);
        String base64 = Base64.getEncoder().encodeToString(pdfBytes);
        String nombreArchivo = generarNombreArchivo("reporte_cuentas", fechaInicio, fechaFin, "pdf");
        
        return ResponseEntity.ok(new ArchivoResponseDTO(
            base64, nombreArchivo, "application/pdf", pdfBytes.length
        ));
    }
    
    @Operation(
        summary = "Descargar reporte de cuentas en Excel",
        description = "Genera y retorna un archivo Excel en base64 con el reporte de todas las cuentas"
    )
    @GetMapping("/cuentas/excel")
    public ResponseEntity<ArchivoResponseDTO> descargarReporteCuentasExcel(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin) {
        
        ReporteCuentasDTO reporte = reporteService.generarReporteCuentas(fechaInicio, fechaFin);
        
        byte[] excelBytes = generadorExcelService.generarExcelCuentas(reporte);
        String base64 = Base64.getEncoder().encodeToString(excelBytes);
        String nombreArchivo = generarNombreArchivo("reporte_cuentas", fechaInicio, fechaFin, "xlsx");
        
        return ResponseEntity.ok(new ArchivoResponseDTO(
            base64, nombreArchivo, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes.length
        ));
    }
    
    // ===== ENDPOINTS PARA ENVÍO POR EMAIL =====
    
    @Operation(
        summary = "Enviar reporte de cliente por correo electrónico",
        description = "Genera el reporte en el formato especificado (PDF o Excel) y lo envía por correo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Correo enviado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error al enviar correo o formato inválido")
    })
    @PostMapping("/movimientos/cliente/{clienteId}/enviar")
    public ResponseEntity<String> enviarReporteClientePorEmail(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin,
            @Parameter(description = "Email de destino", example = "usuario@ejemplo.com", required = true)
            @RequestParam String emailDestinatario,
            @Parameter(description = "Formato del reporte", example = "PDF", required = true)
            @RequestParam String formato,
            @Parameter(description = "Asunto personalizado del correo", required = false)
            @RequestParam(required = false) String asunto,
            @Parameter(description = "Mensaje adicional en el cuerpo del correo", required = false)
            @RequestParam(required = false) String mensajeAdicional) {
        
        ReporteMovimientosClienteDTO reporte = reporteService
            .generarReporteMovimientosPorCliente(clienteId, fechaInicio, fechaFin);
        
        byte[] archivo;
        String extension;
        String tipoMime;
        
        if ("PDF".equalsIgnoreCase(formato)) {
            archivo = generadorPdfService.generarPdfMovimientosCliente(reporte);
            extension = "pdf";
            tipoMime = "application/pdf";
        } else if ("EXCEL".equalsIgnoreCase(formato)) {
            archivo = generadorExcelService.generarExcelMovimientosCliente(reporte);
            extension = "xlsx";
            tipoMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            return ResponseEntity.badRequest().body("Formato inválido. Use 'PDF' o 'EXCEL'");
        }
        
        String nombreArchivo = generarNombreArchivo("reporte_cliente_" + clienteId, fechaInicio, fechaFin, extension);
        String asuntoFinal = asunto != null ? asunto : 
            "Reporte de Movimientos - Cliente " + reporte.nombreCliente();
        String cuerpo = generarCuerpoEmail(mensajeAdicional, 
            "reporte de movimientos del cliente " + reporte.nombreCliente());
        
        emailService.enviarCorreoConAdjunto(emailDestinatario, asuntoFinal, cuerpo, 
            archivo, nombreArchivo, tipoMime);
        
        return ResponseEntity.ok("Reporte enviado exitosamente a " + emailDestinatario);
    }
    
    @Operation(
        summary = "Enviar reporte de cuenta por correo electrónico",
        description = "Genera el reporte en el formato especificado (PDF o Excel) y lo envía por correo"
    )
    @PostMapping("/movimientos/cuenta/{cuentaId}/enviar")
    public ResponseEntity<String> enviarReporteCuentaPorEmail(
            @PathVariable Long cuentaId,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin,
            @Parameter(description = "Email de destino", example = "usuario@ejemplo.com", required = true)
            @RequestParam String emailDestinatario,
            @Parameter(description = "Formato del reporte", example = "PDF", required = true)
            @RequestParam String formato,
            @Parameter(description = "Asunto personalizado del correo", required = false)
            @RequestParam(required = false) String asunto,
            @Parameter(description = "Mensaje adicional en el cuerpo del correo", required = false)
            @RequestParam(required = false) String mensajeAdicional) {
        
        ReporteMovimientosCuentaDTO reporte = reporteService
            .generarReporteMovimientosPorCuenta(cuentaId, fechaInicio, fechaFin);
        
        byte[] archivo;
        String extension;
        String tipoMime;
        
        if ("PDF".equalsIgnoreCase(formato)) {
            archivo = generadorPdfService.generarPdfMovimientosCuenta(reporte);
            extension = "pdf";
            tipoMime = "application/pdf";
        } else if ("EXCEL".equalsIgnoreCase(formato)) {
            archivo = generadorExcelService.generarExcelMovimientosCuenta(reporte);
            extension = "xlsx";
            tipoMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            return ResponseEntity.badRequest().body("Formato inválido. Use 'PDF' o 'EXCEL'");
        }
        
        String nombreArchivo = generarNombreArchivo("reporte_cuenta_" + cuentaId, fechaInicio, fechaFin, extension);
        String asuntoFinal = asunto != null ? asunto : 
            "Reporte de Movimientos - Cuenta " + reporte.numeroCuenta();
        String cuerpo = generarCuerpoEmail(mensajeAdicional, 
            "reporte de movimientos de la cuenta " + reporte.numeroCuenta());
        
        emailService.enviarCorreoConAdjunto(emailDestinatario, asuntoFinal, cuerpo, 
            archivo, nombreArchivo, tipoMime);
        
        return ResponseEntity.ok("Reporte enviado exitosamente a " + emailDestinatario);
    }
    
    @Operation(
        summary = "Enviar reporte de cuentas por correo electrónico",
        description = "Genera el reporte en el formato especificado (PDF o Excel) y lo envía por correo"
    )
    @PostMapping("/cuentas/enviar")
    public ResponseEntity<String> enviarReporteCuentasPorEmail(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin,
            @Parameter(description = "Email de destino", example = "usuario@ejemplo.com", required = true)
            @RequestParam String emailDestinatario,
            @Parameter(description = "Formato del reporte", example = "PDF", required = true)
            @RequestParam String formato,
            @Parameter(description = "Asunto personalizado del correo", required = false)
            @RequestParam(required = false) String asunto,
            @Parameter(description = "Mensaje adicional en el cuerpo del correo", required = false)
            @RequestParam(required = false) String mensajeAdicional) {
        
        ReporteCuentasDTO reporte = reporteService.generarReporteCuentas(fechaInicio, fechaFin);
        
        byte[] archivo;
        String extension;
        String tipoMime;
        
        if ("PDF".equalsIgnoreCase(formato)) {
            archivo = generadorPdfService.generarPdfCuentas(reporte);
            extension = "pdf";
            tipoMime = "application/pdf";
        } else if ("EXCEL".equalsIgnoreCase(formato)) {
            archivo = generadorExcelService.generarExcelCuentas(reporte);
            extension = "xlsx";
            tipoMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            return ResponseEntity.badRequest().body("Formato inválido. Use 'PDF' o 'EXCEL'");
        }
        
        String nombreArchivo = generarNombreArchivo("reporte_cuentas", fechaInicio, fechaFin, extension);
        String asuntoFinal = asunto != null ? asunto : 
            "Reporte de Cuentas";
        String cuerpo = generarCuerpoEmail(mensajeAdicional, 
            "reporte general de cuentas");
        
        emailService.enviarCorreoConAdjunto(emailDestinatario, asuntoFinal, cuerpo, 
            archivo, nombreArchivo, tipoMime);
        
        return ResponseEntity.ok("Reporte enviado exitosamente a " + emailDestinatario);
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private String generarNombreArchivo(String prefijo, LocalDate fechaInicio, LocalDate fechaFin, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return String.format("%s_%s_%s.%s", 
            prefijo, 
            fechaInicio.format(formatter), 
            fechaFin.format(formatter),
            extension);
    }
    
    private String generarCuerpoEmail(String mensajeAdicional, String tipoReporte) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Reporte Bancario</h2>");
        html.append("<p>Adjunto encontrará el ").append(tipoReporte).append(" solicitado.</p>");
        
        if (mensajeAdicional != null && !mensajeAdicional.trim().isEmpty()) {
            html.append("<p>").append(mensajeAdicional).append("</p>");
        }
        
        html.append("<br/>");
        html.append("<p><em>Este es un correo automático, por favor no responder.</em></p>");
        html.append("</body></html>");
        
        return html.toString();
    }
}
