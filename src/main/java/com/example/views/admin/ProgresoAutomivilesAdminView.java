package com.example.views.admin;

import com.example.models.Factura;
import com.example.models.GestorDatos;
import com.example.models.GestorFacturas;
import com.example.models.GestorOrdenes;
import com.example.models.OrdenTrabajo;
import com.example.models.personas.Mecanico;
import com.example.models.personas.Persona;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Vista para que el administrador supervise y gestione el progreso de todos los
 * vehículos
 */
public class ProgresoAutomivilesAdminView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JTable tablaOrdenes;
    private DefaultTableModel modeloTabla;
    private JTextArea detallesOrden;
    private JComboBox<String> comboEstado;
    private JComboBox<Mecanico> comboMecanicos;
    private JLabel labelTotalOrdenes;
    private JLabel labelOrdenesEspera;
    private JLabel labelOrdenesServicio;
    private JLabel labelOrdenesListas;

    private Vector<OrdenTrabajo> ordenesFiltradas;
    private OrdenTrabajo ordenSeleccionada;
    private Vector<Mecanico> mecanicosDisponibles;

    public ProgresoAutomivilesAdminView(Persona usuario) {
        super("Progreso de Automóviles");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(0, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Progreso de Automóviles");
        titlePanel.add(titleLabel);

        // Panel de filtros y estadísticas
        JPanel topPanel = createTopPanel();

        // Panel central con tabla
        JPanel centerPanel = createCenterPanel();

        // Panel de detalles
        JPanel detailsPanel = createDetailsPanel();

        // Agregar paneles al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(detailsPanel, BorderLayout.EAST);

        // Cargar datos iniciales
        cargarOrdenes();
        actualizarEstadisticas();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Panel de filtros
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtersPanel.setOpaque(false);

        JLabel lblFiltro = createLabel("Filtrar por estado:");
        String[] estados = { "Todas", "En Espera", "En Servicio", "Listas" };
        JComboBox<String> comboFiltro = new JComboBox<>(estados);

        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(e -> {
            cargarOrdenes();
            actualizarEstadisticas();
        });

        comboFiltro.addActionListener(e -> {
            String filtro = (String) comboFiltro.getSelectedItem();
            aplicarFiltro(filtro);
        });

        filtersPanel.add(lblFiltro);
        filtersPanel.add(comboFiltro);
        filtersPanel.add(refreshButton);

        // Panel de estadísticas
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createTitledBorder("Estadísticas"));

        labelTotalOrdenes = new JLabel("Total: 0");
        labelOrdenesEspera = new JLabel("En espera: 0");
        labelOrdenesServicio = new JLabel("En servicio: 0");
        labelOrdenesListas = new JLabel("Listas: 0");

        statsPanel.add(labelTotalOrdenes);
        statsPanel.add(labelOrdenesEspera);
        statsPanel.add(labelOrdenesServicio);
        statsPanel.add(labelOrdenesListas);

        // Panel principal
        JPanel mainTopPanel = new JPanel(new BorderLayout());
        mainTopPanel.setOpaque(false);
        mainTopPanel.add(filtersPanel, BorderLayout.NORTH);
        mainTopPanel.add(statsPanel, BorderLayout.CENTER);

        panel.add(mainTopPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Modelo de tabla
        String[] columnas = { "Nº Orden", "Cliente", "Automóvil", "Servicio", "Mecánico", "Estado", "Fecha" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaOrdenes = new JTable(modeloTabla);
        tablaOrdenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Personalizar renderizado de la columna de estado
        tablaOrdenes.getColumnModel().getColumn(5).setCellRenderer(new TableCellRenderer() {
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

        JScrollPane scrollPane = new JScrollPane(tablaOrdenes);

        // Listener para detectar selección
        tablaOrdenes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaOrdenes.getSelectedRow() != -1) {
                mostrarDetallesOrden(tablaOrdenes.getSelectedRow());
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Detalles de la Orden"));
        panel.setPreferredSize(new Dimension(300, 0));

        // Panel de información
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Área de texto para detalles
        detallesOrden = new JTextArea(10, 20);
        detallesOrden.setEditable(false);
        detallesOrden.setLineWrap(true);
        detallesOrden.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(detallesOrden);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());

        infoPanel.add(scrollPane);
        infoPanel.add(Box.createVerticalStrut(10));

        // Panel de acciones
        JPanel actionsPanel = new JPanel(new GridBagLayout());
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Acciones"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Combo de estado
        gbc.gridx = 0;
        gbc.gridy = 0;
        actionsPanel.add(createLabel("Cambiar Estado:"), gbc);

        gbc.gridy = 1;
        comboEstado = new JComboBox<>(new String[] { "En Espera", "En Servicio", "Listo" });
        actionsPanel.add(comboEstado, gbc);

        // Combo de mecánicos
        gbc.gridy = 2;
        actionsPanel.add(createLabel("Asignar Mecánico:"), gbc);

        gbc.gridy = 3;
        comboMecanicos = new JComboBox<>();
        actionsPanel.add(comboMecanicos, gbc);

        // Botones de acción
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton btnGuardar = createPrimaryButton("Guardar Cambios");
        btnGuardar.addActionListener(e -> guardarCambios());
        actionsPanel.add(btnGuardar, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 5, 5, 5);
        JButton btnFacturar = createPrimaryButton("Generar Factura");
        btnFacturar.addActionListener(e -> generarFactura());
        actionsPanel.add(btnFacturar, gbc);

        // Agregar componentes al panel principal
        panel.add(infoPanel);
        panel.add(actionsPanel);

        return panel;
    }

    private void cargarOrdenes() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener todas las órdenes
        Vector<OrdenTrabajo> ordenes = GestorOrdenes.getInstancia().getOrdenesServicio();
        ordenesFiltradas = new Vector<>(ordenes);

        for (OrdenTrabajo orden : ordenesFiltradas) {
            // Mapear estado interno a texto mostrado
            String estadoMostrado = mapearEstado(orden.getEstado());

            // Formatear fecha
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String fechaFormateada = sdf.format(orden.getFecha());

            Object[] fila = {
                    orden.getNumeroOrden(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo() + " ("
                            + orden.getAutomovil().getPlaca() + ")",
                    orden.getServicio().getNombre(),
                    orden.getMecanico() != null ? orden.getMecanico().getNombreCompleto() : "No asignado",
                    estadoMostrado,
                    fechaFormateada
            };
            modeloTabla.addRow(fila);
        }

        // Cargar mecánicos disponibles
        cargarMecanicos();

        // Si hay filas, seleccionar la primera
        if (tablaOrdenes.getRowCount() > 0) {
            tablaOrdenes.setRowSelectionInterval(0, 0);
            mostrarDetallesOrden(0);
        } else {
            limpiarDetalles();
        }
    }

    private void cargarMecanicos() {
        // Guardar la selección actual
        Mecanico seleccionado = comboMecanicos.getItemCount() > 0
                ? comboMecanicos.getItemAt(comboMecanicos.getSelectedIndex())
                : null;

        // Limpiar el combo
        comboMecanicos.removeAllItems();

        // Obtener todos los mecánicos
        mecanicosDisponibles = GestorDatos.getInstancia().getMecanicos();

        // Agregar al combo
        for (Mecanico mecanico : mecanicosDisponibles) {
            comboMecanicos.addItem(mecanico);
        }

        // Restaurar selección si es posible
        if (seleccionado != null) {
            for (int i = 0; i < comboMecanicos.getItemCount(); i++) {
                Mecanico m = comboMecanicos.getItemAt(i);
                if (m.getIdentificador().equals(seleccionado.getIdentificador())) {
                    comboMecanicos.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void mostrarDetallesOrden(int rowIndex) {
        // Obtener número de orden
        int numeroOrden = (int) modeloTabla.getValueAt(rowIndex, 0);

        // Buscar la orden
        ordenSeleccionada = null;
        for (OrdenTrabajo orden : ordenesFiltradas) {
            if (orden.getNumeroOrden() == numeroOrden) {
                ordenSeleccionada = orden;
                break;
            }
        }

        if (ordenSeleccionada == null) {
            limpiarDetalles();
            return;
        }

        // Actualizar detalles
        StringBuilder sb = new StringBuilder();
        sb.append("Orden de Trabajo #").append(ordenSeleccionada.getNumeroOrden()).append("\n\n");

        sb.append("Cliente: ").append(ordenSeleccionada.getCliente().getNombreCompleto()).append("\n");
        sb.append("DPI: ").append(ordenSeleccionada.getCliente().getIdentificador()).append("\n");
        sb.append("Tipo: ").append(ordenSeleccionada.getCliente().getTipoCliente()).append("\n\n");

        sb.append("Automóvil: ").append(ordenSeleccionada.getAutomovil().getMarca())
                .append(" ").append(ordenSeleccionada.getAutomovil().getModelo()).append("\n");
        sb.append("Placa: ").append(ordenSeleccionada.getAutomovil().getPlaca()).append("\n\n");

        sb.append("Servicio: ").append(ordenSeleccionada.getServicio().getNombre()).append("\n");
        sb.append("Precio: Q").append(String.format("%.2f", ordenSeleccionada.getServicio().getPrecio())).append("\n");
        sb.append("Tiempo estimado: ").append(ordenSeleccionada.getServicio().getTiempoEstimado()).append(" horas\n\n");

        sb.append("Mecánico: ")
                .append(ordenSeleccionada.getMecanico() != null ? ordenSeleccionada.getMecanico().getNombreCompleto()
                        : "No asignado")
                .append("\n");

        sb.append("Estado: ").append(mapearEstado(ordenSeleccionada.getEstado())).append("\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sb.append("Fecha de ingreso: ").append(sdf.format(ordenSeleccionada.getFecha())).append("\n");

        detallesOrden.setText(sb.toString());

        // Actualizar combo de estado
        switch (ordenSeleccionada.getEstado()) {
            case "espera":
                comboEstado.setSelectedIndex(0);
                break;
            case "servicio":
                comboEstado.setSelectedIndex(1);
                break;
            case "listo":
                comboEstado.setSelectedIndex(2);
                break;
        }

        // Actualizar combo de mecánicos
        if (ordenSeleccionada.getMecanico() != null) {
            for (int i = 0; i < comboMecanicos.getItemCount(); i++) {
                Mecanico m = comboMecanicos.getItemAt(i);
                if (m.getIdentificador().equals(ordenSeleccionada.getMecanico().getIdentificador())) {
                    comboMecanicos.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void limpiarDetalles() {
        detallesOrden.setText("Seleccione una orden para ver sus detalles");
        ordenSeleccionada = null;
    }

    private void aplicarFiltro(String filtro) {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener todas las órdenes
        Vector<OrdenTrabajo> ordenes = GestorOrdenes.getInstancia().getOrdenesServicio();
        ordenesFiltradas = new Vector<>();

        // Filtrar según el criterio seleccionado
        if (filtro.equals("Todas")) {
            ordenesFiltradas = new Vector<>(ordenes);
        } else {
            for (OrdenTrabajo orden : ordenes) {
                String estadoMostrado = mapearEstado(orden.getEstado());
                if (estadoMostrado.equals(filtro)) {
                    ordenesFiltradas.add(orden);
                }
            }
        }

        // Actualizar tabla
        for (OrdenTrabajo orden : ordenesFiltradas) {
            String estadoMostrado = mapearEstado(orden.getEstado());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String fechaFormateada = sdf.format(orden.getFecha());

            Object[] fila = {
                    orden.getNumeroOrden(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo() + " ("
                            + orden.getAutomovil().getPlaca() + ")",
                    orden.getServicio().getNombre(),
                    orden.getMecanico() != null ? orden.getMecanico().getNombreCompleto() : "No asignado",
                    estadoMostrado,
                    fechaFormateada
            };
            modeloTabla.addRow(fila);
        }

        // Si hay filas, seleccionar la primera
        if (tablaOrdenes.getRowCount() > 0) {
            tablaOrdenes.setRowSelectionInterval(0, 0);
            mostrarDetallesOrden(0);
        } else {
            limpiarDetalles();
        }
    }

    private void actualizarEstadisticas() {
        Vector<OrdenTrabajo> ordenes = GestorOrdenes.getInstancia().getOrdenesServicio();

        int total = ordenes.size();
        int espera = 0;
        int servicio = 0;
        int listas = 0;

        for (OrdenTrabajo orden : ordenes) {
            switch (orden.getEstado()) {
                case "espera":
                    espera++;
                    break;
                case "servicio":
                    servicio++;
                    break;
                case "listo":
                    listas++;
                    break;
            }
        }

        labelTotalOrdenes.setText("Total: " + total);
        labelOrdenesEspera.setText("En espera: " + espera);
        labelOrdenesServicio.setText("En servicio: " + servicio);
        labelOrdenesListas.setText("Listas: " + listas);
    }

    private void guardarCambios() {
        if (ordenSeleccionada == null) {
            showErrorMessage("Debe seleccionar una orden para realizar cambios");
            return;
        }

        // Obtener estado seleccionado
        String nuevoEstadoMostrado = (String) comboEstado.getSelectedItem();
        String nuevoEstado = mapearEstadoInterno(nuevoEstadoMostrado);

        // Obtener mecánico seleccionado
        Mecanico nuevoMecanico = (Mecanico) comboMecanicos.getSelectedItem();

        // Verificar si hay cambios
        boolean hayTambios = false;

        if (!ordenSeleccionada.getEstado().equals(nuevoEstado)) {
            // Si cambia de estado, actualizar
            ordenSeleccionada.setEstado(nuevoEstado);
            hayTambios = true;

            // Si pasa a "listo", finalizar la orden
            if (nuevoEstado.equals("listo")) {
                GestorOrdenes.getInstancia().finalizarOrden(ordenSeleccionada);
            }

            // Si pasa a "servicio", iniciar servicio
            if (nuevoEstado.equals("servicio")) {
                GestorOrdenes.getInstancia().iniciarServicioVehiculo(ordenSeleccionada);
            }
        }

        // Verificar si cambió el mecánico
        if ((ordenSeleccionada.getMecanico() == null && nuevoMecanico != null) ||
                (ordenSeleccionada.getMecanico() != null && nuevoMecanico != null &&
                        !ordenSeleccionada.getMecanico().getIdentificador().equals(nuevoMecanico.getIdentificador()))) {

            // Si tenía un mecánico, liberarlo
            if (ordenSeleccionada.getMecanico() != null) {
                ordenSeleccionada.getMecanico().liberarOrden(ordenSeleccionada);
            }

            // Asignar nuevo mecánico
            ordenSeleccionada.setMecanico(nuevoMecanico);
            nuevoMecanico.asignarOrden(ordenSeleccionada);
            hayTambios = true;
        }

        if (hayTambios) {
            // Guardar cambios
            Serializador.guardarDatos();
            Serializador.guardarOrdenes();

            // Mostrar mensaje de éxito
            showSuccessMessage("Cambios guardados correctamente");

            // Recargar datos
            cargarOrdenes();
            actualizarEstadisticas();
        } else {
            showInfoMessage("No se detectaron cambios");
        }
    }

    private void generarFactura() {
        if (ordenSeleccionada == null) {
            showErrorMessage("Debe seleccionar una orden para generar factura");
            return;
        }

        if (!ordenSeleccionada.getEstado().equals("listo")) {
            showErrorMessage("Solo se pueden generar facturas para órdenes finalizadas");
            return;
        }

        // Verificar si ya existe una factura para esta orden
        for (Factura factura : GestorFacturas.getInstancia().getFacturas()) {
            if (factura.getOrdenTrabajo().getNumeroOrden() == ordenSeleccionada.getNumeroOrden()) {
                showInfoMessage("Ya existe una factura para esta orden (Nº " + factura.getNumeroFactura() + ")");
                return;
            }
        }

        // Generar factura
        Factura nuevaFactura = GestorFacturas.getInstancia().crearFactura(ordenSeleccionada);

        if (nuevaFactura != null) {
            // Guardar cambios
            Serializador.guardarFacturas();

            // Mostrar mensaje de éxito
            showSuccessMessage("Factura generada correctamente con Nº " + nuevaFactura.getNumeroFactura());
        } else {
            showErrorMessage("Error al generar la factura");
        }
    }

    // Métodos auxiliares para mapear estados
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

    private String mapearEstadoInterno(String estadoMostrado) {
        switch (estadoMostrado) {
            case "En Espera":
                return "espera";
            case "En Servicio":
                return "servicio";
            case "Listo":
                return "listo";
            default:
                return estadoMostrado.toLowerCase();
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}