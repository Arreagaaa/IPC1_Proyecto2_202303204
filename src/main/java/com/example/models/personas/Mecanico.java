package com.example.models.personas;

import com.example.models.OrdenTrabajo;

import java.io.Serializable;
import java.util.Vector;

/**
 * Modelo que representa un mecánico del taller
 */
public class Mecanico extends Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean disponible;
    private Vector<OrdenTrabajo> ordenesAsignadas;

    /**
     * Constructor para crear un nuevo mecánico
     */
    public Mecanico(String identificador, String nombreCompleto, String usuario, String password) {
        super(identificador, nombreCompleto, usuario, password);
        this.disponible = true;
        this.ordenesAsignadas = new Vector<>();
    }

    /**
     * Verifica si el mecánico está disponible
     */
    public boolean estaDisponible() {
        return disponible && getNumeroOrdenesActivas() == 0;
    }

    /**
     * Establece la disponibilidad del mecánico
     */
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    /**
     * Obtiene las órdenes asignadas al mecánico
     */
    public Vector<OrdenTrabajo> getOrdenesAsignadas() {
        return ordenesAsignadas;
    }

    /**
     * Asigna una orden al mecánico
     */
    public void asignarOrden(OrdenTrabajo orden) {
        ordenesAsignadas.add(orden);
        this.disponible = false;
    }

    /**
     * Libera una orden al mecánico
     */
    public void liberarOrden(OrdenTrabajo orden) {
        ordenesAsignadas.remove(orden);
        // Si no tiene más órdenes activas, está disponible
        this.disponible = getNumeroOrdenesActivas() == 0;
    }

    /**
     * Obtiene el número de órdenes activas (no finalizadas)
     */
    public int getNumeroOrdenesActivas() {
        int activas = 0;
        for (OrdenTrabajo orden : ordenesAsignadas) {
            if (!orden.getEstado().equals("listo")) {
                activas++;
            }
        }
        return activas;
    }

    @Override
    public String toString() {
        return "Mecánico: " + getNombreCompleto() + " (DPI: " + getIdentificador() +
                ", Estado: " + (estaDisponible() ? "Disponible" : "Ocupado") + ")";
    }
}
