package com.tallermecanico.controllers;

import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.utils.GestorBitacora;

import java.util.Collections;
import java.util.Vector;

/**
 * Controlador para la gestión de repuestos
 */
public class RepuestoController {

    /**
     * Registra un nuevo repuesto en el sistema
     * 
     * @return el repuesto creado o null si falló
     */
    public static Repuesto registrarRepuesto(String nombre, String marca, String modelo,
            int existencias, double precio) {
        // Verificar que los datos sean válidos
        if (existencias < 0 || precio <= 0) {
            GestorBitacora.registrarEvento("Sistema", "Registro de repuesto", false,
                    "Datos inválidos: existencias o precio negativos");
            return null;
        }

        // Crear el nuevo repuesto
        int nuevoId = DataController.getNuevoIdRepuesto();
        Repuesto nuevoRepuesto = new Repuesto(nuevoId, nombre, marca, modelo, existencias, precio);

        // Agregarlo al vector de repuestos
        DataController.getRepuestos().add(nuevoRepuesto);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Registro de repuesto", true,
                "Repuesto registrado: " + nuevoId + " - " + nombre);

        return nuevoRepuesto;
    }

    /**
     * Actualiza los datos de un repuesto existente
     * 
     * @return true si se actualizó correctamente
     */
    public static boolean actualizarRepuesto(int id, String nombre, String marca,
            String modelo, int existencias, double precio) {
        // Buscar el repuesto
        Repuesto repuesto = buscarRepuestoPorId(id);

        if (repuesto == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de repuesto", false,
                    "No se encontró repuesto con ID: " + id);
            return false;
        }

        // Verificar que los datos sean válidos
        if (existencias < 0 || precio <= 0) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de repuesto", false,
                    "Datos inválidos: existencias o precio negativos");
            return false;
        }

        // Actualizar datos
        repuesto.setNombre(nombre);
        repuesto.setMarca(marca);
        repuesto.setModelo(modelo);
        repuesto.setExistencias(existencias);
        repuesto.setPrecio(precio);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Actualización de repuesto", true,
                "Repuesto actualizado: " + id);

        return true;
    }

    /**
     * Actualiza un repuesto existente (versión adaptada)
     * 
     * @param idRepuesto  ID del repuesto a actualizar
     * @param nombre      Nuevo nombre
     * @param descripcion Nueva descripción (se usará para marca/modelo)
     * @param precio      Nuevo precio
     * @param cantidad    Nueva cantidad (se usará para existencias)
     * @return true si la operación fue exitosa
     */
    public static boolean actualizarRepuesto(int idRepuesto, String nombre, String descripcion, double precio,
            int cantidad) {
        // Buscar el repuesto por su ID
        Repuesto repuesto = buscarRepuestoPorId(idRepuesto);

        if (repuesto == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualizar Repuesto", false,
                    "No se encontró repuesto con ID: " + idRepuesto);
            return false;
        }

        // Actualizar propiedades que sabemos que existen
        repuesto.setNombre(nombre);
        repuesto.setPrecio(precio);

        // Usar marca y modelo como alternativa a descripción
        repuesto.setMarca(descripcion);
        repuesto.setModelo(descripcion);

        // Usar setExistencias en lugar de setCantidad
        repuesto.setExistencias(cantidad);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Actualizar Repuesto", true,
                "Repuesto #" + idRepuesto + " actualizado: " + nombre);

        return true;
    }

    /**
     * Elimina un repuesto del sistema
     * 
     * @return true si se eliminó correctamente
     */
    public static boolean eliminarRepuesto(int id) {
        Vector<Repuesto> repuestos = DataController.getRepuestos();

        for (int i = 0; i < repuestos.size(); i++) {
            if (repuestos.get(i).getId() == id) {
                repuestos.remove(i);

                // Guardar cambios
                DataController.guardarDatos();

                GestorBitacora.registrarEvento("Sistema", "Eliminación de repuesto", true,
                        "Repuesto eliminado: " + id);

                return true;
            }
        }

        GestorBitacora.registrarEvento("Sistema", "Eliminación de repuesto", false,
                "No se encontró repuesto con ID: " + id);

        return false;
    }

    /**
     * Ajusta el inventario de un repuesto
     * 
     * @return true si se ajustó correctamente
     */
    public static boolean ajustarInventario(int id, int cantidad) {
        // Buscar el repuesto
        Repuesto repuesto = buscarRepuestoPorId(id);

        if (repuesto == null) {
            GestorBitacora.registrarEvento("Sistema", "Ajuste de inventario", false,
                    "No se encontró repuesto con ID: " + id);
            return false;
        }

        // Aplicar el ajuste
        int nuevasExistencias = repuesto.getExistencias() + cantidad;

        // Verificar que no quede negativo
        if (nuevasExistencias < 0) {
            GestorBitacora.registrarEvento("Sistema", "Ajuste de inventario", false,
                    "El ajuste dejaría existencias negativas");
            return false;
        }

        repuesto.setExistencias(nuevasExistencias);

        // Guardar cambios
        DataController.guardarDatos();

        String tipoAjuste = cantidad > 0 ? "incremento" : "decremento";
        GestorBitacora.registrarEvento("Sistema", "Ajuste de inventario", true,
                "Repuesto " + id + ": " + tipoAjuste + " de " + Math.abs(cantidad) + " unidades");

        return true;
    }

    /**
     * Busca un repuesto por su ID
     * 
     * @return el repuesto encontrado o null
     */
    public static Repuesto buscarRepuestoPorId(int id) {
        for (Repuesto repuesto : DataController.getRepuestos()) {
            if (repuesto.getId() == id) {
                return repuesto;
            }
        }
        return null;
    }

    /**
     * Obtiene todos los repuestos del sistema
     */
    public static Vector<Repuesto> obtenerTodosLosRepuestos() {
        return DataController.getRepuestos();
    }

    /**
     * Obtiene repuestos compatibles con un automóvil específico
     */
    public static Vector<Repuesto> obtenerRepuestosCompatibles(Automovil automovil) {
        Vector<Repuesto> repuestosCompatibles = new Vector<>();

        if (automovil == null) {
            return repuestosCompatibles;
        }

        for (Repuesto repuesto : DataController.getRepuestos()) {
            if (repuesto.esCompatibleCon(automovil)) {
                repuestosCompatibles.add(repuesto);
            }
        }

        return repuestosCompatibles;
    }

    /**
     * Busca repuestos por nombre, marca o modelo
     */
    public static Vector<Repuesto> buscarRepuestos(String termino) {
        Vector<Repuesto> resultado = new Vector<>();
        String terminoLower = termino.toLowerCase();

        for (Repuesto repuesto : DataController.getRepuestos()) {
            if (repuesto.getNombre().toLowerCase().contains(terminoLower) ||
                    repuesto.getMarca().toLowerCase().contains(terminoLower) ||
                    repuesto.getModelo().toLowerCase().contains(terminoLower)) {
                resultado.add(repuesto);
            }
        }

        return resultado;
    }

    /**
     * Obtiene repuestos filtrados por marca y modelo
     */
    public static Vector<Repuesto> obtenerRepuestosPorMarcaModelo(String marca, String modelo) {
        Vector<Repuesto> filtrados = new Vector<>();

        for (Repuesto repuesto : DataController.getRepuestos()) {
            // Si el repuesto es para cualquier marca/modelo o coincide con los parámetros
            if (("cualquiera".equalsIgnoreCase(repuesto.getMarca()) || repuesto.getMarca().equalsIgnoreCase(marca)) &&
                    ("cualquiera".equalsIgnoreCase(repuesto.getModelo())
                            || repuesto.getModelo().equalsIgnoreCase(modelo))) {
                filtrados.add(repuesto);
            }
        }

        return filtrados;
    }

    /**
     * Obtiene los repuestos con bajo stock (menos de cierta cantidad)
     */
    public static Vector<Repuesto> obtenerRepuestosConExistenciasBajas(int umbral) {
        Vector<Repuesto> bajos = new Vector<>();

        for (Repuesto repuesto : DataController.getRepuestos()) {
            if (repuesto.getExistencias() <= umbral) {
                bajos.add(repuesto);
            }
        }

        return bajos;
    }

    /**
     * Obtiene los repuestos más utilizados (top N)
     */
    public static Vector<Repuesto> obtenerRepuestosMasUtilizados(int limite) {
        // Hacer una copia del vector de repuestos
        Vector<Repuesto> repuestos = new Vector<>(DataController.getRepuestos());

        // Ordenar por número de veces usado (orden descendente)
        repuestos.sort((r1, r2) -> Integer.compare(r2.getVecesUsado(), r1.getVecesUsado()));

        // Limitar el número de resultados
        if (repuestos.size() > limite) {
            return new Vector<>(repuestos.subList(0, limite));
        }

        return repuestos;
    }

    /**
     * Ordena repuestos por precio
     * 
     * @param ascendente true para orden ascendente, false para descendente
     */
    public static Vector<Repuesto> obtenerRepuestosOrdenadosPorPrecio(boolean ascendente) {
        // Hacer una copia del vector de repuestos
        Vector<Repuesto> repuestos = new Vector<>(DataController.getRepuestos());

        if (ascendente) {
            Collections.sort(repuestos);
        } else {
            Collections.sort(repuestos, Collections.reverseOrder());
        }

        return repuestos;
    }

    /**
     * Agrega un nuevo repuesto
     * 
     * @param nombre      Nombre del repuesto
     * @param descripcion Descripción (se usará como marca y modelo)
     * @param precio      Precio del repuesto
     * @param cantidad    Cantidad inicial
     * @return true si la operación fue exitosa
     */
    public static boolean agregarRepuesto(String nombre, String descripcion, double precio, int cantidad) {
        // Generar un nuevo ID para el repuesto
        int nuevoId = DataController.getNuevoIdRepuesto();

        // Crear el repuesto usando el constructor que existe
        // Formato: (id, nombre, marca, modelo, existencias, precio)
        Repuesto nuevoRepuesto = new Repuesto(nuevoId, nombre, descripcion, descripcion, cantidad, precio);

        // Agregar a la lista
        DataController.getRepuestos().add(nuevoRepuesto);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Agregar Repuesto", true,
                "Nuevo repuesto #" + nuevoId + " agregado: " + nombre);

        return true;
    }
}
