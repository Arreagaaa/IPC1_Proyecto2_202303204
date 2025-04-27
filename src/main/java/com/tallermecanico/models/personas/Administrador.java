package com.tallermecanico.models.personas;

public class Administrador extends Empleado {
    public Administrador(String identificador, String nombreCompleto, String nombreUsuario, String contraseña) {
        super(identificador, nombreCompleto, nombreUsuario, contraseña, "admin", "Administrador");
    }
}
