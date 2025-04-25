package com.example.utils;

import com.example.models.Factura;
import com.example.models.OrdenTrabajo;
import com.example.models.Repuesto;
import com.example.models.Servicio;
import com.example.models.personas.Cliente;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Generador de archivos PDF para facturas
 */
public class PdfGenerator {

    /**
     * Genera un archivo PDF para una factura
     * 
     * @param factura     La factura a convertir en PDF
     * @param rutaDestino La ruta donde se guardará el PDF
     * @return La ruta del archivo generado
     * @throws Exception Si ocurre un error al generar el PDF
     */
    public static String generarFacturaPDF(Factura factura, String rutaDestino) throws Exception {
        // Usaremos una implementación simple sin dependencias externas
        // En un entorno real, sería mejor usar una biblioteca como iText para esto

        // Crear contenido del PDF (texto simple)
        StringBuilder contenido = new StringBuilder();

        // Formatear la factura en texto
        contenido.append("======= TALLER MECÁNICO USAC =======\n\n");

        // Datos de la factura
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String numFactura = String.format("%06d", factura.getNumero());

        contenido.append("FACTURA No. ").append(numFactura).append("\n");
        contenido.append("Fecha: ").append(sdf.format(factura.getFecha())).append("\n\n");

        // Datos del cliente
        OrdenTrabajo orden = factura.getOrdenTrabajo();
        Cliente cliente = orden.getCliente();

        contenido.append("CLIENTE\n");
        contenido.append("Nombre: ").append(cliente.getNombreCompleto()).append("\n");
        contenido.append("DPI: ").append(cliente.getIdentificador()).append("\n");
        contenido.append("Tipo: ").append(cliente.getTipoCliente().toUpperCase()).append("\n\n");

        // Datos del vehículo
        contenido.append("VEHÍCULO\n");
        contenido.append("Placa: ").append(orden.getAutomovil().getPlaca()).append("\n");
        contenido.append("Marca: ").append(orden.getAutomovil().getMarca()).append("\n");
        contenido.append("Modelo: ").append(orden.getAutomovil().getModelo()).append("\n\n");

        // Datos del servicio
        Servicio servicio = orden.getServicio();
        contenido.append("SERVICIO REALIZADO\n");
        contenido.append("Nombre: ").append(servicio.getNombre()).append("\n");
        contenido.append("Precio Mano de Obra: Q").append(String.format("%.2f", servicio.getPrecioManoObra()))
                .append("\n\n");

        // Repuestos utilizados
        Vector<Repuesto> repuestos = servicio.getRepuestos();
        if (!repuestos.isEmpty()) {
            contenido.append("REPUESTOS UTILIZADOS\n");
            double totalRepuestos = 0.0;

            for (int i = 0; i < repuestos.size(); i++) {
                Repuesto repuesto = repuestos.elementAt(i);
                contenido.append("- ").append(repuesto.getNombre())
                        .append(": Q").append(String.format("%.2f", repuesto.getPrecio())).append("\n");
                totalRepuestos += repuesto.getPrecio();
            }

            contenido.append("\nTotal Repuestos: Q").append(String.format("%.2f", totalRepuestos)).append("\n");
        }

        // Totales
        contenido.append("\nSUBTOTAL: Q").append(String.format("%.2f", factura.getSubtotal())).append("\n");
        contenido.append("IVA (12%): Q").append(String.format("%.2f", factura.getIva())).append("\n");
        contenido.append("TOTAL A PAGAR: Q").append(String.format("%.2f", factura.getTotal())).append("\n\n");

        // Estado
        contenido.append("ESTADO: ").append(factura.isPagada() ? "PAGADA" : "PENDIENTE").append("\n");

        if (factura.isPagada() && factura.getFechaPago() != null) {
            contenido.append("Fecha de Pago: ").append(sdf.format(factura.getFechaPago())).append("\n");
        }

        // Notas al pie
        contenido.append("\n======================================\n");
        contenido.append("Gracias por su preferencia\n");
        contenido.append("Para consultas: taller.usac@universidad.edu.gt\n");

        // Escribir el archivo
        try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
            fos.write(contenido.toString().getBytes());
        }

        return rutaDestino;
    }

    /**
     * Genera un PDF para un reporte administrativo
     * 
     * @param tipoReporte Tipo de reporte a generar
     * @param datos       Datos del reporte en formato adecuado
     * @param rutaDestino Ruta donde guardar el PDF
     * @return La ruta del archivo generado
     * @throws Exception Si ocurre un error al generar el PDF
     */
    public static String generarReportePDF(String tipoReporte, Object datos, String rutaDestino) throws Exception {
        // Implementación simplificada para generar un reporte en PDF
        StringBuilder contenido = new StringBuilder();

        // Encabezado
        contenido.append("=============== TALLER MECÁNICO USAC ===============\n\n");
        contenido.append("REPORTE: ").append(tipoReporte.toUpperCase()).append("\n");
        contenido.append("Fecha de generación: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()))
                .append("\n\n");

        // Contenido específico según tipo de reporte
        // (Aquí se procesarían los datos específicos según el tipo de reporte)

        // Escribir el archivo
        try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
            fos.write(contenido.toString().getBytes());
        }

        return rutaDestino;
    }
}