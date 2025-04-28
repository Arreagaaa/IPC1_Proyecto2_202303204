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
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_LIGHT);

        mainPanel.add(crearPanelInfoMecanico(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Cola de Espera", inicializarPanelColaEspera());
        tabbedPane.addTab("En Servicio", inicializarPanelEnServicio());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);

        cargarDatos();

        timer = new Timer(5000, e -> cargarDatos());
        timer.start();
    }

    private JPanel crearPanelInfoMecanico() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblBienvenida = new JLabel("Bienvenido, " + mecanicoActual.getNombreCompleto() +
                " | Mecánico | ID: " + mecanicoActual.getIdentificador());
        lblBienvenida.setForeground(Color.WHITE);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 14));

        JButton btnLogout = new JButton("Cerrar sesión");
        btnLogout.setBackground(new Color(180, 30, 30));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setOpaque(true);
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

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(COLOR_LIGHT);
        JLabel titleLabel = new JLabel("Vehículos en Espera");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_PRIMARY_DARK);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        panel.add(titlePanel, BorderLayout.NORTH);

        String[] columnas = { "Orden", "Cliente", "Vehículo", "Servicio", "Fecha", "Acciones" };
        modeloColaEspera = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        tablaColaEspera = new JTable(modeloColaEspera);
        JScrollPane scrollPane = new JScrollPane(tablaColaEspera);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1));

        tablaColaEspera.setFillsViewportHeight(true);
        estilizarTabla(tablaColaEspera);

        tablaColaEspera.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Tomar"));
        tablaColaEspera.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "Tomar") {
            @Override
            public void buttonClicked() {
                int row = tablaColaEspera.getSelectedRow();
                if (row >= 0 && row < modeloColaEspera.getRowCount()) {
                    int ordenId = (int) modeloColaEspera.getValueAt(row, 0);
                    tomarOrden(ordenId);
                }
            }
        });

        TableColumnModel columnModel = tablaColaEspera.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(100);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(COLOR_LIGHT);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel inicializarPanelEnServicio() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(COLOR_LIGHT);
        JLabel titleLabel = new JLabel("Vehículos en Atención");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_PRIMARY_DARK);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        panel.add(titlePanel, BorderLayout.NORTH);

        String[] columnas = { "Orden", "Cliente", "Vehículo", "Servicio", "Fecha", "Acciones" };
        modeloEnServicio = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        tablaEnServicio = new JTable(modeloEnServicio);
        JScrollPane scrollPane = new JScrollPane(tablaEnServicio);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1));

        tablaEnServicio.setFillsViewportHeight(true);
        estilizarTabla(tablaEnServicio);

        tablaEnServicio.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Finalizar"));
        tablaEnServicio.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "Finalizar") {
            @Override
            public void buttonClicked() {
                int row = tablaEnServicio.getSelectedRow();
                if (row >= 0 && row < modeloEnServicio.getRowCount()) {
                    int ordenId = (int) modeloEnServicio.getValueAt(row, 0);
                    finalizarOrden(ordenId);
                }
            }
        });

        TableColumnModel columnModel = tablaEnServicio.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(100);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(COLOR_LIGHT);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void cargarDatos() {
        modeloColaEspera.setRowCount(0);
        Vector<OrdenTrabajo> ordenesPendientes = OrdenTrabajoController.obtenerOrdenesPorEstado("ESPERA");
        for (OrdenTrabajo orden : ordenesPendientes) {
            modeloColaEspera.addRow(new Object[] {
                    orden.getId(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo() + " (" +
                            orden.getAutomovil().getPlaca() + ")",
                    orden.getServicio().getNombre(),
                    orden.getFecha(),
                    "Tomar"
            });
        }

        modeloEnServicio.setRowCount(0);
        Vector<OrdenTrabajo> ordenesEnProceso = OrdenTrabajoController.obtenerOrdenesPorMecanicoEstado(
                mecanicoActual, "PROCESO");
        for (OrdenTrabajo orden : ordenesEnProceso) {
            modeloEnServicio.addRow(new Object[] {
                    orden.getId(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo() + " (" +
                            orden.getAutomovil().getPlaca() + ")",
                    orden.getServicio().getNombre(),
                    orden.getFecha(),
                    "Finalizar"
            });
        }
    }

    private void tomarOrden(int ordenId) {
        OrdenTrabajo orden = OrdenTrabajoController.obtenerOrdenPorId(ordenId);

        if (orden != null) {
            if (!OrdenTrabajoController.mecanicoDisponible(mecanicoActual)) {
                JOptionPane.showMessageDialog(this,
                        "Ya estás atendiendo otro vehículo. Finaliza ese servicio primero.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (orden.getServicio().esDiagnostico()) {
                realizarDiagnostico(orden);
            } else {
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
        Vector<Servicio> serviciosCompatibles = ServicioController.obtenerServiciosCompatibles(orden.getAutomovil());
        serviciosCompatibles.removeIf(s -> s.esDiagnostico());

        if (serviciosCompatibles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay servicios disponibles para este automóvil.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Random rand = new Random();
        Servicio servicioElegido = serviciosCompatibles.get(rand.nextInt(serviciosCompatibles.size()));

        int option = JOptionPane.showConfirmDialog(this,
                "Diagnóstico completado.\nSe recomienda el servicio: " + servicioElegido.getNombre() +
                        "\nPrecio total: Q" + servicioElegido.getPrecioTotal() +
                        "\n\n¿Desea notificar al cliente?",
                "Resultado del diagnóstico",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            orden.setServicio(servicioElegido);

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
            int option = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea finalizar este servicio?",
                    "Confirmar finalización",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
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
        if (timer != null) {
            timer.stop();
        }
        dispose();
        new LoginView().setVisible(true);
    }

    protected void estilizarTabla(JTable tabla) {
        tabla.setForeground(new Color(40, 40, 40));
        tabla.setBackground(Color.WHITE);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setSelectionBackground(COLOR_PRIMARY);
        tabla.setGridColor(COLOR_PRIMARY);
        tabla.setRowHeight(28);

        JTableHeader header = tabla.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                lbl.setBackground(COLOR_PRIMARY);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Arial", Font.BOLD, 16));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                return lbl;
            }
        });
    }

    protected abstract class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String text;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkbox, String text) {
            super(checkbox);
            this.text = text;
            button = new JButton(text);
            button.setOpaque(true);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(66, 139, 202));
            button.setFocusPainted(false);
            button.setBorderPainted(false);

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                buttonClicked();
            }
            isPushed = false;
            return text;
        }

        public abstract void buttonClicked();
    }

    protected class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text) {
            setText(text);
            setOpaque(true);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Arial", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(new Color(66, 139, 202));
            setForeground(Color.WHITE);
            return this;
        }
    }
}
