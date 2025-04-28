package com.tallermecanico.views;

import com.tallermecanico.controllers.*;
import com.tallermecanico.models.*;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.utils.GeneradorPDF;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Vector;

public class ClienteView extends BaseView {
    private Cliente clienteActual;
    private JTabbedPane tabbedPane;
    private DefaultTableModel modeloAutomoviles;
    private DefaultTableModel modeloServiciosPendientes;
    private DefaultTableModel modeloFacturas;
    private JTable tablaAutomoviles;
    private JTable tablaServiciosPendientes;
    private JTable tablaFacturas;

    public ClienteView(Cliente cliente) {
        super("Taller Mecánico - Cliente");
        this.clienteActual = cliente;

        // Configuración básica de la ventana
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Inicializar el panel principal con un BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_LIGHT);

        // Crear panel de información del cliente en la parte superior
        mainPanel.add(crearPanelInfoCliente(), BorderLayout.NORTH);

        // Crear pestañas para las diferentes secciones
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mis Automóviles", inicializarPanelAutomoviles());
        tabbedPane.addTab("Servicios en Progreso", inicializarPanelServiciosPendientes());
        tabbedPane.addTab("Mis Facturas", inicializarPanelFacturas());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Añadir panel principal a la ventana
        setContentPane(mainPanel);

        // Cargar datos iniciales
        cargarDatos();

