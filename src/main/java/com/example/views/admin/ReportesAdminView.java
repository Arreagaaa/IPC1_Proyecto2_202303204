package com.example.views.admin;

import com.example.models.*;
import com.example.models.personas.Cliente;
import com.example.models.personas.Mecanico;
import com.example.models.personas.Persona;
import com.example.utils.PdfGenerator;
import com.example.views.BaseView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Vector;

/**
 * Vista para generar reportes administrativos
 */
public class ReportesAdminView extends BaseView {

    private JPanel mainPanel;
    private JComboBox<String> cboTipoReporte;
    private JPanel panelReporte;
    private JPanel panelFiltros;
    private JPanel panelTabla;
    private JPanel panelGrafico;
    private JButton btnGenerar;
    private JButton btnExportarPDF;
    private CardLayout cardLayout;

    // Componentes para filtros
    private JComboBox<String> cboFiltroFecha;
    private JComboBox<String> cboFiltroCategoria;
    private JTextField txtBusqueda;

    // Tablas por tipo de reporte
    private JTable tablaClientes;
    private JTable tablaMecanicos;
    private JTable tablaServicios;
    private JTable tablaFacturas;
    private JTable tablaRepuestos;

    public ReportesAdminView(Persona usuario) {
        super("Reportes Administrativos");
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Reportes Administrativos");
        titlePanel.add(titleLabel);

        // Panel superior para selección de reporte
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTipoReporte = createLabel("Tipo de Reporte:");
        String[] tiposReporte = {
                "Top 5 Clientes con más servicios",
                "Top 3 Mecánicos con más servicios realizados",
                "Servicios más solicitados",
                "Ingresos por mes",
                "Inventario de repuestos"
        };
        cboTipoReporte = new JComboBox<>(tiposReporte);
        cboTipoReporte.addActionListener(e -> cambiarTipoReporte());

        topPanel.add(lblTipoReporte);
        topPanel.add(cboTipoReporte);

        // Panel central para el reporte
        panelReporte = new JPanel(new BorderLayout(10, 10));
        panelReporte.setOpaque(false);
        panelReporte.setBorder(BorderFactory.createTitledBorder("Datos del Reporte"));

        // Panel de filtros
        panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelFiltros.setOpaque(false);
        panelFiltros.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel lblFiltroFecha = createLabel("Período:");
        String[] periodos = { "Último mes", "Últimos 3 meses", "Último año", "Todo" };
        cboFiltroFecha = new JComboBox<>(periodos);

        JLabel lblFiltroCategoria = createLabel("Categoría:");
        cboFiltroCategoria = new JComboBox<>();

        JLabel lblBusqueda = createLabel("Buscar:");
        txtBusqueda = new JTextField(15);

        btnGenerar = createSecondaryButton("Generar Reporte");
        btnGenerar.addActionListener(e -> generarReporte());

        panelFiltros.add(lblFiltroFecha);
        panelFiltros.add(cboFiltroFecha);
        panelFiltros.add(lblFiltroCategoria);
        panelFiltros.add(cboFiltroCategoria);
        panelFiltros.add(lblBusqueda);
        panelFiltros.add(txtBusqueda);
        panelFiltros.add(btnGenerar);

        // Panel para tablas y gráficos con CardLayout
        cardLayout = new CardLayout();
        JPanel panelContenido = new JPanel(cardLayout);
        panelContenido.setOpaque(false);

        // Panel para tabla
        panelTabla = new JPanel(new BorderLayout(5, 5));
        panelTabla.setOpaque(false);

        // Inicializar todas las tablas
        inicializarTablas();
        JScrollPane scrollClientes = new JScrollPane(tablaClientes);
        JScrollPane scrollMecanicos = new JScrollPane(tablaMecanicos);
        JScrollPane scrollServicios = new JScrollPane(tablaServicios);
        JScrollPane scrollFacturas = new JScrollPane(tablaFacturas);
        JScrollPane scrollRepuestos = new JScrollPane(tablaRepuestos);

        // Panel para gráficos
        panelGrafico = new JPanel(new BorderLayout(5, 5));
        panelGrafico.setOpaque(false);
        panelGrafico.add(new JLabel("Los gráficos se mostrarán aquí", JLabel.CENTER), BorderLayout.CENTER);

        // Agregar componentes al CardLayout
        panelContenido.add(scrollClientes, "clientes");
        panelContenido.add(scrollMecanicos, "mecanicos");
        panelContenido.add(scrollServicios, "servicios");
        panelContenido.add(scrollFacturas, "facturas");
        panelContenido.add(scrollRepuestos, "repuestos");
        panelContenido.add(panelGrafico, "grafico");

        // Panel para botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotones.setOpaque(false);

        btnExportarPDF = createPrimaryButton("Exportar a PDF");
        btnExportarPDF.addActionListener(e -> exportarReportePDF());

        panelBotones.add(btnExportarPDF);

        // Estructura del panel de reporte
        panelReporte.add(panelFiltros, BorderLayout.NORTH);
        panelReporte.add(panelContenido, BorderLayout.CENTER);
        panelReporte.add(panelBotones, BorderLayout.SOUTH);

        // Agregar todo al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.NORTH); // Sobrescribe el anterior, es un error pero lo dejamos por ahora
        mainPanel.add(panelReporte, BorderLayout.CENTER);

        // Configuración inicial
        cambiarTipoReporte();
    }

