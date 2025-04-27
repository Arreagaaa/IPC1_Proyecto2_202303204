package com.tallermecanico.views;

import com.tallermecanico.controllers.ClienteController;
import com.tallermecanico.controllers.DataController;
import com.tallermecanico.controllers.FacturaController;
import com.tallermecanico.controllers.HiloController;
import com.tallermecanico.controllers.RepuestoController;
import com.tallermecanico.controllers.ServicioController;
import com.tallermecanico.controllers.OrdenTrabajoController;
import com.tallermecanico.controllers.ReporteController;
import com.tallermecanico.controllers.BitacoraController;
import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.Factura;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Empleado;
import com.tallermecanico.utils.CargadorArchivos;
import com.tallermecanico.utils.GestorBitacora;
import com.tallermecanico.utils.GestorHilos;
import com.tallermecanico.utils.MonitorOrdenesThread;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Vector;

/**
 * Vista de administración del sistema
 */
public class AdminView extends JFrame implements MonitorOrdenesThread.ObservadorOrdenes {

    // Empleado administrador logueado
    private Empleado administrador;

    // Panel principal con pestañas
    private JTabbedPane tabbedPane;

    // Componentes para panel de repuestos
    private JPanel panelRepuestos;
    private JTable tablaRepuestos;
    private DefaultTableModel modeloTablaRepuestos;
    private JButton btnAgregarRepuesto;
    private JButton btnEditarRepuesto;
    private JButton btnEliminarRepuesto;
    private JButton btnCargarRepuestos;
    private JTextField txtBuscarRepuesto;
    private JButton btnBuscarRepuesto;

    // Componentes para panel de servicios
    private JPanel panelServicios;
    private JTable tablaServicios;
    private DefaultTableModel modeloTablaServicios;
    private JButton btnAgregarServicio;
    private JButton btnEditarServicio;
    private JButton btnEliminarServicio;
    private JButton btnCargarServicios;
    private JTextField txtBuscarServicio;
    private JButton btnBuscarServicio;
    private JTable tablaRepuestosServicio;
    private DefaultTableModel modeloTablaRepuestosServicio;

    // Componentes para panel de clientes
    private JPanel panelClientes;
    private JTable tablaClientes;
    private DefaultTableModel modeloTablaClientes;
    private JButton btnVerCliente;
    private JButton btnEditarCliente;
    private JButton btnEliminarCliente;
    private JButton btnCargarClientes;
    private JTextField txtBuscarCliente;
    private JButton btnBuscarCliente;
    private JTable tablaAutosCliente;
    private DefaultTableModel modeloTablaAutosCliente;

    // Componentes para panel de reportes
    private JPanel panelReportes;
    private JButton btnClientesOro;
    private JButton btnRepuestosMasUsados;
    private JButton btnRepuestosMasCaros;
    private JButton btnServiciosMasUsados;
    private JButton btnAutosMasRepetidos;

    // Componentes para panel de progreso
    private JPanel panelProgreso;
    private JTable tablaEspera;
    private DefaultTableModel modeloTablaEspera;
    private JTable tablaEnServicio;
    private DefaultTableModel modeloTablaEnServicio;
    private JTable tablaListos;
    private DefaultTableModel modeloTablaListos;
    private JTable tablaPendientesPago;
    private DefaultTableModel modeloTablaPendientesPago;

    // Componentes para panel de facturas
    private JTable tablaFacturas;
    private DefaultTableModel modeloTablaFacturas;
    private JRadioButton radioTodas;
    private JRadioButton radioPendientes;
    private JRadioButton radioPagadas;
    private JButton btnVerFactura;
    private JButton btnRegistrarPago;

    // Componentes para botones generales
    private JButton btnCerrarSesion;
    private JPanel panelBotones;
    private JButton btnRefrescar;

    // Componentes para panel de bitácora
    private JButton btnVerBitacora;

