package com.tallermecanico.models.personas;

import com.tallermecanico.models.OrdenTrabajo;

import java.io.Serializable;
import java.util.Vector;

/**
 * Representa a un mecánico del taller
 */
public class Mecanico extends Empleado implements Serializable {

    private static final long serialVersionUID = 1L;

    private Vector<OrdenTrabajo> ordenesAsignadas;
    private OrdenTrabajo ordenActual;

    /**
     * Constructor por defecto
     */
    public Mecanico() {
        super();
        this.setTipo("mecanico");
        this.ordenesAsignadas = new Vector<>();
        this.ordenActual = null;
    }

    /**
     * Constructor con parámetros
     */
    public Mecanico(String identificador, String nombre, String apellido,
            String nombreUsuario, String contrasena) {
        super(identificador, nombre, apellido, nombreUsuario, contrasena, "mecanico");
        this.ordenesAsignadas = new Vector<>();
        this.ordenActual = null;
    }

    // Getters y Setters

    public Vector<OrdenTrabajo> getOrdenesAsignadas() {
        return ordenesAsignadas;
    }

    public void setOrdenesAsignadas(Vector<OrdenTrabajo> ordenesAsignadas) {
        this.ordenesAsignadas = ordenesAsignadas;
    }

    public OrdenTrabajo getOrdenActual() {
        return ordenActual;
    }

    public void setOrdenActual(OrdenTrabajo ordenActual) {
        this.ordenActual = ordenActual;
        // Actualizar disponibilidad según si tiene o no orden actual
        setDisponible(ordenActual == null);
    }

    /**
     * Asigna una orden de trabajo al mecánico
     * 
     * @return true si se pudo asignar, false si el mecánico está ocupado
     */
    public boolean asignarOrden(OrdenTrabajo orden) {
        if (isDisponible()) {
            setOrdenActual(orden);
            ordenesAsignadas.add(orden);
            return true;
        }
        return false;
    }

    /**
     * Completa la orden actual y libera al mecánico
     */
    public void completarOrdenActual() {
        if (ordenActual != null) {
            ordenActual.setEstado("listo");
            ordenActual = null;
            setDisponible(true);
        }
    }

    /**
     * Obtiene el número de órdenes completadas históricamente
     */
    public int getOrdenesCompletadas() {
        return ordenesAsignadas.size() - (ordenActual != null ? 1 : 0);
    }
}
