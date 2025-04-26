package com.tallermecanico.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase para gestionar la bitácora de eventos del sistema
 */
public class GestorBitacora {

    // Estructura para almacenar eventos
    private static class EventoBitacora {
        private Date fecha;
        private String usuario;
        private String accion;
        private boolean exitoso;
        private String descripcion;

        public EventoBitacora(Date fecha, String usuario, String accion, boolean exitoso, String descripcion) {
            this.fecha = fecha;
            this.usuario = usuario;
            this.accion = accion;
            this.exitoso = exitoso;
            this.descripcion = descripcion;
        }
    }

    // Lista para almacenar eventos en memoria
    private static List<EventoBitacora> eventos = new ArrayList<>();

    // Nombre del archivo para guardar la bitácora
    private static final String ARCHIVO_BITACORA = "bitacora.log";

    /**
     * Registra un nuevo evento en la bitácora
     */
    public static synchronized void registrarEvento(String usuario, String accion, boolean exitoso,
            String descripcion) {
        // Crear el evento
        EventoBitacora evento = new EventoBitacora(new Date(), usuario, accion, exitoso, descripcion);

        // Agregar a la lista en memoria
        eventos.add(evento);

        // Guardar en archivo
        guardarEvento(evento);
    }

    /**
     * Guarda un evento en el archivo de bitácora
     */
    private static void guardarEvento(EventoBitacora evento) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_BITACORA, true))) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Formatear la línea del evento
            String linea = String.format("%s | %s | %s | %s | %s",
                    sdf.format(evento.fecha),
                    evento.usuario,
                    evento.accion,
                    evento.exitoso ? "ÉXITO" : "ERROR",
                    evento.descripcion);

            // Escribir en el archivo
            writer.println(linea);

        } catch (IOException e) {
            System.err.println("Error al escribir en la bitácora: " + e.getMessage());
        }
    }

    /**
     * Obtiene la lista de eventos registrados en memoria
     */
    public static List<EventoBitacora> getEventos() {
        return new ArrayList<>(eventos);
    }

    /**
     * Carga los eventos del archivo a memoria
     * (Método para implementar si se necesita cargar eventos anteriores)
     */
    public static void cargarEventos() {
        // Esta implementación es básica y puede expandirse según necesidades
        File archivo = new File(ARCHIVO_BITACORA);
        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
            } catch (IOException e) {
                System.err.println("Error al crear archivo de bitácora: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene un texto con todos los eventos para mostrar
     */
    public static String obtenerReporteBitacora() {
        StringBuilder reporte = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        reporte.append("REPORTE DE BITÁCORA DE EVENTOS\n");
        reporte.append("=============================\n\n");

        for (EventoBitacora evento : eventos) {
            reporte.append("Fecha: ").append(sdf.format(evento.fecha)).append("\n");
            reporte.append("Usuario: ").append(evento.usuario).append("\n");
            reporte.append("Acción: ").append(evento.accion).append("\n");
            reporte.append("Resultado: ").append(evento.exitoso ? "ÉXITO" : "ERROR").append("\n");
            reporte.append("Descripción: ").append(evento.descripcion).append("\n");
            reporte.append("-----------------------------\n");
        }

        return reporte.toString();
    }
}
