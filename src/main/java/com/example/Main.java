package com.example;

import com.example.controllers.MainController;
import com.example.models.GestorOrdenes;
import com.example.utils.CargadorDatos;
import com.example.utils.Serializador;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Cargar datos serializados
        Serializador.cargarDatos();

        // Cargar datos desde archivos si es necesario
        cargarDatosIniciales();

        // Iniciar la aplicación en el Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainController controller = new MainController();
                controller.run();
            }
        });

        // Registrar hook para guardar datos al cerrar
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // Detener los hilos de procesamiento de órdenes
                GestorOrdenes.getInstancia().shutdown();

                // Guardar datos
                Serializador.guardarDatos();

                System.out.println("Aplicación cerrada correctamente.");
            }
        });
    }

    private static void cargarDatosIniciales() {
        try {
            // Verificar si ya hay datos cargados
            if (Serializador.esNuevoSistema()) {
                System.out.println("Cargando datos iniciales...");

                // Cargar repuestos
                CargadorDatos.cargarRepuestosDesdeArchivo("repuestos.tmr");

                // Cargar servicios
                CargadorDatos.cargarServiciosDesdeArchivo("servicios.tms");

                // Cargar clientes y automóviles
                CargadorDatos.cargarClientesYAutomovilesDesdeArchivo("clientes_automoviles.tmca");

                // Guardar datos cargados
                Serializador.guardarDatos();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar datos iniciales: " + e.getMessage());
            e.printStackTrace();
        }
    }
}