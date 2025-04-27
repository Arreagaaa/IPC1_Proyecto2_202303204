package com.tallermecanico.components;

import com.tallermecanico.controllers.DataController;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.utils.GestorHilos;
import com.tallermecanico.utils.MonitorOrdenesThread;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Panel que muestra el progreso de las órdenes de trabajo en tiempo real
 */
public class EstatusProgresoPanel extends JPanel implements MonitorOrdenesThread.ObservadorOrdenes {

    private JLabel lblEspera;
    private JLabel lblServicio;
    private JLabel lblListas;
    private JProgressBar progressEspera;
    private JProgressBar progressServicio;
    private JProgressBar progressListas;

    // Colores para las barras de progreso
    private final Color COLOR_ESPERA = new Color(230, 126, 34); // Naranja
    private final Color COLOR_SERVICIO = new Color(52, 152, 219); // Azul
    private final Color COLOR_LISTAS = new Color(46, 204, 113); // Verde

    /**
     * Constructor
     */
    public EstatusProgresoPanel() {
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Inicializar componentes
        lblEspera = new JLabel("En espera: 0");
        lblServicio = new JLabel("En servicio: 0");
        lblListas = new JLabel("Listas: 0");

        progressEspera = new JProgressBar(0, 100);
        progressEspera.setStringPainted(true);
        progressEspera.setForeground(COLOR_ESPERA);

        progressServicio = new JProgressBar(0, 100);
        progressServicio.setStringPainted(true);
        progressServicio.setForeground(COLOR_SERVICIO);

        progressListas = new JProgressBar(0, 100);
        progressListas.setStringPainted(true);
        progressListas.setForeground(COLOR_LISTAS);

        // Agregar componentes al panel
        add(lblEspera);
        add(progressEspera);
        add(lblServicio);
        add(progressServicio);
        add(lblListas);
        add(progressListas);

        // Registrarse como observador
        GestorHilos.obtenerInstancia().registrarObservadorOrdenes(this);

        // Actualizar estado inicial
        actualizarEstado();
    }

    /**
     * Libera recursos al eliminar el panel
     */
    public void destruir() {
        GestorHilos.obtenerInstancia().eliminarObservadorOrdenes(this);
    }

    /**
     * Actualiza el estado del panel con los datos actuales
     */
    public void actualizarEstado() {
        // Contar órdenes por estado
        int totalOrdenes = DataController.getOrdenesTrabajo().size();
        if (totalOrdenes == 0)
            totalOrdenes = 1; // Evitar división por cero

        int enEspera = DataController.getColaEspera().size();

        int enServicio = 0;
        int listas = 0;

        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if ("en_servicio".equals(orden.getEstado())) {
                enServicio++;
            } else if ("listo".equals(orden.getEstado())) {
                listas++;
            }
        }

        // Actualizar etiquetas
        lblEspera.setText("En espera: " + enEspera);
        lblServicio.setText("En servicio: " + enServicio);
        lblListas.setText("Listas: " + listas);

        // Actualizar barras de progreso
        progressEspera.setValue((enEspera * 100) / totalOrdenes);
        progressEspera.setString(enEspera + " (" + progressEspera.getValue() + "%)");

        progressServicio.setValue((enServicio * 100) / totalOrdenes);
        progressServicio.setString(enServicio + " (" + progressServicio.getValue() + "%)");

        progressListas.setValue((listas * 100) / totalOrdenes);
        progressListas.setString(listas + " (" + progressListas.getValue() + "%)");
    }

    @Override
    public void ordenesActualizadas(Vector<OrdenTrabajo> ordenesEspera,
            Vector<OrdenTrabajo> ordenesServicio,
            Vector<OrdenTrabajo> ordenesListas) {
        // Actualizar en el EDT
        SwingUtilities.invokeLater(this::actualizarEstado);
    }
}