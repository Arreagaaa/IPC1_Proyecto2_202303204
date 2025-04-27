package com.tallermecanico.models;

import java.io.Serializable;
import java.util.Vector;

/**
 * Representa un servicio ofrecido por el taller
 */
public class Servicio implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String marca;
    private String modelo;
    private Vector<Repuesto> repuestos;
    private double precioManoObra;
    private int vecesUsado; // Contador para estadísticas

    /**
     * Constructor por defecto
     */
    public Servicio() {
        this.repuestos = new Vector<>();
        this.vecesUsado = 0;
    }

    /**
     * Constructor con parámetros
     */
    public Servicio(int id, String nombre, String marca, String modelo, double precioManoObra) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.modelo = modelo;
        this.precioManoObra = precioManoObra;
        this.repuestos = new Vector<>();
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

    public int getVecesUsado() {
        return vecesUsado;
    }

    public void setVecesUsado(int vecesUsado) {
        this.vecesUsado = vecesUsado;
    }

    /**
     * Calcula el precio total del servicio (mano de obra + repuestos)
     */
    public double getPrecioTotal() {
        double precioRepuestos = 0.0;
        for (Repuesto repuesto : repuestos) {
            precioRepuestos += repuesto.getPrecio();
        }
        return precioManoObra + precioRepuestos;
    }

    /**
     * Agrega un repuesto al servicio, verificando compatibilidad
     * 
     * @return true si se agregó correctamente, false si no es compatible
     */
    public boolean agregarRepuesto(Repuesto repuesto) {
        if (repuesto == null) {
            return false;
        }

        // Verificar compatibilidad de marca y modelo
        if ("cualquiera".equalsIgnoreCase(this.marca) ||
                "cualquiera".equalsIgnoreCase(this.modelo) ||
                "cualquiera".equalsIgnoreCase(repuesto.getMarca()) ||
                "cualquiera".equalsIgnoreCase(repuesto.getModelo())) {
            repuestos.add(repuesto);
            return true;
        }

        if (this.marca.equalsIgnoreCase(repuesto.getMarca()) &&
                this.modelo.equalsIgnoreCase(repuesto.getModelo())) {
            repuestos.add(repuesto);
            return true;
        }

        return false;
    }

    /**
     * Quita un repuesto del servicio por su ID
     */
    public boolean quitarRepuesto(int idRepuesto) {
        for (int i = 0; i < repuestos.size(); i++) {
            if (Integer.parseInt(repuestos.get(i).getId()) == idRepuesto) {
                repuestos.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Incrementa el contador de veces usado
     */
    public void incrementarUso() {
        this.vecesUsado++;
    }

    /**
     * Verifica si es un servicio de diagnóstico
     */
    public boolean esDiagnostico() {
        return "Diagnóstico".equalsIgnoreCase(nombre);
    }

    @Override
    public String toString() {
        return id + " - " + nombre + " (" + marca + " " + modelo + ") - Q" + getPrecioTotal();
    }

    public double getPrecioBase() {
        // CALCULAR PRECIO BASE
        double precioBase = 0.0;
        for (Repuesto repuesto : repuestos) {
            precioBase += repuesto.getPrecio();
        }
        return precioBase;
    }

    public String getDescripcion() {
        // ESCRIBIR DESCRIPCION DEL SERVICIO
        StringBuilder descripcion = new StringBuilder("Servicio: " + nombre + "\n");
        descripcion.append("Marca: ").append(marca).append("\n");
        descripcion.append("Modelo: ").append(modelo).append("\n");
        descripcion.append("Precio Mano de Obra: Q").append(precioManoObra).append("\n");
        descripcion.append("Repuestos:\n");
        for (Repuesto repuesto : repuestos) {
            descripcion.append("- ").append(repuesto.getNombre()).append(" (Q").append(repuesto.getPrecio())
                    .append(")\n");
        }
        descripcion.append("Precio Total: Q").append(getPrecioTotal()).append("\n");
        descripcion.append("Veces Usado: ").append(vecesUsado).append("\n");
        return descripcion.toString();
    }

    public char[] getCantidad() {
        // ESCRIBIR CANTIDAD DE REPUESTOS
        StringBuilder cantidad = new StringBuilder();
        for (Repuesto repuesto : repuestos) {
            cantidad.append(repuesto.getCantidad()).append(" ");
        }
        return cantidad.toString().toCharArray();
    }
}
