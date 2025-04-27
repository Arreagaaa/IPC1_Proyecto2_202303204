package com.tallermecanico.views;

import com.tallermecanico.controllers.*;
import com.tallermecanico.models.*;
import com.tallermecanico.models.personas.*;
import com.tallermecanico.utils.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

/**
 * Vista de administración del sistema
 */
public class AdminView extends BaseView {
    // Empleado administrador logueado
    private Empleado administrador;

    // Panel principal con pestañas
    private JTabbedPane tabbedPane;

    public AdminView(Empleado administrador) {
        super("Panel de Administración");
        this.administrador = administrador;
        inicializarComponentes();
        cargarDatos();
    }

    @Override
    protected void inicializarComponentes() {
        // Barra de navegación
        JPanel navBar = crearBarraNavegacion(
                administrador.getNombreCompleto(),
                "Administrador");
        contentPanel.add(navBar, BorderLayout.NORTH);

        // Panel principal con pestañas
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_NORMAL);
        tabbedPane.setBackground(COLOR_PRIMARY);
        tabbedPane.setForeground(COLOR_LIGHT);

        // Inicializar cada panel y añadirlo como pestaña
        tabbedPane.addTab("Repuestos", inicializarPanelRepuestos());
        tabbedPane.addTab("Servicios", inicializarPanelServicios());
        tabbedPane.addTab("Clientes", inicializarPanelClientes());
        tabbedPane.addTab("Progreso de Automóviles", inicializarPanelProgreso());
        tabbedPane.addTab("Facturas", inicializarPanelFacturas());
        tabbedPane.addTab("Reportes", inicializarPanelReportes());