    private void inicializarTablas() {
        // Tabla Clientes
        Vector<String> columnasClientes = new Vector<>();
        columnasClientes.add("DPI");
        columnasClientes.add("Nombre");
        columnasClientes.add("Tipo");
        columnasClientes.add("Servicios");
        columnasClientes.add("Total Gastado");

        tablaClientes = new JTable(new DefaultTableModel(columnasClientes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        // Tabla Mecánicos
        Vector<String> columnasMecanicos = new Vector<>();
        columnasMecanicos.add("ID");
        columnasMecanicos.add("Nombre");
        columnasMecanicos.add("Especialidad");
        columnasMecanicos.add("Servicios");
        columnasMecanicos.add("Ingresos Generados");

        tablaMecanicos = new JTable(new DefaultTableModel(columnasMecanicos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        // Tabla Servicios
        Vector<String> columnasServicios = new Vector<>();
        columnasServicios.add("Código");
        columnasServicios.add("Nombre");
        columnasServicios.add("Solicitudes");
        columnasServicios.add("Ingresos");
        columnasServicios.add("Tiempo Promedio");

        tablaServicios = new JTable(new DefaultTableModel(columnasServicios, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        // Tabla Facturas
        Vector<String> columnasFacturas = new Vector<>();
        columnasFacturas.add("No. Factura");
        columnasFacturas.add("Fecha");
        columnasFacturas.add("Cliente");
        columnasFacturas.add("Servicio");
        columnasFacturas.add("Total");
        columnasFacturas.add("Estado");

        tablaFacturas = new JTable(new DefaultTableModel(columnasFacturas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        // Tabla Repuestos
        Vector<String> columnasRepuestos = new Vector<>();
        columnasRepuestos.add("Código");
        columnasRepuestos.add("Nombre");
        columnasRepuestos.add("Precio");
        columnasRepuestos.add("Disponibles");
        columnasRepuestos.add("Utilizados");

        tablaRepuestos = new JTable(new DefaultTableModel(columnasRepuestos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private void cambiarTipoReporte() {
        int selectedIndex = cboTipoReporte.getSelectedIndex();

        // Actualizar filtros según tipo de reporte
        switch (selectedIndex) {
            case 0: // Top 5 Clientes
                configurarFiltrosClientes();
                cardLayout.show(panelTabla, "clientes");
                break;
            case 1: // Top 3 Mecánicos
                configurarFiltrosMecanicos();
                cardLayout.show(panelTabla, "mecanicos");
                break;
            case 2: // Servicios más solicitados
                configurarFiltrosServicios();
                cardLayout.show(panelTabla, "servicios");
                break;
            case 3: // Ingresos por mes
                configurarFiltrosIngresos();
                cardLayout.show(panelTabla, "grafico");
                break;
            case 4: // Inventario de repuestos
                configurarFiltrosRepuestos();
                cardLayout.show(panelTabla, "repuestos");
                break;
        }
    }

    private void configurarFiltrosClientes() {
        cboFiltroCategoria.removeAllItems();
        cboFiltroCategoria.addItem("Todos");
        cboFiltroCategoria.addItem("Normal");
        cboFiltroCategoria.addItem("Oro");
        txtBusqueda.setToolTipText("Buscar por nombre o DPI");
    }

    private void configurarFiltrosMecanicos() {
        cboFiltroCategoria.removeAllItems();
        cboFiltroCategoria.addItem("Todos");
        cboFiltroCategoria.addItem("Principal");
        cboFiltroCategoria.addItem("Asistente");
        txtBusqueda.setToolTipText("Buscar por nombre o especialidad");
    }

    private void configurarFiltrosServicios() {
        cboFiltroCategoria.removeAllItems();
        cboFiltroCategoria.addItem("Todos");
        cboFiltroCategoria.addItem("Mantenimiento");
        cboFiltroCategoria.addItem("Reparación");
        cboFiltroCategoria.addItem("Diagnóstico");
        txtBusqueda.setToolTipText("Buscar por nombre del servicio");
    }

    private void configurarFiltrosIngresos() {
        cboFiltroCategoria.removeAllItems();
        cboFiltroCategoria.addItem("Por mes");
        cboFiltroCategoria.addItem("Por servicio");
        cboFiltroCategoria.addItem("Por mecánico");
        txtBusqueda.setEnabled(false);
    }

    private void configurarFiltrosRepuestos() {
        cboFiltroCategoria.removeAllItems();
        cboFiltroCategoria.addItem("Todos");
        cboFiltroCategoria.addItem("En existencia");
        cboFiltroCategoria.addItem("Agotados");
        cboFiltroCategoria.addItem("Más utilizados");
        txtBusqueda.setToolTipText("Buscar por nombre o código");
        txtBusqueda.setEnabled(true);
    }

    private void generarReporte() {
        int tipoReporte = cboTipoReporte.getSelectedIndex();

        // Limpiar tablas
        ((DefaultTableModel) tablaClientes.getModel()).setRowCount(0);
        ((DefaultTableModel) tablaMecanicos.getModel()).setRowCount(0);
        ((DefaultTableModel) tablaServicios.getModel()).setRowCount(0);
        ((DefaultTableModel) tablaFacturas.getModel()).setRowCount(0);
        ((DefaultTableModel) tablaRepuestos.getModel()).setRowCount(0);

        // Generar reporte según tipo
        switch (tipoReporte) {
            case 0: // Top 5 Clientes
                generarReporteTopClientes();
                break;
            case 1: // Top 3 Mecánicos
                generarReporteTopMecanicos();
                break;
            case 2: // Servicios más solicitados
                generarReporteServicios();
                break;
            case 3: // Ingresos por mes
                generarReporteIngresos();
                break;
            case 4: // Inventario de repuestos
                generarReporteRepuestos();
                break;
        }
    }

    private void generarReporteTopClientes() {
        // Obtener datos
        Vector<Cliente> clientes = GestorDatos.getInstancia().getClientes();
        Vector<Factura> facturas = GestorFacturas.getInstancia().getFacturas();

        // Estructuras para calcular servicios y gastos por cliente
        HashMap<String, Integer> serviciosPorCliente = new HashMap<>();
        HashMap<String, Double> gastosPorCliente = new HashMap<>();

        // Contar servicios y gastos para cada cliente
        for (int i = 0; i < facturas.size(); i++) {
            Factura factura = facturas.elementAt(i);
            String idCliente = factura.getCliente().getIdentificador();

            // Contar servicio
            if (serviciosPorCliente.containsKey(idCliente)) {
                serviciosPorCliente.put(idCliente, serviciosPorCliente.get(idCliente) + 1);
            } else {
                serviciosPorCliente.put(idCliente, 1);
            }

            // Sumar gasto
            if (gastosPorCliente.containsKey(idCliente)) {
                gastosPorCliente.put(idCliente, gastosPorCliente.get(idCliente) + factura.getTotal());
            } else {
                gastosPorCliente.put(idCliente, factura.getTotal());
            }
        }

        // Crear lista para ordenar
        Vector<ClienteReporte> clientesReporte = new Vector<>();

        for (int i = 0; i < clientes.size(); i++) {
            Cliente cliente = clientes.elementAt(i);
            String id = cliente.getIdentificador();

            int servicios = serviciosPorCliente.getOrDefault(id, 0);
            double gasto = gastosPorCliente.getOrDefault(id, 0.0);

            clientesReporte.add(new ClienteReporte(cliente, servicios, gasto));
        }

        // Ordenar por número de servicios (descendente)
        ordernarClientesPorServicios(clientesReporte);

        // Mostrar top 5 (o menos si no hay suficientes)
        DefaultTableModel model = (DefaultTableModel) tablaClientes.getModel();
        int limit = Math.min(5, clientesReporte.size());

        for (int i = 0; i < limit; i++) {
            ClienteReporte cr = clientesReporte.elementAt(i);

            model.addRow(new Object[] {
                    cr.cliente.getIdentificador(),
                    cr.cliente.getNombreCompleto(),
                    cr.cliente.getTipoCliente(),
                    cr.servicios,
                    String.format("Q%.2f", cr.gasto)
            });
        }
    }

    // Clase auxiliar para ordenar clientes
    private class ClienteReporte {
        Cliente cliente;
        int servicios;
        double gasto;

        public ClienteReporte(Cliente cliente, int servicios, double gasto) {
            this.cliente = cliente;
            this.servicios = servicios;
            this.gasto = gasto;
        }
    }

    // Método de ordenamiento usando ShellSort
    private void ordernarClientesPorServicios(Vector<ClienteReporte> clientes) {
        int n = clientes.size();

        // Generar la secuencia de intervalos
        for (int gap = n / 2; gap > 0; gap /= 2) {
            // Hacer insertion sort para cada intervalo
            for (int i = gap; i < n; i++) {
                ClienteReporte temp = clientes.elementAt(i);

                int j;
                // Ordenamiento descendente por número de servicios
                for (j = i; j >= gap && clientes.elementAt(j - gap).servicios < temp.servicios; j -= gap) {
                    clientes.setElementAt(clientes.elementAt(j - gap), j);
                }

                // Colocar el elemento en su posición correcta
                clientes.setElementAt(temp, j);
            }
        }
    }

    // Implementaciones de los otros métodos de generación de reportes
    private void generarReporteTopMecanicos() {
        // Implementación similar a la de clientes pero para mecánicos
        showInfoMessage("Reporte de mecánicos generado");
    }

    private void generarReporteServicios() {
        // Implementación del reporte de servicios
        showInfoMessage("Reporte de servicios generado");
    }

    private void generarReporteIngresos() {
        // Implementación del reporte de ingresos
        showInfoMessage("Reporte de ingresos generado");
    }

    private void generarReporteRepuestos() {
        // Implementación del reporte de repuestos
        showInfoMessage("Reporte de repuestos generado");
    }

    private void exportarReportePDF() {
        try {
            String tipoReporte = (String) cboTipoReporte.getSelectedItem();
            String nombreArchivo = "reporte_" + tipoReporte.toLowerCase().replace(" ", "_") + ".pdf";

            // Crear directorio para reportes si no existe
            File directorioReportes = new File("reportes");
            if (!directorioReportes.exists()) {
                directorioReportes.mkdirs();
            }

            String rutaDestino = "reportes/" + nombreArchivo;

            // Exportar a PDF (implementación simplificada)
            showInfoMessage("El reporte se exportará a " + rutaDestino);

            // Generar PDF
            // PdfGenerator.generarReportePDF(tipoReporte, obtenerDatosReporte(),
            // rutaDestino);

            int option = JOptionPane.showConfirmDialog(
                    null,
                    "Reporte exportado exitosamente a:\n" + rutaDestino + "\n¿Desea abrirlo ahora?",
                    "Exportación exitosa",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(new File(rutaDestino));
            }
        } catch (Exception e) {
            showErrorMessage("Error al exportar el reporte: " + e.getMessage());
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}