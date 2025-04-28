package com.tallermecanico.utils;

import com.tallermecanico.controllers.RepuestoController;
import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.Factura;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;

/**
 * Clase utilitaria para generar reportes en PDF
 */
public class GeneradorPDF {

    private static final String RUTA_REPORTES = "reportes/";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    // Fuentes para el PDF
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
    private static final Font TEXTO_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font ENCABEZADO_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

    /**
     * Genera el reporte de clientes por tipo (Oro y Normal)
     * 
     * @param clientesOro      Vector con los clientes de tipo oro
     * @param clientesNormales Vector con los clientes de tipo normal
     */
    public static void generarReporteClientesPorTipo(Vector<Cliente> clientesOro, Vector<Cliente> clientesNormales) {
        try {
            // Crear directorio si no existe
            crearDirectorioSiNoExiste();

            // Crear nombre de archivo con timestamp
            String timestamp = dateFormat.format(new Date());
            String rutaArchivo = RUTA_REPORTES + "ReporteClientes_" + timestamp + ".pdf";

            // Crear documento
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            // Agregar título principal
            Paragraph titulo = new Paragraph("Reporte de Clientes por Tipo", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            // Fecha de generación
            Paragraph fechaGeneracion = new Paragraph("Fecha de generación: " + new Date(), TEXTO_NORMAL);
            document.add(fechaGeneracion);
            document.add(Chunk.NEWLINE);

            // Resumen
            document.add(new Paragraph("Resumen:", SUBTITULO));
            document.add(
                    new Paragraph("Clientes Oro: " + (clientesOro != null ? clientesOro.size() : 0), TEXTO_NORMAL));
            document.add(new Paragraph("Clientes Normales: " + (clientesNormales != null ? clientesNormales.size() : 0),
                    TEXTO_NORMAL));
            document.add(new Paragraph("Total de clientes: " +
                    ((clientesOro != null ? clientesOro.size() : 0) +
                            (clientesNormales != null ? clientesNormales.size() : 0)),
                    TEXTO_NORMAL));
            document.add(Chunk.NEWLINE);

            // Tabla de clientes oro
            if (clientesOro != null && !clientesOro.isEmpty()) {
                document.add(new Paragraph("Clientes tipo ORO:", SUBTITULO));
                PdfPTable tablaOro = new PdfPTable(4);
                tablaOro.setWidthPercentage(100);
                tablaOro.setSpacingBefore(10f);

                // Encabezados
                agregarCeldaEncabezado(tablaOro, "Identificador", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tablaOro, "Nombre Completo", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tablaOro, "Usuario", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tablaOro, "Automóviles", new BaseColor(0, 102, 204));

                // Datos
                for (Cliente cliente : clientesOro) {
                    tablaOro.addCell(cliente.getIdentificador());
                    tablaOro.addCell(cliente.getNombreCompleto());
                    tablaOro.addCell(cliente.getNombreUsuario());
                    tablaOro.addCell(
                            String.valueOf(cliente.getAutomoviles() != null ? cliente.getAutomoviles().size() : 0));
                }

                document.add(tablaOro);
                document.add(Chunk.NEWLINE);
            }

            // Tabla de clientes normales
            if (clientesNormales != null && !clientesNormales.isEmpty()) {
                document.add(new Paragraph("Clientes tipo Normal:", SUBTITULO));
                PdfPTable tablaNormal = new PdfPTable(4);
                tablaNormal.setWidthPercentage(100);
                tablaNormal.setSpacingBefore(10f);

                // Encabezados
                agregarCeldaEncabezado(tablaNormal, "Identificador", new BaseColor(100, 100, 100));
                agregarCeldaEncabezado(tablaNormal, "Nombre Completo", new BaseColor(100, 100, 100));
                agregarCeldaEncabezado(tablaNormal, "Usuario", new BaseColor(100, 100, 100));
                agregarCeldaEncabezado(tablaNormal, "Automóviles", new BaseColor(100, 100, 100));

                // Datos
                for (Cliente cliente : clientesNormales) {
                    tablaNormal.addCell(cliente.getIdentificador());
                    tablaNormal.addCell(cliente.getNombreCompleto());
                    tablaNormal.addCell(cliente.getNombreUsuario());
                    tablaNormal.addCell(
                            String.valueOf(cliente.getAutomoviles() != null ? cliente.getAutomoviles().size() : 0));
                }

                document.add(tablaNormal);
            }

            // Pie de página
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Taller Mecánico - Sistema de Gestión", TEXTO_NORMAL));

            document.close();

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de clientes por tipo generado: " + rutaArchivo);
        } catch (DocumentException e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar documento PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera el reporte de los repuestos más utilizados
     * 
     * @param repuestos Vector con los repuestos más usados
     */
    public static void generarReporteRepuestosMasUsados(Vector<Repuesto> repuestos) {
        try {
            // Crear directorio si no existe
            crearDirectorioSiNoExiste();

            // Crear nombre de archivo con timestamp
            String timestamp = dateFormat.format(new Date());
            String rutaArchivo = RUTA_REPORTES + "ReporteRepuestosMasUsados_" + timestamp + ".pdf";

            // Crear documento
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            // Agregar título principal
            Paragraph titulo = new Paragraph("TOP 10 Repuestos Más Usados", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            // Fecha de generación
            Paragraph fechaGeneracion = new Paragraph("Fecha de generación: " + new Date(), TEXTO_NORMAL);
            document.add(fechaGeneracion);
            document.add(Chunk.NEWLINE);

            // Verificar si hay datos
            if (repuestos == null || repuestos.isEmpty()) {
                document.add(new Paragraph("No hay datos de repuestos para mostrar.", TEXTO_NORMAL));
            } else {
                // Tabla de repuestos
                PdfPTable tabla = new PdfPTable(7);
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(10f);

                // Encabezados
                agregarCeldaEncabezado(tabla, "ID", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Nombre", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Marca", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Modelo", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Existencias", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Precio", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Veces Usado", new BaseColor(0, 102, 204));

                // Datos
                for (Repuesto r : repuestos) {
                    tabla.addCell(String.valueOf(r.getId()));
                    tabla.addCell(r.getNombre());
                    tabla.addCell(r.getMarca());
                    tabla.addCell(r.getModelo());
                    tabla.addCell(String.valueOf(r.getExistencias()));
                    tabla.addCell(String.format("Q%.2f", r.getPrecio()));
                    tabla.addCell(String.valueOf(r.getVecesUsado()));
                }

                document.add(tabla);
            }

            // Pie de página
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Taller Mecánico - Sistema de Gestión", TEXTO_NORMAL));

            document.close();

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de repuestos más usados generado: " + rutaArchivo);
        } catch (DocumentException e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar documento PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de repuestos más usados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera el reporte de los repuestos más caros
     * 
     * @param repuestos Vector con los repuestos más caros
     */
    public static void generarReporteRepuestosMasCaros(Vector<Repuesto> repuestos) {
        try {
            // Crear directorio si no existe
            crearDirectorioSiNoExiste();

            // Crear nombre de archivo con timestamp
            String timestamp = dateFormat.format(new Date());
            String rutaArchivo = RUTA_REPORTES + "ReporteRepuestosMasCaros_" + timestamp + ".pdf";

            // Crear documento
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            // Agregar título principal
            Paragraph titulo = new Paragraph("TOP 10 Repuestos Más Caros", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            // Fecha de generación
            Paragraph fechaGeneracion = new Paragraph("Fecha de generación: " + new Date(), TEXTO_NORMAL);
            document.add(fechaGeneracion);
            document.add(Chunk.NEWLINE);

            // Verificar si hay datos
            if (repuestos == null || repuestos.isEmpty()) {
                document.add(new Paragraph("No hay datos de repuestos para mostrar.", TEXTO_NORMAL));
            } else {
                // Tabla de repuestos
                PdfPTable tabla = new PdfPTable(6);
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(10f);

                // Encabezados
                agregarCeldaEncabezado(tabla, "ID", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Nombre", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Marca", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Modelo", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Existencias", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Precio", new BaseColor(0, 102, 204));

                // Datos
                for (Repuesto r : repuestos) {
                    tabla.addCell(String.valueOf(r.getId()));
                    tabla.addCell(r.getNombre());
                    tabla.addCell(r.getMarca());
                    tabla.addCell(r.getModelo());
                    tabla.addCell(String.valueOf(r.getExistencias()));
                    tabla.addCell(String.format("Q%.2f", r.getPrecio()));
                }

                document.add(tabla);
            }

            // Pie de página
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Taller Mecánico - Sistema de Gestión", TEXTO_NORMAL));

            document.close();

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de repuestos más caros generado: " + rutaArchivo);
        } catch (DocumentException e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar documento PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de repuestos más caros: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera el reporte de los servicios más usados
     * 
     * @param servicios Vector con los servicios más usados
     */
    public static void generarReporteServiciosMasUsados(Vector<Servicio> servicios) {
        try {
            // Crear directorio si no existe
            crearDirectorioSiNoExiste();

            // Crear nombre de archivo con timestamp
            String timestamp = dateFormat.format(new Date());
            String rutaArchivo = RUTA_REPORTES + "ReporteServiciosMasUsados_" + timestamp + ".pdf";

            // Crear documento
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            // Agregar título principal
            Paragraph titulo = new Paragraph("TOP 10 Servicios Más Usados", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            // Fecha de generación
            Paragraph fechaGeneracion = new Paragraph("Fecha de generación: " + new Date(), TEXTO_NORMAL);
            document.add(fechaGeneracion);
            document.add(Chunk.NEWLINE);

            // Verificar si hay datos
            if (servicios == null || servicios.isEmpty()) {
                document.add(new Paragraph("No hay datos de servicios para mostrar.", TEXTO_NORMAL));
            } else {
                // Tabla de servicios
                PdfPTable tabla = new PdfPTable(7);
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(10f);

                // Encabezados
                agregarCeldaEncabezado(tabla, "ID", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Nombre", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Marca", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Modelo", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Precio MO", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Precio Total", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Veces Usado", new BaseColor(0, 102, 204));

                // Datos
                for (Servicio s : servicios) {
                    tabla.addCell(String.valueOf(s.getId()));
                    tabla.addCell(s.getNombre());
                    tabla.addCell(s.getMarca());
                    tabla.addCell(s.getModelo());
                    tabla.addCell(String.format("Q%.2f", s.getPrecioManoObra()));
                    tabla.addCell(String.format("Q%.2f", s.getPrecioTotal()));
                    tabla.addCell(String.valueOf(s.getVecesUsado()));
                }

                document.add(tabla);
            }

            // Pie de página
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Taller Mecánico - Sistema de Gestión", TEXTO_NORMAL));

            document.close();

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de servicios más usados generado: " + rutaArchivo);
        } catch (DocumentException e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar documento PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de servicios más usados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera el reporte de los automóviles más repetidos entre clientes
     * 
     * @param top5              Vector con los 5 modelos más repetidos y su cantidad
     * @param ejemplares        Mapa con ejemplares de cada modelo
     * @param clientesPorModelo Mapa con clientes que tienen cada modelo
     */
    public static void generarReporteAutomovilesMasRepetidos(
            Vector<Map.Entry<String, Integer>> top5,
            Map<String, Automovil> ejemplares,
            Map<String, Vector<Cliente>> clientesPorModelo) {

        try {
            // Crear directorio si no existe
            crearDirectorioSiNoExiste();

            // Crear nombre de archivo con timestamp
            String timestamp = dateFormat.format(new Date());
            String rutaArchivo = RUTA_REPORTES + "ReporteAutomovilesMasRepetidos_" + timestamp + ".pdf";

            // Crear documento
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            // Agregar título principal
            Paragraph titulo = new Paragraph("Los 5 Automóviles Más Repetidos", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            // Fecha de generación
            Paragraph fechaGeneracion = new Paragraph("Fecha de generación: " + new Date(), TEXTO_NORMAL);
            document.add(fechaGeneracion);
            document.add(Chunk.NEWLINE);

            // Verificar si hay datos
            if (top5 == null || top5.isEmpty()) {
                document.add(new Paragraph("No hay datos de automóviles para mostrar.", TEXTO_NORMAL));
            } else {
                // Tabla de automóviles
                PdfPTable tabla = new PdfPTable(4);
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(10f);

                // Encabezados
                agregarCeldaEncabezado(tabla, "Modelo", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Placa (ejemplo)", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Cliente", new BaseColor(0, 102, 204));
                agregarCeldaEncabezado(tabla, "Cantidad", new BaseColor(0, 102, 204));

                // Datos
                for (Map.Entry<String, Integer> entry : top5) {
                    String modelo = entry.getKey();
                    int cantidad = entry.getValue();

                    // Validaciones para evitar NullPointerException
                    if (ejemplares != null && ejemplares.containsKey(modelo) &&
                            clientesPorModelo != null && clientesPorModelo.containsKey(modelo) &&
                            !clientesPorModelo.get(modelo).isEmpty()) {

                        Automovil ejemplar = ejemplares.get(modelo);
                        Cliente primerCliente = clientesPorModelo.get(modelo).get(0);

                        tabla.addCell(modelo);
                        tabla.addCell(ejemplar != null ? ejemplar.getPlaca() : "N/A");
                        tabla.addCell(primerCliente != null ? primerCliente.getNombreCompleto() : "N/A");
                        tabla.addCell(String.valueOf(cantidad));
                    } else {
                        tabla.addCell(modelo);
                        tabla.addCell("N/A");
                        tabla.addCell("N/A");
                        tabla.addCell(String.valueOf(cantidad));
                    }
                }

                document.add(tabla);

                // Si hay más de un automóvil, mostrar detalle comparativo
                if (top5.size() >= 2) {
                    document.add(Chunk.NEWLINE);
                    document.add(new Paragraph("Comparativa de los dos modelos más repetidos:", SUBTITULO));
                    document.add(Chunk.NEWLINE);

                    String modelo1 = top5.get(0).getKey();
                    String modelo2 = top5.get(1).getKey();
                    int cantidad1 = top5.get(0).getValue();
                    int cantidad2 = top5.get(1).getValue();

                    Paragraph comparacion = new Paragraph();
                    comparacion.add(
                            new Chunk("El modelo " + modelo1 + " aparece " + cantidad1 + " veces.\n", TEXTO_NORMAL));
                    comparacion.add(
                            new Chunk("El modelo " + modelo2 + " aparece " + cantidad2 + " veces.\n", TEXTO_NORMAL));

                    if (cantidad2 > 0) { // Evitar división por cero
                        double porcentaje = ((double) (cantidad1 - cantidad2) / cantidad2) * 100;
                        comparacion.add(new Chunk("El modelo más común supera al segundo por un " +
                                String.format("%.1f", porcentaje) + "%.", TEXTO_NORMAL));
                    }

                    document.add(comparacion);
                }
            }

            // Pie de página
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Taller Mecánico - Sistema de Gestión", TEXTO_NORMAL));

            document.close();

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de automóviles más repetidos generado: " + rutaArchivo);
        } catch (DocumentException e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar documento PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de automóviles más repetidos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera un PDF de una factura
     * 
     * @param factura La factura a generar en PDF
     */
    public static void generarFacturaPDF(Factura factura) {
        try {
            // Crear directorio si no existe
            crearDirectorioSiNoExiste();

            // Crear nombre de archivo con timestamp
            String timestamp = dateFormat.format(new Date());
            String rutaArchivo = RUTA_REPORTES + "Factura_" + factura.getId() + "_" + timestamp + ".pdf";

            // Crear documento
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            // Agregar título principal
            Paragraph titulo = new Paragraph("TALLER MECÁNICO USAC", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph subTitulo = new Paragraph("FACTURA DE SERVICIO", SUBTITULO);
            subTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitulo);
            document.add(Chunk.NEWLINE);

            // Información de la factura
            Paragraph infoFactura = new Paragraph();
            infoFactura.add(
                    new Chunk("Factura No.: " + factura.getId(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            infoFactura.add(Chunk.TABBING);
            infoFactura.add(new Chunk("Fecha: " + dateFormat.format(new Date()), TEXTO_NORMAL));
            document.add(infoFactura);
            document.add(Chunk.NEWLINE);

            // Información del cliente
            document.add(new Paragraph("DATOS DEL CLIENTE", SUBTITULO));
            PdfPTable tablaCliente = new PdfPTable(2);
            tablaCliente.setWidthPercentage(100);

            agregarCeldaEncabezado(tablaCliente, "Cliente", new BaseColor(66, 139, 202));
            agregarCeldaEncabezado(tablaCliente, "Información", new BaseColor(66, 139, 202));

            tablaCliente.addCell("Nombre");
            tablaCliente.addCell(factura.getCliente().getNombreCompleto());

            tablaCliente.addCell("ID");
            tablaCliente.addCell(factura.getCliente().getIdentificador());

            tablaCliente.addCell("Tipo");
            if (factura.getCliente() instanceof Cliente) {
                Cliente cliente = (Cliente) factura.getCliente();
                tablaCliente.addCell("Cliente " + (cliente.getTipoCliente().equals("oro") ? "Oro" : "Normal"));
            } else {
                tablaCliente.addCell("Cliente Normal");
            }

            document.add(tablaCliente);
            document.add(Chunk.NEWLINE);

            // Información del vehículo
            document.add(new Paragraph("INFORMACIÓN DEL VEHÍCULO", SUBTITULO));
            PdfPTable tablaVehiculo = new PdfPTable(2);
            tablaVehiculo.setWidthPercentage(100);

            agregarCeldaEncabezado(tablaVehiculo, "Detalle", new BaseColor(66, 139, 202));
            agregarCeldaEncabezado(tablaVehiculo, "Valor", new BaseColor(66, 139, 202));

            tablaVehiculo.addCell("Placa");
            tablaVehiculo.addCell(factura.getAutomovil().getPlaca());

            tablaVehiculo.addCell("Marca");
            tablaVehiculo.addCell(factura.getAutomovil().getMarca());

            tablaVehiculo.addCell("Modelo");
            tablaVehiculo.addCell(factura.getAutomovil().getModelo());

            document.add(tablaVehiculo);
            document.add(Chunk.NEWLINE);

            // Detalles del servicio
            document.add(new Paragraph("DETALLES DEL SERVICIO", SUBTITULO));
            PdfPTable tablaServicio = new PdfPTable(4);
            tablaServicio.setWidthPercentage(100);

            agregarCeldaEncabezado(tablaServicio, "Servicio", new BaseColor(66, 139, 202));
            agregarCeldaEncabezado(tablaServicio, "Descripción", new BaseColor(66, 139, 202));
            agregarCeldaEncabezado(tablaServicio, "Precio", new BaseColor(66, 139, 202));
            agregarCeldaEncabezado(tablaServicio, "Subtotal", new BaseColor(66, 139, 202));

            // Servicio principal
            tablaServicio.addCell("Mano de obra");
            tablaServicio.addCell(factura.getServicioAsociado().getNombre());
            tablaServicio.addCell(String.format("Q%.2f", factura.getServicioAsociado().getPrecioManoObra()));
            tablaServicio.addCell(String.format("Q%.2f", factura.getServicioAsociado().getPrecioManoObra()));

            // Repuestos usados
            double totalRepuestos = 0;
            for (Repuesto repuesto : factura.getServicioAsociado().getRepuestos()) {
                if (repuesto != null) {
                    tablaServicio.addCell("Repuesto");
                    tablaServicio.addCell(repuesto.getNombre());
                    tablaServicio.addCell(String.format("Q%.2f", repuesto.getPrecio()));
                    tablaServicio.addCell(String.format("Q%.2f", repuesto.getPrecio()));
                    totalRepuestos += repuesto.getPrecio();
                }
            }

            document.add(tablaServicio);
            document.add(Chunk.NEWLINE);

            // Totales
            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(50);
            tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPCell celdaSubtotal = new PdfPCell(
                    new Phrase("SUBTOTAL:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            celdaSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaSubtotal.setBorderWidth(0);
            tablaTotales.addCell(celdaSubtotal);

            tablaTotales.addCell(String.format("Q%.2f", factura.calcularSubtotal()));

            PdfPCell celdaIVA = new PdfPCell(
                    new Phrase("IVA (12%):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            celdaIVA.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaIVA.setBorderWidth(0);
            tablaTotales.addCell(celdaIVA);

            tablaTotales.addCell(String.format("Q%.2f", factura.getIva()));

            PdfPCell celdaTotal = new PdfPCell(
                    new Phrase("TOTAL A PAGAR:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            celdaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaTotal.setBorderWidth(0);
            tablaTotales.addCell(celdaTotal);

            PdfPCell celdaValorTotal = new PdfPCell(new Phrase(String.format("Q%.2f", factura.getTotal()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            celdaValorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaTotales.addCell(celdaValorTotal);

            document.add(tablaTotales);
            document.add(Chunk.NEWLINE);

            // Estado de la factura
            Paragraph estadoFactura = new Paragraph("Estado: " + factura.getEstado(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            estadoFactura.setAlignment(Element.ALIGN_CENTER);
            document.add(estadoFactura);

            // Pie de página
            document.add(Chunk.NEWLINE);
            Paragraph piePagina = new Paragraph("¡Gracias por su preferencia!",
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            piePagina.setAlignment(Element.ALIGN_CENTER);
            document.add(piePagina);

            document.close();

            // Abrir el archivo PDF automáticamente
            abrirArchivoPDF(rutaArchivo);

            GestorBitacora.registrarEvento("Sistema", "Generación de Factura", true,
                    "Factura PDF generada: " + rutaArchivo);

        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de Factura", false,
                    "Error al generar factura PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Abre un archivo PDF con el visor predeterminado
     * 
     * @param rutaArchivo Ruta del archivo a abrir
     */
    private static void abrirArchivoPDF(String rutaArchivo) {
        try {
            File file = new File(rutaArchivo);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "El PDF se guardó en: " + rutaArchivo + "\nPero no se pudo abrir automáticamente.",
                            "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "El PDF se guardó correctamente, pero no se pudo abrir: " + e.getMessage(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Crea el directorio para reportes si no existe
     */
    private static void crearDirectorioSiNoExiste() {
        File directorioReportes = new File(RUTA_REPORTES);
        if (!directorioReportes.exists()) {
            directorioReportes.mkdirs();
        }
    }

    /**
     * Agrega una celda de encabezado a una tabla
     * 
     * @param tabla      Tabla donde se agregará la celda
     * @param texto      Texto de la celda
     * @param colorFondo Color de fondo de la celda
     */
    private static void agregarCeldaEncabezado(PdfPTable tabla, String texto, BaseColor colorFondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, ENCABEZADO_TABLA));
        celda.setBackgroundColor(colorFondo);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(5);
        tabla.addCell(celda);
    }
}
