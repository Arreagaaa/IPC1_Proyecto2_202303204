package com.example.models.personas;

import java.io.Serializable;

/**
 * Modelo que representa un administrador del sistema
 */
public class Administrador extends Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor para crear un nuevo administrador
     */
    public Administrador(String identificador, String nombreCompleto, String usuario, String password) {
        super(identificador, nombreCompleto, usuario, password);
    }

    @Override
    public String toString() {
        return "Administrador: " + getNombreCompleto() + " (ID: " + getIdentificador() + ")";
    }
}
