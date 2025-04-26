package com.tallermecanico.controllers;

import com.tallermecanico.models.Factura;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Empleado;
import com.tallermecanico.utils.GestorBitacora;
import com.tallermecanico.utils.Serializador;

import java.io.Serializable;
import java.util.Vector;

/**
 * Controlador central para gestionar los datos del sistema
 */
public class DataController {
    // Vectores para almacenar los datos del sistema
    private static Vector<Cliente> clientes = new Vector<>();
    private static Vector<Empleado> empleados = new Vector<>();
    private static Vector<Repuesto> repuestos = new Vector<>();
    private static Vector<Servicio> servicios = new Vector<>();
    private static Vector<OrdenTrabajo> ordenesTrabajo = new Vector<>();
    private static Vector<Factura> facturas = new Vector<>();

    // Colas para gestionar estados de órdenes
    private static Vector<OrdenTrabajo> colaEspera = new Vector<>();
    private static Vector<OrdenTrabajo> colaListos = new Vector<>();

    // Contadores para IDs autoincremental
    private static int ultimoIdRepuesto = 0;
    private static int ultimoIdServicio = 0;
    private static int ultimoNumeroOrden = 0;
    private static int ultimoNumeroFactura = 0;

    // Variables para controlar la serialización
    private static boolean datosInicializados = false;

    /**
     * Inicializa los datos del sistema, creando estructuras mínimas necesarias
     */
    public static void inicializarDatos() {
        // Evitar reinicializar si ya se cargaron los datos
        if (datosInicializados) {
            return;
        }

        // Intentar cargar desde archivo serializado
        if (!cargarDatos()) {
            // Si no hay datos previos, inicializar con valores por defecto
            inicializarDatosPorDefecto();
        }

        datosInicializados = true;
    }

    /**
     * Inicializa datos por defecto cuando no hay datos serializados previos
     */
    private static void inicializarDatosPorDefecto() {
        GestorBitacora.registrarEvento("Sistema", "Inicialización", true,
                "Inicializando sistema con datos por defecto");

        // Crear al menos un empleado administrador
        if (empleados.isEmpty()) {
            Empleado admin = new Empleado("admin", "Administrador", "Sistema", "admin", "admin", "admin");
            empleados.add(admin);
        }

        // Guardar los datos iniciales
        guardarDatos();
    }

    /**
     * Guarda todos los datos del sistema usando serialización
     * 
     * @return true si se guardaron correctamente
     */
    public static boolean guardarDatos() {
        // Crear un objeto serializable con todos los datos del sistema
        DatosSistema datos = new DatosSistema();

        Object[] objetosParaGuardar = { datos };
        return Serializador.guardarDatos(objetosParaGuardar);
    }

    /**
     * Carga los datos del sistema desde un archivo serializado
     * 
     * @return true si se cargaron correctamente
     */
    private static boolean cargarDatos() {
        Object[] datosRecuperados = Serializador.cargarDatos();

        if (datosRecuperados == null || datosRecuperados.length == 0) {
            return false;
        }

        try {
            DatosSistema datos = (DatosSistema) datosRecuperados[0];

            // Restaurar los datos en las estructuras del controlador
            clientes = datos.getClientes();
            empleados = datos.getEmpleados();
            repuestos = datos.getRepuestos();
            servicios = datos.getServicios();
            ordenesTrabajo = datos.getOrdenesTrabajo();
            facturas = datos.getFacturas();
            colaEspera = datos.getColaEspera();
            colaListos = datos.getColaListos();

            // Restaurar contadores
            ultimoIdRepuesto = datos.getUltimoIdRepuesto();
            ultimoIdServicio = datos.getUltimoIdServicio();
            ultimoNumeroOrden = datos.getUltimoNumeroOrden();
            ultimoNumeroFactura = datos.getUltimoNumeroFactura();

            GestorBitacora.registrarEvento("Sistema", "Carga de datos", true,
                    "Se cargaron los datos del sistema correctamente");

            return true;
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Carga de datos", false,
                    "Error al cargar datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Getters para acceder a los vectores de datos
    public static Vector<Cliente> getClientes() {
        return clientes;
    }

    public static Vector<Empleado> getEmpleados() {
        return empleados;
    }

    public static Vector<Repuesto> getRepuestos() {
        return repuestos;
    }

    public static Vector<Servicio> getServicios() {
        return servicios;
    }

    public static Vector<OrdenTrabajo> getOrdenesTrabajo() {
        return ordenesTrabajo;
    }

    public static Vector<Factura> getFacturas() {
        return facturas;
    }

    public static Vector<OrdenTrabajo> getColaEspera() {
        return colaEspera;
    }

    public static Vector<OrdenTrabajo> getColaListos() {
        return colaListos;
    }

    // Métodos para obtener nuevos IDs
    public static int getNuevoIdRepuesto() {
        return ++ultimoIdRepuesto;
    }

    public static int getNuevoIdServicio() {
        return ++ultimoIdServicio;
    }

    public static int getNuevoNumeroOrden() {
        return ++ultimoNumeroOrden;
    }

    public static int getNuevoNumeroFactura() {
        return ++ultimoNumeroFactura;
    }

    // Método para ordenamiento de clientes
    public static void ordenarClientesPorDPI() {
        // Implementar método de burbuja para ordenar clientes por DPI
        int n = clientes.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (clientes.get(j).getIdentificador().compareTo(
                        clientes.get(j + 1).getIdentificador()) > 0) {
                    // Intercambiar elementos
                    Cliente temp = clientes.get(j);
                    clientes.set(j, clientes.get(j + 1));
                    clientes.set(j + 1, temp);
                }
            }
        }
    }

