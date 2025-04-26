package com.tallermecanico.models;

import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Mecanico;

import java.io.Serializable;
import java.util.Date;

/**
 * Representa una orden de trabajo en el taller
 */
public class OrdenTrabajo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private Automovil automovil;
    private Cliente cliente;
    private Servicio servicio;
    private Date fecha;
    private Mecanico mecanico;
    private String estado; // "espera", "en_servicio", "listo"
    private boolean pagado;
    private Factura factura;

    /**
     * Constructor por defecto
     */
    public OrdenTrabajo() {
        this.fecha = new Date();
        this.estado = "espera";
        this.pagado = false;
    }

    /**
     * Constructor con parámetros
     */
    public OrdenTrabajo(int numero, Automovil automovil, Cliente cliente, Servicio servicio) {
        this.numero = numero;
        this.automovil = automovil;
        this.cliente = cliente;
        this.servicio = servicio;
        this.fecha = new Date();
        this.estado = "espera";
        this.pagado = false;
    }

    // Getters y Setters

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Automovil getAutomovil() {
        return automovil;
    }

    public void setAutomovil(Automovil automovil) {
        this.automovil = automovil;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
        // Si cambiamos el servicio, ya no está pagado
        this.pagado = false;
        // Si hay factura, actualizarla
        if (this.factura != null) {
            this.factura.actualizarTotal();
        }
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Mecanico getMecanico() {
        return mecanico;
    }

    public void setMecanico(Mecanico mecanico) {
        this.mecanico = mecanico;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isPagado() {
        return pagado;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    /**
     * Verifica si el automóvil es compatible con el servicio
     */
    public boolean verificarCompatibilidad() {
        if (automovil == null || servicio == null) {
            return false;
        }

        return automovil.esCompatibleConServicio(servicio);
    }

    /**
     * Genera la factura para esta orden
     */
    public Factura generarFactura() {
        if (factura == null && "listo".equals(estado)) {
            factura = new Factura(this);
        }
        return factura;
    }

    /**
     * Registra el pago de la orden
     */
    public void registrarPago() {
        if (factura != null) {
            factura.setPagada(true);
            this.pagado = true;

            // Incrementar contador de servicios realizados para el cliente
            if (cliente != null) {
                cliente.incrementarServiciosRealizados();
            }

            // Incrementar contador de veces usado para el servicio
            if (servicio != null) {
                servicio.incrementarUso();
            }
        }
    }

    @Override
    public String toString() {
        return "Orden #" + numero + " - " +
                (automovil != null ? automovil.getPlaca() : "Sin auto") + " - " +
                (servicio != null ? servicio.getNombre() : "Sin servicio") + " - " +
                estado;
    }
}
