package com.tallermecanico.views;

import com.tallermecanico.controllers.*;
import com.tallermecanico.models.*;
import com.tallermecanico.models.personas.Empleado;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Vector;
import java.util.Random;

public class MecanicoView extends BaseView {
    private Empleado mecanicoActual;
    private JTabbedPane tabbedPane;
    private DefaultTableModel modeloColaEspera;
    private DefaultTableModel modeloEnServicio;
    private JTable tablaColaEspera;
    private JTable tablaEnServicio;
    private Timer timer;

    public MecanicoView(Empleado mecanico) {
        super("Taller Mecánico - Mecánico");
        this.mecanicoActual = mecanico;
        inicializarComponentes();
    }

    @Override
    protected void inicializarComponentes() {
        // Configuración básica de la ventana
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Inicializar el panel principal con un BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_LIGHT);

        // Crear panel de información del mecánico en la parte superior
        mainPanel.add(crearPanelInfoMecanico(), BorderLayout.NORTH);

        // Crear pestañas para las diferentes secciones
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Cola de Espera", inicializarPanelColaEspera());
        tabbedPane.addTab("En Servicio", inicializarPanelEnServicio());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Añadir panel principal a la ventana
        setContentPane(mainPanel);

        // Cargar datos iniciales
        cargarDatos();

