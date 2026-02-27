package com.banco.banco_api.application.service.impl;

import com.banco.banco_api.application.dto.MovimientosResponseDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosClienteDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosCuentaDTO;
import com.banco.banco_api.application.dto.ReporteCuentasDTO;
import com.banco.banco_api.application.service.IGeneradorPdfService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class GeneradorPdfServiceImpl implements IGeneradorPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    @SuppressWarnings("resource")
    public byte[] generarPdfMovimientosCliente(ReporteMovimientosClienteDTO reporte) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título del reporte
            document.add(new Paragraph("REPORTE DE MOVIMIENTOS POR CLIENTE")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

            // Información del cliente
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Cliente: " + reporte.nombreCliente()).setFontSize(12));
            document.add(new Paragraph("Documento: " + reporte.documentoIdentidad()).setFontSize(12));
            document.add(new Paragraph("Período: " + reporte.fechaInicio().format(DATE_FORMATTER) + 
                " - " + reporte.fechaFin().format(DATE_FORMATTER)).setFontSize(12));
            document.add(new Paragraph("Total de Movimientos: " + reporte.totalMovimientos()).setFontSize(12));
            document.add(new Paragraph("\n"));

            // Detalles por cuenta
            for (ReporteMovimientosClienteDTO.CuentaConMovimientos cuenta : reporte.cuentas()) {
                document.add(new Paragraph("CUENTA: " + cuenta.numeroCuenta() + " - " + 
                    cuenta.tipoCuenta()).setBold().setFontSize(14));
                document.add(new Paragraph("Estado: " + (cuenta.estado() ? "Activa" : "Inactiva"))
                    .setFontSize(10));
                document.add(new Paragraph("\n"));

                // Tabla de movimientos
                if (!cuenta.movimientos().isEmpty()) {
                    Table table = new Table(UnitValue.createPercentArray(new float[]{15, 15, 20, 20, 30}));
                    table.setWidth(UnitValue.createPercentValue(100));

                    // Encabezados
                    table.addHeaderCell(new Cell().add(new Paragraph("Fecha").setBold()));
                    table.addHeaderCell(new Cell().add(new Paragraph("Tipo").setBold()));
                    table.addHeaderCell(new Cell().add(new Paragraph("Valor").setBold()));
                    table.addHeaderCell(new Cell().add(new Paragraph("Saldo").setBold()));
                    table.addHeaderCell(new Cell().add(new Paragraph("ID Movimiento").setBold()));

                    // Filas de datos
                    for (MovimientosResponseDTO mov : cuenta.movimientos()) {
                        table.addCell(mov.fechaMovimiento().format(DATETIME_FORMATTER));
                        table.addCell(mov.tipoMovimiento());
                        table.addCell("$" + mov.valor());
                        table.addCell("$" + mov.saldo());
                        table.addCell(String.valueOf(mov.id()));
                    }

                    document.add(table);
                } else {
                    document.add(new Paragraph("Sin movimientos en este período").setItalic());
                }
                
                document.add(new Paragraph("\n\n"));
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("resource")
    public byte[] generarPdfMovimientosCuenta(ReporteMovimientosCuentaDTO reporte) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título del reporte
            document.add(new Paragraph("REPORTE DE MOVIMIENTOS POR CUENTA")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

            // Información de la cuenta
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Número de Cuenta: " + reporte.numeroCuenta()).setFontSize(12));
            document.add(new Paragraph("Tipo de Cuenta: " + reporte.tipoCuenta()).setFontSize(12));
            document.add(new Paragraph("Cliente: " + reporte.nombreCliente()).setFontSize(12));
            document.add(new Paragraph("Saldo Inicial: $" + reporte.saldoInicial()).setFontSize(12));
            document.add(new Paragraph("Saldo Actual: $" + reporte.saldoActual()).setFontSize(12));
            document.add(new Paragraph("Período: " + reporte.fechaInicio().format(DATE_FORMATTER) + 
                " - " + reporte.fechaFin().format(DATE_FORMATTER)).setFontSize(12));
            document.add(new Paragraph("Total de Movimientos: " + reporte.totalMovimientos()).setFontSize(12));
            document.add(new Paragraph("\n"));

            // Tabla de movimientos
            if (!reporte.movimientos().isEmpty()) {
                Table table = new Table(UnitValue.createPercentArray(new float[]{20, 15, 20, 20, 25}));
                table.setWidth(UnitValue.createPercentValue(100));

                // Encabezados
                table.addHeaderCell(new Cell().add(new Paragraph("Fecha").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Tipo").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Valor").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Saldo").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()));

                // Filas de datos
                for (MovimientosResponseDTO mov : reporte.movimientos()) {
                    table.addCell(mov.fechaMovimiento().format(DATETIME_FORMATTER));
                    table.addCell(mov.tipoMovimiento());
                    table.addCell("$" + mov.valor());
                    table.addCell("$" + mov.saldo());
                    table.addCell(String.valueOf(mov.id()));
                }

                document.add(table);
            } else {
                document.add(new Paragraph("Sin movimientos en este período").setItalic());
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("resource")
    public byte[] generarPdfCuentas(ReporteCuentasDTO reporte) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título del reporte
            document.add(new Paragraph("REPORTE DE CUENTAS")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

            // Información general
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Período: " + reporte.fechaInicio().format(DATE_FORMATTER) + 
                " - " + reporte.fechaFin().format(DATE_FORMATTER)).setFontSize(12));
            document.add(new Paragraph("Total de Cuentas: " + reporte.totalCuentas()).setFontSize(12));
            document.add(new Paragraph("\n"));

            // Tabla de cuentas
            if (!reporte.cuentas().isEmpty()) {
                Table table = new Table(UnitValue.createPercentArray(
                    new float[]{15, 12, 18, 15, 15, 10, 15}));
                table.setWidth(UnitValue.createPercentValue(100));

                // Encabezados
                table.addHeaderCell(new Cell().add(new Paragraph("N° Cuenta").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Tipo").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Cliente").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Saldo Inicial").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Saldo Actual").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Estado").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Movimientos").setBold()));

                // Filas de datos
                for (ReporteCuentasDTO.CuentaResumen cuenta : reporte.cuentas()) {
                    table.addCell(cuenta.numeroCuenta());
                    table.addCell(cuenta.tipoCuenta());
                    table.addCell(cuenta.nombreCliente());
                    table.addCell("$" + cuenta.saldoInicial());
                    table.addCell("$" + cuenta.saldoActual());
                    table.addCell(cuenta.estado() ? "Activa" : "Inactiva");
                    table.addCell(String.valueOf(cuenta.cantidadMovimientos()));
                }

                document.add(table);
            } else {
                document.add(new Paragraph("Sin cuentas registradas").setItalic());
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }
}
