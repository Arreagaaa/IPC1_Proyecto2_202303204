package com.example.models;

import java.io.Serializable;
import java.util.Vector;

/**
 * Modelo que representa un servicio ofrecido por el taller
 */
public class Servicio implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nombre;
    private String marca;
    private String modelo;
    private Vector<Repuesto> repuestos;
    private double precioManoObra;
    private int tiempoEstimado; // en horas
    private String descripcion;

    /**
     * Constructor para servicio genérico (cualquier marca/modelo)
     */
    public Servicio(String codigo, String nombre, double precioManoObra, int tiempoEstimado, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioManoObra = precioManoObra;
        this.tiempoEstimado = tiempoEstimado;
        this.descripcion = descripcion;
        this.repuestos = new Vector<>();
        this.marca = "cualquiera";
        this.modelo = "cualquiera";
    }

    /**
     * Constructor para servicio específico para marca/modelo
     */
    public Servicio(String codigo, String nombre, String marca, String modelo, double precioManoObra,
            int tiempoEstimado, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.modelo = modelo;
        this.precioManoObra = precioManoObra;
        this.tiempoEstimado = tiempoEstimado;
        this.descripcion = descripcion;
        this.repuestos = new Vector<>();
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

    public Vector<Repuesto> getRepuestos() {
        return repuestos;
    }

    public void setRepuestos(Vector<Repuesto> repuestos) {
        this.repuestos = repuestos;
    }

    public double getPrecioManoObra() {
        return precioManoObra;
    }

    public void setPrecioManoObra(double precioManoObra) {
        this.precioManoObra = precioManoObra;
    }

    public int getTiempoEstimado() {
        return tiempoEstimado;
    }

    public void setTiempoEstimado(int tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Agrega un repuesto verificando compatibilidad de marca/modelo
     */
    public void agregarRepuesto(Repuesto repuesto) {
        // Verificar compatibilidad
        if (esRepuestoCompatible(repuesto)) {
            this.repuestos.add(repuesto);
        }
    }

    /**
     * Verifica si un repuesto es compatible con el servicio
     */
    public boolean esRepuestoCompatible(Repuesto repuesto) {
        // Compatible si alguno es "cualquiera" o si coinciden marca y modelo
        return (this.marca.equalsIgnoreCase("cualquiera") ||
                repuesto.getMarca().equalsIgnoreCase("cualquiera") ||
                this.marca.equalsIgnoreCase(repuesto.getMarca()))
                &&
                (this.modelo.equalsIgnoreCase("cualquiera") ||
                        repuesto.getModelo().equalsIgnoreCase("cualquiera") ||
                        this.modelo.equalsIgnoreCase(repuesto.getModelo()));
    }

    /**
     * Elimina un repuesto del servicio
     */
    public void eliminarRepuesto(Repuesto repuesto) {
        this.repuestos.remove(repuesto);
    }

    /**
     * Calcula el precio total de los repuestos
     */
    public double getPrecioRepuestos() {
        double total = 0;
        for (Repuesto repuesto : repuestos) {
            total += repuesto.getPrecio();
        }
        return total;
    }

    /**
     * Calcula el precio total del servicio (mano de obra + repuestos)
     */
    public double getPrecio() {
        return precioManoObra + getPrecioRepuestos();
    }

    /**
     * Verifica si el servicio es compatible con un automóvil
     */
    public boolean esCompatible(Automovil automovil) {
        return (this.marca.equalsIgnoreCase("cualquiera") ||
                this.marca.equalsIgnoreCase(automovil.getMarca()))
                &&
                (this.modelo.equalsIgnoreCase("cualquiera") ||
                        this.modelo.equalsIgnoreCase(automovil.getModelo()));
    }

    @Override
    public String toString() {
        return nombre + " (Q" + String.format("%.2f", getPrecio()) + ")";
    }
}
