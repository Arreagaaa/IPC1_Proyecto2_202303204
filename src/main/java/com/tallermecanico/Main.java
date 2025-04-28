package com.tallermecanico;

import com.tallermecanico.controllers.DataController;
import com.tallermecanico.controllers.RepuestoController;
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
            // Inicializar datos desde archivos serializados
            Serializador.inicializarDatos();

            // Limpiar duplicados si existen
            int duplicadosEliminados = RepuestoController.eliminarRepuestosDuplicados();
            if (duplicadosEliminados > 0) {
                System.out.println("Se eliminaron " + duplicadosEliminados + " repuestos duplicados");
            }

            // Cargar archivos SOLO SI el sistema está vacío
            if (sistemaSinDatos()) {
                System.out.println("Sistema sin datos detectado. Cargando archivos iniciales...");

                // Cargar repuestos
                File repuestosFile = new File("repuestos.tmr");
                if (repuestosFile.exists()) {
                    int repuestosCargados = CargadorArchivos.cargarRepuestos(repuestosFile);
                    System.out.println("Repuestos cargados: " + repuestosCargados);
                } else {
                    System.out.println("Archivo repuestos.tmr no encontrado en: " + repuestosFile.getAbsolutePath());
                }

                // Cargar servicios y clientes
                CargadorArchivos.cargarDatosIniciales();
            } else {
                System.out.println("Sistema con datos existentes. No se cargarán archivos iniciales.");
            }

            // Verificar si existe un administrador por defecto
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
     * Verifica si el sistema no tiene datos cargados
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