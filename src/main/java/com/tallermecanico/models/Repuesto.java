package com.tallermecanico.models;

import java.io.Serializable;

/**
 * Representa un repuesto en el inventario del taller
 */
public class Repuesto implements Serializable, Comparable<Repuesto> {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String marca;
    private String modelo;
    private int existencias;
    private double precio;
    private int vecesUsado; // Contador para estadísticas

    /**
     * Constructor por defecto
     */
    public Repuesto() {
        this.vecesUsado = 0;
    }

    /**
     * Constructor con parámetros
     */
    public Repuesto(int id, String nombre, String marca, String modelo, int existencias, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.modelo = modelo;
        this.existencias = existencias;
        this.precio = precio;
        this.vecesUsado = 0;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getVecesUsado() {
        return vecesUsado;
    }

    public void setVecesUsado(int vecesUsado) {
        this.vecesUsado = vecesUsado;
    }

    /**
     * Incrementa el contador de veces usado
     */
    public void incrementarUso() {
        this.vecesUsado++;
    }

    /**
     * Reduce el inventario cuando se usa el repuesto
     * 
     * @param cantidad Cantidad a reducir
     * @return true si hay suficientes existencias, false en caso contrario
     */
    public boolean reducirExistencias(int cantidad) {
        if (existencias >= cantidad) {
            existencias -= cantidad;
            incrementarUso();
            return true;
        }
        return false;
    }

    /**
     * Aumenta el inventario cuando se recibe más repuestos
     * 
     * @param cantidad Cantidad a aumentar
     */
    public void aumentarExistencias(int cantidad) {
        if (cantidad > 0) {
            existencias += cantidad;
        }
    }

    /**
     * Verifica si el repuesto es compatible con un automóvil específico
     */
    public boolean esCompatibleCon(Automovil auto) {
        // Si el repuesto es para cualquier marca/modelo
        if ("cualquiera".equalsIgnoreCase(this.marca) ||
                "cualquiera".equalsIgnoreCase(this.modelo)) {
            return true;
        }

        // Verificar coincidencia de marca y modelo
        return this.marca.equalsIgnoreCase(auto.getMarca()) &&
                this.modelo.equalsIgnoreCase(auto.getModelo());
    }

    /**
     * Implementación de compareTo para ordenamiento
     * Por defecto, compara por precio
     */
    @Override
    public int compareTo(Repuesto otro) {
        return Double.compare(this.precio, otro.precio);
    }

    @Override
    public String toString() {
        return id + " - " + nombre + " (" + marca + " " + modelo + ") - Q" + precio;
    }
}
