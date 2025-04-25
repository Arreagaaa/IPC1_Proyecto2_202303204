package com.example.models.personas;

import java.io.Serializable;

/**
 * Clase base para los usuarios del sistema
 */
public abstract class Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private String identificador;
    private String nombreCompleto;
    private String usuario;
    private String password;

    /**
     * Constructor para crear una nueva persona
     */
    public Persona(String identificador, String nombreCompleto, String usuario, String password) {
        this.identificador = identificador;
        this.nombreCompleto = nombreCompleto;
        this.usuario = usuario;
        this.password = password;
    }

    /**
     * Obtiene el identificador (DPI/ID)
     */
    public String getIdentificador() {
        return identificador;
    }

    /**
     * Establece el identificador
     */
    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    /**
     * Obtiene el nombre completo
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * Establece el nombre completo
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    /**
     * Obtiene el nombre de usuario
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Establece el nombre de usuario
     */
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    /**
     * Obtiene la contraseña
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Verifica si la contraseña coincide
     */
    public boolean verificarPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * Representación en texto (a implementar por subclases)
     */
    @Override
    public abstract String toString();
}