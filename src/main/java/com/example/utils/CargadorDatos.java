package com.example.utils;

import com.example.models.Automovil;
import com.example.models.GestorDatos;
import com.example.models.Repuesto;
import com.example.models.Servicio;
import com.example.models.personas.Cliente;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase utilitaria para cargar datos desde archivos externos
 */
public class CargadorDatos {

    private static final Logger LOGGER = Logger.getLogger(CargadorDatos.class.getName());

    /**
     * Carga repuestos desde un archivo .tmr
     */
    public static void cargarRepuestosDesdeArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Ignorar líneas que empiezan con //
                if (linea.trim().startsWith("//") || linea.trim().isEmpty())
                    continue;

                // Dividir la línea por guiones
                String[] datos = linea.split("-");
                if (datos.length >= 5) {
                    String nombre = datos[0].trim();
                    String marca = datos[1].trim();
                    String modelo = datos[2].trim();

                    // Parsear existencias y precio
                    int existencias = Integer.parseInt(datos[3].trim());
                    double precio = 0.0;
                    if (datos.length > 4) {
                        precio = Double.parseDouble(datos[4].trim());
                    }

                    // Crear el repuesto y agregarlo al gestor
                    Repuesto repuesto = new Repuesto(null, nombre, marca, modelo, existencias, precio);
                    GestorDatos.getInstancia().agregarRepuesto(repuesto);
                }
            }
            LOGGER.info("Repuestos cargados exitosamente desde: " + rutaArchivo);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los repuestos: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Error en formato de números al cargar repuestos: " + e.getMessage(), e);
        }
    }

    /**
     * Carga servicios desde un archivo .tms
     */
    public static void cargarServiciosDesdeArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            // Crear mapa de repuestos por código para fácil acceso
            Map<String, Repuesto> repuestosMap = new HashMap<>();
            for (Repuesto repuesto : GestorDatos.getInstancia().getRepuestos()) {
                repuestosMap.put(repuesto.getCodigo(), repuesto);
            }

            while ((linea = br.readLine()) != null) {
                // Ignorar líneas que empiezan con // o vacías
                if (linea.trim().startsWith("//") || linea.trim().isEmpty())
                    continue;

                // Dividir la línea por guiones
                String[] datos = linea.split("-");
                if (datos.length >= 5) {
                    String nombreServicio = datos[0].trim();
                    String marca = datos[1].trim();
                    String modelo = datos[2].trim();
                    String[] idsRepuestos = datos[3].trim().split(";");
                    double precioManoObra = Double.parseDouble(datos[4].trim());

                    // Tiempo estimado predeterminado (2 horas)
                    int tiempoEstimado = 2;
                    if (datos.length > 5) {
                        try {
                            tiempoEstimado = Integer.parseInt(datos[5].trim());
                        } catch (NumberFormatException e) {
                            // Usar valor predeterminado
                        }
                    }

                    // Descripción predeterminada
                    String descripcion = "Servicio de " + nombreServicio;
                    if (datos.length > 6) {
                        descripcion = datos[6].trim();
                    }

                    // Crear servicio
                    Servicio servicio = new Servicio(null, nombreServicio, marca, modelo,
                            precioManoObra, tiempoEstimado, descripcion);

                    // Agregar repuestos al servicio
                    for (String idRepuesto : idsRepuestos) {
                        Repuesto repuesto = repuestosMap.get(idRepuesto.trim());
                        if (repuesto != null) {
                            servicio.agregarRepuesto(repuesto);
                        }
                    }

                    // Agregar servicio al gestor
                    GestorDatos.getInstancia().agregarServicio(servicio);
                }
            }

            // Agregar servicio de diagnóstico especial (si no existe)
            boolean existeDiagnostico = false;
            for (Servicio servicio : GestorDatos.getInstancia().getServicios()) {
                if (servicio.getNombre().equalsIgnoreCase("Diagnóstico")) {
                    existeDiagnostico = true;
                    break;
                }
            }

            if (!existeDiagnostico) {
                Servicio diagnostico = new Servicio(null, "Diagnóstico", 100.0, 1,
                        "Evaluación inicial del vehículo para determinar problemas");
                GestorDatos.getInstancia().agregarServicio(diagnostico);
            }

            LOGGER.info("Servicios cargados exitosamente desde: " + rutaArchivo);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los servicios: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Error en formato de números al cargar servicios: " + e.getMessage(), e);
        }
    }

    /**
     * Carga clientes y sus automóviles desde un archivo .tmca
     */
    public static void cargarClientesYAutomovilesDesdeArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Ignorar líneas que empiezan con // o vacías
                if (linea.trim().startsWith("//") || linea.trim().isEmpty())
                    continue;

                // Dividir la línea por guiones
                String[] datos = linea.split("-");
                if (datos.length >= 6) {
                    String identificador = datos[0].trim();
                    String nombreCompleto = datos[1].trim();
                    String usuario = datos[2].trim();
                    String contraseña = datos[3].trim();
                    String tipoCliente = datos[4].trim();
                    String datosAutomoviles = datos[5].trim();

                    // Crear cliente
                    Cliente cliente = new Cliente(identificador, nombreCompleto, usuario, contraseña);
                    cliente.setTipoCliente(tipoCliente);

                    // Dividir y agregar automóviles
                    String[] automoviles = datosAutomoviles.split(";");
                    for (String datosAuto : automoviles) {
                        String[] atributosAuto = datosAuto.split(",");
                        if (atributosAuto.length >= 3) { // Al menos necesitamos placa, marca y modelo
                            String placa = atributosAuto[0].trim();
                            String marca = atributosAuto[1].trim();
                            String modelo = atributosAuto[2].trim();

                            // Valores por defecto
                            int anio = 2020;
                            String color = "Negro";
                            String tipo = "Sedan";

                            // Ruta de foto (opcional)
                            String rutaFoto = "";
                            if (atributosAuto.length > 3) {
                                rutaFoto = atributosAuto[3].trim();
                            }

                            // Crear automóvil con los valores disponibles
                            Automovil automovil = new Automovil(placa, marca, modelo, anio, color, tipo);
                            automovil.setRutaFoto(rutaFoto);

                            // Agregar al cliente
                            cliente.agregarAutomovil(automovil);

                            // También agregar al registro global de automóviles
                            GestorDatos.getInstancia().agregarAutomovil(automovil);
                        }
                    }

                    // Agregar cliente al gestor
                    GestorDatos.getInstancia().agregarPersona(cliente);
                }
            }
            LOGGER.info("Clientes y automóviles cargados exitosamente desde: " + rutaArchivo);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los clientes y automóviles: " + e.getMessage(), e);
        }
    }
}
