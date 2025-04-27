package com.tallermecanico.utils;

import com.tallermecanico.controllers.DataController;

import java.io.*;

/**
 * Clase utilitaria para serializar y deserializar los datos del sistema
 */
public class Serializador {

    // Nombre del archivo donde se guardarán los datos
    private static final String ARCHIVO_DATOS = "datos_taller.bin";

    /**
     * Guarda los datos del sistema en un archivo serializado
     * 
     * @param datos Array de objetos a guardar
     * @return true si se guardó correctamente, false en caso contrario
     */
    public static boolean guardarDatos(Object[] datos) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            // Guardar el array completo
            oos.writeObject(datos);

            GestorBitacora.registrarEvento("Sistema", "Serialización", true,
                    "Datos guardados correctamente en " + ARCHIVO_DATOS);

            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar datos: " + e.getMessage());
            GestorBitacora.registrarEvento("Sistema", "Serialización", false,
                    "Error al guardar datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Carga los datos del sistema desde un archivo serializado
     * 
     * @return Array de objetos cargados o null si falló
     */
    public static Object[] cargarDatos() {
        File archivo = new File(ARCHIVO_DATOS);

        // Si no existe el archivo, retornar null
        if (!archivo.exists()) {
            System.out.println("No existe archivo de datos. Se crearán datos por defecto.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            // Leer el array completo
            Object[] datos = (Object[]) ois.readObject();

            GestorBitacora.registrarEvento("Sistema", "Deserialización", true,
                    "Datos cargados correctamente desde " + ARCHIVO_DATOS);

            return datos;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar datos: " + e.getMessage());
            GestorBitacora.registrarEvento("Sistema", "Deserialización", false,
                    "Error al cargar datos: " + e.getMessage());
            e.printStackTrace();

            // Si hay error, crear backup del archivo corrupto para análisis
            try {
                File backup = new File(ARCHIVO_DATOS + ".bak");
                if (archivo.renameTo(backup)) {
                    System.out.println("Se creó backup del archivo corrupto: " + backup.getAbsolutePath());
                }
            } catch (Exception ex) {
                System.err.println("Error al crear backup: " + ex.getMessage());
            }

            return null;
        }
    }

    /**
     * Inicializa los datos del sistema
     * Carga desde archivo si existe, o crea datos de prueba
     */
    public static void inicializarDatos() {
        DataController.inicializarDatos();
    }

    /**
     * Verifica el estado de los datos serializados
     * 
     * @return un reporte de estado
     */
    public static String verificarDatos() {
        StringBuilder reporte = new StringBuilder();

        // Verificar si existe el archivo
        File archivo = new File(ARCHIVO_DATOS);
        reporte.append("Archivo de datos: ").append(archivo.getAbsolutePath()).append("\n");
        reporte.append("Existe: ").append(archivo.exists()).append("\n");

        if (archivo.exists()) {
            reporte.append("Tamaño: ").append(archivo.length()).append(" bytes\n");
            reporte.append("Última modificación: ").append(new java.util.Date(archivo.lastModified())).append("\n\n");
        } else {
            reporte.append("El archivo no existe. Se creará con los datos por defecto.\n\n");
            return reporte.toString();
        }

        // Intentar deserializar para obtener una vista previa
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object[] datos = (Object[]) ois.readObject();

            reporte.append("El archivo contiene ").append(datos.length).append(" objetos serializados\n");

            // Agregar detalles de cada objeto
            for (int i = 0; i < datos.length; i++) {
                reporte.append("Objeto ").append(i + 1).append(": ")
                        .append(datos[i].getClass().getName()).append("\n");
            }

        } catch (IOException | ClassNotFoundException e) {
            reporte.append("Error al analizar el archivo: ").append(e.getMessage()).append("\n");
        }

        return reporte.toString();
    }

    /**
     * Elimina los archivos de datos serializados
     */
    public static void limpiarDatos() {
        File archivo = new File("datosSistema.dat");
        if (archivo.exists()) {
            if (archivo.delete()) {
                GestorBitacora.registrarEvento("Sistema", "Limpieza de datos", true,
                        "Archivo de datos eliminado correctamente");
            } else {
                GestorBitacora.registrarEvento("Sistema", "Limpieza de datos", false,
                        "No se pudo eliminar el archivo de datos");
            }
        }

        // Reiniciar datos en memoria
        DataController.reiniciarDatos();
    }
}
