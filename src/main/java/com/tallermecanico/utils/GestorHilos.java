package com.tallermecanico.utils;

/**
 * Gestor centralizado de hilos del sistema
 * Implementa el patrón Singleton
 */
public class GestorHilos {

    // Instancia única
    private static GestorHilos instancia;

    // Hilo para monitoreo de órdenes
    private MonitorOrdenesThread monitorOrdenes;

    /**
     * Constructor privado (patrón Singleton)
     */
    private GestorHilos() {
        // Inicializar el hilo de monitoreo
        monitorOrdenes = new MonitorOrdenesThread();
    }

    /**
     * Obtiene la instancia única del gestor
     */
    public static synchronized GestorHilos obtenerInstancia() {
        if (instancia == null) {
            instancia = new GestorHilos();
        }
        return instancia;
    }

    /**
     * Inicia los hilos del sistema
     */
    public void iniciarHilos() {
        if (!monitorOrdenes.isAlive()) {
            monitorOrdenes.start();
            GestorBitacora.registrarEvento("Sistema", "Hilos", true,
                    "Iniciado hilo de monitoreo de órdenes");
        }
    }

    /**
     * Detiene los hilos del sistema
     */
    public void detenerHilos() {
        monitorOrdenes.detener();
        GestorBitacora.registrarEvento("Sistema", "Hilos", true,
                "Detenido hilo de monitoreo de órdenes");
    }

    /**
     * Obtiene el monitor de órdenes
     */
    public MonitorOrdenesThread getMonitorOrdenes() {
        return monitorOrdenes;
    }

    /**
     * Registra un observador para monitoreo de órdenes
     */
    public void registrarObservadorOrdenes(MonitorOrdenesThread.ObservadorOrdenes observador) {
        monitorOrdenes.agregarObservador(observador);
    }

    /**
     * Elimina un observador de monitoreo de órdenes
     */
    public void eliminarObservadorOrdenes(MonitorOrdenesThread.ObservadorOrdenes observador) {
        monitorOrdenes.eliminarObservador(observador);
    }
}