package com.tallermecanico.views;

import com.tallermecanico.components.EstatusProgresoPanel;
import com.tallermecanico.controllers.AutomovilController;
import com.tallermecanico.controllers.OrdenTrabajoController;
import com.tallermecanico.controllers.ServicioController;
import com.tallermecanico.controllers.FacturaController;
import com.tallermecanico.models.Automovil;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.Factura;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.utils.GestorBitacora;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Vista principal para clientes
 */
public class ClienteView extends JFrame {

    private Cliente cliente;
    private JTabbedPane tabbedPane;
    private EstatusProgresoPanel panelProgreso;

    // Componentes para gestión de automóviles
    private JTable tablaAutomoviles;
    private DefaultTableModel modeloTablaAutomoviles;
    private JButton btnAgregarAutomovil;
    private JButton btnEditarAutomovil;
    private JButton btnEliminarAutomovil;

    // Componentes para gestión de órdenes
    private JTable tablaOrdenes;
    private DefaultTableModel modeloTablaOrdenes;
    private JButton btnNuevaOrden;
    private JButton btnVerDetalle;
    private JButton btnVerFactura;
    private JButton btnPagarOrden;
    private JButton btnSolicitarServicio;
    private JButton btnVerDetallesOrden;
    private JButton btnVerOrdenes;
    // Removed duplicate declaration of btnPagarFactura

    // Componentes para servicios disponibles
    private JTable tablaServicios;
    private DefaultTableModel modeloTablaServicios;
    private JComboBox<Automovil> comboAutomoviles;
    private JButton btnVerDetallesServicio;
    private JButton btnSolicitarServicioTab;
    private JButton btnFiltrarServicios;
    private JComboBox<String> comboMarcaFiltro;
    private JComboBox<String> comboModeloFiltro;

    // Componentes para gestión de facturas
    private JTable tablaFacturas;
    private DefaultTableModel modeloTablaFacturas;
    private JButton btnVerFacturaDetalle;
    private JButton btnPagarFactura;

    // Otros componentes
    private JButton btnCerrarSesion;

    /**
     * Constructor
     */
    public ClienteView(Cliente cliente) {
        this.cliente = cliente;
        inicializarComponentes();
        cargarDatos();
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarComponentes() {
        // Configuración del JFrame
        setTitle("Panel de Cliente - " + cliente.getNombreCompleto());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout());
        setContentPane(panelPrincipal);

        // Panel superior con información del cliente
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblNombre = new JLabel("Cliente: " + cliente.getNombreCompleto());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel lblTipo = new JLabel("Tipo: " + cliente.getTipoCliente().toUpperCase());

        panelInfo.add(lblNombre);
        panelInfo.add(new JLabel(" | "));
        panelInfo.add(lblTipo);
        panelPrincipal.add(panelInfo, BorderLayout.NORTH);

        // Panel de pestañas
        tabbedPane = new JTabbedPane();
        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);

        // Inicializar pestañas
        inicializarPestanaAutomoviles();
        inicializarPestanaOrdenes();
        inicializarPestanaServicios();
        tabbedPane.addTab("Mis Facturas", inicializarPanelFacturas());

        // Panel inferior con botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCerrarSesion = new JButton("Cerrar Sesión");
        panelBotones.add(btnCerrarSesion);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        // Crear panel de progreso
        panelProgreso = new EstatusProgresoPanel();
        panelPrincipal.add(panelProgreso, BorderLayout.SOUTH);

