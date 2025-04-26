package com.tallermecanico.controllers;

import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.utils.GestorBitacora;

import java.util.Vector;

/**
 * Controlador para la gestión de automóviles
 */
public class AutomovilController {

    /**
     * Registra un nuevo automóvil para un cliente
     * 
     * @return true si se registró correctamente
     */
    public static boolean registrarAutomovil(String idCliente, String placa,
            String marca, String modelo, String rutaFoto) {
        // Buscar el cliente
        Cliente cliente = ClienteController.buscarClientePorIdentificador(idCliente);

        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", false,
                    "No se encontró cliente con identificador: " + idCliente);
            return false;
        }

        // Verificar que no exista un automóvil con la misma placa
        for (Cliente c : DataController.getClientes()) {
            if (c.buscarAutomovil(placa) != null) {
                GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", false,
                        "Ya existe un automóvil con la placa: " + placa);
                return false;
            }
        }

        // Crear y agregar el nuevo automóvil
        Automovil nuevoAuto = new Automovil(placa, marca, modelo, rutaFoto);
        cliente.agregarAutomovil(nuevoAuto);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Registro de automóvil", true,
                "Automóvil registrado: " + placa + " para cliente: " + cliente.getNombreCompleto());

        return true;
    }

    /**
     * Actualiza los datos de un automóvil existente
     * 
     * @return true si se actualizó correctamente
     */
    public static boolean actualizarAutomovil(String idCliente, String placaOriginal,
            String nuevaPlaca, String marca, String modelo, String rutaFoto) {
        // Buscar el cliente
        Cliente cliente = ClienteController.buscarClientePorIdentificador(idCliente);

        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de automóvil", false,
                    "No se encontró cliente con identificador: " + idCliente);
            return false;
        }

        // Buscar el automóvil
        Automovil automovil = cliente.buscarAutomovil(placaOriginal);

        if (automovil == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de automóvil", false,
                    "No se encontró automóvil con placa: " + placaOriginal);
            return false;
        }

        // Si está cambiando la placa, verificar que no exista otra igual
        if (!placaOriginal.equals(nuevaPlaca)) {
            for (Cliente c : DataController.getClientes()) {
                if (c.buscarAutomovil(nuevaPlaca) != null) {
                    GestorBitacora.registrarEvento("Sistema", "Actualización de automóvil", false,
                            "Ya existe un automóvil con la placa: " + nuevaPlaca);
                    return false;
                }
            }
            automovil.setPlaca(nuevaPlaca);
        }

        // Actualizar datos
        automovil.setMarca(marca);
        automovil.setModelo(modelo);

        // Actualizar foto sólo si se proporciona una nueva
        if (rutaFoto != null && !rutaFoto.isEmpty()) {
            automovil.setRutaFoto(rutaFoto);
        }

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Actualización de automóvil", true,
                "Automóvil actualizado: " + nuevaPlaca);

        return true;
    }

    /**
     * Elimina un automóvil
     * 
     * @return true si se eliminó correctamente
     */
    public static boolean eliminarAutomovil(String idCliente, String placa) {
        // Buscar el cliente
        Cliente cliente = ClienteController.buscarClientePorIdentificador(idCliente);

        if (cliente == null) {
            GestorBitacora.registrarEvento("Sistema", "Eliminación de automóvil", false,
                    "No se encontró cliente con identificador: " + idCliente);
            return false;
        }

        // Eliminar el automóvil
        boolean resultado = cliente.eliminarAutomovil(placa);

        if (resultado) {
            // Guardar cambios
            DataController.guardarDatos();

            GestorBitacora.registrarEvento("Sistema", "Eliminación de automóvil", true,
                    "Automóvil eliminado: " + placa);
        } else {
            GestorBitacora.registrarEvento("Sistema", "Eliminación de automóvil", false,
                    "No se encontró automóvil con placa: " + placa);
        }

        return resultado;
    }

    /**
     * Busca un automóvil por su placa en todo el sistema
     * 
     * @return el automóvil encontrado o null
     */
    public static Automovil buscarAutomovilPorPlaca(String placa) {
        for (Cliente cliente : DataController.getClientes()) {
            Automovil auto = cliente.buscarAutomovil(placa);
            if (auto != null) {
                return auto;
            }
        }
        return null;
    }

    /**
     * Obtiene todos los automóviles de un cliente
     */
    public static Vector<Automovil> obtenerAutomovilesCliente(String idCliente) {
        Cliente cliente = ClienteController.buscarClientePorIdentificador(idCliente);

        if (cliente != null) {
            return cliente.getAutomoviles();
        }

        return new Vector<>(); // Devolver vector vacío si no se encuentra el cliente
    }

    /**
     * Obtiene todos los automóviles registrados en el sistema
     */
    public static Vector<Automovil> obtenerTodosLosAutomoviles() {
        Vector<Automovil> todosLosAutos = new Vector<>();

        for (Cliente cliente : DataController.getClientes()) {
            todosLosAutos.addAll(cliente.getAutomoviles());
        }

        return todosLosAutos;
    }
}
