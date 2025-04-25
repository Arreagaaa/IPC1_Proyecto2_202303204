package com.example.views.cliente;

import com.example.models.Automovil;
import com.example.models.GestorOrdenes;
import com.example.models.OrdenTrabajo;
import com.example.models.Repuesto;
import com.example.models.Servicio;
import com.example.models.personas.Cliente;
import com.example.models.personas.Persona;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Vista para que el cliente monitoree el progreso de sus vehículos en el taller
 */
public class ProgresoVehiculosView extends BaseView implements Automovil.AutomovilListener {

    private Persona usuario;
    private JPanel mainPanel;

    // Componentes para la interfaz
    private JTable tablaVehiculos;
    private DefaultTableModel modeloTabla;
    private JProgressBar progresoServicio;
    private JTextArea detallesServicio;
    private JLabel estadoLabel;
    private JLabel tiempoLabel;

    // Componentes para autorización de diagnóstico
    private JPanel panelDiagnostico;
    private JLabel servicioRecomendadoLabel;
    private JButton btnAceptarServicio;
    private JButton btnRechazarServicio;

    // Para mantener una referencia a las órdenes actuales
    private Vector<OrdenTrabajo> ordenesMostradas;

    // Para actualización periódica
    private Timer timer;

    public ProgresoVehiculosView(Persona usuario) {
        super("Progreso de Vehículos");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Progreso de mis Vehículos");
        titlePanel.add(titleLabel);

        // Panel principal dividido horizontalmente
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);

        // Panel izquierdo - Lista de vehículos en progreso
        JPanel leftPanel = createVehiculosList();

        // Panel derecho - Detalles y progreso
        JPanel rightPanel = createProgressPanel();

        // Configurar y agregar paneles al SplitPane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setOneTouchExpandable(true);

        // Agregar componentes al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Iniciar timer para actualización periódica
        iniciarTimerActualizacion();

