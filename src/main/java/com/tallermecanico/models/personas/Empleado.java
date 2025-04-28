package com.tallermecanico.models.personas;

import java.io.Serializable;

/**
 * Representa a un empleado del taller mecánico
 */
public class Empleado extends Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tipo; // "admin" o "mecanico"
    private boolean disponible;

    /**
     * Constructor por defecto
     */
    public Empleado() {
        super();
        this.disponible = true;
    }

    /**
     * Constructor con parámetros
     */
    public Empleado(String identificador, String nombre, String apellido,
            String nombreUsuario, String contrasena, String tipo) {
        super(identificador, nombre, apellido, nombreUsuario, contrasena);
        this.tipo = tipo;
        this.disponible = true;
    }

    // Getters y Setters

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    /**
     * Determina si el empleado es administrador
     */
    public boolean isAdmin() {
        return "admin".equals(tipo);
    }

    /**
     * Determina si el empleado es mecánico
     */
    public boolean isMecanico() {
        return "mecanico".equals(tipo);
    }

    public String getId() {
        return getIdentificador();
    }

    public Object getPassword() {
        return getContrasena();
    }
}