        // Iniciar timer para actualizar los datos cada 5 segundos
        timer = new Timer(5000, e -> cargarDatos());
        timer.start();
    }

    private JPanel crearPanelInfoMecanico() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblBienvenida = new JLabel("Bienvenido, " + mecanicoActual.getNombreCompleto() +
                " | Mecánico | ID: " + mecanicoActual.getId());
        lblBienvenida.setForeground(Color.WHITE);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 14));

        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> cerrarSesion());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_PRIMARY);
        buttonPanel.add(btnLogout);

        panel.add(lblBienvenida, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel inicializarPanelColaEspera() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Crear tabla y modelo
        String[] columnas = { "Orden", "Cliente", "Vehículo", "Servicio", "Fecha", "Acciones" };
        modeloColaEspera = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Solo la columna de acciones es editable
            }
        };

        tablaColaEspera = new JTable(modeloColaEspera);
        JScrollPane scrollPane = new JScrollPane(tablaColaEspera);

        // Configurar tabla
        tablaColaEspera.setFillsViewportHeight(true);
        configureTable(tablaColaEspera);

        // Añadir botón de tomar servicio en la columna de acciones
        tablaColaEspera.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Tomar"));
        tablaColaEspera.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "Tomar") {
            @Override
            public void buttonClicked() {
                int row = tablaColaEspera.getSelectedRow();
                if (row >= 0) {
                    int ordenId = (int) modeloColaEspera.getValueAt(row, 0);
                    tomarOrden(ordenId);
                }
            }
        });

        // Añadir componentes al panel
        JLabel titulo = new JLabel("Vehículos en espera:");
        titulo.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel inicializarPanelEnServicio() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Crear tabla y modelo
        String[] columnas = { "Orden", "Cliente", "Vehículo", "Servicio", "Fecha", "Acciones" };
        modeloEnServicio = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Solo la columna de acciones es editable
            }
        };

        tablaEnServicio = new JTable(modeloEnServicio);
        JScrollPane scrollPane = new JScrollPane(tablaEnServicio);

        // Configurar tabla
        tablaEnServicio.setFillsViewportHeight(true);
        configureTable(tablaEnServicio);

        // Añadir botón de finalizar servicio en la columna de acciones
        tablaEnServicio.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Finalizar"));
        tablaEnServicio.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "Finalizar") {
            @Override
            public void buttonClicked() {
                int row = tablaEnServicio.getSelectedRow();
                if (row >= 0) {
                    int ordenId = (int) modeloEnServicio.getValueAt(row, 0);
                    finalizarOrden(ordenId);
                }
            }
        });

        // Añadir componentes al panel
        JLabel titulo = new JLabel("Vehículos en atención:");
        titulo.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void cargarDatos() {
        // Cargar órdenes en cola de espera
        modeloColaEspera.setRowCount(0);
        Vector<OrdenTrabajo> ordenesPendientes = OrdenTrabajoController.obtenerOrdenesPorEstado("ESPERA");
        for (OrdenTrabajo orden : ordenesPendientes) {
            modeloColaEspera.addRow(new Object[] {
                    orden.getId(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo() + " ("
                            + orden.getAutomovil().getPlaca() + ")",
                    orden.getServicio().getNombre(),
                    orden.getFecha(),
                    "Tomar"
            });
        }

        // Cargar órdenes en servicio asignadas a este mecánico
        modeloEnServicio.setRowCount(0);
        Vector<OrdenTrabajo> ordenesEnProceso = OrdenTrabajoController.obtenerOrdenesPorMecanicoEstado(
                mecanicoActual, "PROCESO");
        for (OrdenTrabajo orden : ordenesEnProceso) {
            modeloEnServicio.addRow(new Object[] {
                    orden.getId(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo() + " ("
                            + orden.getAutomovil().getPlaca() + ")",
                    orden.getServicio().getNombre(),
                    orden.getFecha(),
                    "Finalizar"
            });
        }
    }

    private void tomarOrden(int ordenId) {
        OrdenTrabajo orden = OrdenTrabajoController.obtenerOrdenPorId(ordenId);

        if (orden != null) {
            // Verificar si el mecánico ya está atendiendo otro vehículo
            if (!OrdenTrabajoController.mecanicoDisponible(mecanicoActual)) {
                JOptionPane.showMessageDialog(this,
                        "Ya estás atendiendo otro vehículo. Finaliza ese servicio primero.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Si el servicio es un diagnóstico, mostrar ventana para seleccionar servicio
            if (orden.getServicio().esDiagnostico()) {
                realizarDiagnostico(orden);
            } else {
                // Asignar mecánico y cambiar estado
                boolean ok = OrdenTrabajoController.asignarMecanico(orden, mecanicoActual);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Vehículo tomado para servicio.",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al asignar el vehículo.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void realizarDiagnostico(OrdenTrabajo orden) {
        // Obtener servicios compatibles con el automóvil (excepto el diagnóstico)
        Vector<Servicio> serviciosCompatibles = ServicioController.obtenerServiciosCompatibles(orden.getAutomovil());
        serviciosCompatibles.removeIf(s -> s.esDiagnostico());

        if (serviciosCompatibles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay servicios disponibles para este automóvil.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Seleccionar un servicio aleatorio
        Random rand = new Random();
        Servicio servicioElegido = serviciosCompatibles.get(rand.nextInt(serviciosCompatibles.size()));

        // Mostrar el servicio diagnosticado al mecánico
        int option = JOptionPane.showConfirmDialog(this,
                "Diagnóstico completado.\nSe recomienda el servicio: " + servicioElegido.getNombre() +
                        "\nPrecio total: Q" + servicioElegido.getPrecioTotal() +
                        "\n\n¿Desea notificar al cliente?",
                "Resultado del diagnóstico",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            // Cambiar el servicio en la orden
            orden.setServicio(servicioElegido);

            // Asignar mecánico y cambiar estado
            boolean ok = OrdenTrabajoController.asignarMecanico(orden, mecanicoActual);

            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "El servicio ha sido actualizado y el cliente ha sido notificado.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al actualizar el servicio.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void finalizarOrden(int ordenId) {
        OrdenTrabajo orden = OrdenTrabajoController.obtenerOrdenPorId(ordenId);

        if (orden != null) {
            // Confirmar finalización
            int option = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea finalizar este servicio?",
                    "Confirmar finalización",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                // Finalizar la orden
                boolean ok = OrdenTrabajoController.finalizarOrden(orden);

                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Servicio finalizado. Se ha generado la factura correspondiente.",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al finalizar el servicio.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void cerrarSesion() {
        // Detener el timer
        if (timer != null) {
            timer.stop();
        }

        // Cerrar ventana actual y volver a login
        dispose();
        new LoginView().setVisible(true);
    }
}
