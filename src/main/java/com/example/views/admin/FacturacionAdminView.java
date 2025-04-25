package com.example.views.admin;

import com.example.models.Factura;
import com.example.models.GestorFacturas;
import com.example.models.personas.Cliente;
import com.example.models.personas.Persona;
import com.example.utils.PdfGenerator;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Vista para la gestión de facturación para el administrador
 */
public class FacturacionAdminView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JTable tablaFacturas;
    private DefaultTableModel modeloTabla;
    private JTextArea areaDetalles;
    private JLabel totalVentasLabel;
    private JLabel totalPendienteLabel;
    private JLabel totalCobradoLabel;

    private Factura facturaSeleccionada;

    public FacturacionAdminView(Persona usuario) {
        super("Facturación");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Gestión de Facturación");
        titlePanel.add(titleLabel);

        // Panel de filtros y estadísticas
        JPanel topPanel = createTopPanel();

        // Panel principal dividido horizontalmente
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);

        // Panel izquierdo - Tabla de facturas
        JPanel leftPanel = createInvoicesListPanel();

        // Panel derecho - Detalles de factura
        JPanel rightPanel = createInvoiceDetailPanel();

        // Configurar y agregar paneles al SplitPane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(550);
        splitPane.setOneTouchExpandable(true);

        // Agregar componentes al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarFacturas();
        actualizarEstadisticas();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Panel de filtros
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtersPanel.setOpaque(false);

        JLabel lblFiltro = createLabel("Filtrar por estado:");
        String[] estados = { "Todas", "Pendientes", "Pagadas" };
        JComboBox<String> comboFiltro = new JComboBox<>(estados);

        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(e -> {
            cargarFacturas();
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
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createTitledBorder("Estadísticas"));

        totalVentasLabel = new JLabel("Total: Q0.00");
        totalPendienteLabel = new JLabel("Pendiente: Q0.00");
        totalCobradoLabel = new JLabel("Cobrado: Q0.00");

        statsPanel.add(totalVentasLabel);
        statsPanel.add(totalPendienteLabel);
        statsPanel.add(totalCobradoLabel);

        // Panel principal
        JPanel mainTopPanel = new JPanel(new BorderLayout());
        mainTopPanel.setOpaque(false);
        mainTopPanel.add(filtersPanel, BorderLayout.NORTH);
        mainTopPanel.add(statsPanel, BorderLayout.CENTER);

        panel.add(mainTopPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInvoicesListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Tabla de facturas
        String[] columnas = { "Nº Factura", "Cliente", "Total", "Estado", "Fecha" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaFacturas = new JTable(modeloTabla);
        tablaFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Permitir ordenar la tabla
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaFacturas.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tablaFacturas);

        // Detectar selección
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaFacturas.getSelectedRow() != -1) {
                mostrarFacturaSeleccionada();
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInvoiceDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Detalles de Factura"));

        // Área de texto para mostrar detalles
        areaDetalles = new JTextArea(20, 30);
        areaDetalles.setEditable(false);
        areaDetalles.setLineWrap(true);
        areaDetalles.setWrapStyleWord(true);
        areaDetalles.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(areaDetalles);

        // Panel de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setOpaque(false);

        JButton btnMarcarPagada = createPrimaryButton("Marcar como Pagada");
        JButton btnExportar = createPrimaryButton("Exportar a PDF");

        btnMarcarPagada.addActionListener(e -> marcarComoPagada());
        btnExportar.addActionListener(e -> exportarFactura());

        buttonsPanel.add(btnMarcarPagada);
        buttonsPanel.add(btnExportar);

        // Estructura del panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarFacturas() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener todas las facturas
        Vector<Factura> facturas = GestorFacturas.getInstancia().getFacturas();

        for (Factura factura : facturas) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String fechaFormateada = sdf.format(factura.getFecha());

            Object[] fila = {
                    factura.getNumeroFactura(),
                    factura.getCliente().getNombreCompleto(),
                    String.format("Q%.2f", factura.getTotal()),
                    factura.getEstado().equals("pagada") ? "PAGADA" : "PENDIENTE",
                    fechaFormateada
            };
            modeloTabla.addRow(fila);
        }

        // Si hay filas, seleccionar la primera
        if (tablaFacturas.getRowCount() > 0) {
            tablaFacturas.setRowSelectionInterval(0, 0);
            mostrarFacturaSeleccionada();
        } else {
            areaDetalles.setText("No hay facturas disponibles");
            facturaSeleccionada = null;
        }
    }

    private void mostrarFacturaSeleccionada() {
        int rowIndex = tablaFacturas.getSelectedRow();
        if (rowIndex == -1)
            return;

        // Convertir índice si la tabla está filtrada/ordenada
        int modelRow = tablaFacturas.convertRowIndexToModel(rowIndex);
        int numeroFactura = (int) modeloTabla.getValueAt(modelRow, 0);

        facturaSeleccionada = null;
        for (Factura factura : GestorFacturas.getInstancia().getFacturas()) {
            if (factura.getNumeroFactura() == numeroFactura) {
                facturaSeleccionada = factura;
                break;
            }
        }

        if (facturaSeleccionada != null) {
            // Generar texto de detalles
            StringBuilder sb = new StringBuilder();
            sb.append("FACTURA #").append(facturaSeleccionada.getNumeroFactura()).append("\n");
            sb.append("======================================\n\n");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sb.append("Fecha de emisión: ").append(sdf.format(facturaSeleccionada.getFecha())).append("\n");

            if (facturaSeleccionada.getEstado().equals("pagada") && facturaSeleccionada.getFechaPago() != null) {
                sb.append("Fecha de pago: ").append(sdf.format(facturaSeleccionada.getFechaPago())).append("\n");
            }
            sb.append("\n");

            sb.append("CLIENTE:\n");
            sb.append("Nombre: ").append(facturaSeleccionada.getCliente().getNombreCompleto()).append("\n");
            sb.append("DPI: ").append(facturaSeleccionada.getCliente().getIdentificador()).append("\n");
            sb.append("Tipo: ").append(facturaSeleccionada.getCliente().getTipoCliente().toUpperCase()).append("\n\n");

            sb.append("DETALLES DE SERVICIO:\n");
            sb.append("Orden #: ").append(facturaSeleccionada.getOrdenTrabajo().getNumeroOrden()).append("\n");
            sb.append("Vehículo: ")
                    .append(facturaSeleccionada.getOrdenTrabajo().getAutomovil().getMarca())
                    .append(" ")
                    .append(facturaSeleccionada.getOrdenTrabajo().getAutomovil().getModelo())
                    .append(" (")
                    .append(facturaSeleccionada.getOrdenTrabajo().getAutomovil().getPlaca())
                    .append(")\n");
            sb.append("Servicio: ").append(facturaSeleccionada.getOrdenTrabajo().getServicio().getNombre())
                    .append("\n");

            if (facturaSeleccionada.getOrdenTrabajo().getMecanico() != null) {
                sb.append("Mecánico: ").append(facturaSeleccionada.getOrdenTrabajo().getMecanico().getNombreCompleto())
                        .append("\n");
            }
            sb.append("\n");

            sb.append("TOTALES:\n");
            sb.append("--------------------------------------\n");
            sb.append(String.format("%-20s Q%10.2f\n", "Subtotal:", facturaSeleccionada.getSubtotal()));
            sb.append(String.format("%-20s Q%10.2f\n", "IVA (12%):", facturaSeleccionada.getIva()));
            sb.append("--------------------------------------\n");
            sb.append(String.format("%-20s Q%10.2f\n", "TOTAL:", facturaSeleccionada.getTotal()));
            sb.append("\n");

            sb.append("ESTADO: ").append(facturaSeleccionada.getEstado().toUpperCase()).append("\n");

            areaDetalles.setText(sb.toString());
        }
    }

    private void aplicarFiltro(String filtro) {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener facturas según filtro
        Vector<Factura> facturas;

        if (filtro.equals("Pendientes")) {
            facturas = GestorFacturas.getInstancia().getFacturasPendientes();
        } else if (filtro.equals("Pagadas")) {
            facturas = GestorFacturas.getInstancia().getFacturasPagadas();
        } else {
            facturas = GestorFacturas.getInstancia().getFacturas();
        }

        // Cargar facturas filtradas
        for (Factura factura : facturas) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String fechaFormateada = sdf.format(factura.getFecha());

            Object[] fila = {
                    factura.getNumeroFactura(),
                    factura.getCliente().getNombreCompleto(),
                    String.format("Q%.2f", factura.getTotal()),
                    factura.getEstado().equals("pagada") ? "PAGADA" : "PENDIENTE",
                    fechaFormateada
            };
            modeloTabla.addRow(fila);
        }

        // Si hay filas, seleccionar la primera
        if (tablaFacturas.getRowCount() > 0) {
            tablaFacturas.setRowSelectionInterval(0, 0);
            mostrarFacturaSeleccionada();
        } else {
            areaDetalles.setText("No hay facturas que coincidan con el filtro");
            facturaSeleccionada = null;
        }
    }

    private void actualizarEstadisticas() {
        // Calcular totales
        double totalVentas = 0;
        double totalPendiente = 0;
        double totalCobrado = 0;

        for (Factura factura : GestorFacturas.getInstancia().getFacturas()) {
            totalVentas += factura.getTotal();

            if (factura.getEstado().equals("pagada")) {
                totalCobrado += factura.getTotal();
            } else {
                totalPendiente += factura.getTotal();
            }
        }

        // Actualizar etiquetas
        totalVentasLabel.setText(String.format("Total: Q%.2f", totalVentas));
        totalPendienteLabel.setText(String.format("Pendiente: Q%.2f", totalPendiente));
        totalCobradoLabel.setText(String.format("Cobrado: Q%.2f", totalCobrado));
    }

    private void marcarComoPagada() {
        if (facturaSeleccionada == null) {
            showErrorMessage("Debe seleccionar una factura primero");
            return;
        }

        if (facturaSeleccionada.getEstado().equals("pagada")) {
            showInfoMessage("Esta factura ya está marcada como pagada");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de marcar la factura #" + facturaSeleccionada.getNumeroFactura() + " como pagada?",
                "Confirmar pago",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            // Marcar como pagada
            GestorFacturas.getInstancia().pagarFactura(facturaSeleccionada);

            // Guardar cambios
            Serializador.guardarFacturas();

            // Recargar datos
            cargarFacturas();
            actualizarEstadisticas();

            showSuccessMessage("Factura marcada como pagada correctamente");
        }
    }

    private void exportarFactura() {
        if (facturaSeleccionada == null) {
            showErrorMessage("Debe seleccionar una factura para exportar");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar factura como PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));
        fileChooser.setSelectedFile(new File("Factura_" + facturaSeleccionada.getNumeroFactura() + ".pdf"));

        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Asegurar que tenga extensión .pdf
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".pdf")) {
                path += ".pdf";
                file = new File(path);
            }

            try {
                // Generar PDF
                PdfGenerator.generarFacturaPDF(facturaSeleccionada, file.getAbsolutePath());

                showSuccessMessage("Factura exportada exitosamente a: " + file.getAbsolutePath());

                // Preguntar si desea abrir el archivo
                int openOption = JOptionPane.showConfirmDialog(
                        null,
                        "¿Desea abrir el archivo generado?",
                        "Abrir archivo",
                        JOptionPane.YES_NO_OPTION);

                if (openOption == JOptionPane.YES_OPTION) {
                    // Abrir el archivo con el programa predeterminado
                    Desktop.getDesktop().open(file);
                }

            } catch (Exception e) {
                showErrorMessage("Error al exportar la factura: " + e.getMessage());
            }
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}