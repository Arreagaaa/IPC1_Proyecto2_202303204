package com.tallermecanico.controllers;

import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Persona;
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
     * Actualiza los datos de un cliente existente
     * 
     * @return true si se actualizó correctamente
     */
    public static boolean actualizarCliente(String identificador, String nombre, String apellido,
            String usuario, String password) {
        Cliente cliente = buscarClientePorIdentificador(identificador);

        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de cliente", false,
                    "No se encontró cliente con identificador: " + identificador);
            return false;
        }

        // Verificar que el nuevo usuario no exista (si se está cambiando)
        if (!cliente.getNombreUsuario().equals(usuario)) {
            Cliente clienteExistente = buscarClientePorUsuario(usuario);
            if (clienteExistente != null && !clienteExistente.getIdentificador().equals(identificador)) {
                GestorBitacora.registrarEvento("Sistema", "Actualización de cliente", false,
                        "Ya existe un cliente con el usuario: " + usuario);
                return false;
            }
        }

        // Actualizar datos
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setNombreUsuario(usuario);

        // Solo actualizar la contraseña si se proporciona una nueva
        if (password != null && !password.isEmpty()) {
            cliente.setContrasena(password);
        }

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Actualización de cliente", true,
                "Cliente actualizado: " + identificador);

        return true;
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
}
