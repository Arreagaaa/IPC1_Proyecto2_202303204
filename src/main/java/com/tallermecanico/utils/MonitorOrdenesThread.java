package com.tallermecanico.utils;

import com.tallermecanico.controllers.DataController;
import com.tallermecanico.models.OrdenTrabajo;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Hilo encargado de monitorear órdenes de trabajo y notificar cambios
 */
public class MonitorOrdenesThread extends Thread {

    // Intervalo de actualización en milisegundos
    private static final int INTERVALO_ACTUALIZACION = 2000; // 2 segundos

    // Flag para controlar la ejecución del hilo
    private boolean ejecutando = true;

    // Lista de observadores que serán notificados de cambios
    private List<ObservadorOrdenes> observadores = new ArrayList<>();

    // Almacena el último estado conocido de las órdenes para detectar cambios
    private Vector<OrdenTrabajo> ultimasOrdenesEspera = new Vector<>();
    private Vector<OrdenTrabajo> ultimasOrdenesServicio = new Vector<>();
    private Vector<OrdenTrabajo> ultimasOrdenesListas = new Vector<>();

    /**
     * Constructor que inicia el hilo como demonio para que no impida
     * que la aplicación se cierre
     */
    public MonitorOrdenesThread() {
        setDaemon(true); // Hilo de segundo plano
        setName("Monitor-Ordenes"); // Nombrar el hilo para debugging
    }