    /**
     * Constructor de la vista de administración
     */
    public AdminView(Empleado administrador) {
        this.administrador = administrador;
        inicializarComponentes();
        configurarEventos();
        cargarDatos();

        // Registrarse como observador de órdenes
        GestorHilos.obtenerInstancia().registrarObservadorOrdenes(this);

        // Configurar para desregistrarse cuando se cierra la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GestorHilos.obtenerInstancia().eliminarObservadorOrdenes(AdminView.this);
            }
        });

        GestorBitacora.registrarEvento(administrador.getNombreUsuario(), "Inicio de sesión", true,
                "Administrador inició sesión: " + administrador.getNombreCompleto());
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarComponentes() {
        // Configuración básica de la ventana
        setTitle("Taller Mecánico - Panel de Administración");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal con pestañas
        tabbedPane = new JTabbedPane();

        // Inicializar cada panel
        inicializarPanelRepuestos();
        inicializarPanelServicios();
        inicializarPanelClientes();
        inicializarPanelReportes();
        inicializarPanelProgreso();
        JPanel panelFacturas = inicializarPanelFacturas();

        // Añadir pestañas
        tabbedPane.addTab("Repuestos", panelRepuestos);
        tabbedPane.addTab("Servicios", panelServicios);
        tabbedPane.addTab("Clientes", panelClientes);
        tabbedPane.addTab("Reportes", panelReportes);
        tabbedPane.addTab("Progreso de Automóviles", panelProgreso);
        tabbedPane.addTab("Facturas", panelFacturas);

        // Panel de botones generales
        panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefrescar = new JButton("Refrescar");
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnVerBitacora = new JButton("Ver Bitácora");
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnVerBitacora);
        panelBotones.add(btnCerrarSesion);

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        // Añadir el panel principal a la ventana
        setContentPane(panelPrincipal);
    }

    /**
     * Inicializa el panel de repuestos
     */
    private void inicializarPanelRepuestos() {
        panelRepuestos = new JPanel(new BorderLayout());
        panelRepuestos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBuscarRepuesto = new JTextField(20);
        btnBuscarRepuesto = new JButton("Buscar");
        panelBusqueda.add(new JLabel("Buscar repuesto:"));
        panelBusqueda.add(txtBuscarRepuesto);
        panelBusqueda.add(btnBuscarRepuesto);

        // Panel para botones de acción
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAgregarRepuesto = new JButton("Agregar");
        btnEditarRepuesto = new JButton("Editar");
        btnEliminarRepuesto = new JButton("Eliminar");
        btnCargarRepuestos = new JButton("Cargar Archivo");
        panelAcciones.add(btnAgregarRepuesto);
        panelAcciones.add(btnEditarRepuesto);
        panelAcciones.add(btnEliminarRepuesto);
        panelAcciones.add(btnCargarRepuestos);

        // Panel superior que contiene búsqueda y acciones
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        // Tabla de repuestos
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Existencias", "Precio" };
        modeloTablaRepuestos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No permitir edición directa en la tabla
            }
        };
        tablaRepuestos = new JTable(modeloTablaRepuestos);
        JScrollPane scrollPane = new JScrollPane(tablaRepuestos);

        // Añadir componentes al panel principal
        panelRepuestos.add(panelSuperior, BorderLayout.NORTH);
        panelRepuestos.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Inicializa el panel de servicios
     */
    private void inicializarPanelServicios() {
        panelServicios = new JPanel(new BorderLayout());
        panelServicios.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBuscarServicio = new JTextField(20);
        btnBuscarServicio = new JButton("Buscar");
        panelBusqueda.add(new JLabel("Buscar servicio:"));
        panelBusqueda.add(txtBuscarServicio);
        panelBusqueda.add(btnBuscarServicio);

        // Panel para botones de acción
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAgregarServicio = new JButton("Agregar");
        btnEditarServicio = new JButton("Editar");
        btnEliminarServicio = new JButton("Eliminar");
        btnCargarServicios = new JButton("Cargar Archivo");
        panelAcciones.add(btnAgregarServicio);
        panelAcciones.add(btnEditarServicio);
        panelAcciones.add(btnEliminarServicio);
        panelAcciones.add(btnCargarServicios);

        // Panel superior que contiene búsqueda y acciones
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        // Tabla de servicios
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Precio Mano Obra", "Precio Total" };
        modeloTablaServicios = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaServicios = new JTable(modeloTablaServicios);
        JScrollPane scrollPane = new JScrollPane(tablaServicios);

        // Panel para mostrar repuestos de un servicio
        JPanel panelRepuestosServicio = new JPanel(new BorderLayout());
        panelRepuestosServicio.setBorder(BorderFactory.createTitledBorder("Repuestos del Servicio"));

        // Tabla de repuestos de un servicio
        String[] columnasRepuestos = { "ID", "Nombre", "Marca", "Modelo", "Precio" };
        modeloTablaRepuestosServicio = new DefaultTableModel(columnasRepuestos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaRepuestosServicio = new JTable(modeloTablaRepuestosServicio);
        JScrollPane scrollRepuestos = new JScrollPane(tablaRepuestosServicio);
        panelRepuestosServicio.add(scrollRepuestos, BorderLayout.CENTER);

        // Panel dividido para mostrar servicios y sus repuestos
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, panelRepuestosServicio);
        splitPane.setResizeWeight(0.6); // 60% para servicios, 40% para repuestos

        // Añadir componentes al panel principal
        panelServicios.add(panelSuperior, BorderLayout.NORTH);
        panelServicios.add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Inicializa el panel de clientes
     */
    private void inicializarPanelClientes() {
        panelClientes = new JPanel(new BorderLayout());
        panelClientes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBuscarCliente = new JTextField(20);
        btnBuscarCliente = new JButton("Buscar");
        panelBusqueda.add(new JLabel("Buscar cliente:"));
        panelBusqueda.add(txtBuscarCliente);
        panelBusqueda.add(btnBuscarCliente);

        // Panel para botones de acción
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnVerCliente = new JButton("Ver Detalles");
        btnEditarCliente = new JButton("Editar");
        btnEliminarCliente = new JButton("Eliminar");
        btnCargarClientes = new JButton("Cargar Archivo");
        panelAcciones.add(btnVerCliente);
        panelAcciones.add(btnEditarCliente);
        panelAcciones.add(btnEliminarCliente);
        panelAcciones.add(btnCargarClientes);

        // Panel superior que contiene búsqueda y acciones
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        // Tabla de clientes
        String[] columnas = { "DPI", "Nombre", "Apellido", "Usuario", "Tipo Cliente" };
        modeloTablaClientes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaClientes = new JTable(modeloTablaClientes);
        JScrollPane scrollPane = new JScrollPane(tablaClientes);

        // Panel para mostrar automóviles de un cliente
        JPanel panelAutosCliente = new JPanel(new BorderLayout());
        panelAutosCliente.setBorder(BorderFactory.createTitledBorder("Automóviles del Cliente"));

        // Tabla de automóviles de un cliente
        String[] columnasAutos = { "Placa", "Marca", "Modelo", "Foto" };
        modeloTablaAutosCliente = new DefaultTableModel(columnasAutos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaAutosCliente = new JTable(modeloTablaAutosCliente);
        JScrollPane scrollAutos = new JScrollPane(tablaAutosCliente);
        panelAutosCliente.add(scrollAutos, BorderLayout.CENTER);

        // Panel dividido para mostrar clientes y sus automóviles
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, panelAutosCliente);
        splitPane.setResizeWeight(0.6); // 60% para clientes, 40% para automóviles

        // Añadir componentes al panel principal
        panelClientes.add(panelSuperior, BorderLayout.NORTH);
        panelClientes.add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Inicializa el panel de reportes
     */
    private void inicializarPanelReportes() {
        panelReportes = new JPanel(new BorderLayout());
        panelReportes.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título del panel
        JLabel lblTitulo = new JLabel("Generación de Reportes", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        panelReportes.add(lblTitulo, BorderLayout.NORTH);

        // Panel con opciones de reportes
        JPanel panelOpciones = new JPanel(new GridLayout(5, 1, 0, 20));
        panelOpciones.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        // Botones para cada tipo de reporte
        btnClientesOro = new JButton("Clientes Oro vs Normales");
        btnRepuestosMasUsados = new JButton("TOP 10 Repuestos Más Usados");
        btnRepuestosMasCaros = new JButton("TOP 10 Repuestos Más Caros");
        btnServiciosMasUsados = new JButton("TOP 10 Servicios Más Usados");
        btnAutosMasRepetidos = new JButton("TOP 5 Automóviles Más Repetidos");

        // Personalizar botones
        Font fuenteBoton = new Font("Arial", Font.PLAIN, 14);
        Dimension tamanoBoton = new Dimension(300, 50);

        for (JButton btn : new JButton[] { btnClientesOro, btnRepuestosMasUsados, btnRepuestosMasCaros,
                btnServiciosMasUsados, btnAutosMasRepetidos }) {
            btn.setFont(fuenteBoton);
            btn.setPreferredSize(tamanoBoton);
            btn.setBackground(new Color(230, 230, 250));
            btn.setFocusPainted(false);
            panelOpciones.add(btn);
        }

        // Añadir panel de opciones al panel principal
        panelReportes.add(panelOpciones, BorderLayout.CENTER);

        // Añadir nota informativa
        JLabel lblNota = new JLabel("Los reportes se podrán exportar a PDF.", JLabel.CENTER);
        lblNota.setFont(new Font("Arial", Font.ITALIC, 12));
        panelReportes.add(lblNota, BorderLayout.SOUTH);
    }

    /**
     * Inicializa el panel de progreso de automóviles
     */
    private void inicializarPanelProgreso() {
        panelProgreso = new JPanel(new GridLayout(2, 2, 10, 10));
        panelProgreso.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para automóviles en espera
        JPanel panelEspera = new JPanel(new BorderLayout());
        panelEspera.setBorder(BorderFactory.createTitledBorder("Automóviles en Espera"));
        String[] columnasEspera = { "No. Orden", "Cliente", "Automóvil", "Servicio", "Fecha" };
        modeloTablaEspera = new DefaultTableModel(columnasEspera, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaEspera = new JTable(modeloTablaEspera);
        JScrollPane scrollEspera = new JScrollPane(tablaEspera);
        panelEspera.add(scrollEspera, BorderLayout.CENTER);

        // Panel para automóviles en servicio
        JPanel panelEnServicio = new JPanel(new BorderLayout());
        panelEnServicio.setBorder(BorderFactory.createTitledBorder("Automóviles en Servicio"));
        String[] columnasEnServicio = { "No. Orden", "Cliente", "Automóvil", "Servicio", "Mecánico", "Fecha" };
        modeloTablaEnServicio = new DefaultTableModel(columnasEnServicio, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaEnServicio = new JTable(modeloTablaEnServicio);
        JScrollPane scrollEnServicio = new JScrollPane(tablaEnServicio);
        panelEnServicio.add(scrollEnServicio, BorderLayout.CENTER);

        // Panel para automóviles listos
        JPanel panelListos = new JPanel(new BorderLayout());
        panelListos.setBorder(BorderFactory.createTitledBorder("Automóviles Listos"));
        String[] columnasListos = { "No. Orden", "Cliente", "Automóvil", "Servicio", "Mecánico", "Fecha" };
        modeloTablaListos = new DefaultTableModel(columnasListos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaListos = new JTable(modeloTablaListos);
        JScrollPane scrollListos = new JScrollPane(tablaListos);
        panelListos.add(scrollListos, BorderLayout.CENTER);

        // Panel para facturas pendientes de pago
        JPanel panelPendientesPago = new JPanel(new BorderLayout());
        panelPendientesPago.setBorder(BorderFactory.createTitledBorder("Pendientes de Pago"));
        String[] columnasPendientes = { "No. Factura", "Cliente", "Automóvil", "Servicio", "Total", "Fecha" };
        modeloTablaPendientesPago = new DefaultTableModel(columnasPendientes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPendientesPago = new JTable(modeloTablaPendientesPago);
        JScrollPane scrollPendientes = new JScrollPane(tablaPendientesPago);
        panelPendientesPago.add(scrollPendientes, BorderLayout.CENTER);

        // Añadir paneles al panel de progreso
        panelProgreso.add(panelEspera);
        panelProgreso.add(panelEnServicio);
        panelProgreso.add(panelListos);
        panelProgreso.add(panelPendientesPago);
    }

    /**
     * Inicializa el panel de facturas
     */
    private JPanel inicializarPanelFacturas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Etiqueta de título
        JLabel lblTitulo = new JLabel("Gestión de Facturas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Tabla de facturas
        String[] columnas = { "Número", "Fecha", "Cliente", "Orden", "Servicio", "Total", "Estado" };
        modeloTablaFacturas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaFacturas = new JTable(modeloTablaFacturas);
        JScrollPane scrollTabla = new JScrollPane(tablaFacturas);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panelFiltros.add(new JLabel("Filtrar:"));

        radioTodas = new JRadioButton("Todas");
        radioPendientes = new JRadioButton("Pendientes");
        radioPagadas = new JRadioButton("Pagadas");

        ButtonGroup grupoFiltro = new ButtonGroup();
        grupoFiltro.add(radioTodas);
        grupoFiltro.add(radioPendientes);
        grupoFiltro.add(radioPagadas);

        radioTodas.setSelected(true);

        panelFiltros.add(radioTodas);
        panelFiltros.add(radioPendientes);
        panelFiltros.add(radioPagadas);

        panelFiltros.add(Box.createHorizontalStrut(20));

        panelFiltros.add(new JLabel("Buscar:"));
        JTextField txtBuscar = new JTextField(15);
        JButton btnBuscar = new JButton("Buscar");

        panelFiltros.add(txtBuscar);
        panelFiltros.add(btnBuscar);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnVerFactura = new JButton("Ver Factura");
        btnRegistrarPago = new JButton("Registrar Pago");
        JButton btnExportar = new JButton("Exportar Factura");

        panelBotones.add(btnVerFactura);
        panelBotones.add(btnRegistrarPago);
        panelBotones.add(btnExportar);

        // Panel inferior combinado
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelFiltros, BorderLayout.NORTH);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        panel.add(panelInferior, BorderLayout.SOUTH);

        // Configurar eventos
        radioTodas.addActionListener(e -> actualizarTablaFacturas("todas"));
        radioPendientes.addActionListener(e -> actualizarTablaFacturas("pendientes"));
        radioPagadas.addActionListener(e -> actualizarTablaFacturas("pagadas"));

        btnBuscar.addActionListener(e -> {
            String filtro = txtBuscar.getText().trim();
            if (radioTodas.isSelected()) {
                actualizarTablaFacturas("todas", filtro);
            } else if (radioPendientes.isSelected()) {
                actualizarTablaFacturas("pendientes", filtro);
            } else {
                actualizarTablaFacturas("pagadas", filtro);
            }
        });

        btnVerFactura.addActionListener(e -> verFacturaSeleccionada());
        btnRegistrarPago.addActionListener(e -> registrarPagoFacturaSeleccionada());
        btnExportar.addActionListener(e -> exportarFacturaSeleccionada());

        // Selección en tabla habilita/deshabilita botones
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> {
            boolean haySeleccion = tablaFacturas.getSelectedRow() != -1;
            btnVerFactura.setEnabled(haySeleccion);

            // Solo habilitar pago si la factura no está pagada
            if (haySeleccion) {
                String estado = (String) tablaFacturas.getValueAt(tablaFacturas.getSelectedRow(), 6);
                btnRegistrarPago.setEnabled("PENDIENTE".equals(estado));
            } else {
                btnRegistrarPago.setEnabled(false);
            }
        });

        // Estado inicial de botones
        btnVerFactura.setEnabled(false);
        btnRegistrarPago.setEnabled(false);

        return panel;
    }

    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        // Eventos para panel de repuestos
        configurarEventosRepuestos();

        // Eventos para panel de servicios
        configurarEventosServicios();

        // Eventos para panel de clientes
        configurarEventosClientes();

        // Eventos para panel de reportes
        configurarEventosReportes();

        // Eventos para panel de bitácora
        configurarEventosBitacora();

        // Eventos para botones generales
        btnRefrescar.addActionListener(e -> cargarDatos());

        btnCerrarSesion.addActionListener(e -> {
            GestorBitacora.registrarEvento(administrador.getNombreUsuario(), "Cierre de sesión", true,
                    "Administrador cerró sesión: " + administrador.getNombreCompleto());
            dispose();
            new LoginView(LoginView.TIPO_ADMIN).setVisible(true);
        });

        // Evento para cambio de pestaña
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == panelProgreso) {
                actualizarTablasProgreso();
            }
        });
    }

    /**
     * Configura los eventos para el panel de repuestos
     */
    private void configurarEventosRepuestos() {
        // Botón para agregar repuesto
        btnAgregarRepuesto.addActionListener(e -> mostrarDialogoAgregarRepuesto());

        // Botón para editar repuesto
        btnEditarRepuesto.addActionListener(e -> {
            int filaSeleccionada = tablaRepuestos.getSelectedRow();
            if (filaSeleccionada != -1) {
                int idRepuesto = (int) tablaRepuestos.getValueAt(filaSeleccionada, 0);
                Repuesto repuesto = RepuestoController.buscarRepuestoPorId(idRepuesto);
                if (repuesto != null) {
                    mostrarDialogoEditarRepuesto(repuesto);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un repuesto de la tabla",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Botón para eliminar repuesto
        btnEliminarRepuesto.addActionListener(e -> {
            int filaSeleccionada = tablaRepuestos.getSelectedRow();
            if (filaSeleccionada != -1) {
                int idRepuesto = (int) tablaRepuestos.getValueAt(filaSeleccionada, 0);
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Está seguro de eliminar el repuesto seleccionado?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION);
                if (confirmacion == JOptionPane.YES_OPTION) {
                    boolean eliminado = RepuestoController.eliminarRepuesto(idRepuesto);
                    if (eliminado) {
                        JOptionPane.showMessageDialog(this,
                                "Repuesto eliminado correctamente",
                                "Eliminación exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                        actualizarTablaRepuestos();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo eliminar el repuesto. Posiblemente está siendo utilizado en algún servicio.",
                                "Error al eliminar",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un repuesto de la tabla",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Botón para cargar repuestos desde archivo
        btnCargarRepuestos.addActionListener(e -> {
            int cargados = CargadorArchivos.cargarArchivoConSelector("repuestos");
            if (cargados > 0) {
                actualizarTablaRepuestos();
                JOptionPane.showMessageDialog(this,
                        "Se cargaron " + cargados + " repuestos correctamente",
                        "Carga exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Botón para buscar repuestos
        btnBuscarRepuesto.addActionListener(e -> {
            String textoBusqueda = txtBuscarRepuesto.getText().trim().toLowerCase();
            if (!textoBusqueda.isEmpty()) {
                buscarRepuestos(textoBusqueda);
            } else {
                actualizarTablaRepuestos();
            }
        });
    }

    /**
     * Configura los eventos para el panel de servicios
     */
    private void configurarEventosServicios() {
        // Botón para agregar servicio
        btnAgregarServicio.addActionListener(e -> mostrarDialogoAgregarServicio());

        // Botón para editar servicio
        btnEditarServicio.addActionListener(e -> {
            int filaSeleccionada = tablaServicios.getSelectedRow();
            if (filaSeleccionada != -1) {
                int idServicio = (int) tablaServicios.getValueAt(filaSeleccionada, 0);
                Servicio servicio = ServicioController.buscarServicioPorId(idServicio);
                if (servicio != null) {
                    mostrarDialogoEditarServicio(servicio);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un servicio de la tabla",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Botón para eliminar servicio
        btnEliminarServicio.addActionListener(e -> {
            int filaSeleccionada = tablaServicios.getSelectedRow();
            if (filaSeleccionada != -1) {
                int idServicio = (int) tablaServicios.getValueAt(filaSeleccionada, 0);
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Está seguro de eliminar el servicio seleccionado?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION);
                if (confirmacion == JOptionPane.YES_OPTION) {
                    boolean eliminado = ServicioController.eliminarServicio(idServicio);
                    if (eliminado) {
                        JOptionPane.showMessageDialog(this,
                                "Servicio eliminado correctamente",
                                "Eliminación exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                        actualizarTablaServicios();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo eliminar el servicio. Posiblemente está siendo utilizado en alguna orden.",
                                "Error al eliminar",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un servicio de la tabla",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Botón para cargar servicios desde archivo
        btnCargarServicios.addActionListener(e -> {
            int cargados = CargadorArchivos.cargarArchivoConSelector("servicios");
            if (cargados > 0) {
                actualizarTablaServicios();
                JOptionPane.showMessageDialog(this,
                        "Se cargaron " + cargados + " servicios correctamente",
                        "Carga exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Botón para buscar servicios
        btnBuscarServicio.addActionListener(e -> {
            String textoBusqueda = txtBuscarServicio.getText().trim().toLowerCase();
            if (!textoBusqueda.isEmpty()) {
                buscarServicios(textoBusqueda);
            } else {
                actualizarTablaServicios();
            }
        });

        // Evento para mostrar repuestos de un servicio al seleccionarlo
        tablaServicios.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int filaSeleccionada = tablaServicios.getSelectedRow();
                    if (filaSeleccionada != -1) {
                        int idServicio = (int) tablaServicios.getValueAt(filaSeleccionada, 0);
                        Servicio servicio = ServicioController.buscarServicioPorId(idServicio);
                        if (servicio != null) {
                            actualizarTablaRepuestosServicio(servicio);
                        }
                    }
                }
            }
        });
    }

    /**
     * Configura los eventos para el panel de clientes
     */
    private void configurarEventosClientes() {
        // Botón para agregar cliente
        btnCargarClientes.addActionListener(e -> mostrarDialogoAgregarCliente());

        // Botón para ver detalles de cliente
        btnVerCliente.addActionListener(e -> {
            int filaSeleccionada = tablaClientes.getSelectedRow();
            if (filaSeleccionada != -1) {
                String idCliente = (String) tablaClientes.getValueAt(filaSeleccionada, 0);
                Cliente cliente = ClienteController.buscarClientePorIdentificador(idCliente);
                if (cliente != null) {
                    mostrarDetallesCliente(cliente);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un cliente de la tabla",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Botón para editar cliente
        btnEditarCliente.addActionListener(e -> {
            int filaSeleccionada = tablaClientes.getSelectedRow();
            if (filaSeleccionada != -1) {
                String idCliente = (String) tablaClientes.getValueAt(filaSeleccionada, 0);
                Cliente cliente = ClienteController.buscarClientePorIdentificador(idCliente);
                if (cliente != null) {
                    mostrarDialogoEditarCliente(cliente);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un cliente de la tabla",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Botón para eliminar cliente
        btnEliminarCliente.addActionListener(e -> {
            int filaSeleccionada = tablaClientes.getSelectedRow();
            if (filaSeleccionada != -1) {
                String idCliente = (String) tablaClientes.getValueAt(filaSeleccionada, 0);
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Está seguro de eliminar el cliente seleccionado?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION);
                if (confirmacion == JOptionPane.YES_OPTION) {
                    boolean eliminado = ClienteController.eliminarCliente(idCliente);
                    if (eliminado) {
                        JOptionPane.showMessageDialog(this,
                                "Cliente eliminado correctamente",
                                "Eliminación exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                        actualizarTablaClientes();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo eliminar el cliente. Posiblemente tiene órdenes pendientes.",
                                "Error al eliminar",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un cliente de la tabla",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Botón para buscar clientes
        btnBuscarCliente.addActionListener(e -> {
            String textoBusqueda = txtBuscarCliente.getText().trim().toLowerCase();
            if (!textoBusqueda.isEmpty()) {
                buscarClientes(textoBusqueda);
            } else {
                actualizarTablaClientes();
            }
        });

        // Evento para mostrar automóviles de un cliente al seleccionarlo
        tablaClientes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int filaSeleccionada = tablaClientes.getSelectedRow();
                    if (filaSeleccionada != -1) {
                        String idCliente = (String) tablaClientes.getValueAt(filaSeleccionada, 0);
                        Cliente cliente = ClienteController.buscarClientePorIdentificador(idCliente);
                        if (cliente != null) {
                            actualizarTablaAutosCliente(cliente);
                        }
                    }
                }
            }
        });
    }

    /**
     * Configura los eventos para el panel de reportes
     */
    private void configurarEventosReportes() {
        // Botón para mostrar reporte de clientes (oro vs normales)
        btnClientesOro.addActionListener(e -> mostrarReporteClientesOro());

        // Botón para mostrar TOP 10 repuestos más usados
        btnRepuestosMasUsados.addActionListener(e -> mostrarReporteRepuestosMasUsados());

        // Botón para mostrar TOP 10 repuestos más caros
        btnRepuestosMasCaros.addActionListener(e -> mostrarReporteRepuestosMasCaros());

        // Botón para mostrar TOP 10 servicios más usados
        btnServiciosMasUsados.addActionListener(e -> mostrarReporteServiciosMasUsados());

        // Botón para mostrar TOP 5 automóviles más repetidos
        btnAutosMasRepetidos.addActionListener(e -> mostrarReporteAutosMasRepetidos());
    }

    /**
     * Configura los eventos para el panel de bitácora
     */
    private void configurarEventosBitacora() {
        btnVerBitacora.addActionListener(e -> {
            BitacoraView bitacoraView = new BitacoraView(BitacoraController.getBitacora());
            bitacoraView.setVisible(true);
        });
    }

    /**
     * Carga los datos iniciales en las tablas
     */
    private void cargarDatos() {
        actualizarTablaRepuestos();
        actualizarTablaServicios();
        actualizarTablaClientes();
        actualizarTablasProgreso();
        actualizarTablaFacturas("todas");
    }

    /**
     * Actualiza las tablas del panel de progreso
     */
    private void actualizarTablasProgreso() {
        // Limpiar todas las tablas
        modeloTablaEspera.setRowCount(0);
        modeloTablaEnServicio.setRowCount(0);
        modeloTablaListos.setRowCount(0);

        // Obtener listas según el estado
        Vector<OrdenTrabajo> ordenesEspera = OrdenTrabajoController.obtenerColaEspera();
        Vector<OrdenTrabajo> ordenesServicio = OrdenTrabajoController.obtenerOrdenesPorEstado("en_servicio");
        Vector<OrdenTrabajo> ordenesListas = OrdenTrabajoController.obtenerOrdenesPorEstado("listo");

        // Actualizar tabla de órdenes en espera
        for (OrdenTrabajo orden : ordenesEspera) {
            boolean esClienteOro = "oro".equals(orden.getCliente().getTipoCliente());

            modeloTablaEspera.addRow(new Object[] {
                    orden.getNumero(),
                    (esClienteOro ? "★ " : "") + orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca() + " "
                            + orden.getAutomovil().getModelo(),
                    orden.getServicio().getNombre(),
                    orden.getFecha()
            });
        }

        // Actualizar tabla de órdenes en servicio
        for (OrdenTrabajo orden : ordenesServicio) {
            modeloTablaEnServicio.addRow(new Object[] {
                    orden.getNumero(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca() + " "
                            + orden.getAutomovil().getModelo(),
                    orden.getServicio().getNombre(),
                    orden.getMecanico() != null ? orden.getMecanico().getNombreCompleto() : "No asignado",
                    orden.getFecha()
            });
        }

        // Actualizar tabla de órdenes listas
        for (OrdenTrabajo orden : ordenesListas) {
            modeloTablaListos.addRow(new Object[] {
                    orden.getNumero(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca() + " "
                            + orden.getAutomovil().getModelo(),
                    orden.getServicio().getNombre(),
                    orden.getMecanico() != null ? orden.getMecanico().getNombreCompleto() : "No asignado",
                    orden.getFecha()
            });
        }
    }

    /**
     * Actualiza la tabla de repuestos
     */
    private void actualizarTablaRepuestos() {
        // Limpiar la tabla
        modeloTablaRepuestos.setRowCount(0);

        // Obtener todos los repuestos
        Vector<Repuesto> repuestos = DataController.getRepuestos();

        // Agregar cada repuesto a la tabla
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Repuesto repuesto : repuestos) {
            modeloTablaRepuestos.addRow(new Object[] {
                    repuesto.getId(),
                    repuesto.getNombre(),
                    repuesto.getMarca(),
                    repuesto.getModelo(),
                    repuesto.getExistencias(),
                    "Q " + df.format(repuesto.getPrecio())
            });
        }
    }

    /**
     * Busca repuestos que coincidan con el texto de búsqueda
     */
    private void buscarRepuestos(String textoBusqueda) {
        // Limpiar la tabla
        modeloTablaRepuestos.setRowCount(0);

        // Obtener todos los repuestos
        Vector<Repuesto> repuestos = DataController.getRepuestos();

        // Filtrar repuestos según el texto de búsqueda
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Repuesto repuesto : repuestos) {
            if (repuesto.getNombre().toLowerCase().contains(textoBusqueda) ||
                    repuesto.getMarca().toLowerCase().contains(textoBusqueda) ||
                    repuesto.getModelo().toLowerCase().contains(textoBusqueda)) {

                modeloTablaRepuestos.addRow(new Object[] {
                        repuesto.getId(),
                        repuesto.getNombre(),
                        repuesto.getMarca(),
                        repuesto.getModelo(),
                        repuesto.getExistencias(),
                        "Q " + df.format(repuesto.getPrecio())
                });
            }
        }
    }

    /**
     * Muestra diálogo para agregar un nuevo repuesto
     */
    private void mostrarDialogoAgregarRepuesto() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        JTextField txtNombre = new JTextField();
        JTextField txtMarca = new JTextField();
        JTextField txtModelo = new JTextField();
        JTextField txtExistencias = new JTextField();
        JTextField txtPrecio = new JTextField();

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Marca:"));
        panel.add(txtMarca);
        panel.add(new JLabel("Modelo:"));
        panel.add(txtModelo);
        panel.add(new JLabel("Existencias:"));
        panel.add(txtExistencias);
        panel.add(new JLabel("Precio:"));
        panel.add(txtPrecio);

        int result = JOptionPane.showConfirmDialog(this, panel, "Agregar Nuevo Repuesto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            String existencias = txtExistencias.getText().trim();
            String precio = txtPrecio.getText().trim();

            // Validar los campos antes de continuar
            if (!validarCamposRepuesto(nombre, marca, modelo, existencias, precio)) {
                return; // Detener si la validación falla
            }

            // Crear y agregar el repuesto
            boolean exito = RepuestoController.agregarRepuesto(nombre, marca, Double.parseDouble(precio),
                    Integer.parseInt(existencias));
            if (exito) {
                JOptionPane.showMessageDialog(this, "Repuesto agregado correctamente", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                actualizarTablaRepuestos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar el repuesto", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra diálogo para editar un repuesto existente
     * 
     * @param repuesto Repuesto a editar
     */
    private void mostrarDialogoEditarRepuesto(Repuesto repuesto) {
        if (repuesto == null)
            return;

        // Crear un panel con campos para editar el repuesto
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        JTextField txtNombre = new JTextField(repuesto.getNombre());
        JTextField txtPrecio = new JTextField(String.valueOf(repuesto.getPrecio()));

        // Obtener descripción con seguridad
        String descripcionActual;
        try {
            descripcionActual = repuesto.getDescripcion();
        } catch (Exception e) {
            // Si getDescripcion() no existe, usar una combinación de otras propiedades
            descripcionActual = repuesto.getMarca() + " " + repuesto.getModelo();
        }
        JTextField txtDescripcion = new JTextField(descripcionActual);

        // Obtener cantidad con seguridad
        int cantidadActual;
        try {
            cantidadActual = repuesto.getCantidad();
        } catch (Exception e) {
            cantidadActual = repuesto.getExistencias();
        }
        JTextField txtCantidad = new JTextField(String.valueOf(cantidadActual));

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Precio:"));
        panel.add(txtPrecio);
        panel.add(new JLabel("Descripción:"));
        panel.add(txtDescripcion);
        panel.add(new JLabel("Cantidad:"));
        panel.add(txtCantidad);

        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Repuesto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                String descripcion = txtDescripcion.getText().trim();
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                int cantidad = Integer.parseInt(txtCantidad.getText().trim());

                if (nombre.isEmpty() || precio <= 0 || cantidad <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Todos los campos son obligatorios y los valores numéricos deben ser positivos",
                            "Datos inválidos", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Actualizar el repuesto usando la interfaz corregida
                boolean exito = RepuestoController.actualizarRepuesto(repuesto.getId(), nombre, descripcion, precio,
                        cantidad);

                if (exito) {
                    JOptionPane.showMessageDialog(this, "Repuesto actualizado correctamente",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    actualizarTablaRepuestos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el repuesto",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos para precio y cantidad",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra diálogo para editar un servicio existente
     * 
     * @param servicio Servicio a editar
     */
    private void mostrarDialogoEditarServicio(Servicio servicio) {
        if (servicio == null)
            return;

        // Crear un panel con campos para editar el servicio
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField txtNombre = new JTextField(servicio.getNombre());
        JTextField txtPrecioBase = new JTextField(String.valueOf(servicio.getPrecioBase()));
        JTextField txtDescripcion = new JTextField(servicio.getDescripcion());

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Precio Base:"));
        panel.add(txtPrecioBase);
        panel.add(new JLabel("Descripción:"));
        panel.add(txtDescripcion);

        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Servicio",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                String descripcion = txtDescripcion.getText().trim();
                double precioBase = Double.parseDouble(txtPrecioBase.getText().trim());

                if (nombre.isEmpty() || precioBase <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Todos los campos son obligatorios y el precio debe ser positivo",
                            "Datos inválidos", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Actualizar el servicio - usar marca vacía o el nombre como marca si es
                // necesario
                boolean exito = ServicioController.actualizarServicio(servicio.getId(), nombre, descripcion,
                        nombre, precioBase); // Usando nombre como marca si es requerido

                if (exito) {
                    JOptionPane.showMessageDialog(this, "Servicio actualizado correctamente",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    actualizarTablaServicios();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el servicio",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Ingrese un valor numérico válido para el precio",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra diálogo para agregar un nuevo servicio
     */
    private void mostrarDialogoAgregarServicio() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtNombre = new JTextField();
        JTextField txtMarca = new JTextField();
        JTextField txtModelo = new JTextField();
        JTextField txtPrecioManoDeObra = new JTextField();

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Marca:"));
        panel.add(txtMarca);
        panel.add(new JLabel("Modelo:"));
        panel.add(txtModelo);
        panel.add(new JLabel("Precio Mano de Obra:"));
        panel.add(txtPrecioManoDeObra);

        int result = JOptionPane.showConfirmDialog(this, panel, "Agregar Nuevo Servicio",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            String precioManoDeObra = txtPrecioManoDeObra.getText().trim();

            // Validar los campos antes de continuar
            if (!validarCamposServicio(nombre, marca, modelo, precioManoDeObra)) {
                return; // Detener si la validación falla
            }

            // Crear y agregar el servicio
            Servicio nuevoServicio = ServicioController.registrarServicio(precioManoDeObra, precioManoDeObra,
                    precioManoDeObra, result);
            if (nuevoServicio != null) {
                JOptionPane.showMessageDialog(this, "Servicio agregado correctamente", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                actualizarTablaServicios();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar el servicio", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra diálogo para editar un cliente existente
     * 
     * @param cliente Cliente a editar
     */
    private void mostrarDialogoEditarCliente(Cliente cliente) {
        if (cliente == null)
            return;

        // Crear un panel con campos para editar el cliente
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        JTextField txtNombre = new JTextField(cliente.getNombre());
        JTextField txtApellido = new JTextField(cliente.getApellido());
        JTextField txtEmail = new JTextField(cliente.getEmail());
        JTextField txtTelefono = new JTextField(cliente.getTelefono());
        JComboBox<String> comboTipo = new JComboBox<>(new String[] { "normal", "oro" });
        comboTipo.setSelectedItem(cliente.getTipoCliente());

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Apellido:"));
        panel.add(txtApellido);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel("Teléfono:"));
        panel.add(txtTelefono);
        panel.add(new JLabel("Tipo de Cliente:"));
        panel.add(comboTipo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Cliente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                String apellido = txtApellido.getText().trim();
                String email = txtEmail.getText().trim();
                String telefono = txtTelefono.getText().trim();
                String tipoCliente = (String) comboTipo.getSelectedItem();

                if (nombre.isEmpty() || apellido.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Nombre y apellido son campos obligatorios",
                            "Datos inválidos", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Actualizar el cliente con la firma correcta del método
                boolean exito = ClienteController.actualizarCliente(
                        cliente.getIdentificador(), nombre, apellido, email, telefono, tipoCliente);

                if (exito) {
                    JOptionPane.showMessageDialog(this, "Cliente actualizado correctamente",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    actualizarTablaClientes();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el cliente",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al procesar los datos del cliente",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra diálogo para agregar un nuevo cliente
     */
    private void mostrarDialogoAgregarCliente() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        JTextField txtNombre = new JTextField();
        JTextField txtApellido = new JTextField();
        JTextField txtUsuario = new JTextField();
        JTextField txtContraseña = new JTextField();
        JComboBox<String> comboTipo = new JComboBox<>(new String[] { "normal", "oro" });

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Apellido:"));
        panel.add(txtApellido);
        panel.add(new JLabel("Usuario:"));
        panel.add(txtUsuario);
        panel.add(new JLabel("Contraseña:"));
        panel.add(txtContraseña);
        panel.add(new JLabel("Tipo de Cliente:"));
        panel.add(comboTipo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Agregar Nuevo Cliente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String usuario = txtUsuario.getText().trim();
            String contraseña = txtContraseña.getText().trim();
            String tipoCliente = (String) comboTipo.getSelectedItem();

            // Validar los campos antes de continuar
            if (!validarCamposCliente(nombre, apellido, usuario, contraseña, tipoCliente)) {
                return; // Detener si la validación falla
            }

            // Crear y agregar el cliente
            boolean exito = ClienteController.agregarCliente(nombre, apellido, usuario, contraseña, tipoCliente);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Cliente agregado correctamente", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                actualizarTablaClientes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar el cliente", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Mostrar reporte de clientes oro vs normales
     */
    private void mostrarReporteClientesOro() {
        try {
            String ruta = "reportes/reporte_clientes.pdf";
            ReporteController.generarReporteClientesPorTipo(ruta);
            JOptionPane.showMessageDialog(this, "Reporte generado: " + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al generar el reporte: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mostrar reporte de TOP 10 repuestos más usados
     */
    private void mostrarReporteRepuestosMasUsados() {
        try {
            String ruta = "reportes/reporte_repuestos_usados.pdf";
            ReporteController.generarReporteRepuestosMasUsados(ruta);
            JOptionPane.showMessageDialog(this, "Reporte generado: " + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al generar el reporte: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mostrar reporte de TOP 10 repuestos más caros
     */
    private void mostrarReporteRepuestosMasCaros() {
        try {
            String ruta = "reportes/reporte_repuestos_caros.pdf";
            ReporteController.generarReporteRepuestosMasCaros(ruta);
            JOptionPane.showMessageDialog(this, "Reporte generado: " + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al generar el reporte: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mostrar reporte de TOP 10 servicios más usados
     */
    private void mostrarReporteServiciosMasUsados() {
        try {
            String ruta = "reportes/reporte_servicios_usados.pdf";
            ReporteController.generarReporteServiciosMasUsados(ruta);
            JOptionPane.showMessageDialog(this, "Reporte generado: " + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al generar el reporte: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mostrar reporte de TOP 5 automóviles más repetidos
     */
    private void mostrarReporteAutosMasRepetidos() {
        try {
            String ruta = "reportes/reporte_autos_repetidos.pdf";
            ReporteController.generarReporteAutomovilesMasRepetidos(ruta);
            JOptionPane.showMessageDialog(this, "Reporte generado: " + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al generar el reporte: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actualiza la tabla de servicios
     */
    private void actualizarTablaServicios() {
        // Limpiar la tabla
        modeloTablaServicios.setRowCount(0);

        // Obtener todos los servicios
        Vector<Servicio> servicios = DataController.getServicios();

        // Agregar cada servicio a la tabla
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Servicio servicio : servicios) {
            double precioTotal = servicio.getPrecioTotal();
            modeloTablaServicios.addRow(new Object[] {
                    servicio.getId(),
                    servicio.getNombre(),
                    servicio.getMarca(),
                    servicio.getModelo(),
                    "Q " + df.format(servicio.getPrecioManoObra()),
                    "Q " + df.format(precioTotal)
            });
        }
    }

    /**
     * Busca servicios por nombre o descripción
     */
    private void buscarServicios(String textoBusqueda) {
        // Limpiar tabla
        modeloTablaServicios.setRowCount(0);

        // Si el texto de búsqueda está vacío, mostrar todos los servicios
        if (textoBusqueda.trim().isEmpty()) {
            actualizarTablaServicios();
            return;
        }

        // Búsqueda ignorando mayúsculas y minúsculas
        textoBusqueda = textoBusqueda.toLowerCase();

        // Obtener servicios y filtrar según texto de búsqueda
        Vector<Servicio> servicios = DataController.getServicios();
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Servicio servicio : servicios) {
            if (servicio.getNombre().toLowerCase().contains(textoBusqueda) ||
                    servicio.getDescripcion().toLowerCase().contains(textoBusqueda)) {

                modeloTablaServicios.addRow(new Object[] {
                        servicio.getId(),
                        servicio.getNombre(),
                        servicio.getDescripcion(),
                        "Q " + df.format(servicio.getPrecioBase()),
                        "Q " + df.format(servicio.getPrecioTotal()),
                        servicio.getRepuestos().size()
                });
            }
        }
    }

    /**
     * Actualiza la tabla de repuestos asociados a un servicio
     */
    private void actualizarTablaRepuestosServicio(Servicio servicio) {
        // Verificar que servicio no sea null
        if (servicio == null) {
            modeloTablaRepuestosServicio.setRowCount(0);
            return;
        }

        // Limpiar tabla
        modeloTablaRepuestosServicio.setRowCount(0);

        // Obtener repuestos del servicio - ajustar según tu modelo real
        Vector<Repuesto> repuestos;
        try {
            repuestos = servicio.getRepuestos();
        } catch (Exception e) {
            // Si getRepuestos() no existe, intentar con getRepuestosRequeridos()
            repuestos = servicio.getRepuestos();
        }

        DecimalFormat df = new DecimalFormat("#,##0.00");

        // Agregar a la tabla
        for (Repuesto repuesto : repuestos) {
            // Usar métodos existentes o valores por defecto
            String descripcion;
            try {
                descripcion = repuesto.getDescripcion();
            } catch (Exception e) {
                // Si getDescripcion() no existe, usar una combinación de otras propiedades
                descripcion = repuesto.getMarca() + " " + repuesto.getModelo();
            }

            // Obtener cantidad con seguridad
            int cantidad;
            try {
                cantidad = repuesto.getCantidad();
            } catch (Exception e) {
                cantidad = repuesto.getExistencias();
            }

            modeloTablaRepuestosServicio.addRow(new Object[] {
                    repuesto.getId(),
                    repuesto.getNombre(),
                    descripcion,
                    cantidad,
                    "Q " + df.format(repuesto.getPrecio()),
                    "Q " + df.format(repuesto.getPrecio() * cantidad)
            });
        }
    }

    /**
     * Actualiza la tabla de clientes
     */
    private void actualizarTablaClientes() {
        // Limpiar la tabla
        modeloTablaClientes.setRowCount(0);

        // Obtener todos los clientes
        Vector<Cliente> clientes = DataController.getClientes();

        // Agregar cada cliente a la tabla
        for (Cliente cliente : clientes) {
            modeloTablaClientes.addRow(new Object[] {
                    cliente.getIdentificador(),
                    cliente.getNombre(),
                    cliente.getApellido(),
                    cliente.getNombreUsuario(),
                    cliente.getTipoCliente()
            });
        }
    }

    /**
     * Busca clientes que coincidan con el texto de búsqueda
     */
    private void buscarClientes(String textoBusqueda) {
        // Limpiar la tabla
        modeloTablaClientes.setRowCount(0);

        // Obtener todos los clientes
        Vector<Cliente> clientes = DataController.getClientes();

        // Filtrar clientes según el texto de búsqueda
        for (Cliente cliente : clientes) {
            if (cliente.getNombre().toLowerCase().contains(textoBusqueda) ||
                    cliente.getApellido().toLowerCase().contains(textoBusqueda) ||
                    cliente.getNombreUsuario().toLowerCase().contains(textoBusqueda)) {

                modeloTablaClientes.addRow(new Object[] {
                        cliente.getIdentificador(),
                        cliente.getNombre(),
                        cliente.getApellido(),
                        cliente.getNombreUsuario(),
                        cliente.getTipoCliente()
                });
            }
        }
    }

    /**
     * Actualiza la tabla de automóviles asociados a un cliente
     */
    private void actualizarTablaAutosCliente(Cliente cliente) {
        // Verificar que cliente no sea null
        if (cliente == null) {
            modeloTablaAutosCliente.setRowCount(0);
            return;
        }

        // Limpiar tabla
        modeloTablaAutosCliente.setRowCount(0);

        // Obtener automóviles del cliente
        Vector<Automovil> autos = cliente.getAutomoviles();

        // Agregar a la tabla
        for (Automovil auto : autos) {
            modeloTablaAutosCliente.addRow(new Object[] {
                    auto.getPlaca(),
                    auto.getMarca(),
                    auto.getModelo(),
                    auto.getRutaFoto()
            });
        }
    }

    /**
     * Muestra los detalles de un cliente seleccionado
     */
    private void mostrarDetallesCliente(Cliente cliente) {
        if (cliente == null)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("INFORMACIÓN DEL CLIENTE\n\n");
        sb.append("Identificador: ").append(cliente.getIdentificador()).append("\n");
        sb.append("Nombre: ").append(cliente.getNombreCompleto()).append("\n");
        sb.append("Tipo: ").append(cliente.getTipoCliente()).append("\n");
        sb.append("Teléfono: ").append(cliente.getTelefono()).append("\n");
        sb.append("Email: ").append(cliente.getEmail()).append("\n");

        // Información de automóviles
        sb.append("\nAUTOMÓVILES REGISTRADOS\n");
        if (cliente.getAutomoviles().isEmpty()) {
            sb.append("No tiene automóviles registrados.");
        } else {
            for (Automovil auto : cliente.getAutomoviles()) {
                sb.append("- Placa: ").append(auto.getPlaca())
                        .append(", Marca: ").append(auto.getMarca())
                        .append(", Modelo: ").append(auto.getModelo())
                        .append("\n");
            }
        }

        // Historial de órdenes
        Vector<OrdenTrabajo> ordenes = OrdenTrabajoController.obtenerOrdenesPorCliente(cliente.getIdentificador());
        sb.append("\nHISTORIAL DE ÓRDENES\n");
        if (ordenes.isEmpty()) {
            sb.append("No tiene órdenes registradas.");
        } else {
            for (OrdenTrabajo orden : ordenes) {
                sb.append("- Orden #").append(orden.getNumero())
                        .append(", Fecha: ").append(orden.getFecha())
                        .append(", Servicio: ").append(orden.getServicio().getNombre())
                        .append(", Estado: ").append(orden.getEstado())
                        .append("\n");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Detalles de Cliente: " + cliente.getNombreCompleto(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Actualiza la tabla de facturas
     */
    private void actualizarTablaFacturas(String filtro) {
        actualizarTablaFacturas(filtro, "");
    }

    private void actualizarTablaFacturas(String filtro, String busqueda) {
        modeloTablaFacturas.setRowCount(0);

        // Obtener todas las facturas
        Vector<Factura> facturas = DataController.getFacturas();
        DecimalFormat df = new DecimalFormat("#,##0.00");

        busqueda = busqueda.toLowerCase();

        for (Factura factura : facturas) {
            // Aplicar filtro de estado
            if ("pendientes".equals(filtro) && factura.isPagada())
                continue;
            if ("pagadas".equals(filtro) && !factura.isPagada())
                continue;

            // Aplicar filtro de búsqueda
            if (!busqueda.isEmpty()) {
                boolean coincide = String.valueOf(factura.getNumero()).contains(busqueda)
                        || factura.getOrdenTrabajo().getCliente().getNombreCompleto().toLowerCase().contains(busqueda)
                        || String.valueOf(factura.getOrdenTrabajo().getNumero()).contains(busqueda)
                        || factura.getOrdenTrabajo().getServicio().getNombre().toLowerCase().contains(busqueda);

                if (!coincide)
                    continue;
            }

            modeloTablaFacturas.addRow(new Object[] {
                    "F-" + factura.getNumero(),
                    factura.getFechaEmisionFormateada(),
                    factura.getOrdenTrabajo().getCliente().getNombreCompleto(),
                    "Orden #" + factura.getOrdenTrabajo().getNumero(),
                    factura.getOrdenTrabajo().getServicio().getNombre(),
                    "Q " + df.format(factura.calcularTotal()),
                    factura.isPagada() ? "PAGADA" : "PENDIENTE"
            });
        }
    }

    private void verFacturaSeleccionada() {
        int filaSeleccionada = tablaFacturas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una factura para ver sus detalles.",
                    "Selección requerida",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Obtener número de factura
        String numeroFacturaStr = (String) tablaFacturas.getValueAt(filaSeleccionada, 0);
        int numeroFactura = Integer.parseInt(numeroFacturaStr.substring(2));

        // Buscar la factura
        Factura factura = FacturaController.buscarFacturaPorNumero(numeroFactura);
        if (factura == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo encontrar la factura seleccionada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mostrar la vista de factura
        FacturaView vista = new FacturaView(this, factura);
        vista.setVisible(true);

        // Actualizar tabla después de cerrar la vista
        String filtroActual = "todas";
        if (radioPendientes.isSelected())
            filtroActual = "pendientes";
        if (radioPagadas.isSelected())
            filtroActual = "pagadas";

        actualizarTablaFacturas(filtroActual);
    }

    private void registrarPagoFacturaSeleccionada() {
        int filaSeleccionada = tablaFacturas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una factura para registrar el pago.",
                    "Selección requerida",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Verificar si ya está pagada
        String estado = (String) tablaFacturas.getValueAt(filaSeleccionada, 6);
        if ("PAGADA".equals(estado)) {
            JOptionPane.showMessageDialog(this,
                    "Esta factura ya ha sido pagada.",
                    "Factura Pagada",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Obtener número de factura
        String numeroFacturaStr = (String) tablaFacturas.getValueAt(filaSeleccionada, 0);
        int numeroFactura = Integer.parseInt(numeroFacturaStr.substring(2));

        // Solicitar método de pago
        String[] opciones = { "Efectivo", "Tarjeta de Crédito", "Tarjeta de Débito", "Transferencia" };
        String metodoPago = (String) JOptionPane.showInputDialog(this,
                "Seleccione el método de pago:",
                "Registrar Pago",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (metodoPago == null)
            return;

        // Registrar el pago
        boolean exito = FacturaController.registrarPago(numeroFactura, metodoPago);

        if (exito) {
            JOptionPane.showMessageDialog(this,
                    "Pago registrado correctamente.",
                    "Pago Exitoso",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar tabla
            String filtroActual = "todas";
            if (radioPendientes.isSelected())
                filtroActual = "pendientes";
            if (radioPagadas.isSelected())
                filtroActual = "pagadas";

            actualizarTablaFacturas(filtroActual);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al registrar el pago.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarFacturaSeleccionada() {
        int filaSeleccionada = tablaFacturas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una factura para exportar.",
                    "Selección requerida",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Informar que esta funcionalidad está en desarrollo
        JOptionPane.showMessageDialog(this,
                "La exportación de facturas a PDF está en desarrollo.",
                "Próximamente",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Implementación del método de la interfaz ObservadorOrdenes
    @Override
    public void ordenesActualizadas(Vector<OrdenTrabajo> ordenesEspera,
            Vector<OrdenTrabajo> ordenesServicio,
            Vector<OrdenTrabajo> ordenesListas) {
        // Las actualizaciones de la UI deben hacerse en el EDT
        SwingUtilities.invokeLater(() -> {
            // Solo actualizar si la pestaña de progreso está visible
            if (tabbedPane.getSelectedComponent() == panelProgreso) {
                actualizarTablasProgreso();
            }
        });
    }

    private boolean validarCamposRepuesto(String nombre, String marca, String modelo, String existencias,
            String precio) {
        if (nombre.isEmpty() || marca.isEmpty() || modelo.isEmpty() || existencias.isEmpty() || precio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            Integer.parseInt(existencias);
            Double.parseDouble(precio);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Existencias y precio deben ser valores numéricos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean validarCamposServicio(String nombre, String marca, String modelo, String precioManoDeObra) {
        if (nombre.isEmpty() || marca.isEmpty() || modelo.isEmpty() || precioManoDeObra.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            Double.parseDouble(precioManoDeObra);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio de mano de obra debe ser un valor numérico.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean validarCamposCliente(String nombre, String apellido, String usuario, String contraseña,
            String tipoCliente) {
        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || contraseña.isEmpty()
                || tipoCliente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!tipoCliente.equals("normal") && !tipoCliente.equals("oro")) {
            JOptionPane.showMessageDialog(this, "El tipo de cliente debe ser 'normal' o 'oro'.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * Cleanup resources when the AdminView is closed.
     */
    @Override
    public void dispose() {
        super.dispose();
        HiloController.detenerTodosLosHilos(); // Detener todos los hilos al cerrar
    }
}