        // Panel central con las pestañas
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Inicializa el panel de repuestos
     */
    private JPanel inicializarPanelRepuestos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel superior con botones y búsqueda
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JLabel lblBuscar = crearEtiqueta("Buscar:");
        JTextField txtBuscar = crearCampoTexto();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        JButton btnBuscar = crearBoton("Buscar");

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton btnAgregar = crearBotonPrimario("Agregar");
        JButton btnEditar = crearBoton("Editar");
        JButton btnEliminar = crearBoton("Eliminar");
        JButton btnCargar = crearBoton("Cargar Archivo");

        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCargar);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Tabla de repuestos
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Existencias", "Precio" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaRepuestos = new JTable(modeloTabla);
        estilizarTabla(tablaRepuestos);

        JScrollPane scrollPane = new JScrollPane(tablaRepuestos);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Añadir componentes al panel principal
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Inicializa el panel de servicios
     */
    private JPanel inicializarPanelServicios() {
        // Implementación similar a inicializarPanelRepuestos pero para servicios
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel superior con botones y búsqueda
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JLabel lblBuscar = crearEtiqueta("Buscar:");
        JTextField txtBuscar = crearCampoTexto();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        JButton btnBuscar = crearBoton("Buscar");

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton btnAgregar = crearBotonPrimario("Agregar");
        JButton btnEditar = crearBoton("Editar");
        JButton btnEliminar = crearBoton("Eliminar");
        JButton btnCargar = crearBoton("Cargar Archivo");

        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCargar);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Tabla de servicios
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Precio Mano de Obra", "Precio Total" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaServicios = new JTable(modeloTabla);
        estilizarTabla(tablaServicios);

        JScrollPane scrollPane = new JScrollPane(tablaServicios);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Añadir componentes al panel principal
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Inicializa el panel de clientes
     */
    private JPanel inicializarPanelClientes() {
        // Implementación similar a los paneles anteriores pero para clientes
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel superior con botones y búsqueda
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JLabel lblBuscar = crearEtiqueta("Buscar:");
        JTextField txtBuscar = crearCampoTexto();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        JButton btnBuscar = crearBoton("Buscar");

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton btnAgregar = crearBotonPrimario("Agregar");
        JButton btnEditar = crearBoton("Editar");
        JButton btnEliminar = crearBoton("Eliminar");
        JButton btnCargar = crearBoton("Cargar Archivo");

        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCargar);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Tabla de clientes
        String[] columnas = { "DPI", "Nombre Completo", "Usuario", "Tipo Cliente", "Automóviles" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaClientes = new JTable(modeloTabla);
        estilizarTabla(tablaClientes);

        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Añadir componentes al panel principal
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Inicializa el panel de progreso de automóviles
     */
    private JPanel inicializarPanelProgreso() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Crear pestañas para los diferentes estados
        JTabbedPane tabProgreso = new JTabbedPane();
        tabProgreso.setFont(FONT_NORMAL);
        tabProgreso.setBackground(COLOR_SECONDARY);
        tabProgreso.setForeground(COLOR_LIGHT);

        // Tabla de órdenes en espera
        String[] columnasEspera = { "Orden", "Cliente", "Automóvil", "Servicio", "Fecha", "Estado" };
        DefaultTableModel modeloEspera = new DefaultTableModel(columnasEspera, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaEspera = new JTable(modeloEspera);
        estilizarTabla(tablaEspera);

        JScrollPane scrollEspera = new JScrollPane(tablaEspera);
        scrollEspera.setOpaque(false);
        scrollEspera.getViewport().setOpaque(false);

        // Tabla de órdenes en servicio
        String[] columnasServicio = { "Orden", "Cliente", "Automóvil", "Servicio", "Mecánico", "Fecha", "Estado" };
        DefaultTableModel modeloServicio = new DefaultTableModel(columnasServicio, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaServicio = new JTable(modeloServicio);
        estilizarTabla(tablaServicio);

        JScrollPane scrollServicio = new JScrollPane(tablaServicio);
        scrollServicio.setOpaque(false);
        scrollServicio.getViewport().setOpaque(false);

        // Tabla de órdenes completadas
        String[] columnasCompletadas = { "Orden", "Cliente", "Automóvil", "Servicio", "Mecánico", "Fecha", "Estado" };
        DefaultTableModel modeloCompletadas = new DefaultTableModel(columnasCompletadas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaCompletadas = new JTable(modeloCompletadas);
        estilizarTabla(tablaCompletadas);

        JScrollPane scrollCompletadas = new JScrollPane(tablaCompletadas);
        scrollCompletadas.setOpaque(false);
        scrollCompletadas.getViewport().setOpaque(false);

        // Añadir pestañas
        tabProgreso.addTab("En Espera", scrollEspera);
        tabProgreso.addTab("En Servicio", scrollServicio);
        tabProgreso.addTab("Completadas", scrollCompletadas);

        panel.add(tabProgreso, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton btnRefrescar = crearBoton("Refrescar");
        buttonPanel.add(btnRefrescar);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Inicializa el panel de facturas
     */
    private JPanel inicializarPanelFacturas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel superior con botones y búsqueda
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JLabel lblBuscar = crearEtiqueta("Buscar:");
        JTextField txtBuscar = crearCampoTexto();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        JButton btnBuscar = crearBoton("Buscar");

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton btnVer = crearBotonPrimario("Ver Detalle");
        JButton btnExportar = crearBoton("Exportar PDF");

        buttonPanel.add(btnVer);
        buttonPanel.add(btnExportar);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Tabla de facturas
        String[] columnas = { "N° Factura", "Cliente", "Automóvil", "Servicio", "Fecha", "Total", "Estado" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaFacturas = new JTable(modeloTabla);
        estilizarTabla(tablaFacturas);

        JScrollPane scrollPane = new JScrollPane(tablaFacturas);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Añadir componentes al panel principal
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Inicializa el panel de reportes
     */
    private JPanel inicializarPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel central para los botones de reportes
        JPanel centerPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        centerPanel.setOpaque(false);

        // Crear tarjetas para cada tipo de reporte
        JPanel reporteClientes = crearTarjetaReporte(
                "Clientes por Tipo",
                "Muestra los clientes separados por su tipo (oro y normal)");

        JPanel reporteRepuestosUsados = crearTarjetaReporte(
                "TOP 10 Repuestos Más Usados",
                "Muestra los repuestos más utilizados en el taller");

        JPanel reporteRepuestosCaros = crearTarjetaReporte(
                "TOP 10 Repuestos Más Caros",
                "Muestra los repuestos con precios más elevados");

        JPanel reporteServiciosUsados = crearTarjetaReporte(
                "TOP 10 Servicios Más Usados",
                "Muestra los servicios más solicitados por los clientes");

        JPanel reporteAutomoviles = crearTarjetaReporte(
                "TOP 5 Automóviles Más Repetidos",
                "Muestra los modelos de automóviles más comunes entre los clientes");

        centerPanel.add(reporteClientes);
        centerPanel.add(reporteRepuestosUsados);
        centerPanel.add(reporteRepuestosCaros);
        centerPanel.add(reporteServiciosUsados);
        centerPanel.add(reporteAutomoviles);

        // Panel con título y descripción
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = crearTitulo("Reportes del Sistema");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = crearEtiqueta("Seleccione un reporte para generar");
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblDesc);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea una tarjeta para un reporte específico
     */
    private JPanel crearTarjetaReporte(String titulo, String descripcion) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 60, 90, 180));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 120, 180, 50), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblTitle = crearSubtitulo(titulo);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel(descripcion);
        lblDesc.setFont(FONT_SMALL);
        lblDesc.setForeground(new Color(220, 220, 220));
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnGenerar = crearBotonPrimario("Generar");
        btnGenerar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGenerar.setMaximumSize(new Dimension(150, 35));

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblDesc);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnGenerar);

        return panel;
    }

    /**
     * Carga los datos iniciales en las tablas
     */
    private void cargarDatos() {
        // Código para cargar datos en las tablas
        // Esta implementación dependerá de los controladores y modelos
    }
}