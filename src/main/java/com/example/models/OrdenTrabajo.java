package com.example.models;

import com.example.models.personas.Cliente;
import com.example.models.personas.Mecanico;

import java.io.Serializable;
import java.util.Date;

/**
 * Modelo que representa una orden de trabajo en el taller
 */
public class OrdenTrabajo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numeroOrden;
    private Cliente cliente;
    private Mecanico mecanico;
    private Automovil automovil;
    private Servicio servicio;
    private String estado; // "espera", "servicio", "listo"
    private Date fecha;
    private Servicio servicioRecomendado;
    private boolean servicioRecomendadoGestionado;

    /**
     * Constructor para crear una nueva orden de trabajo
     */
    public OrdenTrabajo(int numeroOrden, Cliente cliente, Mecanico mecanico, Automovil automovil,
            Servicio servicio, String estado, Date fecha) {
        this.numeroOrden = numeroOrden;
        this.cliente = cliente;
        this.mecanico = mecanico;
        this.automovil = automovil;
        this.servicio = servicio;
        this.estado = estado;
        this.fecha = fecha;
    }

    /**
     * Constructor por defecto para crear una orden de trabajo
     */
    public OrdenTrabajo() {
        this.numeroOrden = Integer.parseInt("OT-" + System.currentTimeMillis());
        this.fecha = new Date();
        this.estado = "espera";
        this.servicioRecomendadoGestionado = false;
    }

    /**
     * Obtiene el número de orden
     */
    public int getNumeroOrden() {
        return numeroOrden;
    }

    /**
     * Establece el número de orden
     */
    public void setNumeroOrden(int numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

    /**
     * Obtiene el cliente asociado a la orden
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Establece el cliente de la orden
     */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Obtiene el mecánico asignado
     */
    public Mecanico getMecanico() {
        return mecanico;
    }

    /**
     * Asigna un mecánico a la orden
     */
    public void setMecanico(Mecanico mecanico) {
        this.mecanico = mecanico;
    }

    /**
     * Obtiene el automóvil asociado a la orden
     */
    public Automovil getAutomovil() {
        return automovil;
    }

    /**
     * Establece el automóvil de la orden
     */
    public void setAutomovil(Automovil automovil) {
        this.automovil = automovil;
    }

    /**
     * Obtiene el servicio a realizar
     */
    public Servicio getServicio() {
        return servicio;
    }

    /**
     * Establece el servicio de la orden
     */
    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    /**
     * Obtiene el estado actual de la orden
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Establece el estado de la orden
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtiene la fecha de creación de la orden
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha de la orden
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el servicio recomendado después de un diagnóstico
     * 
     * @return El servicio recomendado
     */
    public Servicio getServicioRecomendado() {
        return servicioRecomendado;
    }

    /**
     * Establece el servicio recomendado tras un diagnóstico
     * 
     * @param servicioRecomendado El servicio recomendado
     */
    public void setServicioRecomendado(Servicio servicioRecomendado) {
        this.servicioRecomendado = servicioRecomendado;
    }

    /**
     * Verifica si el cliente ya tomó una decisión sobre el servicio recomendado
     * 
     * @return true si ya se gestionó la recomendación (aceptada o rechazada)
     */
    public boolean isServicioRecomendadoGestionado() {
        return servicioRecomendadoGestionado;
    }

    /**
     * Establece si el servicio recomendado ya fue gestionado
     * 
     * @param servicioRecomendadoGestionado true si ya se tomó una decisión
     */
    public void setServicioRecomendadoGestionado(boolean servicioRecomendadoGestionado) {
        this.servicioRecomendadoGestionado = servicioRecomendadoGestionado;
    }

    /**
     * Representación en texto de la orden
     */
    @Override
    public String toString() {
        return "Orden #" + numeroOrden +
                " - Cliente: " + (cliente != null ? cliente.getNombreCompleto() : "No asignado") +
                " - Vehículo: "
                + (automovil != null ? automovil.getMarca() + " " + automovil.getModelo() : "No especificado") +
                " - Estado: " + estado;
    }

    public void setId(String string) {
        // Método vacío, no se utiliza en esta clase
        this.numeroOrden = Integer.parseInt(string);

    }
}
