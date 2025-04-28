package com.tallermecanico.controllers;

import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.utils.GestorBitacora;

import java.util.Vector;

/**
 * Controlador para la gestión de clientes
 */
public class ClienteController {

    /**
     * Registra un nuevo cliente en el sistema
     * 
     * @return el cliente creado o null si falló
     */
    public static Cliente registrarCliente(String identificador, String nombre, String apellido,
            String usuario, String password) {
        // Verificaciones...

        // Crear el nuevo cliente
        Cliente nuevoCliente = new Cliente(identificador, nombre, apellido, usuario, password);

        // Agregarlo al vector de clientes
        DataController.getClientes().add(nuevoCliente);

        // Ordenar clientes por DPI
        DataController.ordenarClientesPorDPI();

        // Guardar cambios
        DataController.guardarDatos();

        // Registrar evento y retornar
        GestorBitacora.registrarEvento("Sistema", "Registro de cliente", true,
                "Cliente registrado: " + identificador + " - " + nombre + " " + apellido);

        return nuevoCliente;
    }

    /**
     * Actualiza los datos de un cliente
     * 
     * @param identificador Identificador del cliente
     * @param nombre        Nuevo nombre
     * @param apellido      Nuevo apellido
     * @param email         Nuevo email
     * @param telefono      Nuevo teléfono
     * @return true si la actualización fue exitosa
     */
    public static boolean actualizarCliente(String identificador, String nombre, String apellido,
            String email, String telefono) {
        // Buscar el cliente
        Cliente cliente = buscarClientePorIdentificador(identificador);
        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualizar Cliente", false,
                    "No se encontró cliente con identificador: " + identificador);
            return false;
        }

        // Actualizar nombre y apellido (estos métodos existen)
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);

        // Actualizar email y teléfono mediante campos directos
        // ya que setEmail y setTelefono no existen
        try {
            // Usar reflexión para acceder a los campos
            java.lang.reflect.Field emailField = Cliente.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(cliente, email);

            java.lang.reflect.Field telefonoField = Cliente.class.getDeclaredField("telefono");
            telefonoField.setAccessible(true);
            telefonoField.set(cliente, telefono);
        } catch (Exception e) {
            // Si la reflexión falla, registrar advertencia pero continuar
            GestorBitacora.registrarEvento("Sistema", "Actualizar Cliente", false,
                    "No se pudieron actualizar email/teléfono: " + e.getMessage());
        }

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Actualizar Cliente", true,
                "Cliente actualizado: " + identificador);

        return true;
    }

    /**
     * Versión sobrecargada que incluye el tipo de cliente
     */
    public static boolean actualizarCliente(String identificador, String nombre, String apellido,
            String email, String telefono, String tipoCliente) {
        // Actualizar datos básicos
        boolean resultado = actualizarCliente(identificador, nombre, apellido, email, telefono);

        if (resultado) {
            // Actualizar tipo de cliente
            Cliente cliente = buscarClientePorIdentificador(identificador);

            try {
                // Intentar usar método setTipoCliente
                cliente.setTipoCliente(tipoCliente);
            } catch (Exception e) {
                // Si no existe, usar reflexión
                try {
                    java.lang.reflect.Field tipoField = Cliente.class.getDeclaredField("tipoCliente");
                    tipoField.setAccessible(true);
                    tipoField.set(cliente, tipoCliente);
                } catch (Exception ex) {
                    GestorBitacora.registrarEvento("Sistema", "Actualizar Cliente", false,
                            "No se pudo actualizar tipo de cliente: " + ex.getMessage());
                    return false;
                }
            }

            // Guardar cambios
            DataController.guardarDatos();
        }

        return resultado;
    }

    /**
     * Elimina un cliente del sistema
     * 
     * @return true si se eliminó correctamente
     */
    public static boolean eliminarCliente(String identificador) {
        Vector<Cliente> clientes = DataController.getClientes();

        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getIdentificador().equals(identificador)) {
                clientes.remove(i);

                // Guardar cambios
                DataController.guardarDatos();

                GestorBitacora.registrarEvento("Sistema", "Eliminación de cliente", true,
                        "Cliente eliminado: " + identificador);

                return true;
            }
        }

        GestorBitacora.registrarEvento("Sistema", "Eliminación de cliente", false,
                "No se encontró cliente con identificador: " + identificador);

        return false;
    }

    /**
     * Busca un cliente por su identificador (DPI)
     * 
     * @return el cliente encontrado o null
     */
    public static Cliente buscarClientePorIdentificador(String identificador) {
        for (Cliente cliente : DataController.getClientes()) {
            if (cliente.getIdentificador().equals(identificador)) {
                return cliente;
            }
        }
        return null;
    }

    /**
     * Busca un cliente por su nombre de usuario
     * 
     * @return el cliente encontrado o null
     */
    public static Cliente buscarClientePorUsuario(String usuario) {
        for (Cliente cliente : DataController.getClientes()) {
            if (cliente.getNombreUsuario().equals(usuario)) {
                return cliente;
            }
        }
        return null;
    }

    /**
     * Autentica a un cliente con sus credenciales
     * 
     * @return el cliente autenticado o null
     */
    public static Cliente autenticarCliente(String usuario, String password) {
        Cliente cliente = buscarClientePorUsuario(usuario);

        if (cliente != null && cliente.verificarCredenciales(usuario, password)) {
            GestorBitacora.registrarEvento(usuario, "Autenticación de cliente", true,
                    "Cliente autenticado: " + cliente.getNombreCompleto());
            return cliente;
        }

        GestorBitacora.registrarEvento(usuario, "Autenticación de cliente", false,
                "Credenciales incorrectas para usuario: " + usuario);

        return null;
    }

    /**
     * Registra un automóvil para un cliente
     * 
     * @return true si se registró correctamente
     */
    public static boolean registrarAutomovil(String idCliente, Automovil automovil) {
        Cliente cliente = buscarClientePorIdentificador(idCliente);

        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", false,
                    "No se encontró cliente con identificador: " + idCliente);
            return false;
        }

        // Verificar que no exista un automóvil con la misma placa
        for (Cliente c : DataController.getClientes()) {
            if (c.buscarAutomovil(automovil.getPlaca()) != null) {
                GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", false,
                        "Ya existe un automóvil con la placa: " + automovil.getPlaca());
                return false;
            }
        }

        // Agregar automóvil al cliente
        cliente.agregarAutomovil(automovil);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", true,
                "Automóvil registrado: " + automovil.getPlaca() + " para cliente: " + cliente.getNombreCompleto());

        return true;
    }

    /**
     * Registra un nuevo automóvil para un cliente
     * 
     * @return true si se registró correctamente
     */
    public static boolean registrarAutomovil(String idCliente, String placa, String marca, String modelo,
            String rutaFoto) {
        // Buscar el cliente
        Cliente cliente = buscarClientePorIdentificador(idCliente);

        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", false,
                    "No se encontró cliente con identificador: " + idCliente);
            return false;
        }

        // Verificar si ya existe un automóvil con esa placa (en cualquier cliente)
        for (Cliente c : DataController.getClientes()) {
            if (c.buscarAutomovil(placa) != null) {
                GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", false,
                        "Ya existe un automóvil con placa: " + placa);
                return false;
            }
        }

        // Crear y agregar el automóvil
        Automovil automovil = new Automovil(placa, marca, modelo, rutaFoto);
        boolean agregado = cliente.agregarAutomovil(automovil);

        if (agregado) {
            // Guardar cambios
            DataController.guardarDatos();

            GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", true,
                    "Automóvil registrado: " + placa + " para cliente: " + cliente.getNombreCompleto());
        }

        return agregado;
    }

    /**
     * Obtiene todos los clientes del sistema
     */
    public static Vector<Cliente> obtenerTodosLosClientes() {
        return DataController.getClientes();
    }

    /**
     * Obtiene los clientes filtrados por tipo
     */
    public static Vector<Cliente> obtenerClientesPorTipo(String tipo) {
        Vector<Cliente> resultado = new Vector<>();

        for (Cliente cliente : DataController.getClientes()) {
            if (cliente.getTipoCliente().equals(tipo)) {
                resultado.add(cliente);
            }
        }

        return resultado;
    }

    /**
     * Actualiza el tipo de cliente y reordena la cola si es necesario
     * 
     * @param idCliente Identificador del cliente
     * @param nuevoTipo Nuevo tipo de cliente ("normal" o "oro")
     * @return true si se realizó el cambio correctamente
     */
    public static boolean actualizarTipoCliente(String idCliente, String nuevoTipo) {
        // Validar parámetros
        if (idCliente == null || idCliente.isEmpty() ||
                (!"normal".equals(nuevoTipo) && !"oro".equals(nuevoTipo))) {
            return false;
        }

        // Buscar el cliente
        Cliente cliente = buscarClientePorIdentificador(idCliente);
        if (cliente == null) {
            return false;
        }

        // Si ya tiene ese tipo, no hacer nada
        if (nuevoTipo.equals(cliente.getTipoCliente())) {
            return true;
        }

        // Verificar si el cliente tiene órdenes en la cola de espera
        boolean clienteEnCola = false;
        for (OrdenTrabajo orden : DataController.getColaEspera()) {
            if (orden.getCliente().getIdentificador().equals(idCliente)) {
                clienteEnCola = true;
                break;
            }
        }

        // Actualizar el tipo
        String tipoAnterior = cliente.getTipoCliente();
        cliente.setTipoCliente(nuevoTipo);

        // Guardar cambios
        DataController.guardarDatos();

        // Si el cliente está en la cola y cambió a oro, reordenar la cola
        if (clienteEnCola && "oro".equals(nuevoTipo)) {
            DataController.reordenarColaEspera();
        }

        GestorBitacora.registrarEvento("Sistema", "Cambio Tipo Cliente", true,
                "Cliente " + cliente.getNombreCompleto() + " cambió de tipo " +
                        tipoAnterior + " a " + nuevoTipo);

        return true;
    }

    /**
     * Adds a new client to the system.
     *
     * @param nombre      The first name of the client.
     * @param apellido    The last name of the client.
     * @param usuario     The username of the client.
     * @param contraseña  The password of the client.
     * @param tipoCliente The type of the client (e.g., "normal" or "oro").
     * @return true if the client was added successfully, false otherwise.
     */
    public static boolean agregarCliente(String nombre, String apellido, String usuario, String contraseña,
            String tipoCliente) {
        if (nombre == null || apellido == null || usuario == null || contraseña == null || tipoCliente == null) {
            return false; // Entrada inválida
        }

        // Crear el nuevo cliente
        Cliente nuevoCliente = new Cliente(nombre, apellido, usuario, contraseña, tipoCliente);

        // Usar el método addCliente de DataController
        return DataController.addCliente(nuevoCliente);
    }

    public static Vector<Cliente> buscarClientes(String texto) {
        Vector<Cliente> resultado = new Vector<>();

        for (Cliente cliente : DataController.getClientes()) {
            if (cliente.getNombre().toLowerCase().contains(texto.toLowerCase()) ||
                    cliente.getApellido().toLowerCase().contains(texto.toLowerCase()) ||
                    cliente.getIdentificador().toLowerCase().contains(texto.toLowerCase())) {
                resultado.add(cliente);
            }
        }

        return resultado;
    }

    public static boolean agregarAutomovilACliente(Cliente clienteActual, Automovil auto) {
        if (clienteActual != null && auto != null) {
            clienteActual.agregarAutomovil(auto);
            DataController.guardarDatos();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifica si un cliente cumple con los requisitos para ser promocionado a tipo
     * ORO
     * y lo actualiza si corresponde
     * 
     * @param cliente Cliente a verificar
     * @return true si fue promocionado, false en caso contrario
     */
    public static boolean verificarPromocionClienteOro(Cliente cliente) {
        // Si ya es cliente oro, no hacer nada
        if (cliente.getTipoCliente().equalsIgnoreCase("oro")) {
            return false;
        }

        // Obtener todas las órdenes finalizadas o facturadas del cliente
        Vector<OrdenTrabajo> ordenes = OrdenTrabajoController.obtenerOrdenesPorCliente(cliente);
        int serviciosCompletados = 0;

        for (OrdenTrabajo orden : ordenes) {
            String estado = orden.getEstado();
            if (estado.equals("FINALIZADO") || estado.equals("FACTURADO")) {
                serviciosCompletados++;
            }
        }

        // Si cumple con 4 o más servicios, promocionar a oro
        if (serviciosCompletados >= 4) {
            cliente.setTipoCliente("oro");
            GestorBitacora.registrarEvento("Sistema", "Promoción Cliente", true,
                    "Cliente " + cliente.getNombreCompleto() + " promocionado a Cliente Oro");
            return true;
        }

        return false;
    }

    public static void actualizarCliente(Cliente nuevoCliente) {
        // Buscar el cliente existente
        Cliente clienteExistente = buscarClientePorIdentificador(nuevoCliente.getIdentificador());

        if (clienteExistente != null) {
            // Actualizar los datos del cliente existente
            clienteExistente.setNombre(nuevoCliente.getNombre());
            clienteExistente.setApellido(nuevoCliente.getApellido());
            clienteExistente.setEmail(nuevoCliente.getEmail());
            clienteExistente.setTelefono(nuevoCliente.getTelefono());
            clienteExistente.setTipoCliente(nuevoCliente.getTipoCliente());

            // Guardar cambios
            DataController.guardarDatos();

            GestorBitacora.registrarEvento("Sistema", "Actualización de Cliente", true,
                    "Cliente actualizado: " + nuevoCliente.getIdentificador() + " - "
                            + nuevoCliente.getNombreCompleto());
        } else {
            GestorBitacora.registrarEvento("Sistema", "Actualización de Cliente", false,
                    "No se encontró cliente con identificador: " + nuevoCliente.getIdentificador());
        }
    }

    public static Cliente obtenerClientePorId(String idCliente) {
        for (Cliente cliente : DataController.getClientes()) {
            if (cliente.getIdentificador().equals(idCliente)) {
                return cliente;
            }
        }
        return null; // Cliente no encontrado
    }

    public static boolean eliminarAutomovil(String identificador, String placa) {
        // Buscar el cliente
        Cliente cliente = buscarClientePorIdentificador(identificador);
        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Eliminación de automóvil", false,
                    "No se encontró cliente con identificador: " + identificador);
            return false;
        }

        // Buscar el automóvil
        Automovil automovil = cliente.buscarAutomovil(placa);
        if (automovil == null) {
            GestorBitacora.registrarEvento("Sistema", "Eliminación de automóvil", false,
                    "No se encontró automóvil con placa: " + placa + " para cliente: " + cliente.getNombreCompleto());
            return false;
        }

        // Eliminar el automóvil
        cliente.eliminarAutomovil(automovil.getPlaca());

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Eliminación de automóvil", true,
                "Automóvil eliminado: " + placa + " para cliente: " + cliente.getNombreCompleto());

        return true;
    }

    public static boolean actualizarAutomovil(String identificador, Automovil auto) {
        // Buscar el cliente
        Cliente cliente = buscarClientePorIdentificador(identificador);
        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de automóvil", false,
                    "No se encontró cliente con identificador: " + identificador);
            return false;
        }

        // Buscar el automóvil
        Automovil automovil = cliente.buscarAutomovil(auto.getPlaca());
        if (automovil == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de automóvil", false,
                    "No se encontró automóvil con placa: " + auto.getPlaca() + " para cliente: "
                            + cliente.getNombreCompleto());
            return false;
        }

        // Actualizar los datos del automóvil
        automovil.setMarca(auto.getMarca());
        automovil.setModelo(auto.getModelo());

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Actualización de automóvil", true,
                "Automóvil actualizado: " + auto.getPlaca() + " para cliente: " + cliente.getNombreCompleto());

        return true; // Ahora devuelve true al completar correctamente
    }
}