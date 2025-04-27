package com.tallermecanico.controllers;

import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.utils.GestorBitacora;
import com.tallermecanico.utils.GeneradorPDF;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Controlador para la generación de reportes del sistema
 */
public class ReporteController {

    /**
     * Genera un reporte PDF del TOP 10 de repuestos más caros
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteRepuestosMasCaros(String rutaArchivo) {
        try {
            validarCarpetaReportes(); // Validar la carpeta antes de generar el reporte

            // Obtener datos
            Vector<Repuesto> repuestos = obtenerRepuestosMasCaros(10);

            // Delegar la generación a la clase GeneradorPDF
            GeneradorPDF.generarReporteRepuestosMasCaros(repuestos);

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de repuestos más caros generado: " + rutaArchivo);
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de repuestos más caros: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera un reporte PDF de distribución de clientes por tipo
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteClientesPorTipo(String rutaArchivo) {
        try {
            validarCarpetaReportes(); // Validar que la carpeta "reportes" exista

            // Obtener datos
            Vector<Cliente> clientesNormales = obtenerClientesPorTipo("normal");
            Vector<Cliente> clientesOro = obtenerClientesPorTipo("oro");

            // Delegar la generación a la clase GeneradorPDF
            GeneradorPDF.generarReporteClientesPorTipo(clientesOro, clientesNormales);

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de clientes por tipo generado: " + rutaArchivo);
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de clientes por tipo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera un reporte PDF del TOP 10 de repuestos más usados
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteRepuestosMasUsados(String rutaArchivo) {
        try {
            validarCarpetaReportes(); // Validar que la carpeta "reportes" exista

            // Obtener datos
            Vector<Repuesto> repuestos = obtenerRepuestosMasUtilizados(10);

            // Delegar la generación a la clase GeneradorPDF
            GeneradorPDF.generarReporteRepuestosMasUsados(repuestos);

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de repuestos más usados generado: " + rutaArchivo);
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de repuestos más usados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera un reporte PDF del TOP 10 de servicios más usados
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteServiciosMasUsados(String rutaArchivo) {
        try {
            validarCarpetaReportes(); // Validar que la carpeta "reportes" exista

            // Obtener datos
            Vector<Servicio> servicios = obtenerServiciosMasUtilizados(10);

            // Delegar la generación a la clase GeneradorPDF
            GeneradorPDF.generarReporteServiciosMasUsados(servicios);

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de servicios más usados generado: " + rutaArchivo);
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de servicios más usados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera un reporte PDF del TOP 5 de automóviles más repetidos
     * 
     * @param rutaArchivo Ruta donde se guardará el archivo PDF
     */
    public static void generarReporteAutomovilesMasRepetidos(String rutaArchivo) {
        try {
            validarCarpetaReportes(); // Validar que la carpeta "reportes" exista

            // Obtener datos
            Map<String, Object> datos = obtenerAutomovilesMasRepetidos(5);
            Vector<Map.Entry<String, Integer>> topModelos = (Vector<Map.Entry<String, Integer>>) datos
                    .get("topModelos");
            Map<String, Automovil> ejemplares = (Map<String, Automovil>) datos.get("ejemplares");
            Map<String, Vector<Cliente>> clientesPorModelo = (Map<String, Vector<Cliente>>) datos
                    .get("clientesPorModelo");

            // Delegar la generación a la clase GeneradorPDF
            GeneradorPDF.generarReporteAutomovilesMasRepetidos(topModelos, ejemplares, clientesPorModelo);

            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", true,
                    "Reporte de automóviles más repetidos generado: " + rutaArchivo);
        } catch (Exception e) {
            GestorBitacora.registrarEvento("Sistema", "Generación de PDF", false,
                    "Error al generar reporte de automóviles más repetidos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valida que la carpeta de reportes exista, si no, la crea
     */
    private static void validarCarpetaReportes() {
        File carpetaReportes = new File("reportes");
        if (!carpetaReportes.exists()) {
            carpetaReportes.mkdir();
        }
    }

    /**
     * Obtiene la lista de clientes separados por tipo
     * 
     * @param tipo Tipo de cliente a filtrar ("oro" o "normal")
     * @return Vector con los clientes del tipo especificado
     */
    public static Vector<Cliente> obtenerClientesPorTipo(String tipo) {
        Vector<Cliente> resultado = new Vector<>();
        Vector<Cliente> clientes = DataController.getClientes();

        if (clientes == null || clientes.isEmpty() || tipo == null) {
            return resultado; // Vector vacío
        }

        // Buscar los clientes del tipo especificado
        for (Cliente cliente : clientes) {
            if (tipo.equalsIgnoreCase(cliente.getTipoCliente())) {
                resultado.add(cliente);
            }
        }

        GestorBitacora.registrarEvento("Sistema", "Reporte", true,
                "Obtenidos " + resultado.size() + " clientes de tipo " + tipo);

        return resultado;
    }

    /**
     * Obtiene los repuestos más utilizados en el sistema
     * 
     * @param limite Cantidad máxima de repuestos a retornar
     * @return Vector con los repuestos más utilizados, ordenados por frecuencia
     */
    public static Vector<Repuesto> obtenerRepuestosMasUtilizados(int limite) {
        // Obtener copia de todos los repuestos
        Vector<Repuesto> repuestos = new Vector<>();
        if (DataController.getRepuestos() != null) {
            repuestos = new Vector<>(DataController.getRepuestos());
        }

        if (repuestos.isEmpty()) {
            return repuestos; // Vector vacío
        }

        // Ordenar por veces usado (descendente) - usando método burbuja
        for (int i = 0; i < repuestos.size() - 1; i++) {
            for (int j = 0; j < repuestos.size() - i - 1; j++) {
                if (repuestos.get(j).getVecesUsado() < repuestos.get(j + 1).getVecesUsado()) {
                    // Intercambiar posiciones
                    Repuesto temp = repuestos.get(j);
                    repuestos.set(j, repuestos.get(j + 1));
                    repuestos.set(j + 1, temp);
                }
            }
        }

        // Limitar cantidad de resultados
        if (repuestos.size() > limite) {
            Vector<Repuesto> limitados = new Vector<>();
            for (int i = 0; i < limite && i < repuestos.size(); i++) {
                limitados.add(repuestos.get(i));
            }
            repuestos = limitados;
        }

        GestorBitacora.registrarEvento("Sistema", "Reporte", true,
                "Obtenidos TOP " + repuestos.size() + " repuestos más utilizados");

        return repuestos;
    }

    /**
     * Obtiene los repuestos más caros del sistema
     * 
     * @param limite Cantidad máxima de repuestos a retornar
     * @return Vector con los repuestos más caros, ordenados por precio
     */
    public static Vector<Repuesto> obtenerRepuestosMasCaros(int limite) {
        // Obtener copia de todos los repuestos
        Vector<Repuesto> repuestos = new Vector<>();
        if (DataController.getRepuestos() != null) {
            repuestos = new Vector<>(DataController.getRepuestos());
        }

        if (repuestos.isEmpty()) {
            return repuestos; // Vector vacío
        }

        // Ordenar por precio (descendente) - usando método burbuja
        for (int i = 0; i < repuestos.size() - 1; i++) {
            for (int j = 0; j < repuestos.size() - i - 1; j++) {
                if (repuestos.get(j).getPrecio() < repuestos.get(j + 1).getPrecio()) {
                    // Intercambiar posiciones
                    Repuesto temp = repuestos.get(j);
                    repuestos.set(j, repuestos.get(j + 1));
                    repuestos.set(j + 1, temp);
                }
            }
        }

        // Limitar cantidad de resultados
        if (repuestos.size() > limite) {
            Vector<Repuesto> limitados = new Vector<>();
            for (int i = 0; i < limite && i < repuestos.size(); i++) {
                limitados.add(repuestos.get(i));
            }
            repuestos = limitados;
        }

        GestorBitacora.registrarEvento("Sistema", "Reporte", true,
                "Obtenidos TOP " + repuestos.size() + " repuestos más caros");

        return repuestos;
    }

    /**
     * Obtiene los servicios más utilizados en el sistema
     * 
     * @param limite Cantidad máxima de servicios a retornar
     * @return Vector con los servicios más utilizados, ordenados por frecuencia
     */
    public static Vector<Servicio> obtenerServiciosMasUtilizados(int limite) {
        // Obtener copia de todos los servicios
        Vector<Servicio> servicios = new Vector<>();
        if (DataController.getServicios() != null) {
            servicios = new Vector<>(DataController.getServicios());
        }

        if (servicios.isEmpty()) {
            return servicios; // Vector vacío
        }

        // Ordenar por veces usado (descendente) - usando método burbuja
        for (int i = 0; i < servicios.size() - 1; i++) {
            for (int j = 0; j < servicios.size() - i - 1; j++) {
                if (servicios.get(j).getVecesUsado() < servicios.get(j + 1).getVecesUsado()) {
                    // Intercambiar posiciones
                    Servicio temp = servicios.get(j);
                    servicios.set(j, servicios.get(j + 1));
                    servicios.set(j + 1, temp);
                }
            }
        }

        // Limitar cantidad de resultados
        if (servicios.size() > limite) {
            Vector<Servicio> limitados = new Vector<>();
            for (int i = 0; i < limite && i < servicios.size(); i++) {
                limitados.add(servicios.get(i));
            }
            servicios = limitados;
        }

        GestorBitacora.registrarEvento("Sistema", "Reporte", true,
                "Obtenidos TOP " + servicios.size() + " servicios más utilizados");

        return servicios;
    }

    /**
     * Obtiene los automóviles más repetidos entre todos los clientes
     * 
     * @param limite Cantidad máxima de modelos a retornar
     * @return Mapa con los datos necesarios para el reporte
     */
    public static Map<String, Object> obtenerAutomovilesMasRepetidos(int limite) {
        Map<String, Object> resultado = new HashMap<>();
        Map<String, Integer> conteoModelos = new HashMap<>();
        Map<String, Automovil> ejemplares = new HashMap<>();
        Map<String, Vector<Cliente>> clientesPorModelo = new HashMap<>();

        // Contar ocurrencias de cada modelo de automóvil
        Vector<Cliente> clientes = DataController.getClientes();
        if (clientes != null) {
            for (Cliente cliente : clientes) {
                if (cliente.getAutomoviles() == null)
                    continue;

                for (Automovil auto : cliente.getAutomoviles()) {
                    String clave = auto.getMarca() + " " + auto.getModelo();

                    // Contar ocurrencia
                    conteoModelos.put(clave, conteoModelos.getOrDefault(clave, 0) + 1);

                    // Guardar un ejemplar
                    if (!ejemplares.containsKey(clave)) {
                        ejemplares.put(clave, auto);
                    }

                    // Agregar cliente a la lista
                    if (!clientesPorModelo.containsKey(clave)) {
                        clientesPorModelo.put(clave, new Vector<>());
                    }
                    clientesPorModelo.get(clave).add(cliente);
                }
            }
        }

        // Ordenar y obtener los más comunes
        Vector<Map.Entry<String, Integer>> entradas = new Vector<>(conteoModelos.entrySet());
        entradas.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Tomar solo los primeros (según el límite)
        int cantidad = Math.min(limite, entradas.size());
        Vector<Map.Entry<String, Integer>> topModelos = new Vector<>();
        for (int i = 0; i < cantidad; i++) {
            topModelos.add(entradas.get(i));
        }

        // Guardar los resultados en el mapa
        resultado.put("topModelos", topModelos);
        resultado.put("ejemplares", ejemplares);
        resultado.put("clientesPorModelo", clientesPorModelo);

        GestorBitacora.registrarEvento("Sistema", "Reporte", true,
                "Obtenidos TOP " + topModelos.size() + " automóviles más repetidos");

        return resultado;
    }

    /**
     * Obtiene todos los clientes del sistema para reportes
     * 
     * @return Vector con todos los clientes
     */
    public static Vector<Cliente> obtenerTodosLosClientes() {
        Vector<Cliente> clientes = DataController.getClientes();

        if (clientes == null) {
            return new Vector<>();
        }

        // Ordenar por DPI
        DataController.ordenarClientesPorDPI();

        GestorBitacora.registrarEvento("Sistema", "Reporte", true,
                "Obtenidos " + clientes.size() + " clientes totales");

        return clientes;
    }
}
