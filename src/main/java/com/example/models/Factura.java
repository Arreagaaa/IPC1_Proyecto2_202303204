package com.example.models;

import com.example.models.personas.Cliente;

import java.io.Serializable;
import java.util.Date;

/**
 * Modelo que representa una factura en el sistema
 */
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numeroFactura;
    private OrdenTrabajo ordenTrabajo;
    private Cliente cliente;
    private Date fecha;
    private double subtotal;
    private double iva;
    private double total;
    private String estado; // "pendiente" o "pagada"
    private Date fechaPago;

    /**
     * Constructor para crear una nueva factura
     * @param object 
     * @param string 
     */
    public Factura(int numeroFactura, OrdenTrabajo ordenTrabajo, Cliente cliente, Date fecha,
            double subtotal, double iva, double total, String string, Object object) {
        this.numeroFactura = numeroFactura;
        this.ordenTrabajo = ordenTrabajo;
        this.cliente = cliente;
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.estado = "pendiente";
        this.fechaPago = null;
    }

    // Getter y Setter para numeroFactura
    public int getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(int numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    // Alias para mantener compatibilidad
    public int getNumero() {
        return getNumeroFactura();
    }

    // Getter y Setter para ordenTrabajo
    public OrdenTrabajo getOrdenTrabajo() {
        return ordenTrabajo;
    }

    public void setOrdenTrabajo(OrdenTrabajo ordenTrabajo) {
        this.ordenTrabajo = ordenTrabajo;
    }

    // Getter y Setter para cliente
    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    // Getter y Setter para fecha
    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    // Getter y Setter para subtotal
    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    // Getter y Setter para iva
    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    // Getter y Setter para total
    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    // Getter y Setter para estado
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Métodos booleanos para facilitar el trabajo en la interfaz
    public boolean isPagada() {
        return "pagada".equals(estado);
    }

    public boolean isPendiente() {
        return "pendiente".equals(estado);
    }

    // Método para compatibilidad con la vista
    public boolean isEstado() {
        return isPagada();
    }

    // Getter y Setter para fechaPago
    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    /**
     * Representación en texto de la factura
     */
    @Override
    public String toString() {
        return "Factura #" + numeroFactura +
                " - Cliente: " + cliente.getNombreCompleto() +
                " - Total: Q" + String.format("%.2f", total) +
                " - Estado: " + (isPagada() ? "Pagada" : "Pendiente");
    }
}