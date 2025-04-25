package com.example.utils;

import com.example.models.GestorDatos;
import com.example.models.GestorFacturas;
import com.example.models.GestorOrdenes;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase utilitaria para manejar la serialización y deserialización de datos del sistema
 */
public class Serializador {
    
    private static final Logger LOGGER = Logger.getLogger(Serializador.class.getName());
    
    // Rutas de archivos para serialización
    private static final String DATOS_PATH = "datos.bin";
    private static final String ORDENES_PATH = "ordenes.bin";
    private static final String FACTURAS_PATH = "facturas.bin";
    
    // Directorio donde se guardarán los datos
    private static final String DATA_DIR = "data";
    
    /**
     * Inicializa el sistema cargando datos serializados o creando nuevos si no existen
     */
    public static void inicializar() {
        // Crear directorio de datos si no existe
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            if (dataDir.mkdir()) {
                LOGGER.info("Directorio de datos creado: " + dataDir.getAbsolutePath());
            } else {
                LOGGER.warning("No se pudo crear el directorio de datos");
            }
        }
        
        boolean datosGenerales = cargarDatos();
        boolean ordenesServicio = cargarOrdenes();
        boolean facturas = cargarFacturas();
        
        if (!datosGenerales || !ordenesServicio || !facturas) {
            LOGGER.info("Algunos datos no pudieron cargarse. Se utilizarán datos iniciales.");
            
            // Si es primera ejecución, guardar datos iniciales
            guardarDatos();
            guardarOrdenes();
            guardarFacturas();
        }
    }
    
    /**
     * Guarda todos los datos del sistema
     * @return true si se guardaron con éxito, false en caso contrario
     */
    public static boolean guardarDatos() {
        return serializarObjeto(GestorDatos.getInstancia(), DATA_DIR + File.separator + DATOS_PATH);
    }
    
    /**
     * Carga los datos generales del sistema
     * @return true si se cargaron con éxito, false en caso contrario
     */
    public static boolean cargarDatos() {
        Object datos = deserializarObjeto(DATA_DIR + File.separator + DATOS_PATH);
        if (datos != null && datos instanceof GestorDatos) {
            GestorDatos.setInstancia((GestorDatos) datos);
            return true;
        }
        return false;
    }
    
    /**
     * Guarda las órdenes de servicio
     * @return true si se guardaron con éxito, false en caso contrario
     */
    public static boolean guardarOrdenes() {
        return serializarObjeto(GestorOrdenes.getInstancia(), DATA_DIR + File.separator + ORDENES_PATH);
    }
    
    /**
     * Carga las órdenes de servicio
     * @return true si se cargaron con éxito, false en caso contrario
     */
    public static boolean cargarOrdenes() {
        Object datos = deserializarObjeto(DATA_DIR + File.separator + ORDENES_PATH);
        if (datos != null && datos instanceof GestorOrdenes) {
            GestorOrdenes.setInstancia((GestorOrdenes) datos);
            return true;
        }
        return false;
    }
    
    /**
     * Guarda las facturas
     * @return true si se guardaron con éxito, false en caso contrario
     */
    public static boolean guardarFacturas() {
        return serializarObjeto(GestorFacturas.getInstancia(), DATA_DIR + File.separator + FACTURAS_PATH);
    }
    
    /**
     * Carga las facturas
     * @return true si se cargaron con éxito, false en caso contrario
     */
    public static boolean cargarFacturas() {
        Object datos = deserializarObjeto(DATA_DIR + File.separator + FACTURAS_PATH);
        if (datos != null && datos instanceof GestorFacturas) {
            GestorFacturas.setInstancia((GestorFacturas) datos);
            return true;
        }
        return false;
    }
    
    /**
     * Guarda todos los datos del sistema (método conveniente)
     * @return true si todos los datos se guardaron con éxito, false si alguno falló
     */
    public static boolean guardarTodo() {
        boolean datosSaved = guardarDatos();
        boolean ordenesSaved = guardarOrdenes();
        boolean facturasSaved = guardarFacturas();
        
        return datosSaved && ordenesSaved && facturasSaved;
    }
    
    /**
     * Serializa un objeto en un archivo
     * @param objeto El objeto a serializar
     * @param rutaArchivo La ruta donde guardar el archivo serializado
     * @return true si la serialización fue exitosa, false en caso contrario
     */
    private static boolean serializarObjeto(Object objeto, String rutaArchivo) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rutaArchivo))) {
            oos.writeObject(objeto);
            LOGGER.info("Objeto serializado correctamente en: " + rutaArchivo);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al serializar objeto: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deserializa un objeto desde un archivo
     * @param rutaArchivo La ruta del archivo serializado
     * @return El objeto deserializado o null si ocurrió un error
     */
    private static Object deserializarObjeto(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            LOGGER.info("El archivo no existe: " + rutaArchivo);
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object obj = ois.readObject();
            LOGGER.info("Objeto deserializado correctamente desde: " + rutaArchivo);
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error al deserializar objeto: " + e.getMessage(), e);
            return null;
        }
    }
}
