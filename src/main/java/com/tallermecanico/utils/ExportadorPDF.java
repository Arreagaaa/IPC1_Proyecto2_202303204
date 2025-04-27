package com.tallermecanico.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Clase para exportar reportes a PDF
 */
public class ExportadorPDF {

    public static void exportarReporte(String nombreArchivo, String titulo, JTable tabla, JFreeChart grafico)
            throws Exception {
        Document documento = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();

            // Agregar título
            Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph parrafoTitulo = new Paragraph(titulo, fontTitulo);
            parrafoTitulo.setAlignment(Element.ALIGN_CENTER);
            parrafoTitulo.setSpacingAfter(20);
            documento.add(parrafoTitulo);

            // Agregar fecha
            Font fontFecha = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Paragraph parrafoFecha = new Paragraph("Fecha de generación: " + new Date().toString(), fontFecha);
            parrafoFecha.setAlignment(Element.ALIGN_RIGHT);
            parrafoFecha.setSpacingAfter(20);
            documento.add(parrafoFecha);

            // Agregar gráfico
            if (grafico != null) {
                try {
                    File tempFile = File.createTempFile("chart", ".png");
                    ChartUtils.saveChartAsPNG(tempFile, grafico, 500, 300);
                    Image imagen = Image.getInstance(tempFile.getAbsolutePath());
                    imagen.setAlignment(Element.ALIGN_CENTER);
                    imagen.scaleToFit(500, 300);
                    documento.add(imagen);
                    tempFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            documento.add(Chunk.NEWLINE);
            documento.add(Chunk.NEWLINE);

            // Agregar tabla
            if (tabla != null) {
                PdfPTable pdfTable = new PdfPTable(tabla.getColumnCount());
                pdfTable.setWidthPercentage(100);

                // Añadir encabezados
                for (int i = 0; i < tabla.getColumnCount(); i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(tabla.getColumnName(i)));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(5);
                    pdfTable.addCell(cell);
                }

                // Añadir datos
                for (int i = 0; i < tabla.getRowCount(); i++) {
                    for (int j = 0; j < tabla.getColumnCount(); j++) {
                        String valor = "";
                        if (tabla.getValueAt(i, j) != null) {
                            valor = tabla.getValueAt(i, j).toString();
                        }
                        PdfPCell cell = new PdfPCell(new Phrase(valor));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5);
                        pdfTable.addCell(cell);
                    }
                }

                documento.add(pdfTable);
            }

            // Agregar pie de página
            Paragraph piePagina = new Paragraph("Taller Mecánico - Sistema de Gestión");
            piePagina.setAlignment(Element.ALIGN_CENTER);
            piePagina.setSpacingBefore(20);
            documento.add(piePagina);

        } catch (Exception e) {
            throw e;
        } finally {
            if (documento != null && documento.isOpen()) {
                documento.close();
            }
        }
    }
}