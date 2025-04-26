package com.tallermecanico;

import com.tallermecanico.controllers.DataController;
import com.tallermecanico.utils.CargadorArchivos;
import com.tallermecanico.utils.GestorHilos;
import com.tallermecanico.utils.Serializador;
import com.tallermecanico.views.MenuView;

import javax.swing.*;

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
                CargadorArchivos.cargarDatosIniciales();
            }

            // Iniciar hilos del sistema
            GestorHilos.obtenerInstancia().iniciarHilos();

            // Crear y mostrar la vista principal
            MenuView menuView = new MenuView();
            menuView.setVisible(true);
        });

        // Agregar un gancho de cierre para detener hilos al cerrar la aplicación
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            GestorHilos.obtenerInstancia().detenerHilos();
        }));
    }

    /**
     * Verifica si hay datos cargados en memoria del sistema
     */
    private static boolean sistemaSinDatos() {
        return DataController.getClientes().isEmpty() &&
                DataController.getRepuestos().isEmpty() &&
                DataController.getServicios().isEmpty();
    }
}