        // Configurar eventos
        configurarEventos();
    }

    /**
     * Inicializa la pestaña de automóviles
     */
    private void inicializarPestanaAutomoviles() {
        JPanel panelAutomoviles = new JPanel(new BorderLayout());

        // Tabla de automóviles
        modeloTablaAutomoviles = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloTablaAutomoviles.addColumn("Placa");
        modeloTablaAutomoviles.addColumn("Marca");
        modeloTablaAutomoviles.addColumn("Modelo");

        tablaAutomoviles = new JTable(modeloTablaAutomoviles);
        JScrollPane scrollAutomoviles = new JScrollPane(tablaAutomoviles);
        panelAutomoviles.add(scrollAutomoviles, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotonesAutomoviles = new JPanel();
        btnAgregarAutomovil = new JButton("Agregar Automóvil");
        btnEditarAutomovil = new JButton("Editar Automóvil");
        btnEliminarAutomovil = new JButton("Eliminar Automóvil");

        panelBotonesAutomoviles.add(btnAgregarAutomovil);
        panelBotonesAutomoviles.add(btnEditarAutomovil);
        panelBotonesAutomoviles.add(btnEliminarAutomovil);
        panelAutomoviles.add(panelBotonesAutomoviles, BorderLayout.SOUTH);

        tabbedPane.addTab("Mis Automóviles", panelAutomoviles);
    }

    /**
     * Inicializa la pestaña de órdenes
     */
    private void inicializarPestanaOrdenes() {
        JPanel panelOrdenes = new JPanel(new BorderLayout());

        // Tabla de órdenes
        modeloTablaOrdenes = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloTablaOrdenes.addColumn("Número");
        modeloTablaOrdenes.addColumn("Vehículo");
        modeloTablaOrdenes.addColumn("Servicio");
        modeloTablaOrdenes.addColumn("Fecha");
        modeloTablaOrdenes.addColumn("Estado");
        modeloTablaOrdenes.addColumn("Mecánico");
        modeloTablaOrdenes.addColumn("Pagado");

        tablaOrdenes = new JTable(modeloTablaOrdenes);
        JScrollPane scrollOrdenes = new JScrollPane(tablaOrdenes);
        panelOrdenes.add(scrollOrdenes, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotonesOrdenes = new JPanel();
        btnNuevaOrden = new JButton("Nueva Orden");
        btnVerDetalle = new JButton("Ver Detalle");
        btnVerFactura = new JButton("Ver Factura");
        btnPagarOrden = new JButton("Pagar Orden");
        btnSolicitarServicio = new JButton("Solicitar Servicio");
        btnVerDetallesOrden = new JButton("Ver Detalles de Orden");
        btnVerOrdenes = new JButton("Ver Órdenes");
        btnPagarFactura = new JButton("Pagar Factura");

        panelBotonesOrdenes.add(btnNuevaOrden);
        panelBotonesOrdenes.add(btnVerDetalle);
        panelBotonesOrdenes.add(btnVerFactura);
        panelBotonesOrdenes.add(btnPagarOrden);
        panelBotonesOrdenes.add(btnSolicitarServicio);
        panelBotonesOrdenes.add(btnVerDetallesOrden);
        panelBotonesOrdenes.add(btnVerOrdenes);
        panelBotonesOrdenes.add(btnPagarFactura);
        panelOrdenes.add(panelBotonesOrdenes, BorderLayout.SOUTH);

        tabbedPane.addTab("Mis Órdenes", panelOrdenes);
    }

    /**
     * Inicializa la pestaña de servicios disponibles
     */
    private void inicializarPestanaServicios() {
        JPanel panelServicios = new JPanel(new BorderLayout());

        // Tabla de servicios
        modeloTablaServicios = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloTablaServicios.addColumn("ID");
        modeloTablaServicios.addColumn("Nombre");
        modeloTablaServicios.addColumn("Marca");
        modeloTablaServicios.addColumn("Modelo");
        modeloTablaServicios.addColumn("Precio");

        tablaServicios = new JTable(modeloTablaServicios);
        JScrollPane scrollServicios = new JScrollPane(tablaServicios);
        panelServicios.add(scrollServicios, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotonesServicios = new JPanel();
        comboAutomoviles = new JComboBox<>();
        btnVerDetallesServicio = new JButton("Ver Detalle del Servicio");
        btnSolicitarServicioTab = new JButton("Solicitar Servicio");
        btnFiltrarServicios = new JButton("Filtrar Servicios");
        comboMarcaFiltro = new JComboBox<>();
        comboModeloFiltro = new JComboBox<>();

        panelBotonesServicios.add(comboAutomoviles);
        panelBotonesServicios.add(btnVerDetallesServicio);
        panelBotonesServicios.add(btnSolicitarServicioTab);
        panelBotonesServicios.add(btnFiltrarServicios);
        panelBotonesServicios.add(comboMarcaFiltro);
        panelBotonesServicios.add(comboModeloFiltro);
        panelServicios.add(panelBotonesServicios, BorderLayout.SOUTH);

        tabbedPane.addTab("Servicios Disponibles", panelServicios);
    }

    /**
     * Inicializa el panel de facturas
     */
    private JPanel inicializarPanelFacturas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Etiqueta de título
        JLabel lblTitulo = new JLabel("Mis Facturas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Tabla de facturas
        String[] columnas = { "Número", "Fecha", "Orden", "Servicio", "Total", "Estado" };
        modeloTablaFacturas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaFacturas = new JTable(modeloTablaFacturas);
        JScrollPane scrollTabla = new JScrollPane(tablaFacturas);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnVerFacturaDetalle = new JButton("Ver Detalles");
        btnPagarFactura = new JButton("Pagar Factura");

        panelBotones.add(btnVerFacturaDetalle);
        panelBotones.add(btnPagarFactura);

        panel.add(panelBotones, BorderLayout.SOUTH);

        // Configurar eventos
        btnVerFacturaDetalle.addActionListener(e -> verFacturaSeleccionada());
        btnPagarFactura.addActionListener(e -> pagarFacturaSeleccionada());

        // Selección en tabla habilita/deshabilita botones
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> {
            boolean haySeleccion = tablaFacturas.getSelectedRow() != -1;
            btnVerFacturaDetalle.setEnabled(haySeleccion);

            // Solo habilitar pago si la factura no está pagada
            if (haySeleccion) {
                String estado = (String) tablaFacturas.getValueAt(tablaFacturas.getSelectedRow(), 5);
                btnPagarFactura.setEnabled("PENDIENTE".equals(estado));
            } else {
                btnPagarFactura.setEnabled(false);
            }
        });

        // Estado inicial de botones
        btnVerFacturaDetalle.setEnabled(false);
        btnPagarFactura.setEnabled(false);

        return panel;
    }

    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        // Eventos para la gestión de automóviles
        configurarEventosAutomoviles();

        // Eventos para la gestión de órdenes
        configurarEventosOrdenes();

        // Eventos para la vista de servicios
        configurarEventosServicios();

        // Cerrar sesión
        btnCerrarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GestorBitacora.registrarEvento(cliente.getNombreUsuario(), "Cierre de sesión", true,
                        "Cliente cerró sesión: " + cliente.getNombreCompleto());
                dispose();
                new LoginView().setVisible(true);
            }
        });
    }

    // Completar configurarEventosAutomoviles()
    private void configurarEventosAutomoviles() {
        // Botón Agregar Automóvil
        btnAgregarAutomovil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarDialogoAgregarAutomovil();
            }
        });

        // Botón Editar Automóvil
        btnEditarAutomovil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaAutomoviles.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    String placa = (String) tablaAutomoviles.getValueAt(filaSeleccionada, 0);
                    Automovil auto = cliente.buscarAutomovil(placa);
                    if (auto != null) {
                        mostrarDialogoEditarAutomovil(auto);
                    }
                } else {
                    JOptionPane.showMessageDialog(ClienteView.this,
                            "Debe seleccionar un automóvil de la tabla",
                            "Selección requerida",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Botón Eliminar Automóvil
        btnEliminarAutomovil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaAutomoviles.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    String placa = (String) tablaAutomoviles.getValueAt(filaSeleccionada, 0);

                    int confirmacion = JOptionPane.showConfirmDialog(ClienteView.this,
                            "¿Está seguro de eliminar el automóvil con placa " + placa + "?",
                            "Confirmar eliminación",
                            JOptionPane.YES_NO_OPTION);

                    if (confirmacion == JOptionPane.YES_OPTION) {
                        boolean eliminado = cliente.eliminarAutomovil(placa);
                        if (eliminado) {
                            JOptionPane.showMessageDialog(ClienteView.this,
                                    "Automóvil eliminado correctamente",
                                    "Éxito",
                                    JOptionPane.INFORMATION_MESSAGE);

                            // Actualizar tabla
                            actualizarTablaAutomoviles();

                            // Registrar evento
                            GestorBitacora.registrarEvento(cliente.getNombreUsuario(), "Eliminación de automóvil", true,
                                    "Automóvil eliminado: " + placa);
                        } else {
                            JOptionPane.showMessageDialog(ClienteView.this,
                                    "No se pudo eliminar el automóvil",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ClienteView.this,
                            "Debe seleccionar un automóvil de la tabla",
                            "Selección requerida",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    // Implementar configurarEventosOrdenes()
    private void configurarEventosOrdenes() {
        // Botón Solicitar Servicio
        btnSolicitarServicio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaAutomoviles.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    String placa = (String) tablaAutomoviles.getValueAt(filaSeleccionada, 0);
                    Automovil auto = cliente.buscarAutomovil(placa);
                    if (auto != null) {
                        mostrarDialogoSolicitarServicio(auto);
                    }
                } else {
                    JOptionPane.showMessageDialog(ClienteView.this,
                            "Debe seleccionar un automóvil de la tabla",
                            "Selección requerida",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Botón Ver Órdenes
        btnVerOrdenes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarDialogoVerOrdenes();
            }
        });

        // Botón Pagar Factura
        btnPagarFactura.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarDialogoPagarFactura();
            }
        });
    }

    // Implementar configurarEventosServicios()
    private void configurarEventosServicios() {
        // Botón para filtrar servicios por marca y modelo
        btnFiltrarServicios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String marca = comboMarcaFiltro.getSelectedItem().toString();
                String modelo = comboModeloFiltro.getSelectedItem().toString();

                actualizarTablaServicios(marca, modelo);
            }
        });

        // Evento para mostrar detalles de un servicio al seleccionarlo
        tablaServicios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tablaServicios.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    int idServicio = (int) tablaServicios.getValueAt(filaSeleccionada, 0);
                    Servicio servicio = ServicioController.buscarServicioPorId(idServicio);

                    if (servicio != null) {
                        actualizarDetallesServicio(servicio);
                    }
                }
            }
        });
    }

    /**
     * Carga los datos en las tablas
     */
    private void cargarDatos() {
        // Cargar datos en la tabla de automóviles
        cargarTablaAutomoviles();

        // Cargar datos en la tabla de órdenes
        cargarTablaOrdenes();

        // Cargar datos en la tabla de servicios
        cargarTablaServicios();

        // Cargar datos en la tabla de facturas
        actualizarTablaFacturas();
    }

    /**
     * Carga los datos en la tabla de automóviles
     */
    private void cargarTablaAutomoviles() {
        // Limpiar tabla
        limpiarTabla(modeloTablaAutomoviles);

        // Cargar datos
        Vector<Automovil> automoviles = cliente.getAutomoviles();

        for (Automovil auto : automoviles) {
            modeloTablaAutomoviles.addRow(new Object[] {
                    auto.getPlaca(),
                    auto.getMarca(),
                    auto.getModelo()
            });
        }
    }

    /**
     * Carga los datos en la tabla de órdenes
     */
    private void cargarTablaOrdenes() {
        // Limpiar tabla
        limpiarTabla(modeloTablaOrdenes);

        // Cargar datos
        Vector<OrdenTrabajo> ordenes = OrdenTrabajoController.obtenerOrdenesPorCliente(cliente.getIdentificador());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (OrdenTrabajo orden : ordenes) {
            String mecanico = orden.getMecanico() != null ? orden.getMecanico().getNombreCompleto() : "Sin asignar";

            modeloTablaOrdenes.addRow(new Object[] {
                    orden.getNumero(),
                    orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca(),
                    orden.getServicio().getNombre(),
                    sdf.format(orden.getFecha()),
                    obtenerEstadoLegible(orden.getEstado()),
                    mecanico,
                    orden.isPagado() ? "Sí" : "No"
            });
        }
    }

    /**
     * Carga los datos en la tabla de servicios
     */
    private void cargarTablaServicios() {
        // Limpiar tabla
        limpiarTabla(modeloTablaServicios);

        // Cargar servicios disponibles
        Vector<Servicio> servicios = ServicioController.obtenerTodosLosServicios();

        for (Servicio servicio : servicios) {
            modeloTablaServicios.addRow(new Object[] {
                    servicio.getId(),
                    servicio.getNombre(),
                    servicio.getMarca(),
                    servicio.getModelo(),
                    servicio.getPrecioTotal()
            });
        }
    }

    /**
     * Actualiza la tabla de facturas
     */
    private void actualizarTablaFacturas() {
        modeloTablaFacturas.setRowCount(0);

        // Obtener facturas del cliente
        Vector<Factura> facturas = FacturaController.obtenerFacturasCliente(cliente.getIdentificador());
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Factura factura : facturas) {
            modeloTablaFacturas.addRow(new Object[] {
                    "F-" + factura.getNumero(),
                    factura.getFechaEmisionFormateada(),
                    "Orden #" + factura.getOrdenTrabajo().getNumero(),
                    factura.getOrdenTrabajo().getServicio().getNombre(),
                    "Q " + df.format(factura.calcularTotal()),
                    factura.isPagada() ? "PAGADA" : "PENDIENTE"
            });
        }
    }

    /**
     * Limpia una tabla para cargar nuevos datos
     */
    private void limpiarTabla(DefaultTableModel modelo) {
        while (modelo.getRowCount() > 0) {
            modelo.removeRow(0);
        }
    }

    /**
     * Convierte el estado de la orden a un formato legible
     */
    private String obtenerEstadoLegible(String estado) {
        switch (estado) {
            case "espera":
                return "En espera";
            case "en_servicio":
                return "En servicio";
            case "listo":
                return "Listo para entrega";
            default:
                return estado;
        }
    }

    private void mostrarDialogoAgregarAutomovil() {
        JDialog dialogo = new JDialog(this, "Agregar Automóvil", true);
        dialogo.setSize(400, 300);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblPlaca = new JLabel("Placa:");
        JTextField txtPlaca = new JTextField();

        JLabel lblMarca = new JLabel("Marca:");
        JTextField txtMarca = new JTextField();

        JLabel lblModelo = new JLabel("Modelo:");
        JTextField txtModelo = new JTextField();

        JLabel lblFoto = new JLabel("Foto:");
        JPanel panelFoto = new JPanel(new BorderLayout());
        JTextField txtFoto = new JTextField();
        JButton btnSeleccionar = new JButton("...");
        panelFoto.add(txtFoto, BorderLayout.CENTER);
        panelFoto.add(btnSeleccionar, BorderLayout.EAST);

        panel.add(lblPlaca);
        panel.add(txtPlaca);
        panel.add(lblMarca);
        panel.add(txtMarca);
        panel.add(lblModelo);
        panel.add(txtModelo);
        panel.add(lblFoto);
        panel.add(panelFoto);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        dialogo.add(panel, BorderLayout.CENTER);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        // Configurar eventos
        btnSeleccionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Imágenes", "jpg", "jpeg", "png", "gif"));

                int resultado = fileChooser.showOpenDialog(dialogo);
                if (resultado == JFileChooser.APPROVE_OPTION) {
                    txtFoto.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogo.dispose();
            }
        });

        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String placa = txtPlaca.getText().trim();
                String marca = txtMarca.getText().trim();
                String modelo = txtModelo.getText().trim();
                String rutaFoto = txtFoto.getText().trim();

                if (placa.isEmpty() || marca.isEmpty() || modelo.isEmpty()) {
                    JOptionPane.showMessageDialog(dialogo,
                            "Todos los campos son obligatorios",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean resultado = AutomovilController.registrarAutomovil(
                        cliente.getIdentificador(), placa, marca, modelo, rutaFoto);

                if (resultado) {
                    JOptionPane.showMessageDialog(dialogo,
                            "Automóvil registrado correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialogo.dispose();
                    cargarTablaAutomoviles();
                } else {
                    JOptionPane.showMessageDialog(dialogo,
                            "Error al registrar el automóvil",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialogo.setVisible(true);
    }

    private void mostrarDialogoEditarAutomovil(Automovil auto) {
        // Similar a mostrarDialogoAgregarAutomovil pero cargando los datos del auto
        // existente
        if (auto == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró el automóvil",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialogo = new JDialog(this, "Editar Automóvil", true);
        dialogo.setSize(400, 300);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());
    }

    private void actualizarTablaAutomoviles() {
        cargarTablaAutomoviles();
    }

    private void mostrarDialogoSolicitarServicio(Automovil auto) {
        // Implementar diálogo para solicitar un servicio para el automóvil seleccionado
        // ...
    }

    private void mostrarDialogoVerOrdenes() {
        // Implementar diálogo para ver órdenes
        // ...
    }

    private void mostrarDialogoPagarFactura() {
        // Implementar diálogo para pagar factura
        // ...
    }

    private void actualizarTablaServicios(String marca, String modelo) {
        // Implementar lógica para actualizar la tabla de servicios según los filtros
        // ...
    }

    private void actualizarDetallesServicio(Servicio servicio) {
        // Implementar lógica para actualizar los detalles del servicio seleccionado
        // ...
    }

    private void verFacturaSeleccionada() {
        int filaSeleccionada = tablaFacturas.getSelectedRow();
        if (filaSeleccionada == -1)
            return;

        // Obtener número de factura (quitar el prefijo "F-")
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
        actualizarTablaFacturas();
    }

    private void pagarFacturaSeleccionada() {
        int filaSeleccionada = tablaFacturas.getSelectedRow();
        if (filaSeleccionada == -1)
            return;

        // Verificar si ya está pagada
        String estado = (String) tablaFacturas.getValueAt(filaSeleccionada, 5);
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

        // Mostrar la vista de factura para pagar
        Factura factura = FacturaController.buscarFacturaPorNumero(numeroFactura);
        if (factura == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo encontrar la factura seleccionada.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        FacturaView vista = new FacturaView(this, factura);
        vista.setVisible(true);

        // Actualizar tabla después de cerrar la vista
        actualizarTablaFacturas();
    }

    @Override
    public void dispose() {
        // Liberar recursos del panel de progreso
        panelProgreso.destruir();
        super.dispose();
    }
}