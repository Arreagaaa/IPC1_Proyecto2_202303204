package com.tallermecanico.controllers;

import com.tallermecanico.models.Factura;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Empleado;
import com.tallermecanico.models.personas.Administrador;
import com.tallermecanico.models.personas.Mecanico;
import com.tallermecanico.models.Automovil;
import com.tallermecanico.utils.GestorBitacora;
import com.tallermecanico.utils.Serializador;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

    // Contador para números de factura
    private static int contadorFacturas = 1000;

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
            // Cambiar esta línea para incluir la contraseña:
            Empleado admin = new Administrador("1", "Administrador", "General", "admin");
            empleados.add(admin);
        }

        // Guardar los datos iniciales
        guardarDatos();
    }

    /**
     * Verifica si existe al menos un administrador en el sistema.
     * Si no existe, crea un administrador por defecto.
     */
    public static void verificarAdministradorPorDefecto() {
        // Verificar administrador por defecto
        boolean existeAdmin = false;
        for (Empleado empleado : getEmpleados()) {
            if (empleado.getTipo().equalsIgnoreCase("administrador")) {
                existeAdmin = true;
                break;
            }
        }

        if (!existeAdmin) {
            // Crear administrador por defecto
            Empleado admin = new Empleado("admin", "Administrador", "General", "admin", "admin", "administrador");
            getEmpleados().add(admin);
            GestorBitacora.registrarEvento("Sistema", "Creación de usuarios", true,
                    "Se creó el usuario administrador por defecto");
        }

        // Verificar mecánico por defecto
        boolean existeMecanico = false;
        for (Empleado empleado : getEmpleados()) {
            if (empleado.getTipo().equalsIgnoreCase("mecanico")) {
                existeMecanico = true;
                break;
            }
        }

        if (!existeMecanico) {
            // Crear mecánico por defecto
            Mecanico mecanico = new Mecanico("M001", "Jake", "elPerro", "mecanico", "123456");
            mecanico.setDisponible(true);
            getEmpleados().add(mecanico);
            GestorBitacora.registrarEvento("Sistema", "Creación de usuarios", true,
                    "Se creó el usuario mecánico por defecto");
        }

        // Verificar servicio de diagnóstico por defecto
        boolean existeDiagnostico = false;
        for (Servicio servicio : getServicios()) {
            if (servicio.getNombre().equalsIgnoreCase("Diagnóstico") &&
                    servicio.getMarca().equalsIgnoreCase("cualquiera") &&
                    servicio.getModelo().equalsIgnoreCase("cualquiera")) {
                existeDiagnostico = true;
                break;
            }
        }

        if (!existeDiagnostico) {
            // Crear servicio de diagnóstico genérico
            Servicio diagnostico = new Servicio(contadorFacturas, "Diagnóstico", "cualquiera", "cualquiera",
                    contadorFacturas);
            diagnostico.setPrecioManoObra(100.0); // Precio básico de diagnóstico
            getServicios().add(diagnostico);
            GestorBitacora.registrarEvento("Sistema", "Creación de servicio", true,
                    "Se creó el servicio de diagnóstico por defecto");
        }

        // Guardar los cambios
        Serializador.guardarDatos(null);
    }

    /**
     * Guarda todos los datos del sistema usando serialización
     */
    public static void guardarDatos() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("datosSistema.dat"))) {
            oos.writeObject(new DatosSistema());
        } catch (IOException e) {
            System.err.println("Error al guardar los datos: " + e.getMessage());
        }
    }

    /**
     * Carga los datos del sistema desde un archivo serializado
     *
     * @return true si se cargaron correctamente
     */
    public static boolean cargarDatos() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("datosSistema.dat"))) {
            DatosSistema datos = (DatosSistema) ois.readObject();

            // Restaurar los datos en las estructuras del controlador
            clientes = datos.getClientes() != null ? datos.getClientes() : new Vector<>();
            empleados = datos.getEmpleados() != null ? datos.getEmpleados() : new Vector<>();
            repuestos = datos.getRepuestos() != null ? datos.getRepuestos() : new Vector<>();
            servicios = datos.getServicios() != null ? datos.getServicios() : new Vector<>();
            ordenesTrabajo = datos.getOrdenesTrabajo() != null ? datos.getOrdenesTrabajo() : new Vector<>();
            facturas = datos.getFacturas() != null ? datos.getFacturas() : new Vector<>();
            colaEspera = datos.getColaEspera() != null ? datos.getColaEspera() : new Vector<>();
            colaListos = datos.getColaListos() != null ? datos.getColaListos() : new Vector<>();

            // Restaurar contadores
            ultimoIdRepuesto = datos.getUltimoIdRepuesto();
            ultimoIdServicio = datos.getUltimoIdServicio();
            ultimoNumeroOrden = datos.getUltimoNumeroOrden();
            ultimoNumeroFactura = datos.getUltimoNumeroFactura();

            GestorBitacora.registrarEvento("Sistema", "Carga de datos", true,
                    "Se cargaron los datos del sistema correctamente");

            return true;
        } catch (IOException | ClassNotFoundException e) {
            GestorBitacora.registrarEvento("Sistema", "Carga de datos", false,
                    "Error al cargar datos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reinicia todos los vectores de datos en memoria
     */
    public static void reiniciarDatos() {
        clientes = new Vector<>();
        repuestos = new Vector<>();
        servicios = new Vector<>();
        ordenesTrabajo = new Vector<>();
        facturas = new Vector<>();
        empleados = new Vector<>();
        colaEspera = new Vector<>();
        colaListos = new Vector<>();

        // Corregido: usar los nombres de variables que existen en la clase
        ultimoIdRepuesto = 0;
        ultimoIdServicio = 0;
        ultimoNumeroOrden = 0;
        ultimoNumeroFactura = 0;
        contadorFacturas = 1000;

        // Añadir el administrador por defecto
        verificarAdministradorPorDefecto();

        // Guardar los datos reiniciados
        guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Reinicio", true,
                "Datos del sistema reiniciados completamente");
    }

    // Getters para acceder a los vectores de datos
    public static Vector<Cliente> getClientes() {
        return clientes != null ? clientes : new Vector<>();
    }

    public static Vector<Empleado> getEmpleados() {
        return empleados != null ? empleados : new Vector<>();
    }

    public static Vector<Repuesto> getRepuestos() {
        return repuestos != null ? repuestos : new Vector<>();
    }

    public static Vector<Servicio> getServicios() {
        return servicios != null ? servicios : new Vector<>();
    }

    public static Vector<OrdenTrabajo> getOrdenesTrabajo() {
        return ordenesTrabajo != null ? ordenesTrabajo : new Vector<>();
    }

    public static Vector<Factura> getFacturas() {
        return facturas != null ? facturas : new Vector<>();
    }

    public static Vector<OrdenTrabajo> getColaEspera() {
        return colaEspera != null ? colaEspera : new Vector<>();
    }

    public static Vector<OrdenTrabajo> getColaListos() {
        return colaListos != null ? colaListos : new Vector<>();
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

    public static synchronized int getNuevoNumeroFactura() {
        return contadorFacturas++;
    }

    // Método para ordenamiento de clientes
    public static void ordenarClientesPorDPI() {
        for (int i = 0; i < clientes.size() - 1; i++) {
            for (int j = 0; j < clientes.size() - i - 1; j++) {
                if (clientes.get(j).getIdentificador().compareTo(clientes.get(j + 1).getIdentificador()) > 0) {
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
        boolean esClienteOro = "oro".equalsIgnoreCase(orden.getCliente().getTipoCliente());

        if (esClienteOro) {
            // Si es cliente oro, buscar la posición donde debe insertarse
            // (después del último cliente oro en la cola)
            int posicion = 0;

            for (int i = 0; i < colaEspera.size(); i++) {
                OrdenTrabajo ordenEnCola = colaEspera.get(i);
                if (!"oro".equalsIgnoreCase(ordenEnCola.getCliente().getTipoCliente())) {
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
            if ("oro".equalsIgnoreCase(orden.getCliente().getTipoCliente())) {
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
     * Carga repuestos desde un archivo y los agrega al sistema
     *
     * @param rutaArchivo Ruta del archivo a cargar
     */
    public static void cargarRepuestosDesdeArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Validar formato: nombreRepuesto-marca-modelo-existencias-precio
                String[] partes = linea.split("-");
                if (partes.length != 5) {
                    GestorBitacora.registrarEvento("Sistema", "Carga Masiva Repuestos", false,
                            "Formato inválido en la línea: " + linea);
                    continue;
                }

                String nombre = partes[0];
                String marca = partes[1];
                String modelo = partes[2];
                int existencias;
                double precio;

                try {
                    existencias = Integer.parseInt(partes[3]);
                    precio = Double.parseDouble(partes[4]);
                } catch (NumberFormatException e) {
                    GestorBitacora.registrarEvento("Sistema", "Carga Masiva Repuestos", false,
                            "Error al convertir existencias o precio en la línea: " + linea);
                    continue;
                }

                // Crear y agregar el repuesto
                Repuesto nuevoRepuesto = new Repuesto();
                DataController.getRepuestos().add(nuevoRepuesto);
                GestorBitacora.registrarEvento("Sistema", "Carga Masiva Repuestos", true,
                        "Repuesto agregado: " + nombre);
            }
            guardarDatos();
        } catch (IOException e) {
            GestorBitacora.registrarEvento("Sistema", "Carga Masiva Repuestos", false,
                    "Error al leer el archivo: " + e.getMessage());
        }
    }

    /**
     * Carga servicios desde un archivo y los agrega al sistema
     *
     * @param rutaArchivo Ruta del archivo a cargar
     */
    public static void cargarServiciosDesdeArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Validar formato: nombreServicio-marca-modelo-listaRepuestos-precioManoDeObra
                String[] partes = linea.split("-");
                if (partes.length != 5) {
                    GestorBitacora.registrarEvento("Sistema", "Carga Masiva Servicios", false,
                            "Formato inválido en la línea: " + linea);
                    continue;
                }

                String nombre = partes[0];
                String marca = partes[1];
                String modelo = partes[2];
                String[] listaRepuestos = partes[3].split(";");
                double precioManoDeObra;

                try {
                    precioManoDeObra = Double.parseDouble(partes[4]);
                } catch (NumberFormatException e) {
                    GestorBitacora.registrarEvento("Sistema", "Carga Masiva Servicios", false,
                            "Error al convertir precio de mano de obra en la línea: " + linea);
                    continue;
                }

                // Validar repuestos
                List<Repuesto> repuestos = new ArrayList<>();
                for (String idRepuesto : listaRepuestos) {
                    try {
                        int id = Integer.parseInt(idRepuesto);
                        Repuesto repuesto = DataController.buscarRepuestoPorId(id);
                        if (repuesto == null || !repuesto.getMarca().equals(marca)
                                || !repuesto.getModelo().equals(modelo)) {
                            GestorBitacora.registrarEvento("Sistema", "Carga Masiva Servicios", false,
                                    "Repuesto inválido o no coincide con marca/modelo en la línea: " + linea);
                            continue;
                        }
                        repuestos.add(repuesto);
                    } catch (NumberFormatException e) {
                        GestorBitacora.registrarEvento("Sistema", "Carga Masiva Servicios", false,
                                "ID de repuesto inválido en la línea: " + linea);
                    }
                }

                // Crear y agregar el servicio
                Servicio nuevoServicio = new Servicio(DataController.getNuevoIdServicio(), nombre, marca, modelo,
                        precioManoDeObra);
                DataController.getServicios().add(nuevoServicio);
                GestorBitacora.registrarEvento("Sistema", "Carga Masiva Servicios", true,
                        "Servicio agregado: " + nombre);
            }
            guardarDatos();
        } catch (IOException e) {
            GestorBitacora.registrarEvento("Sistema", "Carga Masiva Servicios", false,
                    "Error al leer el archivo: " + e.getMessage());
        }
    }

    /**
     * Carga clientes desde un archivo y los agrega al sistema
     *
     * @param rutaArchivo Ruta del archivo a cargar
     */
    public static void cargarClientesDesdeArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Validar formato:
                // identificador-nombreCompleto-usuario-contraseña-tipoCliente-listaAutomoviles
                String[] partes = linea.split("-");
                if (partes.length != 6) {
                    GestorBitacora.registrarEvento("Sistema", "Carga Masiva Clientes", false,
                            "Formato inválido en la línea: " + linea);
                    continue;
                }

                String identificador = partes[0];
                String nombreCompleto = partes[1];
                String usuario = partes[2];
                String contraseña = partes[3];
                String tipoCliente = partes[4];
                String[] listaAutomoviles = partes[5].split(";");

                // Validar tipo de cliente
                if (!tipoCliente.equals("normal") && !tipoCliente.equals("oro")) {
                    GestorBitacora.registrarEvento("Sistema", "Carga Masiva Clientes", false,
                            "Tipo de cliente inválido en la línea: " + linea);
                    continue;
                }

                // Crear cliente
                Cliente nuevoCliente = new Cliente(identificador, nombreCompleto, usuario, contraseña, tipoCliente);

                // Validar automóviles
                for (String autoData : listaAutomoviles) {
                    String[] autoPartes = autoData.split(",");
                    if (autoPartes.length != 4) {
                        GestorBitacora.registrarEvento("Sistema", "Carga Masiva Clientes", false,
                                "Formato inválido de automóvil en la línea: " + autoData);
                        continue;
                    }

                    String placa = autoPartes[0];
                    String marca = autoPartes[1];
                    String modelo = autoPartes[2];
                    String foto = autoPartes[3];

                    // Validar que la foto sea .jpg
                    if (!foto.endsWith(".jpg")) {
                        GestorBitacora.registrarEvento("Sistema", "Carga Masiva Clientes", false,
                                "Formato de foto inválido (debe ser .jpg) en la línea: " + autoData);
                        continue;
                    }

                    Automovil nuevoAutomovil = new Automovil(placa, marca, modelo, foto);
                    nuevoCliente.getAutomoviles().add(nuevoAutomovil);
                }

                // Agregar cliente
                DataController.getClientes().add(nuevoCliente);
                GestorBitacora.registrarEvento("Sistema", "Carga Masiva Clientes", true,
                        "Cliente agregado: " + nombreCompleto);
            }
            ordenarClientesPorDPI();
            guardarDatos();
        } catch (IOException e) {
            GestorBitacora.registrarEvento("Sistema", "Carga Masiva Clientes", false,
                    "Error al leer el archivo: " + e.getMessage());
        }
    }

    private static Repuesto buscarRepuestoPorId(int id) {
        for (Repuesto repuesto : repuestos) {
            if (Integer.parseInt(repuesto.getId()) == id) {
                return repuesto;
            }
        }
        return null; // No se encontró el repuesto
    }

    /**
     * Agrega un cliente al sistema
     *
     * @param cliente El cliente a agregar
     * @return true si se agregó correctamente, false en caso contrario
     */
    public static boolean addCliente(Cliente cliente) {
        if (cliente == null) {
            return false; // No se puede agregar un cliente nulo
        }

        // Obtener el vector de clientes
        Vector<Cliente> clientes = getClientes();

        // Verificar si el cliente ya existe (por identificador o usuario)
        for (Cliente c : clientes) {
            if (c.getIdentificador().equals(cliente.getIdentificador())
                    || c.getNombreUsuario().equals(cliente.getNombreUsuario())) {
                GestorBitacora.registrarEvento("Sistema", "Agregar Cliente", false,
                        "El cliente ya existe: " + cliente.getIdentificador());
                return false; // Cliente duplicado
            }
        }

        // Agregar el cliente al vector
        clientes.add(cliente);

        // Ordenar los clientes por DPI
        ordenarClientesPorDPI();

        // Guardar los datos
        guardarDatos();

        // Registrar el evento en la bitácora
        GestorBitacora.registrarEvento("Sistema", "Agregar Cliente", true,
                "Cliente agregado: " + cliente.getIdentificador());

        return true;
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
            System.out.println("Constructor DatosSistema llamado");
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
            return clientes != null ? clientes : new Vector<>();
        }

        public Vector<Empleado> getEmpleados() {
            return empleados != null ? empleados : new Vector<>();
        }

        public Vector<Repuesto> getRepuestos() {
            return repuestos != null ? repuestos : new Vector<>();
        }

        public Vector<Servicio> getServicios() {
            return servicios != null ? servicios : new Vector<>();
        }

        public Vector<OrdenTrabajo> getOrdenesTrabajo() {
            return ordenesTrabajo != null ? ordenesTrabajo : new Vector<>();
        }

        public Vector<Factura> getFacturas() {
            return facturas != null ? facturas : new Vector<>();
        }

        public Vector<OrdenTrabajo> getColaEspera() {
            return colaEspera != null ? colaEspera : new Vector<>();
        }

        public Vector<OrdenTrabajo> getColaListos() {
            return colaListos != null ? colaListos : new Vector<>();
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

    public static void setRepuestos(Vector<Repuesto> unicos) {
        DataController.repuestos = unicos;
    }
}
