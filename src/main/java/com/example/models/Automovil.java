package com.example.models;

import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Modelo que representa un automóvil en el sistema
 * Implementa estados y transiciones, y se conecta con el sistema de hilos
 */
public class Automovil implements Serializable {

    private static final long serialVersionUID = 1L;

    // Datos básicos del automóvil
    private String placa;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private String tipo; // Sedan, SUV, Pickup, etc.
    private String rutaFoto;

    // Estados del automóvil
    public static final String ESTADO_DISPONIBLE = "disponible";
    public static final String ESTADO_EN_ESPERA = "en_espera";
    public static final String ESTADO_EN_SERVICIO = "en_servicio";
    public static final String ESTADO_LISTO = "listo";

    private String estadoActual;
    private transient ScheduledFuture<?> tareaProgresiva;
    private transient Vector<AutomovilListener> listeners;
    private int progresoServicio; // 0-100%

    // Conexión con sistema de hilos (transient para que no se serialice)
    private static transient ScheduledExecutorService scheduledExecutor;

    /**
     * Constructor para crear un nuevo automóvil
     */
    public Automovil(String placa, String marca, String modelo, int anio, String color, String tipo) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.tipo = tipo;
        this.estadoActual = ESTADO_DISPONIBLE;
        this.progresoServicio = 0;
        this.listeners = new Vector<>();
        this.rutaFoto = ""; // Por defecto no tiene foto
    }

    /**
     * Inicializa el sistema de hilos si no está inicializado
     */
    private static void inicializarExecutor() {
        if (scheduledExecutor == null || scheduledExecutor.isShutdown()) {
            scheduledExecutor = Executors.newScheduledThreadPool(5);
        }
    }

    /**
     * Detiene el sistema de hilos
     */
    public static void detenerExecutor() {
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
        }
    }

    /**
     * Inicia el servicio del automóvil, actualizando su progreso periódicamente
     * 
     * @param tiempoEstimadoHoras Tiempo estimado del servicio en horas
     */
    public void iniciarServicio(final int tiempoEstimadoHoras) {
        if (tareaProgresiva != null && !tareaProgresiva.isDone()) {
            tareaProgresiva.cancel(true);
        }

        setEstadoActual(ESTADO_EN_SERVICIO);
        progresoServicio = 0;
        notificarCambioProgreso();

        // Convertir horas a segundos para simulación (para demostración aceleramos el
        // tiempo)
        // 1 hora real = 6 segundos en simulación (factor de aceleración de 600x)
        final int segundosSimulacion = tiempoEstimadoHoras * 6;
        final int intervalosActualizacion = 10; // Número de actualizaciones para llegar al 100%
        final int incrementoPorIntervalo = 100 / intervalosActualizacion;

        inicializarExecutor();

        // Programar tarea que actualiza el progreso
        tareaProgresiva = scheduledExecutor.scheduleAtFixedRate(() -> {
            progresoServicio += incrementoPorIntervalo;

            if (progresoServicio >= 100) {
                progresoServicio = 100;
                setEstadoActual(ESTADO_LISTO);
                tareaProgresiva.cancel(false);
            }

            notificarCambioProgreso();
        }, 0, segundosSimulacion / intervalosActualizacion, TimeUnit.SECONDS);
    }

    /**
     * Detiene el servicio actual del automóvil
     */
    public void detenerServicio() {
        if (tareaProgresiva != null && !tareaProgresiva.isDone()) {
            tareaProgresiva.cancel(true);
        }
    }

    /**
     * Agrega un listener para cambios en el automóvil
     */
    public void agregarListener(AutomovilListener listener) {
        if (listeners == null) {
            listeners = new Vector<>();
        }
        listeners.add(listener);
    }

    /**
     * Remueve un listener
     */
    public void removerListener(AutomovilListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifica cambios de estado a todos los listeners
     */
    private void notificarCambioEstado() {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onEstadoCambiado(this, estadoActual);
            }
        }
    }

    /**
     * Notifica cambios de progreso a todos los listeners
     */
    private void notificarCambioProgreso() {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onProgresoCambiado(this, progresoServicio);
            }
        }
    }

    /**
     * Interface para escuchar cambios en el automóvil
     */
    public interface AutomovilListener {
        void onEstadoCambiado(Automovil automovil, String nuevoEstado);

        void onProgresoCambiado(Automovil automovil, int nuevoProgreso);
    }

    // Getters y Setters

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        String estadoAnterior = this.estadoActual;
        this.estadoActual = estadoActual;

        // Si cambió realmente el estado, notificar
        if (!estadoAnterior.equals(estadoActual)) {
            notificarCambioEstado();
        }
    }

    public int getProgresoServicio() {
        return progresoServicio;
    }

    public void setProgresoServicio(int progresoServicio) {
        this.progresoServicio = progresoServicio;
        notificarCambioProgreso();
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = rutaFoto;
    }

    /**
     * Método para verificar si el automóvil está en servicio
     */
    public boolean estaEnServicio() {
        return ESTADO_EN_SERVICIO.equals(estadoActual);
    }

    /**
     * Método para verificar si el automóvil está listo (servicio terminado)
     */
    public boolean estaListo() {
        return ESTADO_LISTO.equals(estadoActual);
    }

    /**
     * Representación textual del automóvil
     */
    @Override
    public String toString() {
        return marca + " " + modelo + " (" + placa + ")";
    }

    /**
     * Cuando se deserializa el objeto, necesitamos reinicializar los campos
     * transient
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new Vector<>();
    }
}