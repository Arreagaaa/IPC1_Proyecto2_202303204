package com.tallermecanico.models.personas;

import java.io.Serializable;

/**
 * Clase abstracta que representa a una persona en el sistema
 */
public abstract class Persona implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String identificador; // DPI, CUI u otro identificador
    protected String nombre;
    protected String apellido;
    protected String nombreUsuario;
    protected String contrasena;

    /**
     * Constructor por defecto
     */
    public Persona() {
        this.identificador = "";
        this.nombre = "";
        this.apellido = "";
        this.nombreUsuario = "";
        this.contrasena = "";
    }

    /**
     * Constructor con parámetros
     */
    public Persona(String identificador, String nombre, String apellido, String nombreUsuario, String contrasena) {
        this.identificador = identificador;
        this.nombre = nombre;
        this.apellido = apellido;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
    }

    // Métodos getters y setters

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    /**
     * Obtiene el nombre completo de la persona
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Verifica las credenciales de la persona
     */
    public boolean verificarCredenciales(String usuario, String password) {
        return nombreUsuario.equals(usuario) && contrasena.equals(password);
    }
}