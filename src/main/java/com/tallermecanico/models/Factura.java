package com.tallermecanico.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Representa una factura por servicios en el taller
 */
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numeroFactura;
    private OrdenTrabajo ordenTrabajo;
    private Date fechaEmision;
    private double total;
    private boolean pagada;

    /**
     * Constructor por defecto
     */
    public Factura() {
        this.fechaEmision = new Date();
        this.pagada = false;
    }

    /**
     * Constructor a partir de una orden de trabajo
     */
    public Factura(OrdenTrabajo ordenTrabajo) {
        this.ordenTrabajo = ordenTrabajo;
        this.fechaEmision = new Date();
        this.pagada = false;

        // Calcular el total basado en el servicio
        actualizarTotal();
    }

    // Getters y Setters

    public int getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(int numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public OrdenTrabajo getOrdenTrabajo() {
        return ordenTrabajo;
    }

    public void setOrdenTrabajo(OrdenTrabajo ordenTrabajo) {
        this.ordenTrabajo = ordenTrabajo;
        actualizarTotal();
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public boolean isPagada() {
        return pagada;
    }

    public void setPagada(boolean pagada) {
        this.pagada = pagada;

        // Actualizar también la orden de trabajo
        if (ordenTrabajo != null) {
            ordenTrabajo.setPagado(pagada);
        }
    }

    /**
     * Actualiza el total de la factura basado en el servicio de la orden
     */
    public void actualizarTotal() {
        if (ordenTrabajo != null && ordenTrabajo.getServicio() != null) {
            this.total = ordenTrabajo.getServicio().getPrecioTotal();
        } else {
            this.total = 0.0;
        }
    }

    /**
     * Obtiene los detalles de la factura en formato texto
     */
    public String getDetallesFactura() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        sb.append("FACTURA #").append(numeroFactura).append("\n");
        sb.append("Fecha: ").append(sdf.format(fechaEmision)).append("\n\n");

        if (ordenTrabajo != null) {
            sb.append("CLIENTE: ").append(ordenTrabajo.getCliente().getNombreCompleto()).append("\n");
            sb.append("DPI: ").append(ordenTrabajo.getCliente().getIdentificador()).append("\n");
            sb.append("VEHÍCULO: ").append(ordenTrabajo.getAutomovil().getMarca()).append(" ");
            sb.append(ordenTrabajo.getAutomovil().getModelo()).append(" (");
            sb.append(ordenTrabajo.getAutomovil().getPlaca()).append(")\n\n");

            sb.append("ORDEN DE TRABAJO #").append(ordenTrabajo.getNumero()).append("\n");
            sb.append("Servicio: ").append(ordenTrabajo.getServicio().getNombre()).append("\n");
            sb.append("Precio mano de obra: Q").append(ordenTrabajo.getServicio().getPrecioManoObra()).append("\n\n");

            sb.append("REPUESTOS UTILIZADOS:\n");
            if (ordenTrabajo.getServicio().getRepuestos().isEmpty()) {
                sb.append("Ninguno\n");
            } else {
                for (Repuesto repuesto : ordenTrabajo.getServicio().getRepuestos()) {
                    sb.append("- ").append(repuesto.getNombre()).append(": Q");
                    sb.append(repuesto.getPrecio()).append("\n");
                }
            }

            sb.append("\nTOTAL A PAGAR: Q").append(total).append("\n");
            sb.append("\nEstado: ").append(pagada ? "PAGADA" : "PENDIENTE");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Factura #" + numeroFactura + " - Orden #" +
                (ordenTrabajo != null ? ordenTrabajo.getNumero() : "N/A") +
                " - Q" + total + " - " + (pagada ? "PAGADA" : "PENDIENTE");
    }
}
