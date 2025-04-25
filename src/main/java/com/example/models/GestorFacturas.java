package com.example.models;

import com.example.models.personas.Cliente;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

/**
 * Gestor de facturas del sistema
 * Implementa el patrón Singleton
 */
public class GestorFacturas implements Serializable {

    private static final long serialVersionUID = 1L;

    // Instancia única (patrón Singleton)
    private static GestorFacturas instancia;

    // Colección de facturas
    private Vector<Factura> facturas;

    // Contador para números de factura
    private int contadorFacturas;

    /**
     * Constructor privado (Singleton)
     */
    private GestorFacturas() {
        this.facturas = new Vector<>();
        this.contadorFacturas = 1;
    }

    /**
     * Obtiene la instancia única del gestor
     */
    public static GestorFacturas getInstancia() {
        if (instancia == null) {
            instancia = new GestorFacturas();
        }
        return instancia;
    }

    /**
     * Establece la instancia del gestor (usado en deserialización)
     */
    public static void setInstancia(GestorFacturas nuevaInstancia) {
        instancia = nuevaInstancia;
    }

    /**
     * Crea una nueva factura a partir de una orden de trabajo
     */
    public Factura crearFactura(OrdenTrabajo orden) {
        // Verificar que la orden esté lista
        if (!orden.getEstado().equals("listo")) {
            return null;
        }

        // Verificar que no exista ya una factura para esta orden
        for (int i = 0; i < facturas.size(); i++) {
            Factura factura = facturas.elementAt(i);
            if (factura.getOrdenTrabajo().getNumeroOrden() == orden.getNumeroOrden()) {
                return factura; // Ya existe
            }
        }

        // Calcular subtotal (precio del servicio)
        double subtotal = orden.getServicio().getPrecio();

        // Aplicar descuento si es cliente oro (10%)
        if (orden.getCliente().getTipoCliente().equals("oro")) {
            subtotal = subtotal * 0.9; // 10% de descuento
        }

        // Calcular IVA (12%)
        double iva = subtotal * 0.12;

        // Calcular total
        double total = subtotal + iva;

        // Crear la factura
        Factura nuevaFactura = new Factura(
                contadorFacturas++,
                orden,
                orden.getCliente(),
                new Date(), // Fecha emisión
                subtotal,
                iva,
                total,
                "pendiente", // Estado inicial
                null // Fecha de pago (se asigna al pagar)
        );

        // Agregar a la colección
        facturas.add(nuevaFactura);

        return nuevaFactura;
    }

    /**
     * Obtiene todas las facturas
     */
    public Vector<Factura> getFacturas() {
        return facturas;
    }

    /**
     * Obtiene las facturas de un cliente específico
     */
    public Vector<Factura> getFacturasPorCliente(Cliente cliente) {
        Vector<Factura> facturaCliente = new Vector<>();

        for (int i = 0; i < facturas.size(); i++) {
            Factura factura = facturas.elementAt(i);
            if (factura.getCliente().getIdentificador().equals(cliente.getIdentificador())) {
                facturaCliente.add(factura);
            }
        }

        return facturaCliente;
    }

    /**
     * Busca una factura por su número
     */
    public Factura buscarFacturaPorNumero(int numeroFactura) {
        for (int i = 0; i < facturas.size(); i++) {
            Factura factura = facturas.elementAt(i);
            if (factura.getNumeroFactura() == numeroFactura) {
                return factura;
            }
        }
        return null;
    }

    /**
     * Marca una factura como pagada
     */
    public boolean pagarFactura(Factura factura) {
        if (factura != null && "pendiente".equals(factura.getEstado())) {
            factura.setEstado("pagada");
            factura.setFechaPago(new Date());

            // Importante: cambiar el estado del automóvil a disponible
            OrdenTrabajo orden = factura.getOrdenTrabajo();
            if (orden != null && orden.getAutomovil() != null) {
                orden.getAutomovil().setEstadoActual(Automovil.ESTADO_DISPONIBLE);
            }

            return true;
        }
        return false;
    }

    /**
     * Marca una factura como pagada por su número
     */
    public boolean pagarFactura(int numeroFactura) {
        Factura factura = buscarFacturaPorNumero(numeroFactura);
        return pagarFactura(factura);
    }

    /**
     * Obtiene las facturas pendientes
     */
    public Vector<Factura> getFacturasPendientes() {
        Vector<Factura> pendientes = new Vector<>();

        for (int i = 0; i < facturas.size(); i++) {
            Factura factura = facturas.elementAt(i);
            if (factura.getEstado().equals("pendiente")) {
                pendientes.add(factura);
            }
        }

        return pendientes;
    }

    /**
     * Obtiene las facturas pagadas
     */
    public Vector<Factura> getFacturasPagadas() {
        Vector<Factura> pagadas = new Vector<>();

        for (int i = 0; i < facturas.size(); i++) {
            Factura factura = facturas.elementAt(i);
            if (factura.getEstado().equals("pagada")) {
                pagadas.add(factura);
            }
        }

        return pagadas;
    }

    /**
     * Agrega una factura al sistema
     */
    public void agregarFactura(Factura factura) {
        // Si no tiene número, asignar el siguiente
        if (factura.getNumeroFactura() <= 0) {
            factura.setNumeroFactura(contadorFacturas++);
        } else if (factura.getNumeroFactura() >= contadorFacturas) {
            // Actualizar contador si es necesario
            contadorFacturas = factura.getNumeroFactura() + 1;
        }

        facturas.add(factura);
    }

    /**
     * Calcula el total de ventas en un período
     */
    public double calcularTotalVentas(Date fechaInicio, Date fechaFin) {
        double total = 0;

        for (int i = 0; i < facturas.size(); i++) {
            Factura factura = facturas.elementAt(i);
            Date fechaFactura = factura.getFecha();

            if (fechaFactura.after(fechaInicio) && fechaFactura.before(fechaFin)) {
                total += factura.getTotal();
            }
        }

        return total;
    }

    /**
     * Obtiene el siguiente número de factura disponible
     */
    public int getNextNumeroFactura() {
        return contadorFacturas;
    }
}