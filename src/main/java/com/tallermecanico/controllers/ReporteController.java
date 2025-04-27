package com.tallermecanico.controllers;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Controlador para generar reportes en formato PDF
 */
public class ReporteController {

    /**
     * Genera un reporte PDF del TOP 10 de repuestos más caros
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteRepuestosMasCaros(String rutaArchivo) throws IOException {
        validarCarpetaReportes(); // Validar la carpeta antes de generar el reporte

        PdfWriter writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("TOP 10 Repuestos Más Caros").setBold().setFontSize(16));

        // Obtener datos
        Vector<Repuesto> repuestos = DataController.getRepuestos();
        for (int i = 0; i < repuestos.size() - 1; i++) {
            for (int j = 0; j < repuestos.size() - i - 1; j++) {
                if (repuestos.get(j).getPrecio() < repuestos.get(j + 1).getPrecio()) {
                    Repuesto temp = repuestos.get(j);
                    repuestos.set(j, repuestos.get(j + 1));
                    repuestos.set(j + 1, temp);
                }
            }
        }

        if (repuestos.size() > 10) {
            repuestos.setSize(10); // Limitar a los 10 más caros
        }

        // Crear tabla
        Table table = new Table(3);
        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Precio");

        for (Repuesto repuesto : repuestos) {
            table.addCell(String.valueOf(repuesto.getId()));
            table.addCell(repuesto.getNombre());
            table.addCell(String.format("Q %.2f", repuesto.getPrecio()));
        }

        document.add(table);
        document.close();
    }

    /**
     * Genera un reporte PDF de distribución de clientes por tipo
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteClientesPorTipo(String rutaArchivo) throws IOException {
        validarCarpetaReportes(); // Validar que la carpeta "reportes" exista

        PdfWriter writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Reporte de Clientes por Tipo").setBold().setFontSize(16));

        // Contar clientes por tipo
        int clientesNormales = 0;
        int clientesOro = 0;

        Vector<Cliente> clientes = DataController.getClientes();
        for (Cliente cliente : clientes) {
            if ("normal".equals(cliente.getTipoCliente())) {
                clientesNormales++;
            } else if ("oro".equals(cliente.getTipoCliente())) {
                clientesOro++;
            }
        }

        // Crear tabla con datos
        Table table = new Table(2);
        table.addCell("Tipo de Cliente");
        table.addCell("Cantidad");
        table.addCell("Normal");
        table.addCell(String.valueOf(clientesNormales));
        table.addCell("Oro");
        table.addCell(String.valueOf(clientesOro));
        document.add(table);

        // Agregar gráfico de pastel
        agregarGraficoPastel(document, clientesNormales, clientesOro);

        document.close();
    }

    /**
     * Genera un reporte PDF del TOP 10 de repuestos más usados
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteRepuestosMasUsados(String rutaArchivo) throws IOException {
        validarCarpetaReportes(); // Validar que la carpeta "reportes" exista

        PdfWriter writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("TOP 10 Repuestos Más Usados").setBold().setFontSize(16));

        Map<Integer, Integer> frecuenciaUso = new HashMap<>();
        Map<Integer, Repuesto> mapRepuestos = new HashMap<>();

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            Servicio servicio = orden.getServicio();
            if (servicio != null) {
                for (Repuesto repuesto : servicio.getRepuestos()) {
                    int idRepuesto = repuesto.getId();
                    mapRepuestos.put(idRepuesto, repuesto);
                    frecuenciaUso.put(idRepuesto, frecuenciaUso.getOrDefault(idRepuesto, 0) + 1);
                }
            }
        }

        Vector<Map.Entry<Repuesto, Integer>> resultado = new Vector<>();
        for (Map.Entry<Integer, Integer> entry : frecuenciaUso.entrySet()) {
            Repuesto repuesto = mapRepuestos.get(entry.getKey());
            resultado.add(new AbstractMap.SimpleEntry<>(repuesto, entry.getValue()));
        }

        // Ordenar usando el método de burbuja
        for (int i = 0; i < resultado.size() - 1; i++) {
            for (int j = 0; j < resultado.size() - i - 1; j++) {
                if (resultado.get(j).getValue() < resultado.get(j + 1).getValue()) {
                    Map.Entry<Repuesto, Integer> temp = resultado.get(j);
                    resultado.set(j, resultado.get(j + 1));
                    resultado.set(j + 1, temp);
                }
            }
        }

        if (resultado.size() > 10) {
            resultado.setSize(10); // Limitar a los 10 más usados
        }

        Table table = new Table(3);
        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Frecuencia de Uso");

        for (Map.Entry<Repuesto, Integer> entry : resultado) {
            Repuesto repuesto = entry.getKey();
            table.addCell(String.valueOf(repuesto.getId()));
            table.addCell(repuesto.getNombre());
            table.addCell(String.valueOf(entry.getValue()));
        }

        document.add(table);

        // Convertir los datos a un Vector para el gráfico de barras
        Vector<Map.Entry<String, Integer>> datosGrafico = new Vector<>();
        for (Map.Entry<Repuesto, Integer> entry : resultado) {
            datosGrafico.add(new AbstractMap.SimpleEntry<>(entry.getKey().getNombre(), entry.getValue()));
        }

        // Agregar gráfico de barras
        agregarGraficoBarras(document, datosGrafico, "Frecuencia de Uso de Repuestos");

        document.close();
    }

    /**
     * Genera un reporte PDF del TOP 10 de servicios más usados
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteServiciosMasUsados(String rutaArchivo) throws IOException {
        File carpetaReportes = new File("reportes");
        if (!carpetaReportes.exists()) {
            carpetaReportes.mkdir();
        }

        PdfWriter writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("TOP 10 Servicios Más Usados").setBold().setFontSize(16));

        Map<Integer, Integer> frecuenciaUso = new HashMap<>();
        Map<Integer, Servicio> mapServicios = new HashMap<>();

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            Servicio servicio = orden.getServicio();
            if (servicio != null) {
                int idServicio = servicio.getId();
                mapServicios.put(idServicio, servicio);
                frecuenciaUso.put(idServicio, frecuenciaUso.getOrDefault(idServicio, 0) + 1);
            }
        }

        Vector<Map.Entry<Servicio, Integer>> resultado = new Vector<>();
        for (Map.Entry<Integer, Integer> entry : frecuenciaUso.entrySet()) {
            Servicio servicio = mapServicios.get(entry.getKey());
            resultado.add(new AbstractMap.SimpleEntry<>(servicio, entry.getValue()));
        }

        // Ordenar usando burbuja
        for (int i = 0; i < resultado.size() - 1; i++) {
            for (int j = 0; j < resultado.size() - i - 1; j++) {
                if (resultado.get(j).getValue() < resultado.get(j + 1).getValue()) {
                    Map.Entry<Servicio, Integer> temp = resultado.get(j);
                    resultado.set(j, resultado.get(j + 1));
                    resultado.set(j + 1, temp);
                }
            }
        }

        if (resultado.size() > 10) {
            resultado.setSize(10); // Limitar a los 10 más usados
        }

        Table table = new Table(3);
        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Frecuencia de Uso");

        for (Map.Entry<Servicio, Integer> entry : resultado) {
            Servicio servicio = entry.getKey();
            table.addCell(String.valueOf(servicio.getId()));
            table.addCell(servicio.getNombre());
            table.addCell(String.valueOf(entry.getValue()));
        }

        document.add(table);
        document.close();
    }

    /**
     * Genera un reporte PDF del TOP 5 de automóviles más repetidos
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteAutomovilesMasRepetidos(String rutaArchivo) throws IOException {
        validarCarpetaReportes(); // Validar que la carpeta "reportes" exista

        PdfWriter writer = new PdfWriter(rutaArchivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("TOP 5 Automóviles Más Repetidos").setBold().setFontSize(16));

        // Contar la frecuencia de los modelos
        Map<String, Integer> frecuenciaModelos = new HashMap<>();
        for (Cliente cliente : DataController.getClientes()) {
            for (Automovil auto : cliente.getAutomoviles()) {
                String modeloKey = auto.getMarca() + " " + auto.getModelo();
                frecuenciaModelos.put(modeloKey, frecuenciaModelos.getOrDefault(modeloKey, 0) + 1);
            }
        }

        // Convertir el mapa a un Vector
        Vector<Map.Entry<String, Integer>> resultado = new Vector<>();
        for (Map.Entry<String, Integer> entry : frecuenciaModelos.entrySet()) {
            resultado.add(entry);
        }

        // Ordenar usando el método de burbuja
        for (int i = 0; i < resultado.size() - 1; i++) {
            for (int j = 0; j < resultado.size() - i - 1; j++) {
                if (resultado.get(j).getValue() < resultado.get(j + 1).getValue()) {
                    Map.Entry<String, Integer> temp = resultado.get(j);
                    resultado.set(j, resultado.get(j + 1));
                    resultado.set(j + 1, temp);
                }
            }
        }

        // Limitar a los 5 más repetidos
        if (resultado.size() > 5) {
            resultado.setSize(5);
        }

        // Crear tabla
        Table table = new Table(2);
        table.addCell("Modelo");
        table.addCell("Cantidad");

        for (Map.Entry<String, Integer> entry : resultado) {
            table.addCell(entry.getKey());
            table.addCell(String.valueOf(entry.getValue()));
        }

        document.add(table);
        document.close();
    }

    private static void agregarGraficoBarras(Document document, Vector<Map.Entry<String, Integer>> datos, String titulo)
            throws IOException {
        PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().addNewPage());
        Rectangle rect = new Rectangle(50, 500, 500, 200); // Posición y tamaño del gráfico
        canvas.rectangle(rect);
        canvas.stroke();

        // Dibujar título
        canvas.beginText();
        canvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD), 12);
        canvas.moveText(50, 720);
        canvas.showText(titulo);
        canvas.endText();

        int maxValor = 1;
        for (Map.Entry<String, Integer> entry : datos) {
            if (entry.getValue() > maxValor) {
                maxValor = entry.getValue();
            }
        }

        int barWidth = 40;
        int x = 60;

        for (Map.Entry<String, Integer> entry : datos) {
            int barHeight = (int) ((entry.getValue() / (double) maxValor) * 150);
            canvas.rectangle(x, 500, barWidth, barHeight);
            canvas.fill();
            x += barWidth + 10;

            // Etiquetas de barras
            canvas.beginText();
            canvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA), 10);
            canvas.moveText(x - barWidth - 10, 490);
            canvas.showText(entry.getKey());
            canvas.endText();
        }
    }

    private static void agregarGraficoPastel(Document document, int clientesNormales, int clientesOro)
            throws IOException {
        // Crear una imagen para el gráfico
        int width = 500;
        int height = 300;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fondo blanco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Dibujar el gráfico de pastel
        int totalClientes = clientesNormales + clientesOro;
        int anguloNormales = (int) Math.round((clientesNormales / (double) totalClientes) * 360);
        int anguloOro = 360 - anguloNormales;

        g2d.setColor(Color.BLUE);
        g2d.fillArc(50, 50, 200, 200, 0, anguloNormales);

        g2d.setColor(Color.YELLOW);
        g2d.fillArc(50, 50, 200, 200, anguloNormales, anguloOro);

        // Etiquetas
        g2d.setColor(Color.BLACK);
        g2d.drawString("Clientes Normales: " + clientesNormales, 300, 100);
        g2d.drawString("Clientes Oro: " + clientesOro, 300, 130);

        g2d.dispose();

        // Agregar la imagen al PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        Image img = new Image(ImageDataFactory.create(baos.toByteArray()));
        img.setWidth(500);
        img.setHeight(300);
        document.add(img);
    }

    private static void validarCarpetaReportes() {
        File carpetaReportes = new File("reportes");
        if (!carpetaReportes.exists()) {
            carpetaReportes.mkdir();
        }
    }
}
