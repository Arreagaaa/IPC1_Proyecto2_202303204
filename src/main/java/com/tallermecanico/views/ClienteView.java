package com.tallermecanico.views;

import com.tallermecanico.models.personas.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Vista principal para clientes
 */
public class ClienteView extends BaseView {

    private Cliente cliente;
    private JTabbedPane tabbedPane;

    // Componentes para gestión de automóviles
    private JTable tablaAutomoviles;
    private DefaultTableModel modeloTablaAutomoviles;

    // Componentes para gestión de órdenes
    private JTable tablaOrdenes;
    private DefaultTableModel modeloTablaOrdenes;

    // Componentes para servicios disponibles
    private JTable tablaServicios;
    private DefaultTableModel modeloTablaServicios;

    // Componentes para gestión de facturas
    private JTable tablaFacturas;
    private DefaultTableModel modeloTablaFacturas;

    /**
     * Constructor
     */
    public ClienteView(Cliente cliente) {
        super("Panel de Cliente - " + cliente.getNombreCompleto());
        this.cliente = cliente;
        inicializarComponentes();
        cargarDatos();
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    @Override
    protected void inicializarComponentes() {
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setOpaque(false); // Fondo transparente para mostrar el wallpaper
        setContentPane(mainPanel);

        // Información del cliente
        JLabel lblCliente = new JLabel("Cliente: " + cliente.getNombreCompleto(), JLabel.LEFT);
        lblCliente.setFont(new Font("Arial", Font.BOLD, 16));
        lblCliente.setForeground(new Color(0, 0, 128)); // Azul oscuro

        JLabel lblTipo = new JLabel("Tipo: " + cliente.getTipoCliente().toUpperCase(), JLabel.LEFT);
        lblTipo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTipo.setForeground(Color.DARK_GRAY);

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInfo.setOpaque(false);
        panelInfo.add(lblCliente);
        panelInfo.add(new JLabel(" | "));
        panelInfo.add(lblTipo);

        mainPanel.add(panelInfo, BorderLayout.NORTH);

        // Pestañas
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mis Automóviles", inicializarPestanaAutomoviles());
        tabbedPane.addTab("Mis Órdenes", inicializarPestanaOrdenes());
        tabbedPane.addTab("Servicios Disponibles", inicializarPestanaServicios());
        tabbedPane.addTab("Mis Facturas", inicializarPestanaFacturas());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Botón de cerrar sesión
        JButton btnCerrarSesion = crearBoton("Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(btnCerrarSesion);
        mainPanel.add(panelBotones, BorderLayout.SOUTH);
    }

    /**
     * Inicializa la pestaña de automóviles
     */
    private JPanel inicializarPestanaAutomoviles() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Tabla de automóviles
        modeloTablaAutomoviles = new DefaultTableModel(new String[] { "Placa", "Marca", "Modelo" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaAutomoviles = new JTable(modeloTablaAutomoviles);
        JScrollPane scroll = new JScrollPane(tablaAutomoviles);
        panel.add(scroll, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(crearBoton("Agregar Automóvil"));
        panelBotones.add(crearBoton("Editar Automóvil"));
        panelBotones.add(crearBoton("Eliminar Automóvil"));
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Inicializa la pestaña de órdenes
     */
    private JPanel inicializarPestanaOrdenes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Tabla de órdenes
        modeloTablaOrdenes = new DefaultTableModel(
                new String[] { "Número", "Vehículo", "Servicio", "Fecha", "Estado", "Mecánico", "Pagado" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaOrdenes = new JTable(modeloTablaOrdenes);
        JScrollPane scroll = new JScrollPane(tablaOrdenes);
        panel.add(scroll, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(crearBoton("Nueva Orden"));
        panelBotones.add(crearBoton("Ver Detalle"));
        panelBotones.add(crearBoton("Pagar Orden"));
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Inicializa la pestaña de servicios disponibles
     */
    private JPanel inicializarPestanaServicios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Tabla de servicios
        modeloTablaServicios = new DefaultTableModel(new String[] { "ID", "Nombre", "Marca", "Modelo", "Precio" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaServicios = new JTable(modeloTablaServicios);
        JScrollPane scroll = new JScrollPane(tablaServicios);
        panel.add(scroll, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(crearBoton("Ver Detalles"));
        panelBotones.add(crearBoton("Solicitar Servicio"));
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Inicializa la pestaña de facturas
     */
    private JPanel inicializarPestanaFacturas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Tabla de facturas
        modeloTablaFacturas = new DefaultTableModel(
                new String[] { "Número", "Fecha", "Orden", "Servicio", "Total", "Estado" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaFacturas = new JTable(modeloTablaFacturas);
        JScrollPane scroll = new JScrollPane(tablaFacturas);
        panel.add(scroll, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false);
        panelBotones.add(crearBoton("Ver Detalles"));
        panelBotones.add(crearBoton("Pagar Factura"));
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Carga los datos en las tablas
     */
    private void cargarDatos() {
        // Cargar datos en las tablas (implementación similar a la original)
    }

    /**
     * Cierra la sesión del cliente
     */
    private void cerrarSesion() {
        dispose();
        new LoginView().setVisible(true);
    }
}