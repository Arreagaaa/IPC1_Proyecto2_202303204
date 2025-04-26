package com.tallermecanico.controllers;

import com.tallermecanico.models.personas.Empleado;
import com.tallermecanico.models.personas.Mecanico;
import com.tallermecanico.models.personas.Persona;
import com.tallermecanico.utils.GestorBitacora;

import java.util.Vector;

/**
 * Controlador para la gestión de empleados
 */
public class EmpleadoController {

    /**
     * Registra un nuevo empleado en el sistema
     * 
     * @return el empleado creado o null si falló
     */
    public static Empleado registrarEmpleado(String identificador, String nombre, String apellido,
            String usuario, String password, String tipo) {
        // Verificar que no exista un empleado con ese ID o usuario
        if (buscarEmpleadoPorIdentificador(identificador) != null) {
            GestorBitacora.registrarEvento("Sistema", "Registro de empleado", false,
                    "Ya existe un empleado con el identificador: " + identificador);
            return null;
        }

        if (buscarEmpleadoPorUsuario(usuario) != null) {
            GestorBitacora.registrarEvento("Sistema", "Registro de empleado", false,
                    "Ya existe un empleado con el usuario: " + usuario);
            return null;
        }

        // Verificar que el tipo sea válido
        if (!"admin".equals(tipo) && !"mecanico".equals(tipo)) {
            GestorBitacora.registrarEvento("Sistema", "Registro de empleado", false,
                    "Tipo de empleado inválido: " + tipo);
            return null;
        }

        // Crear el empleado según su tipo
        Empleado nuevoEmpleado;

        if ("mecanico".equals(tipo)) {
            nuevoEmpleado = new Mecanico(identificador, nombre, apellido, usuario, password);
        } else {
            nuevoEmpleado = new Empleado(identificador, nombre, apellido, usuario, password, tipo);
        }

        // Agregarlo al vector de empleados
        DataController.getEmpleados().add(nuevoEmpleado);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Registro de empleado", true,
                "Empleado registrado: " + identificador + " - " + nombre + " " + apellido + " (" + tipo + ")");

        return nuevoEmpleado;
    }

    /**
     * Actualiza los datos de un empleado existente
     * 
     * @return true si se actualizó correctamente
     */
    public static boolean actualizarEmpleado(String identificador, String nombre, String apellido,
            String usuario, String password) {
        Empleado empleado = buscarEmpleadoPorIdentificador(identificador);

        if (empleado == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de empleado", false,
                    "No se encontró empleado con identificador: " + identificador);
            return false;
        }

        // Verificar que el nuevo usuario no exista (si se está cambiando)
        if (!empleado.getNombreUsuario().equals(usuario)) {
            Empleado empleadoExistente = buscarEmpleadoPorUsuario(usuario);
            if (empleadoExistente != null && !empleadoExistente.getIdentificador().equals(identificador)) {
                GestorBitacora.registrarEvento("Sistema", "Actualización de empleado", false,
                        "Ya existe un empleado con el usuario: " + usuario);
                return false;
            }
        }

        // Actualizar datos
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setNombreUsuario(usuario);

        // Solo actualizar la contraseña si se proporciona una nueva
        if (password != null && !password.isEmpty()) {
            empleado.setContrasena(password);
        }

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Actualización de empleado", true,
                "Empleado actualizado: " + identificador);

        return true;
    }

    /**
     * Elimina un empleado del sistema
     * 
     * @return true si se eliminó correctamente
     */
    public static boolean eliminarEmpleado(String identificador) {
        Vector<Empleado> empleados = DataController.getEmpleados();

        for (int i = 0; i < empleados.size(); i++) {
            if (empleados.get(i).getIdentificador().equals(identificador)) {
                empleados.remove(i);

                // Guardar cambios
                DataController.guardarDatos();

                GestorBitacora.registrarEvento("Sistema", "Eliminación de empleado", true,
                        "Empleado eliminado: " + identificador);

                return true;
            }
        }

        GestorBitacora.registrarEvento("Sistema", "Eliminación de empleado", false,
                "No se encontró empleado con identificador: " + identificador);

        return false;
    }

    /**
     * Busca un empleado por su identificador
     * 
     * @return el empleado encontrado o null
     */
    public static Empleado buscarEmpleadoPorIdentificador(String identificador) {
        for (Empleado empleado : DataController.getEmpleados()) {
            if (empleado.getIdentificador().equals(identificador)) {
                return empleado;
            }
        }
        return null;
    }

    /**
     * Busca un empleado por su nombre de usuario
     * 
     * @return el empleado encontrado o null
     */
    public static Empleado buscarEmpleadoPorUsuario(String usuario) {
        for (Empleado empleado : DataController.getEmpleados()) {
            if (empleado.getNombreUsuario().equals(usuario)) {
                return empleado;
            }
        }
        return null;
    }

    /**
     * Autentica a un empleado con sus credenciales
     * 
     * @return el empleado autenticado o null
     */
    public static Empleado autenticarEmpleado(String usuario, String password) {
        Empleado empleado = buscarEmpleadoPorUsuario(usuario);

        if (empleado != null && empleado.verificarCredenciales(usuario, password)) {
            GestorBitacora.registrarEvento(usuario, "Autenticación de empleado", true,
                    "Empleado autenticado: " + empleado.getNombreCompleto());
            return empleado;
        }

        GestorBitacora.registrarEvento(usuario, "Autenticación de empleado", false,
                "Credenciales incorrectas para usuario: " + usuario);

        return null;
    }

    /**
     * Obtiene todos los empleados del sistema
     */
    public static Vector<Empleado> obtenerTodosLosEmpleados() {
        return DataController.getEmpleados();
    }

    /**
     * Obtiene los mecánicos del sistema
     */
    public static Vector<Mecanico> obtenerMecanicos() {
        Vector<Mecanico> mecanicos = new Vector<>();

        for (Empleado empleado : DataController.getEmpleados()) {
            if (empleado instanceof Mecanico) {
                mecanicos.add((Mecanico) empleado);
            }
        }

        return mecanicos;
    }

    /**
     * Obtiene los mecánicos disponibles
     */
    public static Vector<Mecanico> obtenerMecanicosDisponibles() {
        Vector<Mecanico> disponibles = new Vector<>();

        for (Empleado empleado : DataController.getEmpleados()) {
            if (empleado instanceof Mecanico && ((Mecanico) empleado).isDisponible()) {
                disponibles.add((Mecanico) empleado);
            }
        }

        return disponibles;
    }
}