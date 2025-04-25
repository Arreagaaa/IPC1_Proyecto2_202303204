package com.example.views.cliente;

import com.example.models.Factura;
import com.example.models.GestorFacturas;
import com.example.models.OrdenTrabajo;
import com.example.models.personas.Cliente;
import com.example.models.personas.Persona;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Vista para que el cliente pueda ver y pagar sus facturas
 */
public class FacturasClienteView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JTable tablaFacturas;
    private DefaultTableModel modeloTabla;
    private JTextArea detallesFactura;
    private JLabel totalLabel;
    private JButton btnPagar;
    private Factura facturaSeleccionada;

    public FacturasClienteView(Persona usuario) {
        super("");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(0, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Mis Facturas");
        titlePanel.add(titleLabel);

        // Panel principal dividido en dos secciones
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Panel de tabla
        JPanel tablePanel = createTablePanel();

        // Panel de detalles
        JPanel detailsPanel = createDetailsPanel();

        // Agregar paneles al panel de contenido
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        contentPanel.add(detailsPanel, BorderLayout.EAST);

        // Agregar paneles al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarFacturas();
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Lista de Facturas"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Modelo de tabla
        String[] columnas = { "Nº Factura", "Fecha", "Servicio", "Total", "Estado" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaFacturas = new JTable(modeloTabla);
        tablaFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Personalizar el renderizado de la columna de estado
        tablaFacturas.getColumnModel().getColumn(4)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    JLabel label = new JLabel(value.toString());
                    label.setOpaque(true);
                    label.setHorizontalAlignment(SwingConstants.CENTER);

                    if (isSelected) {
                        label.setBackground(table.getSelectionBackground());
                        label.setForeground(table.getSelectionForeground());
                    } else {
                        label.setForeground(Color.WHITE);
                        if (value.toString().equals("Pagada")) {
                            label.setBackground(new Color(46, 139, 87)); // Verde
                        } else {
                            label.setBackground(new Color(178, 34, 34)); // Rojo
                        }
                    }

                    return label;
                });

        JScrollPane scrollPane = new JScrollPane(tablaFacturas);

        // Listener para detectar selección
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaFacturas.getSelectedRow() != -1) {
                seleccionarFactura(tablaFacturas.getSelectedRow());
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de filtros
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtersPanel.setOpaque(false);

        JLabel lblFiltro = createLabel("Filtrar:");
        JComboBox<String> comboFiltro = new JComboBox<>(new String[] { "Todas", "Pendientes", "Pagadas" });

        comboFiltro.addActionListener(e -> {
            String filtro = (String) comboFiltro.getSelectedItem();
            aplicarFiltro(filtro);
        });

        JButton btnRefrescar = new JButton("Actualizar");
        btnRefrescar.addActionListener(e -> cargarFacturas());

        filtersPanel.add(lblFiltro);
        filtersPanel.add(comboFiltro);
        filtersPanel.add(Box.createHorizontalStrut(20));
        filtersPanel.add(btnRefrescar);

        panel.add(filtersPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Detalles de Factura"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setPreferredSize(new Dimension(300, 0));

        // Área de texto para detalles
        JLabel lblDetalles = createLabel("Información de factura:");
        lblDetalles.setAlignmentX(Component.LEFT_ALIGNMENT);

        detallesFactura = new JTextArea(15, 20);
        detallesFactura.setEditable(false);
        detallesFactura.setLineWrap(true);
        detallesFactura.setWrapStyleWord(true);
        detallesFactura.setFont(REGULAR_FONT);

        JScrollPane scrollPane = new JScrollPane(detallesFactura);
        scrollPane.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel para el total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setOpaque(false);
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTotal = createLabel("Total a pagar:");
        totalLabel = createLabel("Q 0.00");
        totalLabel.setFont(new Font(totalLabel.getFont().getName(), Font.BOLD, 16));

        totalPanel.add(lblTotal);
        totalPanel.add(totalLabel);

        // Panel para el botón de pago
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paymentPanel.setOpaque(false);
        paymentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnPagar = createPrimaryButton("Realizar Pago");
        btnPagar.setEnabled(false);
        btnPagar.addActionListener(e -> realizarPago());

        paymentPanel.add(btnPagar);

        // Agregar componentes al panel principal
        panel.add(lblDetalles);
        panel.add(Box.createVerticalStrut(5));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));
        panel.add(totalPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(paymentPanel);

        return panel;
    }

    private void cargarFacturas() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener facturas del cliente
        Cliente cliente = (Cliente) usuario;
        Vector<Factura> facturas = GestorFacturas.getInstancia().getFacturas();

        for (Factura factura : facturas) {
            if (factura.getCliente().getIdentificador().equals(cliente.getIdentificador())) {
                // Formato de fecha
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String fechaStr = sdf.format(factura.getFecha());

                // Convertir moneda a formato
                String totalStr = String.format("Q %.2f", factura.getTotal());

                Object[] fila = {
                        factura.getNumeroFactura(),
                        fechaStr,
                        factura.getOrdenTrabajo().getServicio().getNombre(),
                        totalStr,
                        factura.getEstado().equals("pagada") ? "Pagada" : "Pendiente"
                };
                modeloTabla.addRow(fila);
            }
        }

        // Si no hay filas seleccionadas y hay datos, seleccionar la primera
        if (tablaFacturas.getSelectedRow() == -1 && tablaFacturas.getRowCount() > 0) {
            tablaFacturas.setRowSelectionInterval(0, 0);
            seleccionarFactura(0);
        } else if (tablaFacturas.getRowCount() == 0) {
            // Si no hay datos, limpiar detalles
            limpiarDetalles();
        }
    }

    private void seleccionarFactura(int rowIndex) {
        // Obtener número de factura
        int numeroFactura = (int) tablaFacturas.getValueAt(rowIndex, 0);

        // Buscar la factura en el gestor
        facturaSeleccionada = null;
        for (Factura factura : GestorFacturas.getInstancia().getFacturas()) {
            if (factura.getNumeroFactura() == numeroFactura) {
                facturaSeleccionada = factura;
                break;
            }
        }

        if (facturaSeleccionada == null) {
            limpiarDetalles();
            return;
        }

        // Actualizar detalles
        StringBuilder detalles = new StringBuilder();

        // Información general
        detalles.append("FACTURA Nº: ").append(facturaSeleccionada.getNumeroFactura()).append("\n\n");

        // Formato de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        detalles.append("Fecha de emisión: ").append(sdf.format(facturaSeleccionada.getFecha())).append("\n\n");

        // Cliente
        detalles.append("Cliente: ").append(facturaSeleccionada.getCliente().getNombreCompleto()).append("\n");
        detalles.append("DPI: ").append(facturaSeleccionada.getCliente().getIdentificador()).append("\n\n");

        // Orden de trabajo
        OrdenTrabajo orden = facturaSeleccionada.getOrdenTrabajo();
        detalles.append("Orden de trabajo: ").append(orden.getNumeroOrden()).append("\n");
        detalles.append("Servicio: ").append(orden.getServicio().getNombre()).append("\n");
        detalles.append("Automóvil: ").append(orden.getAutomovil().getMarca()).append(" ");
        detalles.append(orden.getAutomovil().getModelo()).append(" (").append(orden.getAutomovil().getPlaca())
                .append(")\n");
        detalles.append("Mecánico: ").append(orden.getMecanico().getNombreCompleto()).append("\n\n");

        // Detalles económicos
        double precio = orden.getServicio().getPrecio();
        Cliente cliente = (Cliente) usuario;

        detalles.append("Detalle de cobro:\n");
        detalles.append("Precio del servicio: Q").append(String.format("%.2f", precio)).append("\n");

        if (cliente.getTipoCliente().equals("oro")) {
            detalles.append("Descuento cliente oro: 10%\n");
            double descuento = precio * 0.1;
            detalles.append("Monto descuento: Q").append(String.format("%.2f", descuento)).append("\n");
            precio = precio * 0.9;
        }

        detalles.append("\nSubtotal: Q").append(String.format("%.2f", precio)).append("\n");
        double iva = precio * 0.12;
        detalles.append("IVA (12%): Q").append(String.format("%.2f", iva)).append("\n");
        double total = precio + iva;
        detalles.append("Total: Q").append(String.format("%.2f", total)).append("\n\n");

        // Estado
        detalles.append("Estado: ").append(facturaSeleccionada.getEstado().equals("pagada") ? "PAGADA" : "PENDIENTE")
                .append("\n");

        if (facturaSeleccionada.getEstado().equals("pagada") && facturaSeleccionada.getFechaPago() != null) {
            detalles.append("Fecha de pago: ").append(sdf.format(facturaSeleccionada.getFechaPago())).append("\n");
        }

        detallesFactura.setText(detalles.toString());

        // Actualizar total a pagar
        totalLabel.setText(String.format("Q %.2f", facturaSeleccionada.getTotal()));

        // Habilitar o deshabilitar botón de pago según el estado
        btnPagar.setEnabled(!facturaSeleccionada.getEstado().equals("pagada"));
    }

    private void limpiarDetalles() {
        detallesFactura.setText("Seleccione una factura para ver los detalles.");
        totalLabel.setText("Q 0.00");
        btnPagar.setEnabled(false);
        facturaSeleccionada = null;
    }

    private void aplicarFiltro(String filtro) {
        if (filtro.equals("Todas")) {
            cargarFacturas();
            return;
        }

        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener facturas del cliente que coincidan con el filtro
        Cliente cliente = (Cliente) usuario;
        Vector<Factura> facturas = GestorFacturas.getInstancia().getFacturas();

        String estadoFiltro = filtro.equals("Pagadas") ? "pagada" : "pendiente";

        for (Factura factura : facturas) {
            if (factura.getCliente().getIdentificador().equals(cliente.getIdentificador()) &&
                    factura.getEstado().equals(estadoFiltro)) {

                // Formato de fecha
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String fechaStr = sdf.format(factura.getFecha());

                // Convertir moneda a formato
                String totalStr = String.format("Q %.2f", factura.getTotal());

                Object[] fila = {
                        factura.getNumeroFactura(),
                        fechaStr,
                        factura.getOrdenTrabajo().getServicio().getNombre(),
                        totalStr,
                        factura.getEstado().equals("pagada") ? "Pagada" : "Pendiente"
                };
                modeloTabla.addRow(fila);
            }
        }

        // Limpiar detalles si no se seleccionó ninguna fila
        if (tablaFacturas.getRowCount() > 0) {
            tablaFacturas.setRowSelectionInterval(0, 0);
            seleccionarFactura(0);
        } else {
            limpiarDetalles();
        }
    }

    private void realizarPago() {
        if (facturaSeleccionada == null) {
            showErrorMessage("Debe seleccionar una factura para pagar");
            return;
        }

        if (facturaSeleccionada.getEstado().equals("pagada")) {
            showErrorMessage("Esta factura ya está pagada");
            return;
        }

        // Confirmar el pago
        if (!showConfirmDialog("¿Está seguro que desea pagar esta factura por " +
                String.format("Q %.2f", facturaSeleccionada.getTotal()) + "?")) {
            return;
        }

        // Procesar el pago
        facturaSeleccionada.setEstado("pagada");
        facturaSeleccionada.setFechaPago(new Date());

        // Actualizar orden relacionada
        facturaSeleccionada.getOrdenTrabajo().setEstado("listo");

        // Guardar cambios
        Serializador.guardarDatos();

        // Mostrar mensaje de éxito
        showSuccessMessage("Pago realizado exitosamente");

        // Actualizar vista
        cargarFacturas();
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
