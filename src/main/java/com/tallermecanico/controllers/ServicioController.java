package com.tallermecanico.controllers;

import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.utils.GestorBitacora;

import java.util.Vector;

/**
 * Controlador para la gestión de servicios
 */
public class ServicioController {

    /**
     * Registra un nuevo servicio en el sistema
     * 
     * @return el servicio creado o null si falló
     */
    public static Servicio registrarServicio(String nombre, String marca, String modelo,
            double precioManoObra) {
        // Verificar que no exista un servicio idéntico
        for (Servicio s : DataController.getServicios()) {
            if (s.getNombre().equalsIgnoreCase(nombre) &&
                    s.getMarca().equalsIgnoreCase(marca) &&
                    s.getModelo().equalsIgnoreCase(modelo)) {

                GestorBitacora.registrarEvento("Sistema", "Registro de servicio", false,
                        "Ya existe un servicio idéntico: " + nombre + " para " + marca + " " + modelo);
                return null;
            }
        }

        // Crear el nuevo servicio
        int nuevoId = DataController.getNuevoIdServicio();
        Servicio nuevoServicio = new Servicio(nuevoId, nombre, marca, modelo, precioManoObra);

        // Agregarlo al vector de servicios
        DataController.getServicios().add(nuevoServicio);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Registro de servicio", true,
                "Servicio registrado: " + nuevoServicio.getId() + " - " + nombre);

