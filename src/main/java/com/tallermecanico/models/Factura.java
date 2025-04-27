package com.tallermecanico.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import com.tallermecanico.models.personas.Persona;

/**
 * Representa una factura del taller mecánico
 */
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private OrdenTrabajo ordenTrabajo;
    private Date fechaEmision;
    private Date fechaPago;
    private boolean pagada;
    private String metodoPago;
    private Vector<DetalleFactura> detalles;

    /**
     * Constructor para crear una nueva factura
     * 
     * @param numero       Número de factura
     * @param ordenTrabajo Orden de trabajo asociada
     */
    public Factura(int numero, OrdenTrabajo ordenTrabajo) {
        this.numero = numero;
        this.ordenTrabajo = ordenTrabajo;
        this.fechaEmision = new Date();
        this.pagada = false;
        this.detalles = new Vector<>();

        // Generar los detalles de la factura automáticamente
        generarDetalles();
    }

    /**
     * Genera los detalles de la factura basados en el servicio y repuestos
     */
    private void generarDetalles() {
        // Agregar el servicio como un detalle
        Servicio servicio = ordenTrabajo.getServicio();
        DetalleFactura detalleServicio = new DetalleFactura(
                "SERV-" + servicio.getId(),
                servicio.getNombre(),
                1,
                servicio.getPrecioBase(),
                0);
        detalles.add(detalleServicio);

        // Agregar cada repuesto como un detalle
        for (Repuesto repuesto : servicio.getRepuestos()) {
            DetalleFactura detalleRepuesto = new DetalleFactura(
                    "REP-" + repuesto.getId(),
                    repuesto.getNombre(),
                    repuesto.getExistencias(),
                    repuesto.getPrecio(),
                    0);
            detalles.add(detalleRepuesto);
        }
    }

    /**
     * Registra el pago de la factura
     * 
     * @param metodoPago Método utilizado para el pago
     */
    public void registrarPago(String metodoPago) {
        this.pagada = true;
        this.fechaPago = new Date();
        this.metodoPago = metodoPago;

        // También marcar la orden como pagada
        this.ordenTrabajo.setPagado(true);
    }

    /**
     * Calcula el subtotal (suma de todos los detalles sin descuentos)
     * 
     * @return Valor del subtotal
     */
    public double calcularSubtotal() {
        double subtotal = 0;
        for (DetalleFactura detalle : detalles) {
            subtotal += detalle.calcularTotal();
        }
        return subtotal;
    }

    /**
     * Calcula el descuento total aplicado
     * 
     * @return Valor del descuento
     */
    public double calcularDescuento() {
        // Si es cliente oro, aplicar 10% de descuento
        if (ordenTrabajo.getCliente().getTipoCliente().equals("oro")) {
            return calcularSubtotal() * 0.10;
        }
        return 0;
    }

    /**
     * Calcula el total a pagar (subtotal - descuento)
     * 
     * @return Valor total a pagar
     */
    public double calcularTotal() {
        return calcularSubtotal() - calcularDescuento();
    }

    // Getters y setters

    public int getNumero() {
        return numero;
    }

    public OrdenTrabajo getOrdenTrabajo() {
        return ordenTrabajo;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public String getFechaEmisionFormateada() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(fechaEmision);
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public String getFechaPagoFormateada() {
        if (fechaPago == null)
            return "No pagada";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(fechaPago);
    }

    public boolean isPagada() {
        return pagada;
    }

    public String getMetodoPago() {
        return metodoPago != null ? metodoPago : "No registrado";
    }

    public Vector<DetalleFactura> getDetalles() {
        return detalles;
    }

    /**
     * Devuelve información resumida de la factura
     */
    @Override
    public String toString() {
        return "Factura #" + numero + " - Orden #" + ordenTrabajo.getNumero() +
                " - Cliente: " + ordenTrabajo.getCliente().getNombreCompleto() +
                " - Total: Q" + String.format("%.2f", calcularTotal()) +
                " - Estado: " + (pagada ? "Pagada" : "Pendiente");
    }

    /**
     * Clase interna para representar una línea de detalle en la factura
     */
    public static class DetalleFactura implements Serializable {
        private static final long serialVersionUID = 1L;

        private String codigo;
        private String descripcion;
        private int cantidad;
        private double precioUnitario;
        private double descuento;

        public DetalleFactura(String codigo, String descripcion, int cantidad,
                double precioUnitario, double descuento) {
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.descuento = descuento;
        }

        public double calcularTotal() {
            return (cantidad * precioUnitario) - descuento;
        }

        // Getters
        public String getCodigo() {
            return codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public int getCantidad() {
            return cantidad;
        }

        public double getPrecioUnitario() {
            return precioUnitario;
        }

        public double getDescuento() {
            return descuento;
        }
    }

    public void actualizarTotal() {
        double total = 0;
        for (DetalleFactura detalle : detalles) {
            total += detalle.calcularTotal();
        }
        ordenTrabajo.setTotal(total);
    }

    public void setTotal(double total) {
        ordenTrabajo.setTotal(total);
    }

    public void setPagada(boolean b) {
        // Funcion para cambiar el estado de la factura a pagada
        this.pagada = b;
    }

    public void setNumeroFactura(int nuevoNumeroFactura) {
        // Cambia el número de la factura
        this.numero = nuevoNumeroFactura;
    }

    public Persona getCliente() {
        // Devuelve el cliente asociado a la factura
        return ordenTrabajo.getCliente();
    }

    public Object getEstado() {
        // Devuelve el estado de la factura
        return pagada ? "Pagada" : "Pendiente";
    }

    public Object getTotal() {
        // Devuelve el total de la factura
        return calcularTotal();
    }

    public void setDescuento(double descuento) {
        // Cambia el descuento de la factura
        for (DetalleFactura detalle : detalles) {
            detalle.descuento = descuento;
        }
    }

    public void setFecha(Date date) {
        // Cambia la fecha de la factura
        this.fechaEmision = date;
    }

}