        // Cargar datos iniciales
        cargarVehiculosEnTaller();
    }

    private JPanel createVehiculosList() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Vehículos en Taller"));

        // Definir columnas para la tabla
        Vector<String> columnas = new Vector<>();
        columnas.add("Placa");
        columnas.add("Vehículo");
        columnas.add("Servicio");
        columnas.add("Estado");
        columnas.add("Progreso");
        columnas.add("Fecha");

        // Crear modelo de tabla
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4)
                    return JProgressBar.class;
                return Object.class;
            }
        };

        // Crear tabla
        tablaVehiculos = new JTable(modeloTabla);
        tablaVehiculos.setRowHeight(30);

        // Configurar renderer para barra de progreso
        tablaVehiculos.getColumnModel().getColumn(4).setCellRenderer(new ProgressBarRenderer());

        // Configurar renderer para columna de estado (colorear según estado)
        tablaVehiculos.getColumnModel().getColumn(3).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);

                if (isSelected) {
                    label.setBackground(table.getSelectionBackground());
                    label.setForeground(table.getSelectionForeground());
                } else {
                    label.setForeground(Color.WHITE);

                    String estado = value.toString();
                    if (estado.equals("En Espera")) {
                        label.setBackground(new Color(178, 34, 34)); // Rojo
                    } else if (estado.equals("En Servicio")) {
                        label.setBackground(new Color(255, 140, 0)); // Naranja
                    } else if (estado.equals("Listo")) {
                        label.setBackground(new Color(46, 139, 87)); // Verde
                    }
                }

                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaVehiculos);

        // Detectar selección
        tablaVehiculos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaVehiculos.getSelectedRow() != -1) {
                mostrarDetallesVehiculo();
            }
        });

        // Panel de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setOpaque(false);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarVehiculosEnTaller());

        buttonsPanel.add(btnActualizar);

        // Estructura del panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Detalles y Progreso"));

        // Panel de información general
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Estado actual
        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(createLabel("Estado:"), gbc);

        gbc.gridx = 1;
        estadoLabel = new JLabel("No seleccionado");
        estadoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(estadoLabel, gbc);

        // Tiempo estimado
        gbc.gridx = 0;
        gbc.gridy = 1;
        infoPanel.add(createLabel("Tiempo estimado:"), gbc);

        gbc.gridx = 1;
        tiempoLabel = new JLabel("--");
        infoPanel.add(tiempoLabel, gbc);

        // Barra de progreso
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 15, 5);

        progresoServicio = new JProgressBar(0, 100);
        progresoServicio.setStringPainted(true);
        progresoServicio.setString("0%");
        progresoServicio.setValue(0);
        progresoServicio.setPreferredSize(new Dimension(0, 25));
        infoPanel.add(progresoServicio, gbc);

        // Panel para diagnóstico (inicialmente oculto)
        gbc.gridy = 3;
        panelDiagnostico = new JPanel(new GridBagLayout());
        panelDiagnostico.setOpaque(false);
        panelDiagnostico.setBorder(BorderFactory.createTitledBorder("Diagnóstico"));
        panelDiagnostico.setVisible(false);

        GridBagConstraints gbcDiag = new GridBagConstraints();
        gbcDiag.fill = GridBagConstraints.HORIZONTAL;
        gbcDiag.insets = new Insets(5, 5, 5, 5);
        gbcDiag.weightx = 1.0;

        gbcDiag.gridx = 0;
        gbcDiag.gridy = 0;
        gbcDiag.gridwidth = 2;
        JLabel diagLabel = new JLabel("Se recomienda el siguiente servicio:");
        diagLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        panelDiagnostico.add(diagLabel, gbcDiag);

        gbcDiag.gridy = 1;
        servicioRecomendadoLabel = new JLabel("Nombre del servicio");
        servicioRecomendadoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panelDiagnostico.add(servicioRecomendadoLabel, gbcDiag);

        gbcDiag.gridy = 2;
        gbcDiag.gridwidth = 1;
        btnAceptarServicio = new JButton("Aceptar Servicio");
        btnAceptarServicio.addActionListener(e -> aceptarServicioRecomendado());
        panelDiagnostico.add(btnAceptarServicio, gbcDiag);

        gbcDiag.gridx = 1;
        btnRechazarServicio = new JButton("Rechazar");
        btnRechazarServicio.addActionListener(e -> rechazarServicioRecomendado());
        panelDiagnostico.add(btnRechazarServicio, gbcDiag);

        infoPanel.add(panelDiagnostico, gbc);

        // Detalles del servicio
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 5, 5, 5);

        detallesServicio = new JTextArea();
        detallesServicio.setEditable(false);
        detallesServicio.setWrapStyleWord(true);
        detallesServicio.setLineWrap(true);
        JScrollPane scrollDetalles = new JScrollPane(detallesServicio);
        scrollDetalles.setBorder(BorderFactory.createTitledBorder("Detalles del Servicio"));
        infoPanel.add(scrollDetalles, gbc);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private void cargarVehiculosEnTaller() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener cliente
        Cliente cliente = (Cliente) usuario;

        // Obtener órdenes de trabajo del cliente (en todos los estados)
        Vector<OrdenTrabajo> ordenesCliente = new Vector<>();

        // Obtener órdenes en espera
        Vector<OrdenTrabajo> ordenesEspera = GestorOrdenes.getInstancia().getColaEspera();
        for (int i = 0; i < ordenesEspera.size(); i++) {
            OrdenTrabajo orden = ordenesEspera.elementAt(i);
            if (orden.getCliente().getIdentificador().equals(cliente.getIdentificador())) {
                ordenesCliente.add(orden);
            }
        }

        // Obtener órdenes en servicio
        Vector<OrdenTrabajo> ordenesServicio = GestorOrdenes.getInstancia().getOrdenesServicio();
        for (int i = 0; i < ordenesServicio.size(); i++) {
            OrdenTrabajo orden = ordenesServicio.elementAt(i);
            if (orden.getCliente().getIdentificador().equals(cliente.getIdentificador())) {
                ordenesCliente.add(orden);
            }
        }

        // Guardar órdenes mostradas para referencia
        ordenesMostradas = ordenesCliente;

        // Formatear datos para la tabla
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (int i = 0; i < ordenesCliente.size(); i++) {
            OrdenTrabajo orden = ordenesCliente.elementAt(i);
            Automovil auto = orden.getAutomovil();

            // Escuchar cambios en el automóvil
            auto.agregarListener(this);

            // Crear barra de progreso para la tabla
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue(auto.getProgresoServicio());
            progressBar.setStringPainted(true);
            progressBar.setString(auto.getProgresoServicio() + "%");

            // Determinar estado para mostrar
            String estadoMostrado = mapearEstado(orden.getEstado());

            // Crear fila
            Vector<Object> fila = new Vector<>();
            fila.add(auto.getPlaca());
            fila.add(auto.getMarca() + " " + auto.getModelo());
            fila.add(orden.getServicio().getNombre());
            fila.add(estadoMostrado);
            fila.add(progressBar); // La tabla tiene un renderer especial para esto
            fila.add(sdf.format(orden.getFecha()));

            modeloTabla.addRow(fila);
        }

        // Si hay filas, seleccionar la primera
        if (tablaVehiculos.getRowCount() > 0) {
            tablaVehiculos.setRowSelectionInterval(0, 0);
            mostrarDetallesVehiculo();
        } else {
            limpiarDetalles();
        }
    }

    private void mostrarDetallesVehiculo() {
        int rowIndex = tablaVehiculos.getSelectedRow();
        if (rowIndex == -1 || rowIndex >= ordenesMostradas.size()) {
            limpiarDetalles();
            return;
        }

        // Obtener orden de trabajo
        OrdenTrabajo orden = ordenesMostradas.elementAt(rowIndex);
        Automovil auto = orden.getAutomovil();

        // Actualizar labels de estado
        estadoLabel.setText(mapearEstado(orden.getEstado()));

        // Colorear según el estado
        switch (orden.getEstado()) {
            case "espera":
                estadoLabel.setForeground(new Color(178, 34, 34)); // Rojo
                break;
            case "servicio":
                estadoLabel.setForeground(new Color(255, 140, 0)); // Naranja
                break;
            case "listo":
                estadoLabel.setForeground(new Color(46, 139, 87)); // Verde
                break;
        }

        // Actualizar tiempo estimado
        tiempoLabel.setText(orden.getServicio().getTiempoEstimado() + " horas");

        // Actualizar barra de progreso
        progresoServicio.setValue(auto.getProgresoServicio());
        progresoServicio.setString(auto.getProgresoServicio() + "%");

        // Detalles del servicio
        StringBuilder sb = new StringBuilder();
        sb.append("Servicio: ").append(orden.getServicio().getNombre()).append("\n\n");
        sb.append("Descripción: ").append(orden.getServicio().getDescripcion()).append("\n\n");
        sb.append("Precio mano de obra: Q").append(String.format("%.2f", orden.getServicio().getPrecioManoObra()))
                .append("\n");

        // Si no es diagnóstico, mostrar repuestos
        if (!orden.getServicio().getNombre().equalsIgnoreCase("Diagnóstico")) {
            sb.append("\nRepuestos utilizados:\n");
            Vector<Repuesto> repuestos = orden.getServicio().getRepuestos();
            double totalRepuestos = 0;

            for (int i = 0; i < repuestos.size(); i++) {
                Repuesto r = repuestos.elementAt(i);
                sb.append("- ").append(r.getNombre()).append(": Q").append(String.format("%.2f", r.getPrecio()))
                        .append("\n");
                totalRepuestos += r.getPrecio();
            }

            sb.append("\nTotal repuestos: Q").append(String.format("%.2f", totalRepuestos)).append("\n");
            sb.append("Precio total: Q").append(String.format("%.2f", orden.getServicio().getPrecio())).append("\n");
        }

        // Información del mecánico
        if (orden.getMecanico() != null) {
            sb.append("\nMecánico asignado: ").append(orden.getMecanico().getNombreCompleto()).append("\n");
        } else {
            sb.append("\nAún no se ha asignado un mecánico\n");
        }

        detallesServicio.setText(sb.toString());

        // Mostrar panel de diagnóstico si corresponde
        if (orden.getServicio().getNombre().equalsIgnoreCase("Diagnóstico") &&
                orden.getServicioRecomendado() != null &&
                !orden.isServicioRecomendadoGestionado()) {

            mostrarPanelDiagnostico(orden);
        } else {
            panelDiagnostico.setVisible(false);
        }
    }

    private void mostrarPanelDiagnostico(OrdenTrabajo orden) {
        panelDiagnostico.setVisible(true);

        Servicio servicioRecomendado = orden.getServicioRecomendado();
        servicioRecomendadoLabel.setText(
                servicioRecomendado.getNombre() + " - Q" + String.format("%.2f", servicioRecomendado.getPrecio()));

        // Guardar referencia a la orden en los botones
        btnAceptarServicio.putClientProperty("orden", orden);
        btnRechazarServicio.putClientProperty("orden", orden);
    }

    private void aceptarServicioRecomendado() {
        OrdenTrabajo orden = (OrdenTrabajo) btnAceptarServicio.getClientProperty("orden");
        if (orden != null) {
            // Cambiar el servicio de la orden al recomendado
            Servicio servicioRecomendado = orden.getServicioRecomendado();
            orden.setServicio(servicioRecomendado);
            orden.setServicioRecomendadoGestionado(true);

            // Reiniciar estado de progreso si está en servicio
            if (orden.getEstado().equals("servicio")) {
                orden.getAutomovil().iniciarServicio(servicioRecomendado.getTiempoEstimado());
            }

            // Guardar cambios
            Serializador.guardarOrdenes();

            // Actualizar vista
            cargarVehiculosEnTaller();
            panelDiagnostico.setVisible(false);

            showSuccessMessage("Se ha aceptado el servicio recomendado");
        }
    }

    private void rechazarServicioRecomendado() {
        OrdenTrabajo orden = (OrdenTrabajo) btnRechazarServicio.getClientProperty("orden");
        if (orden != null) {
            // Marcar como gestionado pero no cambiar el servicio
            orden.setServicioRecomendadoGestionado(true);

            // Guardar cambios
            Serializador.guardarOrdenes();

            // Actualizar vista
            panelDiagnostico.setVisible(false);

            showInfoMessage("Se ha rechazado el servicio recomendado");
        }
    }

    private void limpiarDetalles() {
        estadoLabel.setText("No seleccionado");
        estadoLabel.setForeground(Color.BLACK);
        tiempoLabel.setText("--");
        progresoServicio.setValue(0);
        progresoServicio.setString("0%");
        detallesServicio.setText("Seleccione un vehículo para ver detalles");
        panelDiagnostico.setVisible(false);
    }

    private String mapearEstado(String estadoInterno) {
        switch (estadoInterno) {
            case "espera":
                return "En Espera";
            case "servicio":
                return "En Servicio";
            case "listo":
                return "Listo";
            default:
                return estadoInterno;
        }
    }

    private void iniciarTimerActualizacion() {
        // Actualizar cada 2 segundos
        timer = new Timer(2000, e -> {
            // Actualizar sólo si hay selección
            int selectedRow = tablaVehiculos.getSelectedRow();
            if (selectedRow != -1) {
                // Actualizar barra de progreso en la tabla
                OrdenTrabajo orden = ordenesMostradas.elementAt(selectedRow);
                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setValue(orden.getAutomovil().getProgresoServicio());
                progressBar.setStringPainted(true);
                progressBar.setString(orden.getAutomovil().getProgresoServicio() + "%");

                modeloTabla.setValueAt(progressBar, selectedRow, 4);

                // Actualizar detalles
                mostrarDetallesVehiculo();
            }
        });
        timer.start();
    }

    @Override
    public void onEstadoCambiado(Automovil automovil, String nuevoEstado) {
        // Actualizar vista cuando cambia el estado de un automóvil
        SwingUtilities.invokeLater(() -> cargarVehiculosEnTaller());
    }

    @Override
    public void onProgresoCambiado(Automovil automovil, int nuevoProgreso) {
        // Actualizar barras de progreso cuando cambia el progreso de un automóvil
        SwingUtilities.invokeLater(() -> {
            int selectedRow = tablaVehiculos.getSelectedRow();
            if (selectedRow != -1) {
                OrdenTrabajo orden = ordenesMostradas.elementAt(selectedRow);
                if (orden.getAutomovil().getPlaca().equals(automovil.getPlaca())) {
                    progresoServicio.setValue(nuevoProgreso);
                    progresoServicio.setString(nuevoProgreso + "%");
                }
            }

            // Actualizar todas las filas de la tabla
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                String placa = (String) modeloTabla.getValueAt(i, 0);
                if (placa.equals(automovil.getPlaca())) {
                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setValue(nuevoProgreso);
                    progressBar.setStringPainted(true);
                    progressBar.setString(nuevoProgreso + "%");

                    modeloTabla.setValueAt(progressBar, i, 4);
                    break;
                }
            }
        });
    }

    // Renderer para barras de progreso en tabla
    private class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressBarRenderer() {
            super(0, 100);
            setStringPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value instanceof JProgressBar) {
                JProgressBar pb = (JProgressBar) value;
                setValue(pb.getValue());
                setString(pb.getString());
            } else {
                setValue(0);
                setString("0%");
            }
            return this;
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    // Detener timer cuando se cierra la vista
    public void dispose() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
}