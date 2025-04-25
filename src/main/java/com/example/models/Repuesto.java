package com.example.models;

import java.io.Serializable;

/**
 * Modelo que representa un repuesto en el inventario
 */
public class Repuesto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nombre;
    private String marca;
    private String modelo;
    private int existencias;
    private double precio;

    /**
     * Constructor para crear un nuevo repuesto
     */
    public Repuesto(String codigo, String nombre, String marca, String modelo, int existencias, double precio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.modelo = modelo;
        this.existencias = existencias;
        this.precio = precio;
    }

    // Getters y setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public int getExistencias() {
        return existencias;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /**
     * Reduce las existencias en la cantidad especificada
     */
    public void reducirExistencia(int cantidad) {
        this.existencias -= cantidad;
        if (this.existencias < 0) {
            this.existencias = 0;
        }
    }

    /**
     * Aumenta las existencias en la cantidad especificada
     */
    public void aumentarExistencia(int cantidad) {
        this.existencias += cantidad;
    }

    /**
     * Verifica si hay suficientes existencias
     */
    public boolean haySuficiente(int cantidad) {
        return this.existencias >= cantidad;
    }

    @Override
    public String toString() {
        return nombre + " - " + marca + " - " + modelo + " (Q" + precio + ")";
    }
}
