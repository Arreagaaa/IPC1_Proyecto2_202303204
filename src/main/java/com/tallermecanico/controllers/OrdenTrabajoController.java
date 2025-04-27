package com.tallermecanico.controllers;

import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.Factura;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Empleado;
import com.tallermecanico.models.personas.Mecanico;
import com.tallermecanico.utils.GestorBitacora;
import com.tallermecanico.utils.Serializador;
import java.util.Date;
import java.util.Vector;

/**
 * Controlador para la gestión de órdenes de trabajo
 */
public class OrdenTrabajoController {

    // Estados estandarizados para órdenes
    public static final String ESTADO_ESPERA = "ESPERA";
    public static final String ESTADO_PROCESO = "PROCESO";
    public static final String ESTADO_FINALIZADO = "FINALIZADO";
    public static final String ESTADO_FACTURADO = "FACTURADO";

    /**
     * Crea una nueva orden de trabajo y la agrega al sistema
     * 
     * @param cliente  Cliente solicitante
     * @param auto     Automóvil a reparar
     * @param servicio Servicio a realizar
     * @return La nueva orden de trabajo creada o null si hay error
     */
    public static OrdenTrabajo crearOrden(Cliente cliente, Automovil auto, Servicio servicio, Empleado mecanico) {
        try {
            // Validaciones básicas
            if (cliente == null || auto == null || servicio == null) {
                GestorBitacora.registrarEvento("Sistema", "Crear Orden", false,
                        "Parámetros inválidos para crear orden de trabajo");
                return null;
            }

            // Crear la orden de trabajo
            int numeroOrden = DataController.getNuevoNumeroOrden();
            OrdenTrabajo orden = new OrdenTrabajo(numeroOrden, cliente, auto, servicio);
            orden.setEstado(ESTADO_ESPERA);
            orden.setFecha(new Date());

            // Asignar mecánico si viene en los parámetros
            if (mecanico != null) {
                orden.setMecanico((Mecanico) mecanico);
                orden.setEstado(ESTADO_PROCESO);
            }

            // Agregar al sistema
            DataController.getOrdenesTrabajo().add(orden);

            // Guardar cambios
            Serializador.guardarDatos(null);

            GestorBitacora.registrarEvento("Sistema", "Crear Orden", true,
                    "Orden #" + numeroOrden + " creada para cliente: " + cliente.getNombreCompleto() +
                            ", automóvil: " + auto.getPlaca() + ", servicio: " + servicio.getNombre());

            return orden;
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Crear Orden", false,
                    "Error al crear orden: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Asigna un mecánico a una orden de trabajo
     * 
     * @param orden    Orden a asignar
     * @param mecanico Mecánico a asignar
     * @return true si se asignó correctamente
     */
    public static boolean asignarMecanico(OrdenTrabajo orden, Empleado mecanico) {
        try {
            // Validaciones
            if (orden == null || mecanico == null) {
                return false;
            }

            // Verificar que el empleado sea un mecánico
            if (!mecanico.getTipo().equalsIgnoreCase("mecanico")) {
                GestorBitacora.registrarEvento("Sistema", "Asignar Mecánico", false,
                        "El empleado no es un mecánico");
                return false;
            }

            // Verificar que el mecánico esté disponible
            if (!mecanicoDisponible(mecanico)) {
                GestorBitacora.registrarEvento("Sistema", "Asignar Mecánico", false,
                        "El mecánico " + mecanico.getNombreCompleto() + " ya está ocupado");
                return false;
            }

            // Asignar mecánico
            orden.setMecanico((Mecanico) mecanico);
            orden.setEstado(ESTADO_PROCESO);

            // Si el mecánico tiene un método para marcar como no disponible
            if (mecanico instanceof Mecanico) {
                ((Mecanico) mecanico).setDisponible(false);
            } else {
                // Si solo es Empleado
                mecanico.setDisponible(false);
            }

            // Guardar cambios
            Serializador.guardarDatos(null);

            GestorBitacora.registrarEvento("Sistema", "Asignar Mecánico", true,
                    "Orden #" + orden.getId() + " asignada al mecánico " + mecanico.getNombreCompleto());

            return true;
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Asignar Mecánico", false,
                    "Error al asignar mecánico: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finaliza una orden de trabajo (servicio completado)
     * 
     * @param orden Orden a finalizar
     * @return true si se finalizó correctamente
     */
    public static boolean finalizarOrden(OrdenTrabajo orden) {
        try {
            if (orden == null) {
                return false;
            }

            // Cambiar estado a FINALIZADO
            orden.setEstado(ESTADO_FINALIZADO);
            orden.setEnProcesoTiempo(false); // Reiniciar para el tiempo de "listo"
            orden.setFechaFinalizacion(new Date());

            // Incrementar contador de uso del servicio
            orden.getServicio().incrementarUso();

            // Incrementar contador de uso de los repuestos
            for (Repuesto repuesto : orden.getServicio().getRepuestos()) {
                repuesto.incrementarUso();
            }

            // Liberar al mecánico
            if (orden.getMecanico() != null) {
                orden.getMecanico().setDisponible(true);
            }

            // Verificar promoción de cliente
            ClienteController.verificarPromocionClienteOro(orden.getCliente());

            // Guardar cambios
            Serializador.guardarDatos(null);

            GestorBitacora.registrarEvento("Sistema", "Finalizar Orden", true,
                    "Orden #" + orden.getId() + " finalizada");

            return true;
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Finalizar Orden", false,
                    "Error al finalizar orden: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Genera una factura para una orden finalizada
     * 
     * @param orden Orden para la que se generará la factura
     * @return true si se generó correctamente la factura
     */
    public static boolean generarFactura(OrdenTrabajo orden) {
        try {
            if (orden == null || !orden.getEstado().equals(ESTADO_FINALIZADO)) {
                return false;
            }

            // Crear factura
            Factura factura = new Factura((int) FacturaController.obtenerSiguienteNumero(), orden);
            factura.setFecha(new Date());

            // Calcular total (con descuento si es cliente oro)
            double total = orden.getServicio().getPrecioTotal();
            double descuento = 0;

            if (orden.getCliente().getTipoCliente().equalsIgnoreCase("oro")) {
                // Descuento del 5% para clientes oro
                descuento = total * 0.05;
            }

            factura.setDescuento(descuento);
            factura.setTotal(total - descuento);
            factura.setPagada(false); // Inicialmente no pagada

            // Cambiar estado de la orden a FACTURADO
            orden.setEstado(ESTADO_FACTURADO);

            // Agregar factura al sistema
            FacturaController.agregarFactura(factura);

            // Guardar cambios
            Serializador.guardarDatos(null);

            GestorBitacora.registrarEvento("Sistema", "Generar Factura", true,
                    "Factura #" + factura.getNumero() + " generada para orden #" + orden.getId());

            return true;
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generar Factura", false,
                    "Error al generar factura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si un mecánico está disponible para nuevas asignaciones
     * 
     * @param mecanico Mecánico a verificar
     * @return true si está disponible
     */
    public static boolean mecanicoDisponible(Empleado mecanico) {
        // Verificar si el mecánico ya tiene órdenes en estado PROCESO
        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if (orden.getEstado().equals(ESTADO_PROCESO) &&
                    orden.getMecanico() != null &&
                    orden.getMecanico().getIdentificador().equals(mecanico.getIdentificador())) {
                return false; // El mecánico está ocupado con una orden en proceso
            }
        }
        return true; // El mecánico está disponible
    }

    /**
     * Asigna el primer mecánico disponible a una orden
     * 
     * @param orden Orden a asignar
     * @return true si se encontró y asignó un mecánico disponible
     */
    public static boolean asignarPrimerMecanicoDisponible(OrdenTrabajo orden) {
        // Obtener mecánicos
        Vector<Empleado> empleados = DataController.getEmpleados();

        for (Empleado empleado : empleados) {
            // Verificar que sea mecánico y esté disponible
            if (empleado.getTipo().equalsIgnoreCase("mecanico") && mecanicoDisponible(empleado)) {
                return asignarMecanico(orden, empleado);
            }
        }

        return false; // No hay mecánicos disponibles
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
    public static Vector<OrdenTrabajo> obtenerOrdenesPorCliente(Cliente cliente) {
        Vector<OrdenTrabajo> ordenes = new Vector<>();

        if (cliente == null)
            return ordenes;

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if (orden.getCliente() != null &&
                    orden.getCliente().getIdentificador().equals(cliente.getIdentificador())) {
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
            if (orden.getEstado().equalsIgnoreCase(estado)) {
                ordenes.add(orden);
            }
        }

        return ordenes;
    }

    /**
     * Obtiene órdenes asignadas a un mecánico en un estado específico
     */
    public static Vector<OrdenTrabajo> obtenerOrdenesPorMecanicoEstado(Empleado mecanico, String estado) {
        Vector<OrdenTrabajo> ordenes = new Vector<>();

        if (mecanico == null)
            return ordenes;

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if (orden.getMecanico() != null &&
                    orden.getMecanico().getIdentificador().equals(mecanico.getIdentificador()) &&
                    orden.getEstado().equalsIgnoreCase(estado)) {
                ordenes.add(orden);
            }
        }

        return ordenes;
    }

    /**
     * Busca una orden por su ID
     */
    public static OrdenTrabajo obtenerOrdenPorId(int id) {
        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if (orden.getId() instanceof Integer && (Integer) orden.getId() == id) {
                return orden;
            }
        }
        return null;
    }

    public static OrdenTrabajo buscarOrdenPorNumero(int numeroOrden) {
        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if ((int) orden.getId() == numeroOrden) {
                return orden;
            }
        }
        return null;
    }
}