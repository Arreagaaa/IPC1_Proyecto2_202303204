package com.tallermecanico.utils;

import com.tallermecanico.controllers.ClienteController;
import com.tallermecanico.controllers.RepuestoController;
import com.tallermecanico.controllers.ServicioController;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.views.AdminView;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para cargar y gestionar archivos en el sistema
 */
public class CargadorArchivos {

    // Carpeta donde se guardarán las imágenes de los automóviles
    private static final String CARPETA_IMAGENES = "img_carros";

    /**
     * Crea la carpeta de imágenes si no existe
     */
    private static void crearCarpetaImagenes() {
        File carpeta = new File(CARPETA_IMAGENES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
    }

    /**
     * Permite al usuario seleccionar una imagen y la copia a la carpeta de imágenes
     * 
     * @param parent          Componente padre para el diálogo
     * @param nombrePreferido Nombre preferido para el archivo (se agregará
     *                        extensión)
     * @return Ruta relativa a la imagen guardada o null si fue cancelado
     */
    public static String seleccionarYGuardarImagen(Component parent, String nombrePreferido) {
        // Crear diálogo para seleccionar archivos
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Imagen");

        // Filtrar solo archivos de imagen
        javax.swing.filechooser.FileFilter imageFilter = new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de Imagen", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(imageFilter);

        // Mostrar diálogo
        int resultado = fileChooser.showOpenDialog(parent);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            String extension = obtenerExtension(archivoSeleccionado.getName());

            // Crear nombre de archivo único
            String nombreArchivo = nombrePreferido + "." + extension;

            // Asegurarse de que existe la carpeta de imágenes
            crearCarpetaImagenes();

            // Crear archivo de destino
            File archivoDestino = new File(CARPETA_IMAGENES + File.separator + nombreArchivo);

            // Si ya existe, generar nombre único
            if (archivoDestino.exists()) {
                nombreArchivo = nombrePreferido + "_" + System.currentTimeMillis() + "." + extension;
                archivoDestino = new File(CARPETA_IMAGENES + File.separator + nombreArchivo);
            }

            try {
                // Copiar archivo
                copiarArchivo(archivoSeleccionado, archivoDestino);

                // Retornar ruta relativa
                return CARPETA_IMAGENES + "/" + nombreArchivo;
            } catch (IOException e) {
                GestorBitacora.registrarEvento("Sistema", "Carga de imagen", false,
                        "Error al copiar imagen: " + e.getMessage());
                JOptionPane.showMessageDialog(parent,
                        "Error al guardar la imagen: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Si se canceló o hubo error
        return null;
    }

    /**
     * Obtiene la extensión de un nombre de archivo
     */
    private static String obtenerExtension(String nombreArchivo) {
        int i = nombreArchivo.lastIndexOf('.');
        if (i > 0) {
            return nombreArchivo.substring(i + 1).toLowerCase();
        }
        return "jpg"; // Extensión por defecto
    }

    /**
     * Copia un archivo a otro destino
     */
    private static void copiarArchivo(File origen, File destino) throws IOException {
        try (FileInputStream fis = new FileInputStream(origen);
                FileOutputStream fos = new FileOutputStream(destino);
                FileChannel canalOrigen = fis.getChannel();
                FileChannel canalDestino = fos.getChannel()) {
            canalOrigen.transferTo(0, canalOrigen.size(), canalDestino);
        }
    }

    /**
     * Carga repuestos desde un archivo con extensión .tmr
     * Formato: ID-Nombre-Marca-Modelo-Existencias-Precio
     * O simplemente: Nombre-Marca-Modelo-Existencias-Precio
     * 
     * @param archivo File con el archivo a cargar
     * @return Número de repuestos cargados con éxito
     */
    public static int cargarRepuestos(File archivo) {
        if (archivo == null || !archivo.exists()) {
            System.err.println(
                    "Archivo de repuestos no encontrado: " + (archivo != null ? archivo.getAbsolutePath() : "null"));
            return 0;
        }

        System.out.println("Cargando repuestos desde: " + archivo.getAbsolutePath());
        int contadorExito = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int lineaNum = 0;

            while ((linea = br.readLine()) != null) {
                lineaNum++;
                // Omitir líneas vacías o comentarios
                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }

                try {
                    // El formato esperado es: ID-Nombre-Marca-Modelo-Existencias-Precio
                    // O simplemente: Nombre-Marca-Modelo-Existencias-Precio
                    String[] partes = linea.split("-");
                    int startIndex = 0;

                    // Si la primera parte parece un ID (empieza con REP), ignorarlo
                    if (partes.length >= 6 && partes[0].trim().toUpperCase().startsWith("REP")) {
                        startIndex = 1;
                    }

                    if (partes.length < (startIndex + 5)) {
                        System.err.println("Línea " + lineaNum + " inválida, formato incorrecto: " + linea);
                        continue;
                    }

                    String nombre = partes[startIndex].trim();
                    String marca = partes[startIndex + 1].trim();
                    String modelo = partes[startIndex + 2].trim();

                    // Intentar diferentes formatos de números
                    String existenciasStr = partes[startIndex + 3].trim().replace(",", ".");
                    String precioStr = partes[startIndex + 4].trim().replace(",", ".");

                    int existencias = Integer.parseInt(existenciasStr);
                    double precio = Double.parseDouble(precioStr);

                    if (nombre.isEmpty() || existencias < 0 || precio < 0) {
                        System.err.println("Línea " + lineaNum + " inválida, valores incorrectos: " + linea);
                        continue;
                    }

                    // Registrar el repuesto con un ID específico si viene en la línea
                    boolean exitoso;
                    if (startIndex == 1) {
                        String idRepuesto = partes[0].trim();
                        exitoso = RepuestoController.registrarRepuestoConId(idRepuesto, nombre, marca, modelo,
                                existencias, precio) != null;
                    } else {
                        exitoso = RepuestoController.registrarRepuesto(nombre, marca, modelo, existencias,
                                precio) != null;
                    }

                    if (exitoso) {
                        contadorExito++;
                        System.out.println("Repuesto cargado: " + nombre + " (" + marca + " " + modelo + ")");
                    }
                } catch (Exception e) {
                    System.err.println("Error en línea " + lineaNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer archivo de repuestos: " + e.getMessage());
        }

        return contadorExito;
    }

    /**
     * Carga servicios desde un archivo con extensión .tms
     * Formato: nombreServicio-marca-modelo-lista repuestos-precioManoDeObra
     * 
     * @param archivo File con el archivo a cargar
     * @return Número de servicios cargados con éxito
     */
    public static int cargarServicios(File archivo) {
        if (archivo == null || !archivo.exists() || !archivo.getName().endsWith(".tms")) {
            GestorBitacora.registrarEvento("Sistema", "Carga de servicios", false,
                    "Archivo inválido o no encontrado: " + (archivo != null ? archivo.getName() : "null"));
            return 0;
        }

        int contadorExito = 0;
        int lineaActual = 0;
        List<String> errores = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                lineaActual++;

                // Omitir líneas vacías o comentarios
                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }

                // Procesar la línea
                try {
                    String[] partes = linea.split("-");

                    if (partes.length < 5) {
                        errores.add("Línea " + lineaActual + ": Formato incorrecto, se esperaban 5 campos");
                        continue;
                    }

                    String nombre = partes[0].trim();
                    String marca = partes[1].trim();
                    String modelo = partes[2].trim();
                    String listaRepuestosStr = partes[3].trim();
                    double precioManoObra = Double.parseDouble(partes[4].trim());

                    if (precioManoObra < 0) {
                        errores.add(
                                "Línea " + lineaActual + ": El precio de mano de obra debe ser mayor o igual a cero");
                        continue;
                    }

                    // Crear el servicio primero
                    Servicio servicio = ServicioController.registrarServicio(nombre, marca, modelo, precioManoObra);

                    if (servicio == null) {
                        errores.add("Línea " + lineaActual + ": No se pudo registrar el servicio");
                        continue;
                    }

                    // Procesar y añadir repuestos al servicio
                    if (!listaRepuestosStr.isEmpty()) {
                        String[] idsRepuestos = listaRepuestosStr.split(";");

                        for (String idStr : idsRepuestos) {
                            if (!idStr.trim().isEmpty()) {
                                try {
                                    String idRepuesto = idStr.trim();
                                    boolean agregado = ServicioController.agregarRepuestoAServicio(servicio.getId(),
                                            idRepuesto);

                                    if (!agregado) {
                                        errores.add("Línea " + lineaActual + ": No se pudo agregar el repuesto ID=" +
                                                idRepuesto + " al servicio " + servicio.getId());
                                    }
                                } catch (NumberFormatException e) {
                                    errores.add("Línea " + lineaActual + ": ID de repuesto inválido: " + idStr);
                                }
                            }
                        }
                    }

                    contadorExito++;

                } catch (NumberFormatException e) {
                    errores.add("Línea " + lineaActual + ": Error en formato numérico - " + e.getMessage());
                } catch (Exception e) {
                    errores.add("Línea " + lineaActual + ": " + e.getMessage());
                }
            }

            // Reportar resultados
            String mensaje = "Servicios cargados: " + contadorExito + " de " + lineaActual + " líneas.";
            GestorBitacora.registrarEvento("Sistema", "Carga de servicios", contadorExito > 0, mensaje);

            // Mostrar errores si los hay
            if (!errores.isEmpty()) {
                mostrarErroresCarga(errores, "Errores en carga de servicios");
            }

            return contadorExito;

        } catch (IOException e) {
            GestorBitacora.registrarEvento("Sistema", "Carga de servicios", false,
                    "Error al leer el archivo: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al leer el archivo: " + e.getMessage(),
                    "Error de lectura",
                    JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    /**
     * Carga clientes y sus automóviles desde un archivo con extensión .tmca
     * Formato: Identificador-NombreCompleto-Usuario-Contraseña-TipoCliente-Lista
     * Automóviles
     * 
     * @param archivo File con el archivo a cargar
     * @return Número de clientes cargados con éxito
     */
    public static int cargarClientesAutomoviles(File archivo) {
        if (archivo == null || !archivo.exists() || !archivo.getName().endsWith(".tmca")) {
            GestorBitacora.registrarEvento("Sistema", "Carga de clientes y automóviles", false,
                    "Archivo inválido o no encontrado: " + (archivo != null ? archivo.getName() : "null"));
            return 0;
        }

        int contadorExito = 0;
        int lineaActual = 0;
        List<String> errores = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                lineaActual++;

                // Omitir líneas vacías o comentarios
                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }

                // Procesar la línea
                try {
                    String[] partes = linea.split("-");

                    if (partes.length < 6) {
                        errores.add("Línea " + lineaActual + ": Formato incorrecto, se esperaban al menos 6 campos");
                        continue;
                    }

                    String identificador = partes[0].trim();
                    String nombreCompleto = partes[1].trim();
                    String usuario = partes[2].trim();
                    String password = partes[3].trim();
                    String tipoCliente = partes[4].trim().toLowerCase();
                    String listaAutosStr = partes[5].trim();

                    // Separar nombre y apellido (asumiendo formato "Nombre Apellido")
                    String[] nombrePartes = nombreCompleto.split(" ", 2);
                    String nombre = nombrePartes[0];
                    String apellido = nombrePartes.length > 1 ? nombrePartes[1] : "";

                    // Verificar tipo de cliente
                    if (!tipoCliente.equals("normal") && !tipoCliente.equals("oro")) {
                        tipoCliente = "normal"; // Default a normal si no es válido
                    }

                    // Registrar el cliente
                    Cliente cliente = ClienteController.registrarCliente(identificador, nombre, apellido, usuario,
                            password);

                    if (cliente == null) {
                        errores.add("Línea " + lineaActual + ": No se pudo registrar el cliente " + identificador);
                        continue;
                    }

                    // Establecer tipo de cliente
                    if (tipoCliente.equals("oro")) {
                        cliente.setTipoCliente("oro");
                    }

                    // Procesar y añadir automóviles al cliente
                    if (!listaAutosStr.isEmpty()) {
                        String[] autos = listaAutosStr.split(";");

                        for (String autoStr : autos) {
                            if (!autoStr.trim().isEmpty()) {
                                String[] autoData = autoStr.trim().split(",");

                                if (autoData.length >= 3) {
                                    String placa = autoData[0].trim();
                                    String marca = autoData[1].trim();
                                    String modelo = autoData[2].trim();
                                    String rutaFoto = autoData.length > 3 ? autoData[3].trim() : "";

                                    // Registrar el automóvil para el cliente
                                    boolean registrado = ClienteController.registrarAutomovil(
                                            cliente.getIdentificador(),
                                            placa, marca, modelo, rutaFoto);

                                    if (!registrado) {
                                        errores.add("Línea " + lineaActual + ": No se pudo registrar el automóvil " +
                                                placa + " para el cliente " + identificador);
                                    }
                                } else {
                                    errores.add(
                                            "Línea " + lineaActual + ": Formato de automóvil incorrecto: " + autoStr);
                                }
                            }
                        }
                    }

                    contadorExito++;

                } catch (Exception e) {
                    errores.add("Línea " + lineaActual + ": " + e.getMessage());
                }
            }

            // Reportar resultados
            String mensaje = "Clientes cargados: " + contadorExito + " de " + lineaActual + " líneas.";
            GestorBitacora.registrarEvento("Sistema", "Carga de clientes y automóviles", contadorExito > 0, mensaje);

            // Mostrar errores si los hay
            if (!errores.isEmpty()) {
                mostrarErroresCarga(errores, "Errores en carga de clientes y automóviles");
            }

            return contadorExito;

        } catch (IOException e) {
            GestorBitacora.registrarEvento("Sistema", "Carga de clientes y automóviles", false,
                    "Error al leer el archivo: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al leer el archivo: " + e.getMessage(),
                    "Error de lectura",
                    JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    /**
     * Método auxiliar para mostrar errores de carga en un cuadro de diálogo
     */
    private static void mostrarErroresCarga(List<String> errores, String titulo) {
        if (errores.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Se encontraron los siguientes errores:\n\n");

        // Limitar a mostrar máximo 10 errores para no sobrecargar el diálogo
        int maxErrores = Math.min(errores.size(), 10);

        for (int i = 0; i < maxErrores; i++) {
            sb.append("• ").append(errores.get(i)).append("\n");
        }

        if (errores.size() > maxErrores) {
            sb.append("\n... y ").append(errores.size() - maxErrores).append(" errores más.");
        }

        JOptionPane.showMessageDialog(null,
                sb.toString(),
                titulo,
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Muestra un selector de archivos y carga el archivo seleccionado según su
     * extensión
     * 
     * @return número de elementos cargados o -1 si se canceló
     */
    public static int cargarArchivoConSelector(String tipoArchivo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de " + tipoArchivo);

        // Configurar filtro según el tipo de archivo
        switch (tipoArchivo.toLowerCase()) {
            case "repuestos":
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".tmr");
                    }

                    @Override
                    public String getDescription() {
                        return "Archivos de repuestos (*.tmr)";
                    }
                });
                break;

            case "servicios":
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".tms");
                    }

                    @Override
                    public String getDescription() {
                        return "Archivos de servicios (*.tms)";
                    }
                });
                break;

            case "clientes":
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".tmca");
                    }

                    @Override
                    public String getDescription() {
                        return "Archivos de clientes y automóviles (*.tmca)";
                    }
                });
                break;
        }

        int resultado = fileChooser.showOpenDialog(null);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();

            // Cargar según el tipo de archivo
            switch (tipoArchivo.toLowerCase()) {
                case "repuestos":
                    return cargarRepuestos(archivoSeleccionado);

                case "servicios":
                    return cargarServicios(archivoSeleccionado);

                case "clientes":
                    return cargarClientesAutomoviles(archivoSeleccionado);
            }
        }

        return -1; // Cancelado o tipo no reconocido
    }

    /**
     * Carga datos iniciales desde archivos predeterminados
     */
    public static void cargarDatosIniciales() {
        // Rutas predeterminadas para los archivos
        File repuestosFile = new File("repuestos.tmr");
        File serviciosFile = new File("servicios.tms");
        File clientesFile = new File("clientes_automoviles.tmca");

        // Verificar si existen los archivos y cargarlos
        int repuestosCargados = 0;
        int serviciosCargados = 0;
        int clientesCargados = 0;

        if (repuestosFile.exists()) {
            repuestosCargados = cargarRepuestos(repuestosFile);
        }

        if (serviciosFile.exists()) {
            serviciosCargados = cargarServicios(serviciosFile);
        }

        if (clientesFile.exists()) {
            clientesCargados = cargarClientesAutomoviles(clientesFile);
        }

        // Mostrar resultado
        StringBuilder mensaje = new StringBuilder("Carga inicial de datos:\n\n");
        mensaje.append("• Repuestos: ").append(repuestosCargados)
                .append(repuestosFile.exists() ? "" : " (archivo no encontrado)").append("\n");
        mensaje.append("• Servicios: ").append(serviciosCargados)
                .append(serviciosFile.exists() ? "" : " (archivo no encontrado)").append("\n");
        mensaje.append("• Clientes: ").append(clientesCargados)
                .append(clientesFile.exists() ? "" : " (archivo no encontrado)").append("\n");

        // Solo mostrar diálogo si se encontró al menos un archivo
        if (repuestosFile.exists() || serviciosFile.exists() || clientesFile.exists()) {
            JOptionPane.showMessageDialog(null,
                    mensaje.toString(),
                    "Carga inicial de datos",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static File seleccionarArchivo(AdminView adminView, String string, String string2, String string3) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(string);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(string2);
            }

            @Override
            public String getDescription() {
                return string3;
            }
        });

        int resultado = fileChooser.showOpenDialog(adminView);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null; // Cancelado o tipo no reconocido
    }

    public static int cargarClientes(File archivo) {
        if (archivo == null || !archivo.exists() || !archivo.getName().endsWith(".tmca")) {
            GestorBitacora.registrarEvento("Sistema", "Carga de clientes", false,
                    "Archivo inválido o no encontrado: " + (archivo != null ? archivo.getName() : "null"));
            return 0;
        }

        int contadorExito = 0;
        int lineaActual = 0;
        List<String> errores = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                lineaActual++;

                // Omitir líneas vacías o comentarios
                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }

                // Procesar la línea
                try {
                    String[] partes = linea.split("-");

                    if (partes.length < 5) {
                        errores.add("Línea " + lineaActual + ": Formato incorrecto, se esperaban al menos 5 campos");
                        continue;
                    }

                    String identificador = partes[0].trim();
                    String nombreCompleto = partes[1].trim();
                    String usuario = partes[2].trim();
                    String password = partes[3].trim();
                    String tipoCliente = partes[4].trim().toLowerCase();

                    // Separar nombre y apellido (asumiendo formato "Nombre Apellido")
                    String[] nombrePartes = nombreCompleto.split(" ", 2);
                    String nombre = nombrePartes[0];
                    String apellido = nombrePartes.length > 1 ? nombrePartes[1] : "";

                    // Verificar tipo de cliente
                    if (!tipoCliente.equals("normal") && !tipoCliente.equals("oro")) {
                        tipoCliente = "normal"; // Default a normal si no es válido
                    }

                    // Registrar el cliente
                    Cliente cliente = ClienteController.registrarCliente(identificador, nombre, apellido, usuario,
                            password);

                    if (cliente == null) {
                        errores.add("Línea " + lineaActual + ": No se pudo registrar el cliente " + identificador);
                        continue;
                    }

                    // Establecer tipo de cliente
                    if (tipoCliente.equals("oro")) {
                        cliente.setTipoCliente("oro");
                    }

                    contadorExito++;
                } catch (Exception e) {
                    errores.add("Línea " + lineaActual + ": " + e.getMessage());
                }
            }

            // Reportar resultados
            String mensaje = "Clientes cargados: " + contadorExito + " de " + lineaActual + " líneas.";
            GestorBitacora.registrarEvento("Sistema", "Carga de clientes", contadorExito > 0, mensaje);

            // Mostrar errores si los hay
            if (!errores.isEmpty()) {
                mostrarErroresCarga(errores, "Errores en carga de clientes");
            }

            return contadorExito;

        } catch (IOException e) {
            GestorBitacora.registrarEvento("Sistema", "Carga de clientes", false,
                    "Error al leer el archivo: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al leer el archivo: " + e.getMessage(),
                    "Error de lectura",
                    JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }
}
