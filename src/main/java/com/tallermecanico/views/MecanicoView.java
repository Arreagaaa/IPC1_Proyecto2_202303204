package com.tallermecanico.views;

import com.tallermecanico.controllers.OrdenTrabajoController;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.personas.Mecanico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

/**
 * Vista para el mecánico
 */
public class MecanicoView extends BaseView {

    private Mecanico mecanico;

    // Componentes principales
    private JTabbedPane pestanas;
    private JTable tablaOrdenes;
    private DefaultTableModel modeloTablaOrdenes;

    public MecanicoView(Mecanico mecanico) {
        super("Panel de Mecánico - " + mecanico.getNombreCompleto());
        this.mecanico = mecanico;
        inicializarComponentes();
        cargarDatos();
    }

    @Override
    protected void inicializarComponentes() {
        // Panel principal con pestañas
        pestanas = new JTabbedPane();

        // Panel de órdenes en espera
        JPanel panelOrdenes = new JPanel(new BorderLayout());
        modeloTablaOrdenes = new DefaultTableModel(
                new String[] { "N° Orden", "Cliente", "Automóvil", "Servicio", "Fecha" }, 0);
        tablaOrdenes = new JTable(modeloTablaOrdenes);
        panelOrdenes.add(new JScrollPane(tablaOrdenes), BorderLayout.CENTER);

        // Botón asignar
        JButton btnAsignar = crearBoton("Asignar a Mí");
        btnAsignar.addActionListener(e -> asignarOrden());
        panelOrdenes.add(btnAsignar, BorderLayout.SOUTH);

        // Indicador si no hay órdenes
        if (modeloTablaOrdenes.getRowCount() == 0) {
            JLabel lblSinOrdenes = new JLabel("No hay órdenes en espera.", JLabel.CENTER);
            lblSinOrdenes.setFont(new Font("Arial", Font.ITALIC, 14));
            lblSinOrdenes.setForeground(Color.GRAY);
            panelOrdenes.add(lblSinOrdenes, BorderLayout.NORTH);
        }

        // Agregar pestañas
        pestanas.addTab("Órdenes en Espera", panelOrdenes);

        // Agregar pestañas al panel principal
        add(pestanas, BorderLayout.CENTER);
    }

    private void cargarDatos() {
        // Cargar datos en la tabla de órdenes
        modeloTablaOrdenes.setRowCount(0);
        Vector<OrdenTrabajo> ordenes = OrdenTrabajoController.obtenerColaEspera();
        for (OrdenTrabajo orden : ordenes) {
            modeloTablaOrdenes.addRow(new Object[] {
                    orden.getNumero(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getPlaca(),
                    orden.getServicio().getNombre(),
                    orden.getFecha()
            });
        }
    }

    private void asignarOrden() {
        // Lógica para asignar una orden al mecánico
        int filaSeleccionada = tablaOrdenes.getSelectedRow();
        if (filaSeleccionada >= 0) {
            int numeroOrden = (int) modeloTablaOrdenes.getValueAt(filaSeleccionada, 0);
            boolean asignado = OrdenTrabajoController.asignarOrdenAMecanico(numeroOrden, mecanico.getIdentificador());
            if (asignado) {
                JOptionPane.showMessageDialog(this, "Orden asignada correctamente.");
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo asignar la orden.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una orden de la tabla.");
        }
    }
}
