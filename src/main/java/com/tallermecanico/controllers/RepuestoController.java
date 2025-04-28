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
     * Modelo de datos para la tabla de repuestos
     */
    private static javax.swing.table.DefaultTableModel modeloRepuestos = new javax.swing.table.DefaultTableModel();

    /**
     * Registra un nuevo repuesto en el sistema
     * 
     * @return el repuesto creado o null si falló
     */
    public static Repuesto registrarRepuesto(String nombre, String marca, String modelo, int existencias,
            double precio) {
        String id = generarNuevoId(); // Debe generar un ID único tipo String
        Repuesto r = new Repuesto(id, nombre, marca, modelo, existencias, precio);
        DataController.getRepuestos().add(r); // Debe agregarlo a la lista global
        DataController.guardarDatos(); // Debe guardar los datos si usas persistencia
        // cargarDatos(); // Removed as the method is undefined
        modeloRepuestos.fireTableDataChanged();
        return r;
    }

    private static String generarNuevoId() {
        // Generar un nuevo ID único para el repuesto
        int nuevoId = DataController.getNuevoIdRepuesto();
        return String.valueOf(nuevoId);
    }

    /**
     * Registra un repuesto con un ID específico, verificando duplicados
     */
    public static Repuesto registrarRepuestoConId(String idRepuesto, String nombre, String marca, String modelo,
            int existencias, double precio) {
        // Validar que los datos sean correctos
        if (nombre == null || nombre.isEmpty() || existencias < 0 || precio < 0) {
            return null;
        }

        // SOLUCIÓN: Verificar si ya existe un repuesto con este ID
        for (Repuesto existente : DataController.getRepuestos()) {
            if (existente.getId().equals(idRepuesto)) {
                // Ya existe, no crear duplicado
                GestorBitacora.registrarEvento("Sistema", "Registro de repuesto", false,
                        "Se intentó registrar un repuesto con ID duplicado: " + idRepuesto);
                return existente; // Retornamos el existente en lugar de null
            }
        }

        // Crear el repuesto con ID específico solo si no existe
        Repuesto repuesto = new Repuesto();
        repuesto.setId(idRepuesto);
        repuesto.setNombre(nombre);
        repuesto.setMarca(marca);
        repuesto.setModelo(modelo);
        repuesto.setExistencias(existencias);
        repuesto.setPrecio(precio);

        // Agregar el repuesto al vector
        DataController.getRepuestos().add(repuesto);

        GestorBitacora.registrarEvento("Sistema", "Registro de repuesto", true,
                "Repuesto registrado: " + idRepuesto + " - " + nombre);

        return repuesto;
    }

    /**
     * Actualiza los datos de un repuesto existente
     * 
     * @return true si se actualizó correctamente
     */
    public static boolean actualizarRepuesto(String idRepuesto, String nombre, String marca,
            String modelo, int existencias, double precio) {
        // Buscar el repuesto
        Repuesto repuesto = buscarRepuestoPorId(idRepuesto);

        if (repuesto == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de repuesto", false,
                    "No se encontró repuesto con ID: " + idRepuesto);
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
                "Repuesto actualizado: " + idRepuesto);

        return true;
    }

    /**
     * Actualiza un repuesto existente (versión adaptada)
     * 
     * @deprecated Usar la versión con String idRepuesto
     */
    @Deprecated
    public static boolean actualizarRepuesto(int idRepuesto, String nombre, String descripcion, double precio,
            int cantidad) {
        // NO USAR, ELIMINAR O ADAPTAR A STRING
        return false;
    }

    /**
     * Elimina un repuesto del sistema
     * 
     * @return true si se eliminó correctamente
     */
    public static boolean eliminarRepuesto(String idRepuesto) {
        Vector<Repuesto> repuestos = DataController.getRepuestos();

        for (int i = 0; i < repuestos.size(); i++) {
            if (repuestos.get(i).getId().equals(String.valueOf(idRepuesto))) {
                repuestos.remove(i);

                // Guardar cambios
                DataController.guardarDatos();

                GestorBitacora.registrarEvento("Sistema", "Eliminación de repuesto", true,
                        "Repuesto eliminado: " + idRepuesto);

                return true;
            }
        }

        GestorBitacora.registrarEvento("Sistema", "Eliminación de repuesto", false,
                "No se encontró repuesto con ID: " + idRepuesto);

        return false;
    }

    /**
     * Ajusta el inventario de un repuesto
     * 
     * @deprecated Usar la versión con String idRepuesto
     */
    @Deprecated
    public static boolean ajustarInventario(int id, int cantidad) {
        // NO USAR, ELIMINAR O ADAPTAR A STRING
        return false;
    }

    /**
     * Busca un repuesto por su ID
     * 
     * @return el repuesto encontrado o null
     */
    public static Repuesto buscarRepuestoPorId(String idRepuesto) {
        for (Repuesto repuesto : DataController.getRepuestos()) {
            if (repuesto.getId().equals(idRepuesto)) {
                return repuesto;
            }
        }
        return null;
    }

    /**
     * Obtiene todos los repuestos del sistema
     */
    public static Vector<Repuesto> obtenerTodosLosRepuestos() {
        return new Vector<>(DataController.getRepuestos());
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
        // Generar un nuevo ID como String
        String nuevoId = String.valueOf(DataController.getNuevoIdRepuesto());

        // Crear el repuesto usando el constructor adecuado
        Repuesto nuevoRepuesto = new Repuesto();
        nuevoRepuesto.setId(nuevoId);
        nuevoRepuesto.setNombre(nombre);
        nuevoRepuesto.setMarca(descripcion);
        nuevoRepuesto.setModelo(descripcion);
        nuevoRepuesto.setExistencias(cantidad);
        nuevoRepuesto.setPrecio(precio);

        // Agregar a la lista
        DataController.getRepuestos().add(nuevoRepuesto);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Agregar Repuesto", true,
                "Nuevo repuesto #" + nuevoId + " agregado: " + nombre);

        return true;
    }

    /**
     * Obtiene los repuestos más caros (top N)
     */
    public static Vector<Repuesto> obtenerRepuestosMasCaros(int limite) {
        // Hacer una copia del vector de repuestos
        Vector<Repuesto> repuestos = new Vector<>(DataController.getRepuestos());

        // Ordenar por precio (orden descendente)
        for (int i = 0; i < repuestos.size() - 1; i++) {
            for (int j = 0; j < repuestos.size() - i - 1; j++) {
                if (repuestos.get(j).getPrecio() < repuestos.get(j + 1).getPrecio()) {
                    Repuesto temp = repuestos.get(j);
                    repuestos.set(j, repuestos.get(j + 1));
                    repuestos.set(j + 1, temp);
                }
            }
        }

        // Limitar el número de resultados
        if (repuestos.size() > limite) {
            Vector<Repuesto> limitados = new Vector<>();
            for (int i = 0; i < limite; i++) {
                limitados.add(repuestos.get(i));
            }
            return limitados;
        }

        return repuestos;
    }

    /**
     * Elimina duplicados del vector de repuestos basándose en el ID
     * 
     * @return cantidad de duplicados eliminados
     */
    public static int eliminarRepuestosDuplicados() {
        Vector<Repuesto> repuestos = DataController.getRepuestos();
        Vector<Repuesto> unicos = new Vector<>();
        Vector<String> idsAgregados = new Vector<>();
        int eliminados = 0;

        for (Repuesto r : repuestos) {
            if (!idsAgregados.contains(r.getId())) {
                unicos.add(r);
                idsAgregados.add(r.getId());
            } else {
                eliminados++;
                System.out.println("Eliminando repuesto duplicado: " + r.getId() + " - " + r.getNombre());
            }
        }

        // Actualizar el vector sin duplicados
        if (eliminados > 0) {
            DataController.setRepuestos(unicos);
            DataController.guardarDatos();
            GestorBitacora.registrarEvento("Sistema", "Limpieza de datos", true,
                    "Se eliminaron " + eliminados + " repuestos duplicados");
        }

        return eliminados;
    }
}
