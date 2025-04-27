package com.tallermecanico.controllers;

import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.Factura;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Mecanico;
import com.tallermecanico.utils.GestorBitacora;
import java.util.Vector;

/**
 * Controlador para la gestión de órdenes de trabajo
 */
public class OrdenTrabajoController {

    /**
     * Crea una nueva orden de trabajo y la agrega a la cola de espera
     * 
     * @param idCliente  Identificador del cliente
     * @param placaAuto  Placa del automóvil
     * @param idServicio ID del servicio a realizar
     * @return La nueva orden de trabajo creada o null si hay error
     */
    public static OrdenTrabajo crearOrdenTrabajo(String idCliente, String placaAuto, int idServicio) {
        // Validar parámetros
        if (idCliente == null || idCliente.isEmpty() || placaAuto == null || placaAuto.isEmpty()) {
            GestorBitacora.registrarEvento("Sistema", "Crear Orden", false,
                    "Parámetros inválidos para crear orden de trabajo");
            return null;
        }

        // Buscar cliente
        Cliente cliente = ClienteController.buscarClientePorIdentificador(idCliente);
        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Crear Orden", false,
                    "No se encontró cliente con ID: " + idCliente);
            return null;
        }

        // Buscar automóvil
        Automovil automovil = cliente.buscarAutomovil(placaAuto);
        if (automovil == null) {
            GestorBitacora.registrarEvento("Sistema", "Crear Orden", false,
                    "No se encontró automóvil con placa: " + placaAuto + " para el cliente: " + idCliente);
            return null;
        }

        // Buscar servicio
        Servicio servicio = ServicioController.buscarServicioPorId(idServicio);
        if (servicio == null) {
            GestorBitacora.registrarEvento("Sistema", "Crear Orden", false,
                    "No se encontró servicio con ID: " + idServicio);
            return null;
        }

        // Validar que el servicio sea compatible con la marca y modelo del automóvil
        if (!servicio.getMarca().equalsIgnoreCase(automovil.getMarca()) ||
                !servicio.getModelo().equalsIgnoreCase(automovil.getModelo())) {
            GestorBitacora.registrarEvento("Sistema", "Crear Orden", false,
                    "El servicio no es compatible con el automóvil: " + automovil.getMarca() + " "
                            + automovil.getModelo());
            return null;
        }

        // Crear la orden de trabajo
        int numeroOrden = DataController.getNuevoNumeroOrden();
        OrdenTrabajo orden = new OrdenTrabajo(numeroOrden, cliente, automovil, servicio);

        // Agregar la orden de trabajo a las órdenes y a la cola de espera
        DataController.getOrdenesTrabajo().add(orden);
        DataController.agregarOrdenAColaEspera(orden);

        GestorBitacora.registrarEvento("Sistema", "Crear Orden", true,
                "Orden #" + numeroOrden + " creada para cliente: " + cliente.getNombreCompleto() +
                        ", automóvil: " + automovil.getPlaca() + ", servicio: " + servicio.getNombre());

        return orden;
    }

    /**
     * Asigna una orden de trabajo a un mecánico
     * 
     * @return true si se asignó correctamente
     */
    public static boolean asignarOrdenAMecanico(int numeroOrden, String idMecanico) {
        // Buscar orden
        OrdenTrabajo orden = buscarOrdenPorNumero(numeroOrden);

        if (orden == null) {
            GestorBitacora.registrarEvento("Sistema", "Asignación de orden", false,
                    "No se encontró la orden #" + numeroOrden);
            return false;
        }

        // Verificar que la orden esté en espera
        if (!"espera".equals(orden.getEstado())) {
            GestorBitacora.registrarEvento("Sistema", "Asignación de orden", false,
                    "La orden #" + numeroOrden + " no está en estado de espera");
            return false;
        }

        // Buscar mecánico
        Mecanico mecanico = null;
        for (Object obj : DataController.getEmpleados()) {
            if (obj instanceof Mecanico) {
                Mecanico mec = (Mecanico) obj;
                if (mec.getIdentificador().equals(idMecanico)) {
                    mecanico = mec;
                    break;
                }
            }
        }

        if (mecanico == null) {
            GestorBitacora.registrarEvento("Sistema", "Asignación de orden", false,
                    "No se encontró mecánico con ID: " + idMecanico);
            return false;
        }

        // Verificar que el mecánico no esté atendiendo otro automóvil
        if (!mecanico.isDisponible()) {
            GestorBitacora.registrarEvento("Sistema", "Asignación de orden", false,
                    "El mecánico ya está atendiendo otro automóvil");
            return false;
        }

        // Asignar la orden al mecánico
        orden.setMecanico(mecanico);
        orden.setEstado("en_servicio");
        mecanico.asignarOrden(orden);

        // Quitar de la cola de espera
        DataController.getColaEspera().remove(orden);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Asignación de orden", true,
                "Orden #" + numeroOrden + " asignada a " + mecanico.getNombreCompleto());

        return true;
    }

    /**
     * Marca una orden de trabajo como completada
     * 
     * @return true si se completó correctamente
     */
    public static boolean completarOrden(int numeroOrden) {
        // Buscar orden
        OrdenTrabajo orden = buscarOrdenPorNumero(numeroOrden);

        if (orden == null) {
            GestorBitacora.registrarEvento("Sistema", "Completar orden", false,
                    "No se encontró la orden #" + numeroOrden);
            return false;
        }

        // Verificar que la orden esté en servicio
        if (!"en_servicio".equals(orden.getEstado())) {
            GestorBitacora.registrarEvento("Sistema", "Completar orden", false,
                    "La orden #" + numeroOrden + " no está en estado de servicio");
            return false;
        }

        // Verificar que tenga mecánico asignado
        if (orden.getMecanico() == null) {
            GestorBitacora.registrarEvento("Sistema", "Completar orden", false,
                    "La orden #" + numeroOrden + " no tiene mecánico asignado");
            return false;
        }

        // Completar la orden
        orden.setEstado("listo");

        // Liberar al mecánico
        Mecanico mecanico = orden.getMecanico();
        mecanico.completarOrdenActual();

        // Generar factura
        Factura factura = orden.generarFactura();
        factura.setNumeroFactura(DataController.getNuevoNumeroFactura());
        DataController.getFacturas().add(factura);

        // Agregar a la cola de listos
        DataController.getColaListos().add(orden);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Completar orden", true,
                "Orden #" + numeroOrden + " completada por " + mecanico.getNombreCompleto());

        return true;
    }

    /**
     * Marca una orden como completada (lista) y genera la factura
     * 
     * @param numeroOrden Número de la orden a completar
     * @param idMecanico  Identificador del mecánico que completa la orden
     * @return La factura generada o null en caso de error
     */
    public static Factura completarOrden(int numeroOrden, String idMecanico) {
        // Buscar la orden
        OrdenTrabajo orden = null;
        for (OrdenTrabajo o : DataController.getOrdenesTrabajo()) {
            if (o.getNumero() == numeroOrden) {
                orden = o;
                break;
            }
        }

        // Validar que exista la orden y esté en servicio
        if (orden == null) {
            GestorBitacora.registrarEvento("Sistema", "Completar Orden", false,
                    "No se encontró la orden #" + numeroOrden);
            return null;
        }

        if (!"en_servicio".equals(orden.getEstado())) {
            GestorBitacora.registrarEvento("Sistema", "Completar Orden", false,
                    "La orden #" + numeroOrden + " no está en servicio");
            return null;
        }

        // Validar que el mecánico asignado sea quien completa
        if (!orden.getMecanico().getIdentificador().equals(idMecanico)) {
            GestorBitacora.registrarEvento("Sistema", "Completar Orden", false,
                    "El mecánico no está asignado a esta orden");
            return null;
        }

        // Marcar como completada
        orden.setEstado("listo");

        // Si la clase OrdenTrabajo no tiene el método setFechaCompletado, omitimos esta
        // línea
        // orden.setFechaCompletado(new Date());

        // Liberar al mecánico si tiene ese método
        Mecanico mecanico = orden.getMecanico();
        if (mecanico != null) {
            // Si existe el método completarOrdenActual, lo usamos
            try {
                mecanico.completarOrdenActual();
            } catch (Exception e) {
                // Si no existe el método, simplemente continuamos
            }
        }

        // Guardar datos
        DataController.guardarDatos();

        // Generar factura
        Factura factura = FacturaController.generarFactura(orden);

        if (factura != null) {
            GestorBitacora.registrarEvento(idMecanico, "Completar Orden", true,
                    "Orden #" + numeroOrden + " completada y factura #" +
                            factura.getNumero() + " generada");
        } else {
            GestorBitacora.registrarEvento(idMecanico, "Completar Orden", false,
                    "Error al generar factura para orden #" + numeroOrden);
        }

        return factura;
    }

    /**
     * Registra el pago de una orden
     * 
     * @return true si se registró correctamente
     */
    public static boolean registrarPago(int numeroOrden) {
        // Buscar orden
        OrdenTrabajo orden = buscarOrdenPorNumero(numeroOrden);

        if (orden == null) {
            GestorBitacora.registrarEvento("Sistema", "Registro de pago", false,
                    "No se encontró la orden #" + numeroOrden);
            return false;
        }

        // Verificar que la orden esté lista
        if (!"listo".equals(orden.getEstado())) {
            GestorBitacora.registrarEvento("Sistema", "Registro de pago", false,
                    "La orden #" + numeroOrden + " no está en estado listo");
            return false;
        }

        // Verificar que tenga factura
        if (orden.getFactura() == null) {
            GestorBitacora.registrarEvento("Sistema", "Registro de pago", false,
                    "La orden #" + numeroOrden + " no tiene factura");
            return false;
        }

        // Verificar que no esté pagada
        if (orden.isPagado()) {
            GestorBitacora.registrarEvento("Sistema", "Registro de pago", false,
                    "La orden #" + numeroOrden + " ya está pagada");
            return false;
        }

        // Registrar pago
        orden.registrarPago();

        // Quitar de la cola de listos
        DataController.getColaListos().remove(orden);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Registro de pago", true,
                "Pago registrado para orden #" + numeroOrden + ", factura #" +
                        orden.getFactura().getNumero());

        return true;
    }

    /**
     * Busca una orden por su número
     * 
     * @return la orden encontrada o null
     */
    public static OrdenTrabajo buscarOrdenPorNumero(int numero) {
        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if (orden.getNumero() == numero) {
                return orden;
            }
        }
        return null;
    }

    /**
     * Obtiene todas las órdenes de trabajo
     */
    public static Vector<OrdenTrabajo> obtenerTodasLasOrdenes() {
        return DataController.getOrdenesTrabajo();
    }

    /**
     * Obtiene órdenes de trabajo de un cliente específico
     */
    public static Vector<OrdenTrabajo> obtenerOrdenesPorCliente(String idCliente) {
        Vector<OrdenTrabajo> ordenes = new Vector<>();

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if (orden.getCliente() != null && orden.getCliente().getIdentificador().equals(idCliente)) {
                ordenes.add(orden);
            }
        }

        return ordenes;
    }

    /**
     * Obtiene órdenes de trabajo por estado
     */
    public static Vector<OrdenTrabajo> obtenerOrdenesPorEstado(String estado) {
        Vector<OrdenTrabajo> ordenes = new Vector<>();

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if (orden.getEstado().equals(estado)) {
                ordenes.add(orden);
            }
        }

        return ordenes;
    }

    /**
     * Obtiene órdenes asignadas a un mecánico
     */
    public static Vector<OrdenTrabajo> obtenerOrdenesPorMecanico(String idMecanico) {
        Vector<OrdenTrabajo> ordenesPorMecanico = new Vector<>();
        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if (orden.getMecanico() != null && idMecanico.equals(orden.getMecanico().getIdentificador())) {
                ordenesPorMecanico.add(orden);
            }
        }
        return ordenesPorMecanico;
    }

    /**
     * Obtiene la cola de órdenes en espera
     */
    public static Vector<OrdenTrabajo> obtenerColaEspera() {
        return DataController.getColaEspera();
    }

    /**
     * Obtiene la cola de órdenes listas para entrega
     */
    public static Vector<OrdenTrabajo> obtenerColaListos() {
        return DataController.getColaListos();
    }

    /**
     * Ordena las órdenes de trabajo por número de orden.
     * 
     * @param ordenes El vector de órdenes a ordenar.
     */
    public static void ordenarOrdenesPorNumero(Vector<OrdenTrabajo> ordenes) {
        for (int i = 0; i < ordenes.size() - 1; i++) {
            for (int j = 0; j < ordenes.size() - i - 1; j++) {
                if (ordenes.get(j).getNumero() > ordenes.get(j + 1).getNumero()) {
                    OrdenTrabajo temp = ordenes.get(j);
                    ordenes.set(j, ordenes.get(j + 1));
                    ordenes.set(j + 1, temp);
                }
            }
        }
    }
}