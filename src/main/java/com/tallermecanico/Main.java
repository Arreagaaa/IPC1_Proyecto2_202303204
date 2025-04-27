package com.tallermecanico;

import com.tallermecanico.controllers.DataController;
import com.tallermecanico.utils.CargadorArchivos;
import com.tallermecanico.utils.GestorHilos;
import com.tallermecanico.utils.Serializador;
import com.tallermecanico.views.LoginView;

import javax.swing.*;
import java.io.File;

/**
 * Clase principal que inicia la aplicación
 */
public class Main {
    public static void main(String[] args) {
        // Configurar el look and feel para mejorar la apariencia
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Iniciar la aplicación en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Inicializar datos (cargar desde archivos serializados si existen)
            Serializador.inicializarDatos();

            // Intentar cargar datos iniciales desde archivos si el sistema está vacío
            if (sistemaSinDatos()) {
                // PRIMERO: Cargar repuestos (antes que cualquier otra cosa)
                File repuestosFile = new File("repuestos.tmr");
                System.out.println("Archivo repuestos.tmr existe: " + repuestosFile.exists() +
                        " (Ruta: " + repuestosFile.getAbsolutePath() + ")");
                int repuestosCargados = CargadorArchivos.cargarRepuestos(repuestosFile);
                System.out.println("Repuestos cargados: " + repuestosCargados);

                // SEGUNDO: Verificar que haya repuestos cargados antes de continuar
                if (repuestosCargados > 0) {
                    // TERCERO: Cargar servicios y clientes
                    CargadorArchivos.cargarDatosIniciales();
                } else {
                    System.err.println("ADVERTENCIA: No se pudieron cargar repuestos, los servicios podrían fallar");
                }
            }

            // Verificar si existe un administrador por defecto, y crearlo si no existe
            DataController.verificarAdministradorPorDefecto();

            // Iniciar hilos del sistema
            GestorHilos.obtenerInstancia().iniciarHilos();

            // Crear y mostrar la vista de login
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });

        // Agregar un gancho de cierre más completo
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando aplicación...");

            // Guardar datos explícitamente (si no se hace ya en otro lugar)
            try {
                Serializador.guardarDatos(args); // Si existe este método
                System.out.println("Datos guardados correctamente");
            } catch (Exception e) {
                System.err.println("Error al guardar datos: " + e.getMessage());
            }

            // Detener hilos
            GestorHilos.obtenerInstancia().detenerHilos();
            System.out.println("Hilos detenidos correctamente");
        }));
    }

    /**
     * Verifica si el sistema está vacío (sin datos)
     */
    private static boolean sistemaSinDatos() {
        // Verificar los vectores principales
        boolean clientesVacios = DataController.getClientes() == null || DataController.getClientes().isEmpty();
        boolean repuestosVacios = DataController.getRepuestos() == null || DataController.getRepuestos().isEmpty();
        boolean serviciosVacios = DataController.getServicios() == null || DataController.getServicios().isEmpty();

        // Retornar true si todos están vacíos
        return clientesVacios && repuestosVacios && serviciosVacios;
    }
}