        return nuevoServicio;
    }

    /**
     * Agrega un nuevo servicio al sistema
     * 
     * @param nombre      Nombre del servicio
     * @param descripcion Descripción del servicio
     * @param precioBase  Precio base del servicio
     * @return true si la operación fue exitosa
     */
    public static boolean agregarServicio(String nombre, String descripcion, double precioBase) {
        // Verificar datos
        if (nombre == null || nombre.trim().isEmpty() || precioBase <= 0) {
            GestorBitacora.registrarEvento("Sistema", "Agregar Servicio", false,
                    "Datos inválidos para nuevo servicio");
            return false;
        }

        // Generar nuevo ID
        int nuevoId = DataController.getNuevoIdServicio();

        // Crear el nuevo servicio
        Servicio nuevoServicio = new Servicio(nuevoId, nombre, descripcion, descripcion, precioBase);

        // Agregar a la lista de servicios
        DataController.getServicios().add(nuevoServicio);

        // Guardar cambios
        DataController.guardarDatos();

        // Registrar en bitácora
        GestorBitacora.registrarEvento("Sistema", "Agregar Servicio", true,
                "Servicio #" + nuevoId + " agregado: " + nombre);

        return true;
    }

    /**
     * Actualiza los datos de un servicio existente
     * 
     * @return true si se actualizó correctamente
     */
    public static boolean actualizarServicio(int id, String nombre, String marca,
            String modelo, double precioManoObra) {
        Servicio servicio = buscarServicioPorId(id);

        if (servicio == null) {
            GestorBitacora.registrarEvento("Sistema", "Actualización de servicio", false,
                    "No se encontró servicio con ID: " + id);
            return false;
        }

        // Actualizar datos
        servicio.setNombre(nombre);
        servicio.setMarca(marca);
        servicio.setModelo(modelo);
        servicio.setPrecioManoObra(precioManoObra);

        // Guardar cambios
        DataController.guardarDatos();

        GestorBitacora.registrarEvento("Sistema", "Actualización de servicio", true,
                "Servicio actualizado: " + id);

        return true;
    }

    /**
     * Elimina un servicio del sistema
     * 
     * @return true si se eliminó correctamente
     */
    public static boolean eliminarServicio(int id) {
        Vector<Servicio> servicios = DataController.getServicios();

        for (int i = 0; i < servicios.size(); i++) {
            if (servicios.get(i).getId() == id) {
                servicios.remove(i);

                // Guardar cambios
                DataController.guardarDatos();

                GestorBitacora.registrarEvento("Sistema", "Eliminación de servicio", true,
                        "Servicio eliminado: " + id);

                return true;
            }
        }

        GestorBitacora.registrarEvento("Sistema", "Eliminación de servicio", false,
                "No se encontró servicio con ID: " + id);

        return false;
    }

    /**
     * Busca un servicio por su ID
     * 
     * @return el servicio encontrado o null
     */
    public static Servicio buscarServicioPorId(int id) {
        for (Servicio servicio : DataController.getServicios()) {
            if (servicio.getId() == id) {
                return servicio;
            }
        }
        return null;
    }

    /**
     * Agrega un repuesto a un servicio
     * 
     * @return true si se agregó correctamente
     */
    public static boolean agregarRepuestoAServicio(int idServicio, String idRepuesto) {
        Servicio servicio = buscarServicioPorId(idServicio);
        if (servicio == null) {
            GestorBitacora.registrarEvento("Sistema", "Agregar repuesto a servicio", false,
                    "No se encontró servicio con ID: " + idServicio);
            return false;
        }

        Repuesto repuesto = RepuestoController.buscarRepuestoPorId(idRepuesto);
        if (repuesto == null) {
            GestorBitacora.registrarEvento("Sistema", "Agregar repuesto a servicio", false,
                    "No se encontró repuesto con ID: " + idRepuesto);
            return false;
        }

        // Verificar si el repuesto ya está en el servicio
        for (Repuesto r : servicio.getRepuestos()) {
            if (r.getId().equals(String.valueOf(idRepuesto))) {
                GestorBitacora.registrarEvento("Sistema", "Agregar repuesto a servicio", false,
                        "El repuesto ya está en el servicio: " + idRepuesto);
                return false;
            }
        }

        // Agregar el repuesto al servicio
        boolean resultado = servicio.agregarRepuesto(repuesto);

        if (resultado) {
            // Guardar cambios
            DataController.guardarDatos();

            GestorBitacora.registrarEvento("Sistema", "Agregar repuesto a servicio", true,
                    "Repuesto " + idRepuesto + " agregado al servicio " + idServicio);
        } else {
            GestorBitacora.registrarEvento("Sistema", "Agregar repuesto a servicio", false,
                    "El repuesto no es compatible con el servicio");
        }

        return resultado;
    }

    /**
     * Quita un repuesto de un servicio
     * 
     * @return true si se quitó correctamente
     */
    public static boolean quitarRepuestoDeServicio(int idServicio, int idRepuesto) {
        Servicio servicio = buscarServicioPorId(idServicio);

        if (servicio == null) {
            GestorBitacora.registrarEvento("Sistema", "Quitar repuesto de servicio", false,
                    "No se encontró servicio con ID: " + idServicio);
            return false;
        }

        boolean resultado = servicio.quitarRepuesto(idRepuesto);

        if (resultado) {
            // Guardar cambios
            DataController.guardarDatos();

            GestorBitacora.registrarEvento("Sistema", "Quitar repuesto de servicio", true,
                    "Repuesto " + idRepuesto + " quitado del servicio " + idServicio);
        } else {
            GestorBitacora.registrarEvento("Sistema", "Quitar repuesto de servicio", false,
                    "No se encontró el repuesto en el servicio");
        }

        return resultado;
    }

    /**
     * Obtiene todos los servicios del sistema
     */
    public static Vector<Servicio> obtenerTodosLosServicios() {
        return DataController.getServicios();
    }

    /**
     * Obtiene servicios compatibles con un automóvil específico
     */
    public static Vector<Servicio> obtenerServiciosCompatibles(Automovil auto) {
        Vector<Servicio> serviciosCompatibles = new Vector<>();
        Vector<Servicio> servicios = DataController.getServicios();

        for (Servicio servicio : servicios) {
            // Caso 1: El servicio es para cualquier marca y cualquier modelo
            boolean esCualquiera = servicio.getMarca().equalsIgnoreCase("cualquiera") &&
                    servicio.getModelo().equalsIgnoreCase("cualquiera");

            // Caso 2: Coincidencia exacta de marca y modelo
            boolean coincideExacto = servicio.getMarca().equalsIgnoreCase(auto.getMarca()) &&
                    servicio.getModelo().equalsIgnoreCase(auto.getModelo());

            // Caso 3: Coincidencia de marca con modelo "cualquiera"
            boolean coincideMarca = servicio.getMarca().equalsIgnoreCase(auto.getMarca()) &&
                    servicio.getModelo().equalsIgnoreCase("cualquiera");

            // Caso 4: Modelo específico para cualquier marca
            boolean coincideModelo = servicio.getMarca().equalsIgnoreCase("cualquiera") &&
                    servicio.getModelo().equalsIgnoreCase(auto.getModelo());

            if (esCualquiera || coincideExacto || coincideMarca || coincideModelo) {
                serviciosCompatibles.add(servicio);
            }
        }

        return serviciosCompatibles;
    }

    /**
     * Obtiene servicios filtrados por marca y modelo
     */
    public static Vector<Servicio> obtenerServiciosPorMarcaModelo(String marca, String modelo) {
        Vector<Servicio> filtrados = new Vector<>();

        for (Servicio servicio : DataController.getServicios()) {
            // Si el servicio es para cualquier marca/modelo o coincide con los parámetros
            if (("cualquiera".equalsIgnoreCase(servicio.getMarca()) || servicio.getMarca().equalsIgnoreCase(marca)) &&
                    ("cualquiera".equalsIgnoreCase(servicio.getModelo())
                            || servicio.getModelo().equalsIgnoreCase(modelo))) {
                filtrados.add(servicio);
            }
        }

        return filtrados;
    }

    /**
     * Ordena servicios por precio (de menor a mayor)
     */
    public static Vector<Servicio> ordenarServiciosPorPrecio(Vector<Servicio> servicios, boolean ascendente) {
        Vector<Servicio> resultado = new Vector<>(servicios);

        // Usar algoritmo de ordenamiento por inserción
        for (int i = 1; i < resultado.size(); i++) {
            Servicio key = resultado.get(i);
            int j = i - 1;

            if (ascendente) {
                while (j >= 0 && resultado.get(j).getPrecioTotal() > key.getPrecioTotal()) {
                    resultado.set(j + 1, resultado.get(j));
                    j = j - 1;
                }
            } else {
                while (j >= 0 && resultado.get(j).getPrecioTotal() < key.getPrecioTotal()) {
                    resultado.set(j + 1, resultado.get(j));
                    j = j - 1;
                }
            }

            resultado.set(j + 1, key);
        }

        return resultado;
    }

    /**
     * Obtiene los servicios más utilizados (top N)
     */
    public static Vector<Servicio> obtenerServiciosMasUtilizados(int limite) {
        // Hacer una copia del vector de servicios
        Vector<Servicio> servicios = new Vector<>(DataController.getServicios());

        // Ordenar por número de veces usado (orden descendente)
        servicios.sort((s1, s2) -> Integer.compare(s2.getVecesUsado(), s1.getVecesUsado()));

        // Limitar el número de resultados
        if (servicios.size() > limite) {
            return new Vector<>(servicios.subList(0, limite));
        }

        return servicios;
    }

    public static Vector<Servicio> buscarServicios(String texto) {
        Vector<Servicio> serviciosEncontrados = new Vector<>();

        for (Servicio servicio : DataController.getServicios()) {
            if (servicio.getNombre().toLowerCase().contains(texto.toLowerCase()) ||
                    servicio.getMarca().toLowerCase().contains(texto.toLowerCase()) ||
                    servicio.getModelo().toLowerCase().contains(texto.toLowerCase())) {
                serviciosEncontrados.add(servicio);
            }
        }

        return serviciosEncontrados;
    }
}
