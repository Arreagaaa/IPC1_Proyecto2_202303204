package com.tallermecanico.views;

import com.tallermecanico.controllers.ClienteController;
import com.tallermecanico.controllers.DataController;
import com.tallermecanico.controllers.RepuestoController;
import com.tallermecanico.controllers.ServicioController;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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

    // Componentes para botones generales
    private JButton btnCerrarSesion;
    private JPanel panelBotones;
    private JButton btnRefrescar;

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

        // Añadir pestañas
        tabbedPane.addTab("Repuestos", panelRepuestos);
        tabbedPane.addTab("Servicios", panelServicios);
        tabbedPane.addTab("Clientes", panelClientes);
        tabbedPane.addTab("Reportes", panelReportes);
        tabbedPane.addTab("Progreso de Automóviles", panelProgreso);

        // Panel de botones generales
        panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefrescar = new JButton("Refrescar");
        btnCerrarSesion = new JButton("Cerrar Sesión");
        panelBotones.add(btnRefrescar);
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

        // Botón para cargar clientes desde archivo
        btnCargarClientes.addActionListener(e -> {
            int cargados = CargadorArchivos.cargarArchivoConSelector("clientes");
            if (cargados > 0) {
                actualizarTablaClientes();
                JOptionPane.showMessageDialog(this,
                        "Se cargaron " + cargados + " clientes correctamente",
                        "Carga exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
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
     * Carga los datos iniciales en las tablas
     */
    private void cargarDatos() {
        actualizarTablaRepuestos();
        actualizarTablaServicios();
        actualizarTablaClientes();
        actualizarTablasProgreso();
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
     * Muestra un diálogo para agregar un nuevo repuesto
     */
    private void mostrarDialogoAgregarRepuesto() {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Agregar Repuesto", true);
        dialogo.setSize(400, 300);
        dialogo.setLocationRelativeTo(this);

        // Panel principal
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Componentes
        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField();

        JLabel lblMarca = new JLabel("Marca:");
        JTextField txtMarca = new JTextField();

        JLabel lblModelo = new JLabel("Modelo:");
        JTextField txtModelo = new JTextField();

        JLabel lblExistencias = new JLabel("Existencias:");
        JSpinner spExistencias = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));

        JLabel lblPrecio = new JLabel("Precio (Q):");
        JSpinner spPrecio = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 99999.99, 0.01));

        // Botones
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");

        // Añadir componentes al panel
        panel.add(lblNombre);
        panel.add(txtNombre);
        panel.add(lblMarca);
        panel.add(txtMarca);
        panel.add(lblModelo);
        panel.add(txtModelo);
        panel.add(lblExistencias);
        panel.add(spExistencias);
        panel.add(lblPrecio);
        panel.add(spPrecio);
        panel.add(btnCancelar);
        panel.add(btnGuardar);

        // Configurar eventos
        btnCancelar.addActionListener(e -> dialogo.dispose());

        btnGuardar.addActionListener(e -> {
            // Validar campos
            if (txtNombre.getText().trim().isEmpty() || txtMarca.getText().trim().isEmpty()
                    || txtModelo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo,
                        "Todos los campos son obligatorios",
                        "Campos incompletos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener valores
            String nombre = txtNombre.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            int existencias = (int) spExistencias.getValue();
            double precio = (double) spPrecio.getValue();

            // Registrar repuesto
            Repuesto repuesto = RepuestoController.registrarRepuesto(nombre, marca, modelo, existencias, precio);

            if (repuesto != null) {
                JOptionPane.showMessageDialog(dialogo,
                        "Repuesto agregado correctamente",
                        "Registro exitoso",
                        JOptionPane.INFORMATION_MESSAGE);

                // Actualizar tabla
                actualizarTablaRepuestos();

                dialogo.dispose();
            } else {
                JOptionPane.showMessageDialog(dialogo,
                        "No se pudo agregar el repuesto",
                        "Error al registrar",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Mostrar diálogo
        dialogo.setContentPane(panel);
        dialogo.setVisible(true);
    }

    /**
     * Muestra un diálogo para editar un repuesto existente
     */
    private void mostrarDialogoEditarRepuesto(Repuesto repuesto) {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Editar Repuesto", true);
        dialogo.setSize(400, 300);
        dialogo.setLocationRelativeTo(this);

        // Panel principal
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Componentes
        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField(repuesto.getNombre());

        JLabel lblMarca = new JLabel("Marca:");
        JTextField txtMarca = new JTextField(repuesto.getMarca());

        JLabel lblModelo = new JLabel("Modelo:");
        JTextField txtModelo = new JTextField(repuesto.getModelo());

        JLabel lblExistencias = new JLabel("Existencias:");
        JSpinner spExistencias = new JSpinner(new SpinnerNumberModel(repuesto.getExistencias(), 0, 9999, 1));

        JLabel lblPrecio = new JLabel("Precio (Q):");
        JSpinner spPrecio = new JSpinner(new SpinnerNumberModel(repuesto.getPrecio(), 0.0, 99999.99, 0.01));

        // Botones
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");

        // Añadir componentes al panel
        panel.add(lblNombre);
        panel.add(txtNombre);
        panel.add(lblMarca);
        panel.add(txtMarca);
        panel.add(lblModelo);
        panel.add(txtModelo);
        panel.add(lblExistencias);
        panel.add(spExistencias);
        panel.add(lblPrecio);
        panel.add(spPrecio);
        panel.add(btnCancelar);
        panel.add(btnGuardar);

        // Configurar eventos
        btnCancelar.addActionListener(e -> dialogo.dispose());

        btnGuardar.addActionListener(e -> {
            // Validar campos
            if (txtNombre.getText().trim().isEmpty() || txtMarca.getText().trim().isEmpty()
                    || txtModelo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo,
                        "Todos los campos son obligatorios",
                        "Campos incompletos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener valores
            String nombre = txtNombre.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            int existencias = (int) spExistencias.getValue();
            double precio = (double) spPrecio.getValue();

            // Actualizar repuesto
            repuesto.setNombre(nombre);
            repuesto.setMarca(marca);
            repuesto.setModelo(modelo);
            repuesto.setExistencias(existencias);
            repuesto.setPrecio(precio);

            // Guardar cambios
            DataController.guardarDatos();

            JOptionPane.showMessageDialog(dialogo,
                    "Repuesto actualizado correctamente",
                    "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar tabla
            actualizarTablaRepuestos();

            dialogo.dispose();
        });

        // Mostrar diálogo
        dialogo.setContentPane(panel);
        dialogo.setVisible(true);
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
     * Busca servicios que coincidan con el texto de búsqueda
     */
    private void buscarServicios(String textoBusqueda) {
        // Limpiar la tabla
        modeloTablaServicios.setRowCount(0);

        // Obtener todos los servicios
        Vector<Servicio> servicios = DataController.getServicios();

        // Filtrar servicios según el texto de búsqueda
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Servicio servicio : servicios) {
            if (servicio.getNombre().toLowerCase().contains(textoBusqueda) ||
                    servicio.getMarca().toLowerCase().contains(textoBusqueda) ||
                    servicio.getModelo().toLowerCase().contains(textoBusqueda)) {

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
    }

    /**
     * Actualiza la tabla de repuestos de un servicio
     */
    private void actualizarTablaRepuestosServicio(Servicio servicio) {
        // Limpiar la tabla
        modeloTablaRepuestosServicio.setRowCount(0);

        // Obtener repuestos del servicio
        Vector<Repuesto> repuestos = servicio.getRepuestos();

        // Agregar cada repuesto a la tabla
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Repuesto repuesto : repuestos) {
            modeloTablaRepuestosServicio.addRow(new Object[] {
                    repuesto.getId(),
                    repuesto.getNombre(),
                    repuesto.getMarca(),
                    repuesto.getModelo(),
                    "Q " + df.format(repuesto.getPrecio())
            });
        }
    }

    /**
     * Muestra un diálogo para agregar un nuevo servicio
     */
    private void mostrarDialogoAgregarServicio() {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Agregar Servicio", true);
        dialogo.setSize(500, 400);
        dialogo.setLocationRelativeTo(this);

        // Panel principal con layout de borde
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para datos básicos
        JPanel panelDatos = new JPanel(new GridLayout(4, 2, 5, 5));

        // Componentes para datos básicos
        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField();

        JLabel lblMarca = new JLabel("Marca:");
        JTextField txtMarca = new JTextField();

        JLabel lblModelo = new JLabel("Modelo:");
        JTextField txtModelo = new JTextField();

        JLabel lblPrecioManoObra = new JLabel("Precio Mano de Obra (Q):");
        JSpinner spPrecioManoObra = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 99999.99, 0.01));

        // Añadir componentes al panel de datos
        panelDatos.add(lblNombre);
        panelDatos.add(txtNombre);
        panelDatos.add(lblMarca);
        panelDatos.add(txtMarca);
        panelDatos.add(lblModelo);
        panelDatos.add(txtModelo);
        panelDatos.add(lblPrecioManoObra);
        panelDatos.add(spPrecioManoObra);

        // Panel para repuestos
        JPanel panelRepuestos = new JPanel(new BorderLayout(5, 5));
        panelRepuestos.setBorder(BorderFactory.createTitledBorder("Seleccionar Repuestos"));

        // Tabla de repuestos disponibles
        String[] columnasRepuestos = { "ID", "Nombre", "Marca", "Modelo", "Precio" };
        DefaultTableModel modeloTablaRepuestosDisp = new DefaultTableModel(columnasRepuestos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        JTable tablaRepuestosDisp = new JTable(modeloTablaRepuestosDisp);
        JScrollPane scrollRepuestos = new JScrollPane(tablaRepuestosDisp);
        panelRepuestos.add(scrollRepuestos, BorderLayout.CENTER);

        // Cargar repuestos disponibles
        DecimalFormat df = new DecimalFormat("#,##0.00");
        Vector<Repuesto> repuestosDisponibles = DataController.getRepuestos();

        for (Repuesto repuesto : repuestosDisponibles) {
            modeloTablaRepuestosDisp.addRow(new Object[] {
                    repuesto.getId(),
                    repuesto.getNombre(),
                    repuesto.getMarca(),
                    repuesto.getModelo(),
                    "Q " + df.format(repuesto.getPrecio())
            });
        }

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        // Añadir paneles al panel principal
        panelPrincipal.add(panelDatos, BorderLayout.NORTH);
        panelPrincipal.add(panelRepuestos, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        // Configurar eventos
        btnCancelar.addActionListener(e -> dialogo.dispose());

        btnGuardar.addActionListener(e -> {
            // Validar campos
            if (txtNombre.getText().trim().isEmpty() || txtMarca.getText().trim().isEmpty()
                    || txtModelo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo,
                        "Los campos Nombre, Marca y Modelo son obligatorios",
                        "Campos incompletos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener valores
            String nombre = txtNombre.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            double precioManoObra = (double) spPrecioManoObra.getValue();

            // Crear servicio
            Servicio servicio = ServicioController.registrarServicio(nombre, marca, modelo, precioManoObra);

            if (servicio != null) {
                // Agregar repuestos seleccionados
                int[] filasSeleccionadas = tablaRepuestosDisp.getSelectedRows();
                if (filasSeleccionadas.length > 0) {
                    for (int fila : filasSeleccionadas) {
                        int idRepuesto = (int) tablaRepuestosDisp.getValueAt(fila, 0);
                        ServicioController.agregarRepuestoAServicio(servicio.getId(), idRepuesto);
                    }
                }

                JOptionPane.showMessageDialog(dialogo,
                        "Servicio agregado correctamente",
                        "Registro exitoso",
                        JOptionPane.INFORMATION_MESSAGE);

                // Actualizar tabla
                actualizarTablaServicios();

                dialogo.dispose();
            } else {
                JOptionPane.showMessageDialog(dialogo,
                        "No se pudo agregar el servicio",
                        "Error al registrar",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Mostrar diálogo
        dialogo.setContentPane(panelPrincipal);
        dialogo.setVisible(true);
    }

    /**
     * Muestra un diálogo para editar un servicio existente
     */
    private void mostrarDialogoEditarServicio(Servicio servicio) {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Editar Servicio", true);
        dialogo.setSize(500, 400);
        dialogo.setLocationRelativeTo(this);

        // Panel principal con layout de borde
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para datos básicos
        JPanel panelDatos = new JPanel(new GridLayout(4, 2, 5, 5));

        // Componentes para datos básicos
        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField(servicio.getNombre());

        JLabel lblMarca = new JLabel("Marca:");
        JTextField txtMarca = new JTextField(servicio.getMarca());

        JLabel lblModelo = new JLabel("Modelo:");
        JTextField txtModelo = new JTextField(servicio.getModelo());

        JLabel lblPrecioManoObra = new JLabel("Precio Mano de Obra (Q):");
        JSpinner spPrecioManoObra = new JSpinner(
                new SpinnerNumberModel(servicio.getPrecioManoObra(), 0.0, 99999.99, 0.01));

        // Añadir componentes al panel de datos
        panelDatos.add(lblNombre);
        panelDatos.add(txtNombre);
        panelDatos.add(lblMarca);
        panelDatos.add(txtMarca);
        panelDatos.add(lblModelo);
        panelDatos.add(txtModelo);
        panelDatos.add(lblPrecioManoObra);
        panelDatos.add(spPrecioManoObra);

        // Panel con pestañas para gestionar repuestos
        JTabbedPane pestanasRepuestos = new JTabbedPane();

        // Panel para repuestos actuales
        JPanel panelRepuestosActuales = new JPanel(new BorderLayout(5, 5));

        // Tabla de repuestos actuales
        String[] columnasRepuestos = { "ID", "Nombre", "Marca", "Modelo", "Precio" };
        DefaultTableModel modeloTablaRepuestosAct = new DefaultTableModel(columnasRepuestos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        JTable tablaRepuestosAct = new JTable(modeloTablaRepuestosAct);
        JScrollPane scrollRepuestosAct = new JScrollPane(tablaRepuestosAct);
        panelRepuestosActuales.add(scrollRepuestosAct, BorderLayout.CENTER);

        // Botón para eliminar repuesto de servicio
        JButton btnEliminarRepuesto = new JButton("Eliminar Repuesto Seleccionado");
        JPanel panelBotonEliminar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonEliminar.add(btnEliminarRepuesto);
        panelRepuestosActuales.add(panelBotonEliminar, BorderLayout.SOUTH);

        // Panel para agregar repuestos
        JPanel panelAgregarRepuestos = new JPanel(new BorderLayout(5, 5));

        // Tabla de repuestos disponibles
        DefaultTableModel modeloTablaRepuestosDisp = new DefaultTableModel(columnasRepuestos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        JTable tablaRepuestosDisp = new JTable(modeloTablaRepuestosDisp);
        JScrollPane scrollRepuestosDisp = new JScrollPane(tablaRepuestosDisp);
        panelAgregarRepuestos.add(scrollRepuestosDisp, BorderLayout.CENTER);

        // Botón para agregar repuesto al servicio
        JButton btnAgregarRepuesto = new JButton("Agregar Repuesto Seleccionado");
        JPanel panelBotonAgregar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonAgregar.add(btnAgregarRepuesto);
        panelAgregarRepuestos.add(panelBotonAgregar, BorderLayout.SOUTH);

        // Añadir pestañas
        pestanasRepuestos.addTab("Repuestos Actuales", panelRepuestosActuales);
        pestanasRepuestos.addTab("Agregar Repuestos", panelAgregarRepuestos);

        // Cargar repuestos actuales
        DecimalFormat df = new DecimalFormat("#,##0.00");
        Vector<Repuesto> repuestosActuales = servicio.getRepuestos();

        for (Repuesto repuesto : repuestosActuales) {
            modeloTablaRepuestosAct.addRow(new Object[] {
                    repuesto.getId(),
                    repuesto.getNombre(),
                    repuesto.getMarca(),
                    repuesto.getModelo(),
                    "Q " + df.format(repuesto.getPrecio())
            });
        }

        // Cargar repuestos disponibles (que no estén en el servicio)
        Vector<Repuesto> repuestosDisponibles = new Vector<>();
        for (Repuesto repuesto : DataController.getRepuestos()) {
            boolean yaIncluido = false;
            for (Repuesto r : repuestosActuales) {
                if (r.getId() == repuesto.getId()) {
                    yaIncluido = true;
                    break;
                }
            }
            if (!yaIncluido) {
                repuestosDisponibles.add(repuesto);
            }
        }

        for (Repuesto repuesto : repuestosDisponibles) {
            modeloTablaRepuestosDisp.addRow(new Object[] {
                    repuesto.getId(),
                    repuesto.getNombre(),
                    repuesto.getMarca(),
                    repuesto.getModelo(),
                    "Q " + df.format(repuesto.getPrecio())
            });
        }

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        // Añadir paneles al panel principal
        panelPrincipal.add(panelDatos, BorderLayout.NORTH);
        panelPrincipal.add(pestanasRepuestos, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        // Configurar eventos
        btnEliminarRepuesto.addActionListener(e -> {
            int filaSeleccionada = tablaRepuestosAct.getSelectedRow();
            if (filaSeleccionada != -1) {
                int idRepuesto = (int) tablaRepuestosAct.getValueAt(filaSeleccionada, 0);
                if (ServicioController.quitarRepuestoDeServicio(servicio.getId(), idRepuesto)) {
                    // Actualizar tablas
                    modeloTablaRepuestosAct.removeRow(filaSeleccionada);

                    // Agregar a disponibles
                    Repuesto repuesto = RepuestoController.buscarRepuestoPorId(idRepuesto);
                    if (repuesto != null) {
                        modeloTablaRepuestosDisp.addRow(new Object[] {
                                repuesto.getId(),
                                repuesto.getNombre(),
                                repuesto.getMarca(),
                                repuesto.getModelo(),
                                "Q " + df.format(repuesto.getPrecio())
                        });
                    }
                }
            } else {
                JOptionPane.showMessageDialog(dialogo,
                        "Debe seleccionar un repuesto para eliminar",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        btnAgregarRepuesto.addActionListener(e -> {
            int filaSeleccionada = tablaRepuestosDisp.getSelectedRow();
            if (filaSeleccionada != -1) {
                int idRepuesto = (int) tablaRepuestosDisp.getValueAt(filaSeleccionada, 0);
                if (ServicioController.agregarRepuestoAServicio(servicio.getId(), idRepuesto)) {
                    // Actualizar tablas
                    Repuesto repuesto = RepuestoController.buscarRepuestoPorId(idRepuesto);
                    if (repuesto != null) {
                        modeloTablaRepuestosAct.addRow(new Object[] {
                                repuesto.getId(),
                                repuesto.getNombre(),
                                repuesto.getMarca(),
                                repuesto.getModelo(),
                                "Q " + df.format(repuesto.getPrecio())
                        });
                    }

                    // Eliminar de disponibles
                    modeloTablaRepuestosDisp.removeRow(filaSeleccionada);
                }
            } else {
                JOptionPane.showMessageDialog(dialogo,
                        "Debe seleccionar un repuesto para agregar",
                        "Selección requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());

        btnGuardar.addActionListener(e -> {
            // Validar campos
            if (txtNombre.getText().trim().isEmpty() || txtMarca.getText().trim().isEmpty()
                    || txtModelo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo,
                        "Los campos Nombre, Marca y Modelo son obligatorios",
                        "Campos incompletos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener valores
            String nombre = txtNombre.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            double precioManoObra = (double) spPrecioManoObra.getValue();

            // Actualizar servicio
            servicio.setNombre(nombre);
            servicio.setMarca(marca);
            servicio.setModelo(modelo);
            servicio.setPrecioManoObra(precioManoObra);

            // Guardar cambios
            DataController.guardarDatos();

            JOptionPane.showMessageDialog(dialogo,
                    "Servicio actualizado correctamente",
                    "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar tabla
            actualizarTablaServicios();

            dialogo.dispose();
        });

        // Mostrar diálogo
        dialogo.setContentPane(panelPrincipal);
        dialogo.setVisible(true);
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
            if (cliente.getIdentificador().toLowerCase().contains(textoBusqueda) ||
                    cliente.getNombre().toLowerCase().contains(textoBusqueda) ||
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
     * Actualiza la tabla de automóviles de un cliente
     */
    private void actualizarTablaAutosCliente(Cliente cliente) {
        // Limpiar la tabla
        modeloTablaAutosCliente.setRowCount(0);

        // Obtener automóviles del cliente
        Vector<Automovil> automoviles = cliente.getAutomoviles();

        // Agregar cada automóvil a la tabla
        for (Automovil auto : automoviles) {
            modeloTablaAutosCliente.addRow(new Object[] {
                    auto.getPlaca(),
                    auto.getMarca(),
                    auto.getModelo(),
                    auto.getRutaFoto().isEmpty() ? "No" : "Sí"
            });
        }
    }

    /**
     * Muestra un diálogo con los detalles de un cliente
     */
    private void mostrarDetallesCliente(Cliente cliente) {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Detalles del Cliente", true);
        dialogo.setSize(600, 500);
        dialogo.setLocationRelativeTo(this);

        // Panel principal con layout de borde
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de información general
        JPanel panelInfo = new JPanel(new GridLayout(5, 2, 5, 5));
        panelInfo.setBorder(BorderFactory.createTitledBorder("Información General"));

        // Agregar etiquetas con información
        JLabel lblDPI = new JLabel("DPI/CUI:");
        JLabel lblDPIValor = new JLabel(cliente.getIdentificador());

        JLabel lblNombre = new JLabel("Nombre:");
        JLabel lblNombreValor = new JLabel(cliente.getNombre());

        JLabel lblApellido = new JLabel("Apellido:");
        JLabel lblApellidoValor = new JLabel(cliente.getApellido());

        JLabel lblUsuario = new JLabel("Usuario:");
        JLabel lblUsuarioValor = new JLabel(cliente.getNombreUsuario());

        JLabel lblTipo = new JLabel("Tipo de Cliente:");
        JLabel lblTipoValor = new JLabel(cliente.getTipoCliente().equals("oro") ? "Oro" : "Normal");
        lblTipoValor.setForeground(cliente.getTipoCliente().equals("oro") ? Color.ORANGE.darker() : Color.BLACK);

        // Añadir componentes al panel de información
        panelInfo.add(lblDPI);
        panelInfo.add(lblDPIValor);
        panelInfo.add(lblNombre);
        panelInfo.add(lblNombreValor);
        panelInfo.add(lblApellido);
        panelInfo.add(lblApellidoValor);
        panelInfo.add(lblUsuario);
        panelInfo.add(lblUsuarioValor);
        panelInfo.add(lblTipo);
        panelInfo.add(lblTipoValor);

        // Panel de automóviles
        JPanel panelAutos = new JPanel(new BorderLayout(5, 5));
        panelAutos.setBorder(BorderFactory.createTitledBorder("Automóviles"));

        // Tabla de automóviles
        String[] columnasAutos = { "Placa", "Marca", "Modelo", "Tiene Foto" };
        DefaultTableModel modeloAutos = new DefaultTableModel(columnasAutos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaAutos = new JTable(modeloAutos);
        JScrollPane scrollAutos = new JScrollPane(tablaAutos);
        panelAutos.add(scrollAutos, BorderLayout.CENTER);

        // Cargar automóviles
        Vector<Automovil> automoviles = cliente.getAutomoviles();
        for (Automovil auto : automoviles) {
            modeloAutos.addRow(new Object[] {
                    auto.getPlaca(),
                    auto.getMarca(),
                    auto.getModelo(),
                    auto.getRutaFoto().isEmpty() ? "No" : "Sí"
            });
        }

        // Panel para visualizar la foto del automóvil seleccionado
        JPanel panelFoto = new JPanel(new BorderLayout(5, 5));
        panelFoto.setBorder(BorderFactory.createTitledBorder("Foto del Automóvil"));

        // Etiqueta para mostrar la foto
        JLabel lblFoto = new JLabel("Seleccione un automóvil para ver su foto", JLabel.CENTER);
        panelFoto.add(lblFoto, BorderLayout.CENTER);

        // Evento para mostrar la foto al seleccionar un automóvil
        tablaAutos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int filaSeleccionada = tablaAutos.getSelectedRow();
                    if (filaSeleccionada != -1) {
                        String placa = (String) tablaAutos.getValueAt(filaSeleccionada, 0);
                        Automovil auto = null;

                        // Buscar el automóvil
                        for (Automovil a : automoviles) {
                            if (a.getPlaca().equals(placa)) {
                                auto = a;
                                break;
                            }
                        }

                        if (auto != null && !auto.getRutaFoto().isEmpty()) {
                            // Cargar y mostrar la imagen
                            try {
                                File archivo = new File(auto.getRutaFoto());
                                if (archivo.exists()) {
                                    ImageIcon icono = new ImageIcon(archivo.getAbsolutePath());
                                    // Redimensionar la imagen
                                    Image img = icono.getImage();
                                    Image imgEscalada = img.getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                                    lblFoto.setIcon(new ImageIcon(imgEscalada));
                                    lblFoto.setText("");
                                } else {
                                    lblFoto.setIcon(null);
                                    lblFoto.setText("No se encontró la imagen");
                                }
                            } catch (Exception ex) {
                                lblFoto.setIcon(null);
                                lblFoto.setText("Error al cargar la imagen");
                            }
                        } else {
                            lblFoto.setIcon(null);
                            lblFoto.setText("Este automóvil no tiene foto");
                        }
                    }
                }
            }
        });

        // Botón para cerrar
        JButton btnCerrar = new JButton("Cerrar");
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(btnCerrar);

        // Configurar evento para cerrar
        btnCerrar.addActionListener(e -> dialogo.dispose());

        // Panel dividido para mostrar automóviles y foto
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelAutos, panelFoto);
        splitPane.setResizeWeight(0.6); // 60% para automóviles, 40% para foto

        // Añadir componentes al panel principal
        panelPrincipal.add(panelInfo, BorderLayout.NORTH);
        panelPrincipal.add(splitPane, BorderLayout.CENTER);
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        // Mostrar diálogo
        dialogo.setContentPane(panelPrincipal);
        dialogo.setVisible(true);
    }

    /**
     * Muestra un diálogo para editar un cliente existente
     */
    private void mostrarDialogoEditarCliente(Cliente cliente) {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Editar Cliente", true);
        dialogo.setSize(500, 300);
        dialogo.setLocationRelativeTo(this);

        // Panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para datos del cliente
        JPanel panelDatos = new JPanel(new GridLayout(6, 2, 5, 5));

        // Componentes para datos del cliente
        JLabel lblDPI = new JLabel("DPI/CUI:");
        JTextField txtDPI = new JTextField(cliente.getIdentificador());
        txtDPI.setEditable(false); // No permitir cambiar el DPI

        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField(cliente.getNombre());

        JLabel lblApellido = new JLabel("Apellido:");
        JTextField txtApellido = new JTextField(cliente.getApellido());

        JLabel lblUsuario = new JLabel("Usuario:");
        JTextField txtUsuario = new JTextField(cliente.getNombreUsuario());

        JLabel lblPassword = new JLabel("Contraseña:");
        JPasswordField txtPassword = new JPasswordField();
        txtPassword.setToolTipText("Dejar en blanco para mantener la contraseña actual");

        JLabel lblTipo = new JLabel("Tipo de Cliente:");
        String[] tiposCliente = { "Normal", "Oro" };
        JComboBox<String> comboTipo = new JComboBox<>(tiposCliente);
        comboTipo.setSelectedIndex(cliente.getTipoCliente().equals("oro") ? 1 : 0);

        // Añadir componentes al panel de datos
        panelDatos.add(lblDPI);
        panelDatos.add(txtDPI);
        panelDatos.add(lblNombre);
        panelDatos.add(txtNombre);
        panelDatos.add(lblApellido);
        panelDatos.add(txtApellido);
        panelDatos.add(lblUsuario);
        panelDatos.add(txtUsuario);
        panelDatos.add(lblPassword);
        panelDatos.add(txtPassword);
        panelDatos.add(lblTipo);
        panelDatos.add(comboTipo);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        // Añadir paneles al panel principal
        panel.add(panelDatos, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Configurar eventos
        btnCancelar.addActionListener(e -> dialogo.dispose());

        btnGuardar.addActionListener(e -> {
            // Validar campos
            if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()
                    || txtUsuario.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo,
                        "Los campos Nombre, Apellido y Usuario son obligatorios",
                        "Campos incompletos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener valores
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String usuario = txtUsuario.getText().trim();
            String password = new String(txtPassword.getPassword());
            String tipo = comboTipo.getSelectedIndex() == 1 ? "oro" : "normal";

            // Actualizar cliente
            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setNombreUsuario(usuario);
            if (!password.isEmpty()) {
                cliente.setContrasena(password);
            }
            cliente.setTipoCliente(tipo);

            // Guardar cambios
            DataController.guardarDatos();

            JOptionPane.showMessageDialog(dialogo,
                    "Cliente actualizado correctamente",
                    "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar tabla
            actualizarTablaClientes();

            dialogo.dispose();
        });

        // Mostrar diálogo
        dialogo.setContentPane(panel);
        dialogo.setVisible(true);
    }

    /**
     * Actualiza las tablas del panel de progreso de automóviles
     */
    private void actualizarTablasProgreso() {
        // Limpiar todas las tablas
        modeloTablaEspera.setRowCount(0);
        modeloTablaEnServicio.setRowCount(0);
        modeloTablaListos.setRowCount(0);
        modeloTablaPendientesPago.setRowCount(0);

        // Obtener listas de órdenes
        Vector<OrdenTrabajo> ordenesEspera = DataController.getColaEspera();
        Vector<OrdenTrabajo> ordenesServicio = new Vector<>();
        Vector<OrdenTrabajo> ordenesListas = new Vector<>();
        Vector<OrdenTrabajo> ordenesPendientesPago = new Vector<>();

        // Clasificar órdenes según su estado
        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if ("espera".equals(orden.getEstado())) {
                // Ya tenemos la lista de espera de DataController
            } else if ("en_servicio".equals(orden.getEstado())) {
                ordenesServicio.add(orden);
            } else if ("listo".equals(orden.getEstado())) {
                ordenesListas.add(orden);

                // Si no está pagada, agregar a pendientes de pago
                if (!orden.isPagado()) {
                    ordenesPendientesPago.add(orden);
                }
            }
        }

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
                    orden.getMecanico().getNombreCompleto(),
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
                    orden.getMecanico().getNombreCompleto(),
                    orden.getFecha()
            });
        }

        // Actualizar tabla de facturas pendientes de pago
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (OrdenTrabajo orden : ordenesPendientesPago) {
            double total = orden.getServicio().getPrecioTotal();

            modeloTablaPendientesPago.addRow(new Object[] {
                    "F-" + orden.getNumero(), // Simulación de número de factura
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca() + " "
                            + orden.getAutomovil().getModelo(),
                    orden.getServicio().getNombre(),
                    "Q " + df.format(total),
                    orden.getFecha()
            });
        }
    }

    /**
     * Muestra el reporte de clientes oro vs normales
     */
    private void mostrarReporteClientesOro() {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Reporte: Clientes Oro vs Normales", true);
        dialogo.setSize(700, 500);
        dialogo.setLocationRelativeTo(this);

        // Panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del reporte
        JLabel lblTitulo = new JLabel("Reporte: Clientes Oro vs Normales", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Panel para gráfica y datos
        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 10, 10));

        // Panel para gráfica
        JPanel panelGrafica = new JPanel(new BorderLayout());
        panelGrafica.setBorder(BorderFactory.createTitledBorder("Distribución"));

        // Contar clientes por tipo
        int clientesNormales = 0;
        int clientesOro = 0;

        for (Cliente cliente : DataController.getClientes()) {
            if (cliente.getTipoCliente().equals("oro")) {
                clientesOro++;
            } else {
                clientesNormales++;
            }
        }

        // Crear gráfico de pastel simple
        final int normal = clientesNormales;
        final int oro = clientesOro;

        JPanel grafico = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Configurar antialising
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Calcular total y ángulos
                int total = normal + oro;
                int anguloNormal = total > 0 ? (normal * 360) / total : 0;

                // Dibujar gráfico
                int ancho = getWidth() - 40;
                int alto = getHeight() - 40;
                int x = 20;
                int y = 20;

                // Dibujar sector para clientes normales (azul)
                g2d.setColor(new Color(100, 149, 237)); // Azul acero claro
                g2d.fillArc(x, y, ancho, alto, 0, anguloNormal);

                // Dibujar sector para clientes oro (dorado)
                g2d.setColor(new Color(218, 165, 32)); // Dorado
                g2d.fillArc(x, y, ancho, alto, anguloNormal, 360 - anguloNormal);

                // Dibujar leyenda
                g2d.setColor(Color.BLACK);
                g2d.drawString("Azul: Clientes Normales", 20, getHeight() - 15);
                g2d.drawString("Dorado: Clientes Oro", getWidth() / 2, getHeight() - 15);
            }
        };

        panelGrafica.add(grafico, BorderLayout.CENTER);

        // Panel para datos
        JPanel panelDatos = new JPanel(new BorderLayout());
        panelDatos.setBorder(BorderFactory.createTitledBorder("Estadísticas"));

        // Tabla con estadísticas
        String[] columnas = { "Tipo de Cliente", "Cantidad", "Porcentaje" };
        DefaultTableModel modeloEstadisticas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaEstadisticas = new JTable(modeloEstadisticas);
        JScrollPane scrollEstadisticas = new JScrollPane(tablaEstadisticas);
        panelDatos.add(scrollEstadisticas, BorderLayout.CENTER);

        // Calcular porcentajes
        int total = clientesNormales + clientesOro;
        double porcentajeNormal = total > 0 ? (clientesNormales * 100.0) / total : 0;
        double porcentajeOro = total > 0 ? (clientesOro * 100.0) / total : 0;

        // Agregar datos a la tabla
        DecimalFormat df = new DecimalFormat("#,##0.00");
        modeloEstadisticas.addRow(new Object[] { "Normal", clientesNormales, df.format(porcentajeNormal) + "%" });
        modeloEstadisticas.addRow(new Object[] { "Oro", clientesOro, df.format(porcentajeOro) + "%" });
        modeloEstadisticas.addRow(new Object[] { "Total", total, "100.00%" });

        // Añadir paneles al panel centro
        panelCentro.add(panelGrafica);
        panelCentro.add(panelDatos);

        // Panel para botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportar = new JButton("Exportar a PDF");
        JButton btnCerrar = new JButton("Cerrar");

        panelBotones.add(btnExportar);
        panelBotones.add(btnCerrar);

        // Configurar eventos
        btnExportar.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialogo,
                    "Funcionalidad de exportación a PDF en desarrollo",
                    "Próximamente",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnCerrar.addActionListener(e -> dialogo.dispose());

        // Añadir paneles al panel principal
        panel.add(panelCentro, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Mostrar diálogo
        dialogo.setContentPane(panel);
        dialogo.setVisible(true);
    }

    /**
     * Muestra el reporte de TOP 10 repuestos más usados
     */
    private void mostrarReporteRepuestosMasUsados() {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Reporte: TOP 10 Repuestos Más Usados", true);
        dialogo.setSize(700, 500);
        dialogo.setLocationRelativeTo(this);

        // Panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del reporte
        JLabel lblTitulo = new JLabel("Reporte: TOP 10 Repuestos Más Usados", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Tabla con los repuestos más usados
        String[] columnas = { "Posición", "ID", "Nombre", "Marca", "Modelo", "Veces Usado" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaRepuestos = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaRepuestos);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Calcular repuestos más usados (implementación básica)
        Vector<Repuesto> repuestos = DataController.getRepuestos();
        Vector<Vector<Object>> estadisticasRepuestos = new Vector<>();

        // Contar usos de cada repuesto
        for (Repuesto repuesto : repuestos) {
            int vecesUsado = contarUsosRepuesto(repuesto.getId());

            Vector<Object> fila = new Vector<>();
            fila.add(0); // Posición (se asignará después)
            fila.add(repuesto.getId());
            fila.add(repuesto.getNombre());
            fila.add(repuesto.getMarca());
            fila.add(repuesto.getModelo());
            fila.add(vecesUsado);

            estadisticasRepuestos.add(fila);
        }

        // Ordenar por veces usado (de mayor a menor)
        estadisticasRepuestos.sort((v1, v2) -> {
            Integer usos1 = (Integer) v1.get(5);
            Integer usos2 = (Integer) v2.get(5);
            return usos2.compareTo(usos1);
        });

        // Asignar posiciones y añadir a la tabla (TOP 10)
        int limite = Math.min(10, estadisticasRepuestos.size());
        for (int i = 0; i < limite; i++) {
            Vector<Object> fila = estadisticasRepuestos.get(i);
            fila.set(0, i + 1); // Asignar posición

            modeloTabla.addRow(new Object[] {
                    fila.get(0),
                    fila.get(1),
                    fila.get(2),
                    fila.get(3),
                    fila.get(4),
                    fila.get(5)
            });
        }

        // Panel para botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportar = new JButton("Exportar a PDF");
        JButton btnCerrar = new JButton("Cerrar");

        panelBotones.add(btnExportar);
        panelBotones.add(btnCerrar);

        // Configurar eventos
        btnExportar.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialogo,
                    "Funcionalidad de exportación a PDF en desarrollo",
                    "Próximamente",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnCerrar.addActionListener(e -> dialogo.dispose());

        // Añadir panel de botones
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Mostrar diálogo
        dialogo.setContentPane(panel);
        dialogo.setVisible(true);
    }

    /**
     * Método auxiliar para contar usos de un repuesto
     */
    private int contarUsosRepuesto(int idRepuesto) {
        int contador = 0;

        // Contar en servicios completados
        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if ("listo".equals(orden.getEstado()) || "pagado".equals(orden.getEstado())) {
                Servicio servicio = orden.getServicio();
                if (servicio != null) {
                    for (Repuesto repuesto : servicio.getRepuestos()) {
                        if (repuesto.getId() == idRepuesto) {
                            contador++;
                            break; // Contar solo una vez por servicio
                        }
                    }
                }
            }
        }

        return contador;
    }

    /**
     * Muestra el reporte de TOP 10 repuestos más caros
     */
    private void mostrarReporteRepuestosMasCaros() {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Reporte: TOP 10 Repuestos Más Caros", true);
        dialogo.setSize(700, 500);
        dialogo.setLocationRelativeTo(this);

        // Panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del reporte
        JLabel lblTitulo = new JLabel("Reporte: TOP 10 Repuestos Más Caros", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Tabla con los repuestos más caros
        String[] columnas = { "Posición", "ID", "Nombre", "Marca", "Modelo", "Precio" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaRepuestos = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaRepuestos);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Ordenar repuestos por precio (de mayor a menor)
        Vector<Repuesto> repuestos = new Vector<>(DataController.getRepuestos());
        repuestos.sort((r1, r2) -> Double.compare(r2.getPrecio(), r1.getPrecio()));

        // Agregar a la tabla (TOP 10)
        DecimalFormat df = new DecimalFormat("#,##0.00");
        int limite = Math.min(10, repuestos.size());

        for (int i = 0; i < limite; i++) {
            Repuesto repuesto = repuestos.get(i);
            modeloTabla.addRow(new Object[] {
                    i + 1, // Posición
                    repuesto.getId(),
                    repuesto.getNombre(),
                    repuesto.getMarca(),
                    repuesto.getModelo(),
                    "Q " + df.format(repuesto.getPrecio())
            });
        }

        // Panel para botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportar = new JButton("Exportar a PDF");
        JButton btnCerrar = new JButton("Cerrar");

        panelBotones.add(btnExportar);
        panelBotones.add(btnCerrar);

        // Configurar eventos
        btnExportar.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialogo,
                    "Funcionalidad de exportación a PDF en desarrollo",
                    "Próximamente",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnCerrar.addActionListener(e -> dialogo.dispose());

        // Añadir panel de botones
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Mostrar diálogo
        dialogo.setContentPane(panel);
        dialogo.setVisible(true);
    }

    /**
     * Muestra el reporte de TOP 10 servicios más usados
     */
    private void mostrarReporteServiciosMasUsados() {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Reporte: TOP 10 Servicios Más Usados", true);
        dialogo.setSize(700, 500);
        dialogo.setLocationRelativeTo(this);

        // Panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del reporte
        JLabel lblTitulo = new JLabel("Reporte: TOP 10 Servicios Más Usados", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Tabla con los servicios más usados
        String[] columnas = { "Posición", "ID", "Nombre", "Marca", "Modelo", "Veces Usado" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaServicios = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaServicios);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Calcular servicios más usados
        Vector<Servicio> servicios = DataController.getServicios();
        Vector<Object[]> estadisticasServicios = new Vector<>();

        // Contar usos de cada servicio
        for (Servicio servicio : servicios) {
            int vecesUsado = contarUsosServicio(servicio.getId());

            estadisticasServicios.add(new Object[] {
                    0, // Posición (se asignará después)
                    servicio.getId(),
                    servicio.getNombre(),
                    servicio.getMarca(),
                    servicio.getModelo(),
                    vecesUsado
            });
        }

        // Ordenar por veces usado (de mayor a menor)
        estadisticasServicios.sort((s1, s2) -> {
            Integer usos1 = (Integer) s1[5];
            Integer usos2 = (Integer) s2[5];
            return usos2.compareTo(usos1);
        });

        // Asignar posiciones y añadir a la tabla (TOP 10)
        int limite = Math.min(10, estadisticasServicios.size());
        for (int i = 0; i < limite; i++) {
            Object[] fila = estadisticasServicios.get(i);
            fila[0] = i + 1; // Asignar posición

            modeloTabla.addRow(fila);
        }

        // Panel para botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportar = new JButton("Exportar a PDF");
        JButton btnCerrar = new JButton("Cerrar");

        panelBotones.add(btnExportar);
        panelBotones.add(btnCerrar);

        // Configurar eventos
        btnExportar.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialogo,
                    "Funcionalidad de exportación a PDF en desarrollo",
                    "Próximamente",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnCerrar.addActionListener(e -> dialogo.dispose());

        // Añadir panel de botones
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Mostrar diálogo
        dialogo.setContentPane(panel);
        dialogo.setVisible(true);
    }

    /**
     * Método auxiliar para contar usos de un servicio
     */
    private int contarUsosServicio(int idServicio) {
        int contador = 0;

        // Contar en servicios completados
        for (OrdenTrabajo orden : DataController.getOrdenesTrabajo()) {
            if ("listo".equals(orden.getEstado()) || "pagado".equals(orden.getEstado())) {
                Servicio servicio = orden.getServicio();
                if (servicio != null && servicio.getId() == idServicio) {
                    contador++;
                }
            }
        }

        return contador;
    }

    /**
     * Muestra el reporte de TOP 5 automóviles más repetidos
     */
    private void mostrarReporteAutosMasRepetidos() {
        // Crear el diálogo
        JDialog dialogo = new JDialog(this, "Reporte: TOP 5 Automóviles Más Repetidos", true);
        dialogo.setSize(700, 500);
        dialogo.setLocationRelativeTo(this);

        // Panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del reporte
        JLabel lblTitulo = new JLabel("Reporte: TOP 5 Automóviles Más Repetidos", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Tabla con los automóviles más repetidos
        String[] columnas = { "Posición", "Marca", "Modelo", "Cantidad" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaAutos = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaAutos);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Crear mapa para contar frecuencias de marca y modelo
        java.util.HashMap<String, Integer> frecuencias = new java.util.HashMap<>();

        // Analizar todos los clientes y sus automóviles
        for (Cliente cliente : DataController.getClientes()) {
            for (Automovil auto : cliente.getAutomoviles()) {
                // Usar marca+modelo como clave
                String clave = auto.getMarca() + " " + auto.getModelo();
                frecuencias.put(clave, frecuencias.getOrDefault(clave, 0) + 1);
            }
        }

        // Convertir a lista para ordenar
        java.util.List<java.util.Map.Entry<String, Integer>> lista = new java.util.ArrayList<>(frecuencias.entrySet());

        // Ordenar de mayor a menor frecuencia
        lista.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Agregar a la tabla (TOP 5)
        int limite = Math.min(5, lista.size());

        for (int i = 0; i < limite; i++) {
            java.util.Map.Entry<String, Integer> entrada = lista.get(i);
            String clave = entrada.getKey();
            Integer cantidad = entrada.getValue();

            // Separar marca y modelo
            String[] partes = clave.split(" ", 2);
            String marca = partes[0];
            String modelo = partes.length > 1 ? partes[1] : "";

            modeloTabla.addRow(new Object[] {
                    i + 1, // Posición
                    marca,
                    modelo,
                    cantidad
            });
        }

        // Panel para gráfica de barras
        JPanel panelGrafica = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Configurar antialising
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dimensiones útiles
                int ancho = getWidth();
                int alto = getHeight();
                int margen = 40;
                int anchoUtil = ancho - 2 * margen;
                int altoUtil = alto - 2 * margen;

                // Dibujar ejes
                g2d.setColor(Color.BLACK);
                g2d.drawLine(margen, alto - margen, ancho - margen, alto - margen); // eje X
                g2d.drawLine(margen, margen, margen, alto - margen); // eje Y

                // Si no hay datos, salir
                if (lista.isEmpty()) {
                    g2d.drawString("No hay datos disponibles", ancho / 2 - 60, alto / 2);
                    return;
                }

                // Encontrar el valor máximo para escalar
                int maximo = lista.get(0).getValue();

                // Ancho de cada barra
                int anchoBarra = anchoUtil / (limite * 2);

                // Dibujar barras
                for (int i = 0; i < limite; i++) {
                    java.util.Map.Entry<String, Integer> entrada = lista.get(i);
                    Integer cantidad = entrada.getValue();

                    // Calcular altura proporcional
                    int altura = (int) (((double) cantidad / maximo) * altoUtil);

                    // Calcular posición X
                    int x = margen + (i * 2 + 1) * anchoBarra;

                    // Dibujar barra
                    g2d.setColor(new Color(100, 149, 237)); // Azul acero claro
                    g2d.fillRect(x, alto - margen - altura, anchoBarra, altura);

                    // Dibujar borde
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, alto - margen - altura, anchoBarra, altura);

                    // Dibujar valor
                    g2d.drawString(cantidad.toString(), x + anchoBarra / 2 - 5, alto - margen - altura - 5);

                    // Dibujar etiqueta en eje X
                    g2d.drawString(String.valueOf(i + 1), x + anchoBarra / 2 - 5, alto - margen + 15);
                }

                // Título del eje Y
                g2d.drawString("Cantidad", 5, alto / 2);

                // Título del eje X
                g2d.drawString("Posición", ancho / 2, alto - 10);
            }
        };

        // Panel que contenga tabla y gráfica
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabla, panelGrafica);
        splitPane.setResizeWeight(0.5); // 50% para tabla, 50% para gráfica
        panel.add(splitPane, BorderLayout.CENTER);

        // Panel para botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportar = new JButton("Exportar a PDF");
        JButton btnCerrar = new JButton("Cerrar");

        panelBotones.add(btnExportar);
        panelBotones.add(btnCerrar);

        // Configurar eventos
        btnExportar.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialogo,
                    "Funcionalidad de exportación a PDF en desarrollo",
                    "Próximamente",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnCerrar.addActionListener(e -> dialogo.dispose());

        // Añadir panel de botones
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Mostrar diálogo
        dialogo.setContentPane(panel);
        dialogo.setVisible(true);
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
}