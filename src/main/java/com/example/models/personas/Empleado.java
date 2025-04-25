package com.example.models.personas;

public abstract class Empleado extends Persona {
    public Empleado(String identificador, String nombreCompleto, String usuario, String contraseña) {
        super(identificador, nombreCompleto, usuario, contraseña);
    }
}
