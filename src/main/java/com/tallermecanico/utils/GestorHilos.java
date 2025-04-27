package com.tallermecanico.utils;

import com.tallermecanico.components.EstatusProgresoPanel;
import com.tallermecanico.controllers.DataController;
import com.tallermecanico.controllers.OrdenTrabajoController;
import com.tallermecanico.models.OrdenTrabajo;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gestor de hilos para simulación de tiempos del taller
 */
public class GestorHilos {
    private static GestorHilos instancia;

    // Tiempos para cada estado en segundos
    private static final int TIEMPO_ESPERA = 12;
    private static final int TIEMPO_SERVICIO = 6;
    private static final int TIEMPO_LISTO = 1;

    private ExecutorService executor;
    private AtomicBoolean ejecutando = new AtomicBoolean(false);

    private GestorHilos() {
        // Constructor privado para patrón Singleton
    }

    public static GestorHilos obtenerInstancia() {
        if (instancia == null) {
            instancia = new GestorHilos();
        }
        return instancia;
    }

    public void iniciarHilos() {
        if (ejecutando.get()) {
            return;
        }

        ejecutando.set(true);
        executor = Executors.newFixedThreadPool(2);

        // Hilo para procesar órdenes en espera
        executor.submit(() -> {
            try {
                while (ejecutando.get() && !Thread.currentThread().isInterrupted()) {
                    procesarOrdenesEnEspera();
                    Thread.sleep(1000); // Revisar cada segundo
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Hilo para procesar órdenes en proceso
        executor.submit(() -> {
            try {
                while (ejecutando.get() && !Thread.currentThread().isInterrupted()) {
                    procesarOrdenesEnProceso();
                    Thread.sleep(1000); // Revisar cada segundo
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        GestorBitacora.registrarEvento("Sistema", "Hilos", true, "Sistema de hilos iniciado correctamente");
    }

    public void detenerHilos() {
        ejecutando.set(false);
        if (executor != null) {
            executor.shutdownNow();
            GestorBitacora.registrarEvento("Sistema", "Hilos", true, "Sistema de hilos detenido correctamente");
        }
    }

    private void procesarOrdenesEnEspera() {
        Vector<OrdenTrabajo> ordenesEnEspera = OrdenTrabajoController.obtenerOrdenesPorEstado("ESPERA");
        for (OrdenTrabajo orden : ordenesEnEspera) {
            // Verificar si está asignada al hilo de espera
            if (!orden.isEnProcesoTiempo()) {
                // Marcar como en proceso de tiempo y calcular tiempo objetivo
                orden.setEnProcesoTiempo(true);
                orden.setTiempoInicio(System.currentTimeMillis());
                orden.setTiempoObjetivo(orden.getTiempoInicio() + (TIEMPO_ESPERA * 1000));
                GestorBitacora.registrarEvento("Sistema", "Cola Espera", true,
                        "Orden #" + orden.getId() + " iniciando espera de " + TIEMPO_ESPERA + " segundos");
            } else if (System.currentTimeMillis() >= orden.getTiempoObjetivo() && orden.getMecanico() == null) {
                // Si el tiempo objetivo se alcanzó, asignar el primer mecánico disponible
                OrdenTrabajoController.asignarPrimerMecanicoDisponible(orden);
                GestorBitacora.registrarEvento("Sistema", "Cola Espera", true,
                        "Orden #" + orden.getId() + " tiempo de espera completado");
            }
        }
    }

    private void procesarOrdenesEnProceso() {
        Vector<OrdenTrabajo> ordenesEnProceso = OrdenTrabajoController.obtenerOrdenesPorEstado("PROCESO");
        for (OrdenTrabajo orden : ordenesEnProceso) {
            // Verificar si está asignada al hilo de proceso
            if (!orden.isEnProcesoTiempo() && orden.getMecanico() != null) {
                // Marcar como en proceso de tiempo y calcular tiempo objetivo
                orden.setEnProcesoTiempo(true);
                orden.setTiempoInicio(System.currentTimeMillis());
                orden.setTiempoObjetivo(orden.getTiempoInicio() + (TIEMPO_SERVICIO * 1000));
                GestorBitacora.registrarEvento("Sistema", "En Servicio", true,
                        "Orden #" + orden.getId() + " iniciando servicio de " + TIEMPO_SERVICIO + " segundos");
            } else if (System.currentTimeMillis() >= orden.getTiempoObjetivo()) {
                // Si el tiempo objetivo se alcanzó, finalizar la orden
                OrdenTrabajoController.finalizarOrden(orden);
                GestorBitacora.registrarEvento("Sistema", "En Servicio", true,
                        "Orden #" + orden.getId() + " servicio completado automáticamente");
            }
        }

        // También procesamos las órdenes finalizadas para pasarlas a listas
        Vector<OrdenTrabajo> ordenesFinalizadas = OrdenTrabajoController.obtenerOrdenesPorEstado("FINALIZADO");
        for (OrdenTrabajo orden : ordenesFinalizadas) {
            if (!orden.isEnProcesoTiempo()) {
                // Marcar como en proceso de tiempo y calcular tiempo objetivo
                orden.setEnProcesoTiempo(true);
                orden.setTiempoInicio(System.currentTimeMillis());
                orden.setTiempoObjetivo(orden.getTiempoInicio() + (TIEMPO_LISTO * 1000));
                GestorBitacora.registrarEvento("Sistema", "Finalizado", true,
                        "Orden #" + orden.getId() + " iniciando tiempo finalizado de " + TIEMPO_LISTO + " segundos");
            } else if (System.currentTimeMillis() >= orden.getTiempoObjetivo()) {
                // Si el tiempo objetivo se alcanzó, generar factura
                OrdenTrabajoController.generarFactura(orden);
                GestorBitacora.registrarEvento("Sistema", "Finalizado", true,
                        "Orden #" + orden.getId() + " factura generada automáticamente");
            }
        }
    }

    public void registrarObservadorOrdenes(EstatusProgresoPanel estatusProgresoPanel) {
        // Registrar el observador para actualizar el estado del panel
        estatusProgresoPanel.actualizarEstado();
    }

    public void eliminarObservadorOrdenes(EstatusProgresoPanel estatusProgresoPanel) {
        // Eliminar el observador
        estatusProgresoPanel.destruir();
        estatusProgresoPanel = null; // Liberar referencia
    }
}