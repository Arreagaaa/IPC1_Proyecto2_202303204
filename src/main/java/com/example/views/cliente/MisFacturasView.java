package com.example.views.cliente;

import com.example.models.Automovil;
import com.example.models.Factura;
import com.example.models.GestorFacturas;
import com.example.models.OrdenTrabajo;
import com.example.models.personas.Cliente;
import com.example.models.personas.Persona;
import com.example.utils.PdfGenerator;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Vista para que el cliente pueda ver y pagar sus facturas
 */
public class MisFacturasView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JTable tablaFacturas;
    private DefaultTableModel modeloTabla;
    private JTextArea areaDetalles;
    private JButton btnPagar;
    private JButton btnVerFacturaPDF;
    private JLabel totalLabel;

    private Vector<Factura> facturasMostradas;
    private Factura facturaSeleccionada;

    public MisFacturasView(Persona usuario) {
        super("Mis Facturas");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Mis Facturas");
        titlePanel.add(titleLabel);

        // Panel principal dividido
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);

        // Panel izquierdo: tabla de facturas
        JPanel leftPanel = createFacturasPanel();

        // Panel derecho: detalles de factura seleccionada
        JPanel rightPanel = createDetallesPanel();

        // Configurar splitPane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setOneTouchExpandable(true);

        // Agregar paneles al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarFacturas();
    }

    private JPanel createFacturasPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Configurar tabla
        Vector<String> columnas = new Vector<>();
        columnas.add("No. Factura");
        columnas.add("Fecha");
        columnas.add("Vehículo");
        columnas.add("Servicio");
        columnas.add("Total");
        columnas.add("Estado");

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        tablaFacturas = new JTable(modeloTabla);
        tablaFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaFacturas.getSelectedRow() != -1) {
                mostrarDetallesFactura();
            }
        });

        // Scroll para la tabla
        JScrollPane scrollPane = new JScrollPane(tablaFacturas);

        // Total a pagar
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setOpaque(false);
        totalLabel = new JLabel("Total pendiente: Q0.00");
        totalLabel.setFont(new Font(totalLabel.getFont().getName(), Font.BOLD, 14));
        totalPanel.add(totalLabel);

        // Panel de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setOpaque(false);

        JButton btnActualizar = createSecondaryButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarFacturas());

        buttonsPanel.add(btnActualizar);

        // Agregar componentes al panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(totalPanel, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetallesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Detalles de la Factura"));

        // Área para detalles
        areaDetalles = new JTextArea();
        areaDetalles.setEditable(false);
        areaDetalles.setLineWrap(true);
        areaDetalles.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(areaDetalles);
        scrollPane.setPreferredSize(new Dimension(300, 300));

        // Panel de botones
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Botón para pagar
        btnPagar = createPrimaryButton("Pagar Factura");
        btnPagar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPagar.addActionListener(e -> pagarFactura());
        btnPagar.setEnabled(false);

        // Botón para ver factura PDF
        btnVerFacturaPDF = createSecondaryButton("Ver Factura en PDF");
        btnVerFacturaPDF.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVerFacturaPDF.addActionListener(e -> verFacturaPDF());
        btnVerFacturaPDF.setEnabled(false);

        // Agregar botones al panel
        buttonsPanel.add(btnPagar);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(btnVerFacturaPDF);

        // Agregar componentes al panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarFacturas() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener facturas del cliente
        Cliente cliente = (Cliente) usuario;
        facturasMostradas = new Vector<>();

        Vector<Factura> todasFacturas = GestorFacturas.getInstancia().getFacturas();

        double totalPendiente = 0.0;

        // Filtrar facturas del cliente actual
        for (int i = 0; i < todasFacturas.size(); i++) {
            Factura factura = todasFacturas.elementAt(i);
            if (factura.getOrdenTrabajo().getCliente().getIdentificador().equals(cliente.getIdentificador())) {
                facturasMostradas.add(factura);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                OrdenTrabajo orden = factura.getOrdenTrabajo();

                // Formatear número de factura
                String numFactura = String.format("%06d", factura.getNumero());

                // Formatear vehículo
                String vehiculo = orden.getAutomovil().getMarca() + " " +
                        orden.getAutomovil().getModelo() + " (" +
                        orden.getAutomovil().getPlaca() + ")";

                // Formatear estado
                String estado = factura.isPagada() ? "Pagada" : "Pendiente";

                // Agregar fila a la tabla
                modeloTabla.addRow(new Object[] {
                        numFactura,
                        sdf.format(factura.getFecha()),
                        vehiculo,
                        orden.getServicio().getNombre(),
                        String.format("Q%.2f", factura.getTotal()),
                        estado
                });

                // Sumar al total pendiente si la factura no está pagada
                if (factura.isPendiente()) {
                    totalPendiente += factura.getTotal();
                }
            }
        }

        // Actualizar etiqueta de total
        totalLabel.setText("Total pendiente: " + String.format("Q%.2f", totalPendiente));

        // Seleccionar primera fila si existe
        if (tablaFacturas.getRowCount() > 0) {
            tablaFacturas.setRowSelectionInterval(0, 0);
            mostrarDetallesFactura();
        } else {
            limpiarDetalles();
        }
    }

    private void mostrarDetallesFactura() {
        int row = tablaFacturas.getSelectedRow();
        if (row == -1 || row >= facturasMostradas.size()) {
            limpiarDetalles();
            return;
        }

        facturaSeleccionada = facturasMostradas.elementAt(row);
        OrdenTrabajo orden = facturaSeleccionada.getOrdenTrabajo();

        // Formatear detalles
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();

        sb.append("FACTURA No. ").append(String.format("%06d", facturaSeleccionada.getNumero())).append("\n\n");
        sb.append("Fecha: ").append(sdf.format(facturaSeleccionada.getFecha())).append("\n");
        sb.append("Cliente: ").append(orden.getCliente().getNombreCompleto()).append("\n");
        sb.append("DPI: ").append(orden.getCliente().getIdentificador()).append("\n");
        sb.append("Tipo de cliente: ").append(orden.getCliente().getTipoCliente().toUpperCase()).append("\n\n");

        sb.append("VEHÍCULO:\n");
        sb.append("Placa: ").append(orden.getAutomovil().getPlaca()).append("\n");
        sb.append("Marca: ").append(orden.getAutomovil().getMarca()).append("\n");
        sb.append("Modelo: ").append(orden.getAutomovil().getModelo()).append("\n\n");

        sb.append("SERVICIO REALIZADO:\n");
        sb.append("Nombre: ").append(orden.getServicio().getNombre()).append("\n");
        sb.append("Precio mano de obra: ").append(String.format("Q%.2f", orden.getServicio().getPrecioManoObra()))
                .append("\n");

        // Listar repuestos si tiene
        if (!orden.getServicio().getRepuestos().isEmpty()) {
            sb.append("\nREPUESTOS UTILIZADOS:\n");
            double totalRepuestos = 0.0;

            for (int i = 0; i < orden.getServicio().getRepuestos().size(); i++) {
                var repuesto = orden.getServicio().getRepuestos().elementAt(i);
                sb.append("- ").append(repuesto.getNombre())
                        .append(": ").append(String.format("Q%.2f", repuesto.getPrecio())).append("\n");
                totalRepuestos += repuesto.getPrecio();
            }

            sb.append("\nTotal repuestos: ").append(String.format("Q%.2f", totalRepuestos)).append("\n");
        }

        sb.append("\nTOTAL A PAGAR: ").append(String.format("Q%.2f", facturaSeleccionada.getTotal())).append("\n");
        sb.append("ESTADO: ").append(facturaSeleccionada.isPagada() ? "PAGADA" : "PENDIENTE").append("\n");

        if (facturaSeleccionada.isPagada()) {
            sb.append("\nFecha de pago: ").append(sdf.format(facturaSeleccionada.getFechaPago()));
        }

        // Mostrar detalles en el área de texto
        areaDetalles.setText(sb.toString());

        // Habilitar o deshabilitar botones según el estado
        btnPagar.setEnabled(!facturaSeleccionada.isPagada());
        btnVerFacturaPDF.setEnabled(true);
    }

    private void limpiarDetalles() {
        areaDetalles.setText("Seleccione una factura para ver sus detalles");
        btnPagar.setEnabled(false);
        btnVerFacturaPDF.setEnabled(false);
        facturaSeleccionada = null;
    }

    private void pagarFactura() {
        if (facturaSeleccionada == null) {
            showErrorMessage("Debe seleccionar una factura");
            return;
        }

        if (facturaSeleccionada.isPagada()) {
            showInfoMessage("Esta factura ya está pagada");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro que desea pagar la factura No. " +
                        String.format("%06d", facturaSeleccionada.getNumero()) +
                        " por un total de " + String.format("Q%.2f", facturaSeleccionada.getTotal()) + "?",
                "Confirmar pago",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            // Marcar factura como pagada
            facturaSeleccionada.setEstado("pagada");
            facturaSeleccionada.setFechaPago(new Date());

            // Liberar el automóvil
            facturaSeleccionada.getOrdenTrabajo().getAutomovil().setEstadoActual(Automovil.ESTADO_DISPONIBLE);

            // Guardar cambios
            Serializador.guardarFacturas();
            Serializador.guardarDatos();

            // Mostrar mensaje y actualizar
            showSuccessMessage("Factura pagada exitosamente");
            cargarFacturas();

            // Generar PDF de la factura
            generarPDF();
        }
    }

    private void verFacturaPDF() {
        if (facturaSeleccionada == null) {
            showErrorMessage("Debe seleccionar una factura");
            return;
        }

        generarPDF();
    }

    private void generarPDF() {
        try {
            String numFactura = String.format("%06d", facturaSeleccionada.getNumero());
            // Generar ruta para guardar el PDF
            String rutaDestino = "facturas/factura_" + numFactura + ".pdf";

            // Asegurarse de que exista la carpeta facturas
            File directorioFacturas = new File("facturas");
            if (!directorioFacturas.exists()) {
                directorioFacturas.mkdirs();
            }

            // Llamar al método con ambos parámetros
            String filePath = PdfGenerator.generarFacturaPDF(facturaSeleccionada, rutaDestino);

            // Preguntar si desea abrir el PDF
            int option = JOptionPane.showConfirmDialog(
                    null,
                    "Se ha generado la factura en PDF.\n¿Desea abrirla ahora?",
                    "Factura generada",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                // Abrir PDF con aplicación predeterminada
                File file = new File(filePath);
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            showErrorMessage("Error al generar o abrir el PDF: " + e.getMessage());
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}