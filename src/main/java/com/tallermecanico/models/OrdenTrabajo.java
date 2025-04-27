package com.tallermecanico.models;

import com.tallermecanico.controllers.DataController;
import com.tallermecanico.controllers.OrdenTrabajoController; // Import OrdenTrabajoController
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Mecanico;
import com.tallermecanico.utils.GestorBitacora;

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
    private Date fechaCompletado;

    private boolean enProcesoTiempo = false;
    private long tiempoInicio = 0;
    private long tiempoObjetivo = 0;

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

    /**
     * Constructor con parámetros adicionales
     */
    public OrdenTrabajo(int numero, Cliente cliente, Automovil automovil, Servicio servicio) {
        this.numero = numero;
        this.cliente = cliente;
        this.automovil = automovil;
        this.servicio = servicio;
        this.estado = "espera"; // Estado inicial de la orden
        this.pagado = false; // Inicialmente no está pagada
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
     * Establece la fecha de completado de la orden
     * 
     * @param fechaCompletado La fecha en que se completó la orden
     */
    public void setFechaCompletado(Date fechaCompletado) {
        this.fechaCompletado = fechaCompletado;
    }

    /**
     * Obtiene la fecha de completado de la orden
     * 
     * @return Fecha de completado
     */
    public Date getFechaCompletado() {
        return fechaCompletado;
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
            factura = new Factura(numero, this);
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

    /**
     * Cambia el estado de la orden de trabajo, validando los tiempos de los
     * estados.
     * 
     * @param nuevoEstado El nuevo estado de la orden ("espera", "en_servicio",
     *                    "listo").
     * @return true si el cambio de estado es válido, false en caso contrario.
     */
    public boolean cambiarEstado(String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.isEmpty()) {
            return false;
        }

        switch (nuevoEstado) {
            case "en_servicio":
                if (!"espera".equals(this.estado)) {
                    return false; // Solo se puede pasar a "en_servicio" desde "espera"
                }
                break;

            case "listo":
                if (!"en_servicio".equals(this.estado)) {
                    return false; // Solo se puede pasar a "listo" desde "en_servicio"
                }
                this.fechaCompletado = new Date(); // Registrar la fecha de completado
                break;

            default:
                return false; // Estado no válido
        }

        this.estado = nuevoEstado;
        return true;
    }

    /**
     * Cambia el estado de una orden de trabajo.
     * 
     * @param numeroOrden El número de la orden.
     * @param nuevoEstado El nuevo estado de la orden ("espera", "en_servicio",
     *                    "listo").
     * @return true si el cambio de estado es válido, false en caso contrario.
     */
    public static boolean cambiarEstadoOrden(int numeroOrden, String nuevoEstado) {
        OrdenTrabajo orden = OrdenTrabajoController.buscarOrdenPorNumero(numeroOrden); // Usar OrdenTrabajoController

        if (orden == null) {
            GestorBitacora.registrarEvento("Sistema", "Cambio de estado", false,
                    "No se encontró la orden #" + numeroOrden);
            return false;
        }

        if (!orden.cambiarEstado(nuevoEstado)) {
            GestorBitacora.registrarEvento("Sistema", "Cambio de estado", false,
                    "Cambio de estado inválido para la orden #" + numeroOrden);
            return false;
        }

        GestorBitacora.registrarEvento("Sistema", "Cambio de estado", true,
                "Estado de la orden #" + numeroOrden + " cambiado a " + nuevoEstado);
        DataController.guardarDatos();
        return true;
    }

    @Override
    public String toString() {
        return "Orden #" + numero + " - " +
                (automovil != null ? automovil.getPlaca() : "Sin auto") + " - " +
                (servicio != null ? servicio.getNombre() : "Sin servicio") + " - " +
                estado;
    }

    public void setTotal(double total) {
        if (factura != null) {
            factura.setTotal(total);
        }
    }

    public Object getId() {
        return numero;
    }

    public boolean isEnProcesoTiempo() {
        return enProcesoTiempo;
    }

    public void setEnProcesoTiempo(boolean enProcesoTiempo) {
        this.enProcesoTiempo = enProcesoTiempo;
    }

    public long getTiempoInicio() {
        return tiempoInicio;
    }

    public void setTiempoInicio(long tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }

    public long getTiempoObjetivo() {
        return tiempoObjetivo;
    }

    public void setTiempoObjetivo(long tiempoObjetivo) {
        this.tiempoObjetivo = tiempoObjetivo;
    }

    /**
     * Obtiene el texto del tiempo restante
     * 
     * @return Texto del tiempo restante
     */
    public String getTiempoRestanteTexto() {
        if (!enProcesoTiempo || tiempoObjetivo == 0) {
            return "Pendiente";
        }

        long tiempoActual = System.currentTimeMillis();
        long tiempoRestante = tiempoObjetivo - tiempoActual;

        if (tiempoRestante <= 0) {
            return "Completando...";
        }

        // Convertir a segundos
        tiempoRestante = tiempoRestante / 1000;
        return tiempoRestante + " seg";
    }

    public void setFechaFinalizacion(Date date) {
        this.fechaCompletado = date;
    }
}
