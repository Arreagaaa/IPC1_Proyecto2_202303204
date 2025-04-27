package com.tallermecanico.controllers;

import com.tallermecanico.models.Factura;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.utils.GestorBitacora;

import java.util.Vector;

/**
 * Controlador para la gestión de facturas
 */
public class FacturaController {

    /**
     * Genera una nueva factura para una orden de trabajo
     * 
     * @param orden Orden de trabajo para la que se genera la factura
     * @return La factura generada
     */
    public static Factura generarFactura(OrdenTrabajo orden) {
        // Validar que la orden esté lista para facturar
        if (orden == null || !"listo".equals(orden.getEstado())) {
            GestorBitacora.registrarEvento("Sistema", "Generar Factura", false,
                    "No se puede generar factura, la orden no está lista");
            return null;
        }

        // Validar que no exista ya una factura para esta orden
        for (Factura factura : DataController.getFacturas()) {
            if (factura.getOrdenTrabajo().getNumero() == orden.getNumero()) {
                GestorBitacora.registrarEvento("Sistema", "Generar Factura", false,
                        "Ya existe una factura para la orden #" + orden.getNumero());
                return factura;
            }
        }

        // Generar número de factura
        int numeroFactura = DataController.getNuevoNumeroFactura();

        // Crear la factura
        Factura factura = new Factura(numeroFactura, orden);

        // Agregar a la lista de facturas
        DataController.getFacturas().add(factura);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Generar Factura", true,
                "Factura #" + numeroFactura + " generada para orden #" + orden.getNumero());

        return factura;
    }

    /**
     * Registra el pago de una factura
     * 
     * @param numeroFactura Número de la factura a pagar
     * @param metodoPago    Método utilizado para el pago
     * @return true si el pago fue exitoso
     */
    public static boolean registrarPago(int numeroFactura, String metodoPago) {
        // Buscar la factura
        Factura factura = null;
        for (Factura f : DataController.getFacturas()) {
            if (f.getNumero() == numeroFactura) {
                factura = f;
                break;
            }
        }

        // Validar que la factura exista y no esté pagada
        if (factura == null) {
            GestorBitacora.registrarEvento("Sistema", "Pago Factura", false,
                    "No se encontró la factura #" + numeroFactura);
            return false;
        }

        if (factura.isPagada()) {
            GestorBitacora.registrarEvento("Sistema", "Pago Factura", false,
                    "La factura #" + numeroFactura + " ya está pagada");
            return false;
        }

        // Registrar el pago
        factura.registrarPago(metodoPago);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Pago Factura", true,
                "Pago registrado para factura #" + numeroFactura +
                        ", método: " + metodoPago);

        return true;
    }

    /**
     * Obtiene todas las facturas pendientes de pago
     * 
     * @return Vector con las facturas pendientes
     */
    public static Vector<Factura> obtenerFacturasPendientes() {
        Vector<Factura> pendientes = new Vector<>();

        for (Factura factura : DataController.getFacturas()) {
            if (!factura.isPagada()) {
                pendientes.add(factura);
            }
        }

        return pendientes;
    }

    /**
     * Obtiene todas las facturas de un cliente específico
     * 
     * @param idCliente Identificador del cliente
     * @return Vector con las facturas del cliente
     */
    public static Vector<Factura> obtenerFacturasCliente(String idCliente) {
        Vector<Factura> facturasCliente = new Vector<>();

        for (Factura factura : DataController.getFacturas()) {
            if (factura.getOrdenTrabajo().getCliente().getIdentificador().equals(idCliente)) {
                facturasCliente.add(factura);
            }
        }

        return facturasCliente;
    }

    /**
     * Busca una factura por su número
     * 
     * @param numeroFactura Número de la factura
     * @return La factura encontrada o null
     */
    public static Factura buscarFacturaPorNumero(int numeroFactura) {
        for (Factura factura : DataController.getFacturas()) {
            if (factura.getNumero() == numeroFactura) {
                return factura;
            }
        }
        return null;
    }

    /**
     * Obtiene la factura asociada a una orden de trabajo
     * 
     * @param numeroOrden Número de la orden
     * @return La factura asociada o null
     */
    public static Factura buscarFacturaPorOrden(int numeroOrden) {
        for (Factura factura : DataController.getFacturas()) {
            if (factura.getOrdenTrabajo().getNumero() == numeroOrden) {
                return factura;
            }
        }
        return null;
    }
}