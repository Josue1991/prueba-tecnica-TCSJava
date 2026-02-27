package com.banco.banco_api.application.service.impl;

import com.banco.banco_api.application.dto.MovimientosResponseDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosClienteDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosCuentaDTO;
import com.banco.banco_api.application.dto.ReporteCuentasDTO;
import com.banco.banco_api.application.service.IGeneradorExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class GeneradorExcelServiceImpl implements IGeneradorExcelService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generarExcelMovimientosCliente(ReporteMovimientosClienteDTO reporte) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Hoja de resumen
            Sheet resumenSheet = workbook.createSheet("Resumen");
            int rowNum = 0;
            
            // Estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle titleStyle = crearEstiloTitulo(workbook);
            
            // Título
            Row titleRow = resumenSheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE MOVIMIENTOS POR CLIENTE");
            titleCell.setCellStyle(titleStyle);
            
            rowNum++;
            
            // Información del cliente
            crearFilaDatos(resumenSheet, rowNum++, "Cliente:", reporte.nombreCliente());
            crearFilaDatos(resumenSheet, rowNum++, "Documento:", reporte.documentoIdentidad());
            crearFilaDatos(resumenSheet, rowNum++, "Período:", 
                reporte.fechaInicio().format(DATE_FORMATTER) + " - " + reporte.fechaFin().format(DATE_FORMATTER));
            crearFilaDatos(resumenSheet, rowNum++, "Total Movimientos:", String.valueOf(reporte.totalMovimientos()));
            
            rowNum++;
            
            // Crear una hoja por cada cuenta
            for (ReporteMovimientosClienteDTO.CuentaConMovimientos cuenta : reporte.cuentas()) {
                Sheet cuentaSheet = workbook.createSheet("Cuenta " + cuenta.numeroCuenta());
                int cuentaRowNum = 0;
                
                // Información de la cuenta
                Row cuentaTitleRow = cuentaSheet.createRow(cuentaRowNum++);
                Cell cuentaTitleCell = cuentaTitleRow.createCell(0);
                cuentaTitleCell.setCellValue("Cuenta: " + cuenta.numeroCuenta() + " - " + cuenta.tipoCuenta());
                cuentaTitleCell.setCellStyle(titleStyle);
                
                cuentaRowNum++;
                crearFilaDatos(cuentaSheet, cuentaRowNum++, "Estado:", cuenta.estado() ? "Activa" : "Inactiva");
                cuentaRowNum++;
                
                // Encabezados de movimientos
                Row headerRow = cuentaSheet.createRow(cuentaRowNum++);
                String[] headers = {"Fecha", "Tipo", "Valor", "Saldo", "ID Movimiento"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
                
                // Datos de movimientos
                for (MovimientosResponseDTO mov : cuenta.movimientos()) {
                    Row dataRow = cuentaSheet.createRow(cuentaRowNum++);
                    dataRow.createCell(0).setCellValue(mov.fechaMovimiento().format(DATETIME_FORMATTER));
                    dataRow.createCell(1).setCellValue(mov.tipoMovimiento());
                    dataRow.createCell(2).setCellValue(mov.valor().doubleValue());
                    dataRow.createCell(3).setCellValue(mov.saldo().doubleValue());
                    dataRow.createCell(4).setCellValue(mov.id());
                }
                
                // Ajustar ancho de columnas
                for (int i = 0; i < headers.length; i++) {
                    cuentaSheet.autoSizeColumn(i);
                }
            }
            
            // Ajustar ancho de columnas del resumen
            resumenSheet.autoSizeColumn(0);
            resumenSheet.autoSizeColumn(1);
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generarExcelMovimientosCuenta(ReporteMovimientosCuentaDTO reporte) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Movimientos");
            int rowNum = 0;
            
            // Estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle titleStyle = crearEstiloTitulo(workbook);
            
            // Título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE MOVIMIENTOS POR CUENTA");
            titleCell.setCellStyle(titleStyle);
            
            rowNum++;
            
            // Información de la cuenta
            crearFilaDatos(sheet, rowNum++, "Número de Cuenta:", reporte.numeroCuenta());
            crearFilaDatos(sheet, rowNum++, "Tipo de Cuenta:", reporte.tipoCuenta());
            crearFilaDatos(sheet, rowNum++, "Cliente:", reporte.nombreCliente());
            crearFilaDatos(sheet, rowNum++, "Saldo Inicial:", "$" + reporte.saldoInicial());
            crearFilaDatos(sheet, rowNum++, "Saldo Actual:", "$" + reporte.saldoActual());
            crearFilaDatos(sheet, rowNum++, "Período:", 
                reporte.fechaInicio().format(DATE_FORMATTER) + " - " + reporte.fechaFin().format(DATE_FORMATTER));
            crearFilaDatos(sheet, rowNum++, "Total Movimientos:", String.valueOf(reporte.totalMovimientos()));
            
            rowNum++;
            
            // Encabezados de movimientos
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Fecha", "Tipo", "Valor", "Saldo", "ID"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos de movimientos
            for (MovimientosResponseDTO mov : reporte.movimientos()) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(mov.fechaMovimiento().format(DATETIME_FORMATTER));
                dataRow.createCell(1).setCellValue(mov.tipoMovimiento());
                dataRow.createCell(2).setCellValue(mov.valor().doubleValue());
                dataRow.createCell(3).setCellValue(mov.saldo().doubleValue());
                dataRow.createCell(4).setCellValue(mov.id());
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generarExcelCuentas(ReporteCuentasDTO reporte) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Cuentas");
            int rowNum = 0;
            
            // Estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle titleStyle = crearEstiloTitulo(workbook);
            
            // Título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE CUENTAS");
            titleCell.setCellStyle(titleStyle);
            
            rowNum++;
            
            // Información general
            crearFilaDatos(sheet, rowNum++, "Período:", 
                reporte.fechaInicio().format(DATE_FORMATTER) + " - " + reporte.fechaFin().format(DATE_FORMATTER));
            crearFilaDatos(sheet, rowNum++, "Total de Cuentas:", String.valueOf(reporte.totalCuentas()));
            
            rowNum++;
            
            // Encabezados
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"N° Cuenta", "Tipo", "Cliente", "Saldo Inicial", "Saldo Actual", "Estado", "Movimientos"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos de cuentas
            for (ReporteCuentasDTO.CuentaResumen cuenta : reporte.cuentas()) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(cuenta.numeroCuenta());
                dataRow.createCell(1).setCellValue(cuenta.tipoCuenta());
                dataRow.createCell(2).setCellValue(cuenta.nombreCliente());
                dataRow.createCell(3).setCellValue(cuenta.saldoInicial().doubleValue());
                dataRow.createCell(4).setCellValue(cuenta.saldoActual().doubleValue());
                dataRow.createCell(5).setCellValue(cuenta.estado() ? "Activa" : "Inactiva");
                dataRow.createCell(6).setCellValue(cuenta.cantidadMovimientos());
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }
    
    // Métodos auxiliares
    private void crearFilaDatos(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
    
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }
}