        // Iniciar actualización automática de servicios en progreso
        iniciarActualizacionAutomatica();
    }

    private JPanel crearPanelInfoCliente() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Info del cliente
        String tipoCliente = "Cliente " + (clienteActual.getTipoCliente().equals("oro") ? "Oro " : "Normal");
        JLabel lblBienvenida = new JLabel("Bienvenido, " + clienteActual.getNombreCompleto() +
                " | " + tipoCliente + " | ID: " + clienteActual.getIdentificador());
        lblBienvenida.setForeground(Color.WHITE);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 14));

        // Botón de cerrar sesión exactamente como en AdminView
        JButton btnLogout = new JButton("Cerrar sesión");
        btnLogout.setBackground(new Color(180, 30, 30));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setOpaque(true);
        btnLogout.addActionListener(e -> cerrarSesion());

        // Panel para el botón con su propio FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_PRIMARY);
        buttonPanel.add(btnLogout);

        // Añadir componentes al panel principal
        panel.add(lblBienvenida, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel inicializarPanelAutomoviles() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel de título
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(COLOR_LIGHT);
        JLabel titleLabel = new JLabel("Mis Automóviles");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_PRIMARY_DARK);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // Panel de botones superior
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtonPanel.setBackground(COLOR_LIGHT);
        JButton btnAgregar = crearBotonPrimario("Registrar Automóvil");
        btnAgregar.addActionListener(e -> registrarAutomovil());
        topButtonPanel.add(btnAgregar);
        titlePanel.add(topButtonPanel, BorderLayout.EAST);

        panel.add(titlePanel, BorderLayout.NORTH);

        // Crear tabla y modelo
        String[] columnas = { "Placa", "Marca", "Modelo", "Acciones" };
        modeloAutomoviles = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Solo la columna de acciones es editable
            }
        };

        tablaAutomoviles = new JTable(modeloAutomoviles);
        JScrollPane scrollPane = new JScrollPane(tablaAutomoviles);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1));

        // Configurar tabla con el mismo estilo que las otras
        tablaAutomoviles.setFillsViewportHeight(true);
        estilizarTabla(tablaAutomoviles);

        // Ajustar ancho de columnas
        TableColumnModel columnModel = tablaAutomoviles.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(3).setPreferredWidth(120);

        // Añadir botón de solicitar servicio en la columna de acciones con estilo
        // mejorado
        tablaAutomoviles.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("Solicitar"));
        tablaAutomoviles.getColumnModel().getColumn(3)
                .setCellEditor(new ButtonEditor(new JCheckBox(), "Solicitar") {
                    @Override
                    public void buttonClicked() {
                        int row = tablaAutomoviles.getSelectedRow();
                        if (row >= 0) {
                            String placa = (String) modeloAutomoviles.getValueAt(row, 0);
                            Automovil auto = buscarAutomovilPorPlaca(placa);
                            if (auto != null) {
                                solicitarServicio(auto);
                            }
                        }
                    }
                });

        // Panel central con la tabla
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(COLOR_LIGHT);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel inicializarPanelServiciosPendientes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel de título
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(COLOR_LIGHT);
        JLabel titleLabel = new JLabel("Servicios en Progreso");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_PRIMARY_DARK);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        panel.add(titlePanel, BorderLayout.NORTH);

        // Crear tabla y modelo
        String[] columnas = { "Orden", "Vehículo", "Servicio", "Estado", "Fecha", "Tiempo Restante" };
        modeloServiciosPendientes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaServiciosPendientes = new JTable(modeloServiciosPendientes);
        JScrollPane scrollPane = new JScrollPane(tablaServiciosPendientes);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1));

        // Configurar tabla
        tablaServiciosPendientes.setFillsViewportHeight(true);
        estilizarTabla(tablaServiciosPendientes);

        // Ajustar anchos de columna
        TableColumnModel columnModel = tablaServiciosPendientes.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(100);

        // Panel central con la tabla
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(COLOR_LIGHT);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel inicializarPanelFacturas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Mis Facturas");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_PRIMARY_DARK);
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columnas = { "Factura", "Vehículo", "Servicio", "Total", "Estado", "Acciones" };
        modeloFacturas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Solo la columna de acciones es editable
            }
        };

        tablaFacturas = new JTable(modeloFacturas);
        JScrollPane scrollPane = new JScrollPane(tablaFacturas);
        estilizarTabla(tablaFacturas);

        // Llenar la tabla con las facturas del cliente
        cargarFacturasCliente();

        // Botón para descargar PDF
        tablaFacturas.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Descargar PDF"));
        tablaFacturas.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "Descargar PDF") {
            @Override
            public void buttonClicked() {
                int row = tablaFacturas.getSelectedRow();
                if (row >= 0 && row < modeloFacturas.getRowCount()) {
                    int facturaId = (int) modeloFacturas.getValueAt(row, 0);
                    Factura factura = FacturaController.obtenerFacturaPorId(facturaId);
                    if (factura != null) {
                        GeneradorPDF.generarFacturaPDF(factura);
                        JOptionPane.showMessageDialog(null, "Factura PDF generada correctamente.");
                    }
                }
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void cargarFacturasCliente() {
        modeloFacturas.setRowCount(0);
        Vector<Factura> facturas = FacturaController.obtenerFacturasPorCliente(clienteActual.getIdentificador());
        for (Factura f : facturas) {
            modeloFacturas.addRow(new Object[] {
                    f.getId(),
                    f.getServicioAsociado().getNombre(),
                    f.getAutomovil().getMarca() + " " + f.getAutomovil().getModelo(),
                    String.format("Q %.2f", f.getTotal()),
                    f.getFechaEmision(),
                    f.getEstado(),
                    "Acciones"
            });
        }
    }

    private void cargarDatos() {
        // Cargar automóviles
        modeloAutomoviles.setRowCount(0);
        for (Automovil auto : clienteActual.getAutomoviles()) {
            modeloAutomoviles.addRow(new Object[] {
                    auto.getPlaca(),
                    auto.getMarca(),
                    auto.getModelo(),
                    "Solicitar Servicio"
            });
        }

        // Cargar servicios pendientes
        actualizarTablaServiciosPendientes();
    }

    private void registrarAutomovil() {
        // Implementar el formulario para registrar un nuevo automóvil
        JTextField txtPlaca = new JTextField();
        JTextField txtMarca = new JTextField();
        txtMarca.setToolTipText("Ejemplo: honda, mazda, toyota");

        JTextField txtModelo = new JTextField();
        txtModelo.setToolTipText("Ejemplo: civic 2005, 3 2017, corolla 2010");

        JLabel ayuda = new JLabel("<html><span style='color: #1976D2;'>"
                + "Marca: <b>honda, mazda, toyota</b> &nbsp;&nbsp; "
                + "Modelo: <b>civic 2005, 3 2017, corolla 2010</b>"
                + "</span></html>");
        ayuda.setFont(new Font("Arial", Font.PLAIN, 12));
        ayuda.setForeground(new Color(25, 118, 210));

        Object[] message = {
                ayuda,
                "Placa:", txtPlaca,
                "Marca:", txtMarca,
                "Modelo:", txtModelo
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Registrar Automóvil",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String placa = txtPlaca.getText().trim();
                String marca = txtMarca.getText().trim();
                String modelo = txtModelo.getText().trim();

                // Validar datos
                if (placa.isEmpty() || marca.isEmpty() || modelo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Crear el automóvil
                Automovil auto = new Automovil(placa, marca, modelo, "");

                // Agregar al cliente
                boolean ok = ClienteController.agregarAutomovilACliente(clienteActual, auto);

                if (ok) {
                    // Actualizar la tabla
                    modeloAutomoviles.addRow(new Object[] {
                            auto.getPlaca(),
                            auto.getMarca(),
                            auto.getModelo(),
                            "Solicitar Servicio"
                    });

                    JOptionPane.showMessageDialog(this, "Automóvil registrado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al registrar el automóvil.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al registrar: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Automovil buscarAutomovilPorPlaca(String placa) {
        for (Automovil auto : clienteActual.getAutomoviles()) {
            if (auto.getPlaca().equals(placa)) {
                return auto;
            }
        }
        return null;
    }

    private void solicitarServicio(Automovil auto) {
        // Obtener servicios disponibles para este auto
        Vector<Servicio> serviciosCompatibles = ServicioController.obtenerServiciosCompatibles(auto);

        if (serviciosCompatibles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay servicios disponibles para este automóvil.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear lista de servicios
        JComboBox<Servicio> comboServicios = new JComboBox<>();
        for (Servicio servicio : serviciosCompatibles) {
            comboServicios.addItem(servicio);
        }

        // Mostrar diálogo
        Object[] message = {
                "Seleccione el servicio:", comboServicios
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Solicitar Servicio",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            Servicio servicioSeleccionado = (Servicio) comboServicios.getSelectedItem();

            // Crear orden de trabajo
            OrdenTrabajo orden = OrdenTrabajoController.crearOrden(
                    clienteActual, auto, servicioSeleccionado, null);

            if (orden != null) {
                JOptionPane.showMessageDialog(this,
                        "Servicio solicitado correctamente. Su automóvil ha sido puesto en cola.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);

                // Actualizar la tabla de servicios pendientes
                actualizarTablaServiciosPendientes();
            } else {
                JOptionPane.showMessageDialog(this, "Error al solicitar el servicio.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarTablaServiciosPendientes() {
        modeloServiciosPendientes.setRowCount(0);
        Vector<OrdenTrabajo> ordenes = OrdenTrabajoController.obtenerOrdenesPorCliente(clienteActual);
        for (OrdenTrabajo orden : ordenes) {
            // Si la orden ya está finalizada o facturada no la mostramos aquí
            if (orden.getEstado().equals("FINALIZADO") || orden.getEstado().equals("FACTURADO")) {
                continue;
            }

            modeloServiciosPendientes.addRow(new Object[] {
                    orden.getId(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo() + " ("
                            + orden.getAutomovil().getPlaca() + ")",
                    orden.getServicio().getNombre(),
                    orden.getEstado(),
                    orden.getFecha(),
                    orden.getTiempoRestanteTexto()
            });
        }
    }

    private void iniciarActualizacionAutomatica() {
        // Actualizar automáticamente los servicios en progreso cada 5 segundos
        new Timer(5000, e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                actualizarTablaServiciosPendientes();
            }
        }).start();
    }

    private void cerrarSesion() {
        // Cerrar ventana actual y volver a login
        dispose();
        new LoginView().setVisible(true);
    }

    @Override
    protected void inicializarComponentes() {
        tabbedPane.addTab("Mis Facturas", inicializarPanelFacturas());
    }

    /**
     * Aplica un estilo consistente a las tablas, con encabezados azules
     */
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

    /**
     * Modificar la clase ButtonRenderer para los botones de la tabla
     */
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
            // Usar un azul más claro para botones
            setBackground(new Color(66, 139, 202)); // Azul más claro
            setForeground(Color.WHITE);
            return this;
        }
    }

    /**
     * Modificar también la clase ButtonEditor
     */
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
            button.setBackground(new Color(66, 139, 202)); // Azul más claro
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
}