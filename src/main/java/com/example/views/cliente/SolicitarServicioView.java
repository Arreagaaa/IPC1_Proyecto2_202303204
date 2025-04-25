package com.example.views.cliente;

import com.example.models.Automovil;
import com.example.models.GestorDatos;
import com.example.models.GestorOrdenes;
import com.example.models.OrdenTrabajo;
import com.example.models.Repuesto;
import com.example.models.Servicio;
import com.example.models.personas.Cliente;
import com.example.models.personas.Persona;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

/**
 * Vista para que el cliente solicite servicios para sus vehículos
 */
public class SolicitarServicioView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JComboBox<Automovil> cboAutomovil;
    private JComboBox<Servicio> cboServicio;
    private JTextArea txtDetalleServicio;
    private JLabel lblPrecio;
    private JLabel lblTiempoEstimado;
    private JLabel lblCompatibilidad;
    private JLabel lblFotoAuto;
    private JLabel lblEstadoAuto;
    private JTable tablaRepuestos;
    private DefaultTableModel modeloTablaRepuestos;

    private Vector<Automovil> automoviles;
    private Vector<Servicio> servicios;

    private Automovil automovilSeleccionado;
    private Servicio servicioSeleccionado;

    public SolicitarServicioView(Persona usuario) {
        super("Solicitar Servicio");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Solicitar Servicio");
        titlePanel.add(titleLabel);

        // Panel principal con formulario
        JPanel formPanel = createFormPanel();

        // Agregar componentes al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarDatos();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Panel izquierdo - Selección de automóvil y servicio
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Selección de Servicio"));

        // Selección de automóvil
        JPanel autoPanel = new JPanel(new BorderLayout(5, 5));
        autoPanel.setOpaque(false);
        autoPanel.add(createLabel("Seleccione su automóvil:"), BorderLayout.NORTH);

        cboAutomovil = new JComboBox<>();
        cboAutomovil.addActionListener(e -> seleccionarAutomovil());
        autoPanel.add(cboAutomovil, BorderLayout.CENTER);

        // Vista previa del automóvil
        JPanel previewPanel = new JPanel(new BorderLayout(5, 5));
        previewPanel.setOpaque(false);
        previewPanel.setBorder(BorderFactory.createTitledBorder("Vista Previa"));

        // Panel para imagen y estado
        JPanel imgStatePanel = new JPanel(new BorderLayout(5, 5));
        imgStatePanel.setOpaque(false);

        lblFotoAuto = new JLabel("Seleccione un automóvil", JLabel.CENTER);
        lblFotoAuto.setPreferredSize(new Dimension(200, 150));
        imgStatePanel.add(lblFotoAuto, BorderLayout.CENTER);

        // Etiqueta para mostrar el estado del automóvil
        lblEstadoAuto = new JLabel("Estado: --", JLabel.CENTER);
        lblEstadoAuto.setFont(new Font(lblEstadoAuto.getFont().getName(), Font.BOLD, 12));
        imgStatePanel.add(lblEstadoAuto, BorderLayout.SOUTH);

        previewPanel.add(imgStatePanel, BorderLayout.CENTER);

        // Selección de servicio
        JPanel servicePanel = new JPanel(new BorderLayout(5, 5));
        servicePanel.setOpaque(false);
        servicePanel.add(createLabel("Seleccione el servicio:"), BorderLayout.NORTH);

        cboServicio = new JComboBox<>();
        cboServicio.addActionListener(e -> seleccionarServicio());
        servicePanel.add(cboServicio, BorderLayout.CENTER);

        // Indicador de compatibilidad
        lblCompatibilidad = createLabel("");
        lblCompatibilidad.setHorizontalAlignment(JLabel.CENTER);
        lblCompatibilidad.setFont(new Font(lblCompatibilidad.getFont().getName(), Font.BOLD, 14));

        // Botón para enviar solicitud
        JButton btnSolicitar = createPrimaryButton("Solicitar Servicio");
        btnSolicitar.addActionListener(e -> solicitarServicio());

        // Agregar componentes al panel izquierdo
        leftPanel.add(autoPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(previewPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(servicePanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(lblCompatibilidad);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(btnSolicitar);

        // Panel derecho - Detalles del servicio
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Servicio"));

        // Detalles de precio y tiempo
        JPanel detailsPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        detailsPanel.setOpaque(false);

        detailsPanel.add(createLabel("Precio:"));
        lblPrecio = createLabel("Q0.00");
        lblPrecio.setFont(new Font(lblPrecio.getFont().getName(), Font.BOLD, 14));
        detailsPanel.add(lblPrecio);

        detailsPanel.add(createLabel("Tiempo Estimado:"));
        lblTiempoEstimado = createLabel("0 horas");
        lblTiempoEstimado.setFont(new Font(lblTiempoEstimado.getFont().getName(), Font.BOLD, 14));
        detailsPanel.add(lblTiempoEstimado);

        // Descripción del servicio
        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setOpaque(false);
        descPanel.add(createLabel("Descripción:"), BorderLayout.NORTH);

        txtDetalleServicio = new JTextArea(5, 30);
        txtDetalleServicio.setEditable(false);
        txtDetalleServicio.setLineWrap(true);
        txtDetalleServicio.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtDetalleServicio);
        descPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel para mostrar los repuestos si no es diagnóstico
        JPanel repuestosPanel = new JPanel(new BorderLayout(5, 5));
        repuestosPanel.setOpaque(false);
        repuestosPanel.setBorder(BorderFactory.createTitledBorder("Repuestos incluidos"));

        // Configurar tabla de repuestos
        Vector<String> columnasRepuestos = new Vector<>();
        columnasRepuestos.add("Código");
        columnasRepuestos.add("Nombre");
        columnasRepuestos.add("Precio");

        modeloTablaRepuestos = new DefaultTableModel(columnasRepuestos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaRepuestos = new JTable(modeloTablaRepuestos);
        JScrollPane scrollRepuestos = new JScrollPane(tablaRepuestos);
        scrollRepuestos.setPreferredSize(new Dimension(300, 150));
        repuestosPanel.add(scrollRepuestos, BorderLayout.CENTER);

        // Agregar componentes al panel derecho
        rightPanel.add(detailsPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(descPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(repuestosPanel);

        // Agregar paneles al panel principal
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(rightPanel, gbc);

        return panel;
    }

    private void cargarDatos() {
        // Cargar automóviles del cliente
        Cliente cliente = (Cliente) usuario;
        automoviles = cliente.getAutomoviles();

        if (automoviles.isEmpty()) {
            showInfoMessage(
                    "No tiene automóviles registrados. Debe registrar al menos un automóvil para solicitar servicios.");
            return;
        }

        // Filtrar solo automóviles disponibles
        Vector<Automovil> automovilesDisponibles = new Vector<>();
        for (int i = 0; i < automoviles.size(); i++) {
            Automovil auto = automoviles.elementAt(i);
            if (auto.getEstadoActual().equals(Automovil.ESTADO_DISPONIBLE)) {
                automovilesDisponibles.add(auto);
            }
        }

        if (automovilesDisponibles.isEmpty()) {
            showInfoMessage(
                    "Todos sus automóviles están actualmente en servicio o en espera. Debe esperar a que alguno esté disponible.");
        }

        cboAutomovil.removeAllItems();
        for (int i = 0; i < automovilesDisponibles.size(); i++) {
            cboAutomovil.addItem(automovilesDisponibles.elementAt(i));
        }

        // También mostrar los no disponibles para que el cliente sepa
        for (int i = 0; i < automoviles.size(); i++) {
            Automovil auto = automoviles.elementAt(i);
            if (!auto.getEstadoActual().equals(Automovil.ESTADO_DISPONIBLE)) {
                cboAutomovil.addItem(auto);
            }
        }

        // Cargar servicios disponibles
        servicios = GestorDatos.getInstancia().getServicios();

        if (servicios.isEmpty()) {
            showErrorMessage("No hay servicios disponibles en el sistema.");
            return;
        }

        cboServicio.removeAllItems();
        for (int i = 0; i < servicios.size(); i++) {
            cboServicio.addItem(servicios.elementAt(i));
        }

        // Seleccionar primeros elementos
        if (cboAutomovil.getItemCount() > 0) {
            cboAutomovil.setSelectedIndex(0);
        }

        if (cboServicio.getItemCount() > 0) {
            cboServicio.setSelectedIndex(0);
        }
    }

    private void seleccionarAutomovil() {
        automovilSeleccionado = (Automovil) cboAutomovil.getSelectedItem();

        if (automovilSeleccionado != null) {
            // Mostrar imagen del automóvil
            mostrarImagenAutomovil();

            // Mostrar estado del automóvil
            String estado = "";
            boolean disponible = true;

            switch (automovilSeleccionado.getEstadoActual()) {
                case Automovil.ESTADO_DISPONIBLE:
                    estado = "Disponible";
                    lblEstadoAuto.setForeground(new Color(0, 128, 0)); // Verde
                    break;
                case Automovil.ESTADO_EN_ESPERA:
                    estado = "En Espera";
                    lblEstadoAuto.setForeground(new Color(255, 140, 0)); // Naranja
                    disponible = false;
                    break;
                case Automovil.ESTADO_EN_SERVICIO:
                    estado = "En Servicio";
                    lblEstadoAuto.setForeground(new Color(178, 34, 34)); // Rojo
                    disponible = false;
                    break;
                case Automovil.ESTADO_LISTO:
                    estado = "Listo para Retiro";
                    lblEstadoAuto.setForeground(new Color(0, 0, 128)); // Azul
                    disponible = false;
                    break;
            }

            lblEstadoAuto.setText("Estado: " + estado);

            // Verificar compatibilidad con el servicio seleccionado
            verificarCompatibilidad();
        }
    }

    private void seleccionarServicio() {
        servicioSeleccionado = (Servicio) cboServicio.getSelectedItem();

        if (servicioSeleccionado != null) {
            // Actualizar detalles del servicio
            lblPrecio.setText(String.format("Q%.2f", servicioSeleccionado.getPrecio()));
            lblTiempoEstimado.setText(servicioSeleccionado.getTiempoEstimado() + " horas");
            txtDetalleServicio.setText(servicioSeleccionado.getDescripcion());

            // Cargar repuestos si no es diagnóstico
            cargarRepuestosServicio();

            // Verificar compatibilidad con el automóvil seleccionado
            verificarCompatibilidad();
        }
    }

    private void cargarRepuestosServicio() {
        // Limpiar tabla
        modeloTablaRepuestos.setRowCount(0);

        if (servicioSeleccionado == null)
            return;

        // Si es diagnóstico, no mostrar repuestos
        if (servicioSeleccionado.getNombre().equalsIgnoreCase("Diagnóstico")) {
            modeloTablaRepuestos.addRow(new Object[] { "--", "Servicio de diagnóstico", "--" });
            return;
        }

        // Agregar repuestos a la tabla
        Vector<Repuesto> repuestos = servicioSeleccionado.getRepuestos();

        if (repuestos.isEmpty()) {
            modeloTablaRepuestos.addRow(new Object[] { "--", "No incluye repuestos", "--" });
        } else {
            for (int i = 0; i < repuestos.size(); i++) {
                Repuesto r = repuestos.elementAt(i);
                modeloTablaRepuestos.addRow(new Object[] {
                        r.getCodigo(),
                        r.getNombre(),
                        String.format("Q%.2f", r.getPrecio())
                });
            }
        }
    }

    private void mostrarImagenAutomovil() {
        if (automovilSeleccionado == null) {
            lblFotoAuto.setIcon(null);
            lblFotoAuto.setText("Seleccione un automóvil");
            return;
        }

        String rutaFoto = automovilSeleccionado.getRutaFoto();

        if (rutaFoto == null || rutaFoto.isEmpty()) {
            lblFotoAuto.setIcon(null);
            lblFotoAuto.setText(automovilSeleccionado.getMarca() + " " + automovilSeleccionado.getModelo());
            return;
        }

        try {
            BufferedImage img = ImageIO.read(new File(rutaFoto));
            if (img != null) {
                ImageIcon icon = new ImageIcon(img.getScaledInstance(180, 120, Image.SCALE_SMOOTH));
                lblFotoAuto.setIcon(icon);
                lblFotoAuto.setText("");
            } else {
                lblFotoAuto.setIcon(null);
                lblFotoAuto.setText(automovilSeleccionado.getMarca() + " " + automovilSeleccionado.getModelo());
            }
        } catch (IOException e) {
            lblFotoAuto.setIcon(null);
            lblFotoAuto.setText(automovilSeleccionado.getMarca() + " " + automovilSeleccionado.getModelo());
        }
    }

    private void verificarCompatibilidad() {
        if (automovilSeleccionado == null || servicioSeleccionado == null) {
            lblCompatibilidad.setText("");
            return;
        }

        // Primero verificar si el auto está disponible
        boolean disponible = automovilSeleccionado.getEstadoActual().equals(Automovil.ESTADO_DISPONIBLE);

        if (!disponible) {
            lblCompatibilidad.setText("✗ Automóvil no disponible");
            lblCompatibilidad.setForeground(Color.RED);
            return;
        }

        // Luego verificar compatibilidad
        boolean compatible = esCompatible();

        if (compatible) {
            lblCompatibilidad.setText("✓ Compatible");
            lblCompatibilidad.setForeground(new Color(0, 128, 0)); // Verde
        } else {
            lblCompatibilidad.setText("✗ No compatible");
            lblCompatibilidad.setForeground(Color.RED);
        }
    }

    private boolean esCompatible() {
        // Si el servicio es diagnóstico o válido para cualquier marca y modelo, es
        // compatible
        if (servicioSeleccionado.getNombre().equalsIgnoreCase("Diagnóstico")) {
            return true;
        }

        if (servicioSeleccionado.getMarca().equalsIgnoreCase("cualquiera") &&
                servicioSeleccionado.getModelo().equalsIgnoreCase("cualquiera")) {
            return true;
        }

        // Verificar compatibilidad de marca y modelo
        boolean marcaCompatible = servicioSeleccionado.getMarca().equalsIgnoreCase("cualquiera") ||
                servicioSeleccionado.getMarca().equalsIgnoreCase(automovilSeleccionado.getMarca());

        boolean modeloCompatible = servicioSeleccionado.getModelo().equalsIgnoreCase("cualquiera") ||
                servicioSeleccionado.getModelo().equalsIgnoreCase(automovilSeleccionado.getModelo());

        return marcaCompatible && modeloCompatible;
    }

    private void solicitarServicio() {
        if (automovilSeleccionado == null) {
            showErrorMessage("Debe seleccionar un automóvil");
            return;
        }

        if (servicioSeleccionado == null) {
            showErrorMessage("Debe seleccionar un servicio");
            return;
        }

        // Verificar que el automóvil esté disponible
        if (!automovilSeleccionado.getEstadoActual().equals(Automovil.ESTADO_DISPONIBLE)) {
            showErrorMessage("El automóvil seleccionado no está disponible. " +
                    "Su estado actual es: " + automovilSeleccionado.getEstadoActual());
            return;
        }

        // Verificar compatibilidad
        if (!esCompatible()) {
            showErrorMessage("El servicio seleccionado no es compatible con el automóvil seleccionado");
            return;
        }

        // Mostrar resumen de la solicitud
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Resumen de la solicitud:\n\n");
        mensaje.append("Automóvil: ").append(automovilSeleccionado.getMarca()).append(" ")
                .append(automovilSeleccionado.getModelo()).append(" (").append(automovilSeleccionado.getPlaca())
                .append(")\n\n");
        mensaje.append("Servicio: ").append(servicioSeleccionado.getNombre()).append("\n");
        mensaje.append("Costo del servicio: Q").append(String.format("%.2f", servicioSeleccionado.getPrecio()))
                .append("\n");
        mensaje.append("Tiempo estimado: ").append(servicioSeleccionado.getTiempoEstimado()).append(" horas\n\n");

        // Instrucciones adicionales según tipo de cliente
        Cliente cliente = (Cliente) usuario;
        if (cliente.getTipoCliente().equalsIgnoreCase("oro")) {
            mensaje.append("Como cliente ORO, su vehículo será atendido con prioridad.\n\n");
        } else {
            mensaje.append("Su vehículo será atendido según disponibilidad de mecánicos.\n\n");
        }

        mensaje.append("¿Confirma la solicitud de este servicio?");

        int option = JOptionPane.showConfirmDialog(
                null,
                mensaje.toString(),
                "Confirmar solicitud",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            GestorOrdenes gestorOrdenes = GestorOrdenes.getInstancia();

            // Cambiar estado del automóvil a "en espera"
            automovilSeleccionado.setEstadoActual(Automovil.ESTADO_EN_ESPERA);

            // Crear orden de trabajo
            OrdenTrabajo orden = new OrdenTrabajo();
            orden.setId("OT-" + System.currentTimeMillis()); 
            orden.setAutomovil(automovilSeleccionado);
            orden.setCliente(cliente);
            orden.setServicio(servicioSeleccionado);
            orden.setFecha(new Date());
            orden.setMecanico(null);
            orden.setEstado("espera");

            // Agregar al gestor de órdenes
            gestorOrdenes.agregarOrden(orden);

            // Guardar cambios
            Serializador.guardarDatos();
            Serializador.guardarOrdenes();

            // Mostrar mensaje de éxito
            showSuccessMessage("Se ha creado la orden de servicio exitosamente.\n" +
                    "Puede dar seguimiento al estado de su vehículo en la sección 'Progreso de Vehículos'.");

            // Recargar datos
            cargarDatos();
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