    /**
     * Agrega una orden de trabajo a la cola de espera respetando prioridades
     * Los clientes oro tienen mayor prioridad y van al frente
     * 
     * @param orden La orden de trabajo a agregar
     */
    public static void agregarOrdenAColaEspera(OrdenTrabajo orden) {
        if (orden == null) {
            return;
        }

        // Si la cola está vacía, simplemente agregar
        if (colaEspera.isEmpty()) {
            colaEspera.add(orden);
            guardarDatos();
            return;
        }

        // Verificar si el cliente es tipo oro
        boolean esClienteOro = "oro".equals(orden.getCliente().getTipoCliente());

        if (esClienteOro) {
            // Si es cliente oro, buscar la posición donde debe insertarse
            // (después del último cliente oro en la cola)
            int posicion = 0;

            for (int i = 0; i < colaEspera.size(); i++) {
                OrdenTrabajo ordenEnCola = colaEspera.get(i);
                if (!"oro".equals(ordenEnCola.getCliente().getTipoCliente())) {
                    // Encontramos el primer cliente normal, insertamos antes de él
                    posicion = i;
                    break;
                }
                posicion = i + 1; // Seguir moviéndonos si encontramos más clientes oro
            }

            // Insertar la orden en la posición calculada
            colaEspera.insertElementAt(orden, posicion);
        } else {
            // Cliente normal, va al final de la cola
            colaEspera.add(orden);
        }

        // Guardar cambios
        guardarDatos();

        // Registrar en bitácora
        GestorBitacora.registrarEvento("Sistema", "Cola de Espera", true,
                "Orden #" + orden.getNumero() + " agregada a cola de espera. Cliente: " +
                        orden.getCliente().getNombreCompleto() + " (" + orden.getCliente().getTipoCliente() + ")");
    }

    /**
     * Reordena la cola de espera completa según la prioridad de los clientes
     * Útil si se cambia el tipo de cliente mientras está en la cola
     */
    public static void reordenarColaEspera() {
        if (colaEspera.size() <= 1) {
            return; // No hay nada que ordenar
        }

        // Separar en dos listas: clientes oro y normales
        Vector<OrdenTrabajo> ordenesOro = new Vector<>();
        Vector<OrdenTrabajo> ordenesNormales = new Vector<>();

        for (OrdenTrabajo orden : colaEspera) {
            if ("oro".equals(orden.getCliente().getTipoCliente())) {
                ordenesOro.add(orden);
            } else {
                ordenesNormales.add(orden);
            }
        }

        // Limpiar la cola actual
        colaEspera.clear();

        // Primero agregar las órdenes de clientes oro
        colaEspera.addAll(ordenesOro);

        // Luego agregar las órdenes de clientes normales
        colaEspera.addAll(ordenesNormales);

        // Guardar cambios
        guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Cola de Espera", true,
                "Cola de espera reordenada por prioridad. Clientes oro: " + ordenesOro.size() +
                        ", Clientes normales: " + ordenesNormales.size());
    }

    /**
     * Clase interna que encapsula todos los datos a serializar
     */
    private static class DatosSistema implements Serializable {
        private static final long serialVersionUID = 1L;

        private Vector<Cliente> clientes;
        private Vector<Empleado> empleados;
        private Vector<Repuesto> repuestos;
        private Vector<Servicio> servicios;
        private Vector<OrdenTrabajo> ordenesTrabajo;
        private Vector<Factura> facturas;
        private Vector<OrdenTrabajo> colaEspera;
        private Vector<OrdenTrabajo> colaListos;
        private int ultimoIdRepuesto;
        private int ultimoIdServicio;
        private int ultimoNumeroOrden;
        private int ultimoNumeroFactura;

        public DatosSistema() {
            // Copiar los datos actuales
            this.clientes = new Vector<>(DataController.clientes);
            this.empleados = new Vector<>(DataController.empleados);
            this.repuestos = new Vector<>(DataController.repuestos);
            this.servicios = new Vector<>(DataController.servicios);
            this.ordenesTrabajo = new Vector<>(DataController.ordenesTrabajo);
            this.facturas = new Vector<>(DataController.facturas);
            this.colaEspera = new Vector<>(DataController.colaEspera);
            this.colaListos = new Vector<>(DataController.colaListos);

            // Copiar contadores
            this.ultimoIdRepuesto = DataController.ultimoIdRepuesto;
            this.ultimoIdServicio = DataController.ultimoIdServicio;
            this.ultimoNumeroOrden = DataController.ultimoNumeroOrden;
            this.ultimoNumeroFactura = DataController.ultimoNumeroFactura;
        }

        // Getters para todos los campos
        public Vector<Cliente> getClientes() {
            return clientes;
        }

        public Vector<Empleado> getEmpleados() {
            return empleados;
        }

        public Vector<Repuesto> getRepuestos() {
            return repuestos;
        }

        public Vector<Servicio> getServicios() {
            return servicios;
        }

        public Vector<OrdenTrabajo> getOrdenesTrabajo() {
            return ordenesTrabajo;
        }

        public Vector<Factura> getFacturas() {
            return facturas;
        }

        public Vector<OrdenTrabajo> getColaEspera() {
            return colaEspera;
        }

        public Vector<OrdenTrabajo> getColaListos() {
            return colaListos;
        }

        public int getUltimoIdRepuesto() {
            return ultimoIdRepuesto;
        }

        public int getUltimoIdServicio() {
            return ultimoIdServicio;
        }

        public int getUltimoNumeroOrden() {
            return ultimoNumeroOrden;
        }

        public int getUltimoNumeroFactura() {
            return ultimoNumeroFactura;
        }
    }
}