    /**
     * Agrega un observador para ser notificado de cambios
     */
    public void agregarObservador(ObservadorOrdenes observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    /**
     * Elimina un observador
     */
    public void eliminarObservador(ObservadorOrdenes observador) {
        if (observador != null) {
            observadores.remove(observador);
        }
    }

    /**
     * Detiene la ejecución del hilo
     */
    public void detener() {
        ejecutando = false;
        interrupt(); // Interrumpir el hilo si está bloqueado en sleep
    }

    /**
     * Método principal del hilo que se ejecuta continuamente
     */
    @Override
    public void run() {
        try {
            // Inicializar con el estado actual
            actualizarUltimoEstadoConocido();

            // Notificar a los observadores inicialmente
            notificarObservadores();

            // Bucle principal
            while (ejecutando) {
                // Verificar cambios
                if (verificarCambios()) {
                    // Si hubo cambios, actualizar el último estado conocido
                    actualizarUltimoEstadoConocido();

                    // Notificar a los observadores
                    notificarObservadores();
                }

                // Dormir por el intervalo definido
                Thread.sleep(INTERVALO_ACTUALIZACION);
            }
        } catch (InterruptedException e) {
            // El hilo fue interrumpido, terminar limpiamente
            GestorBitacora.registrarEvento("Sistema", "Monitor Órdenes", false,
                    "Hilo de monitoreo interrumpido: " + e.getMessage());
        } catch (Exception e) {
            // Capturar cualquier otra excepción para evitar que el hilo muera
            // silenciosamente
            GestorBitacora.registrarEvento("Sistema", "Monitor Órdenes", false,
                    "Error en hilo de monitoreo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza los vectores que almacenan el último estado conocido
     */
    private void actualizarUltimoEstadoConocido() {
        ultimasOrdenesEspera = new Vector<>(DataController.getColaEspera());
        ultimasOrdenesServicio = obtenerOrdenesEnServicio();
        ultimasOrdenesListas = obtenerOrdenesListas();
    }

    /**
     * Obtiene las órdenes que están actualmente en servicio (asignadas a mecánicos)
     */
    private Vector<OrdenTrabajo> obtenerOrdenesEnServicio() {
        Vector<OrdenTrabajo> enServicio = new Vector<>();

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if ("en_servicio".equals(orden.getEstado())) {
                enServicio.add(orden);
            }
        }

        return enServicio;
    }

    /**
     * Obtiene las órdenes que están listas (completadas por mecánicos)
     */
    private Vector<OrdenTrabajo> obtenerOrdenesListas() {
        Vector<OrdenTrabajo> listas = new Vector<>();

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if ("listo".equals(orden.getEstado())) {
                listas.add(orden);
            }
        }

        return listas;
    }

    /**
     * Verifica si hubo cambios en alguna de las colas
     */
    private boolean verificarCambios() {
        // Obtener estado actual
        Vector<OrdenTrabajo> ordenesEspera = DataController.getColaEspera();
        Vector<OrdenTrabajo> ordenesServicio = obtenerOrdenesEnServicio();
        Vector<OrdenTrabajo> ordenesListas = obtenerOrdenesListas();

        // Verificar si cambió el tamaño de alguna cola
        if (ordenesEspera.size() != ultimasOrdenesEspera.size() ||
                ordenesServicio.size() != ultimasOrdenesServicio.size() ||
                ordenesListas.size() != ultimasOrdenesListas.size()) {
            return true;
        }

        // Verificar cambios en órdenes en espera
        for (int i = 0; i < ordenesEspera.size(); i++) {
            if (!ordenesEspera.get(i).getEstado().equals(ultimasOrdenesEspera.get(i).getEstado())) {
                return true;
            }
        }

        // Verificar cambios en órdenes en servicio
        for (OrdenTrabajo orden : ordenesServicio) {
            boolean encontrada = false;
            for (OrdenTrabajo ultimaOrden : ultimasOrdenesServicio) {
                if (orden.getNumero() == ultimaOrden.getNumero()) {
                    encontrada = true;
                    if (!orden.getEstado().equals(ultimaOrden.getEstado())) {
                        return true;
                    }
                    break;
                }
            }
            if (!encontrada) {
                return true;
            }
        }

        // Verificar cambios en órdenes listas
        for (OrdenTrabajo orden : ordenesListas) {
            boolean encontrada = false;
            for (OrdenTrabajo ultimaOrden : ultimasOrdenesListas) {
                if (orden.getNumero() == ultimaOrden.getNumero()) {
                    encontrada = true;
                    if (!orden.getEstado().equals(ultimaOrden.getEstado()) ||
                            orden.isPagado() != ultimaOrden.isPagado()) {
                        return true;
                    }
                    break;
                }
            }
            if (!encontrada) {
                return true;
            }
        }

        return false;
    }

    /**
     * Notifica a todos los observadores sobre cambios en las órdenes
     */
    private void notificarObservadores() {
        // Crear copias de las colecciones actuales para evitar problemas de
        // concurrencia
        final Vector<OrdenTrabajo> ordenesEspera = new Vector<>(DataController.getColaEspera());
        final Vector<OrdenTrabajo> ordenesServicio = obtenerOrdenesEnServicio();
        final Vector<OrdenTrabajo> ordenesListas = obtenerOrdenesListas();

        // Notificar a cada observador
        for (ObservadorOrdenes observador : observadores) {
            try {
                observador.ordenesActualizadas(ordenesEspera, ordenesServicio, ordenesListas);
            } catch (Exception e) {
                // Evitar que el error en un observador afecte a los demás
                GestorBitacora.registrarEvento("Sistema", "Monitor Órdenes", false,
                        "Error al notificar observador: " + e.getMessage());
            }
        }
    }

    /**
     * Interfaz que deben implementar las clases que quieran recibir
     * notificaciones del monitor de órdenes
     */
    public interface ObservadorOrdenes {
        /**
         * Método llamado cuando cambia el estado de las órdenes
         * 
         * @param ordenesEspera   Órdenes en cola de espera
         * @param ordenesServicio Órdenes en servicio (asignadas a mecánicos)
         * @param ordenesListas   Órdenes completadas listas para entrega
         */
        void ordenesActualizadas(Vector<OrdenTrabajo> ordenesEspera,
                Vector<OrdenTrabajo> ordenesServicio,
                Vector<OrdenTrabajo> ordenesListas);
    }
}