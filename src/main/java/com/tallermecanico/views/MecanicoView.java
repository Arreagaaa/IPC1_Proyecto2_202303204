package com.tallermecanico.views;

import com.tallermecanico.controllers.OrdenTrabajoController;
import com.tallermecanico.controllers.ServicioController;
import com.tallermecanico.models.OrdenTrabajo;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Mecanico;
import com.tallermecanico.utils.GestorBitacora;
import com.tallermecanico.utils.GestorHilos;
import com.tallermecanico.utils.MonitorOrdenesThread;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

/**
 * Vista para el mecánico
 */
public class MecanicoView extends JFrame implements MonitorOrdenesThread.ObservadorOrdenes {

    private Mecanico mecanico;

    // Componentes principales
    private JTabbedPane pestanas;
    private JPanel panelOrdenes;
    private JPanel panelActual;
    private JPanel panelCompletadas;

    // Componentes para panel de órdenes en espera
    private JTable tablaOrdenes;
    private DefaultTableModel modeloTablaOrdenes;
    private JButton btnAsignar;
    private JLabel lblTituloOrdenes;

    // Componentes para panel de orden actual
    private JLabel lblTituloActual;
    private JLabel lblNoOrden;
    private JLabel lblCliente;
    private JLabel lblAutomovil;
    private JLabel lblServicio;
    private JLabel lblFecha;
    private JButton btnCompletarOrden;
    private JPanel panelDetalles;

    // Componentes para panel de órdenes completadas
    private JTable tablaCompletadas;
    private DefaultTableModel modeloTablaCompletadas;
    private JLabel lblTituloCompletadas;

    // Componentes compartidos
    private JButton btnRefrescar;
    private JButton btnCerrarSesion;
    private JPanel panelBotones;

    /**
     * Constructor
     */
    public MecanicoView(Mecanico mecanico) {
        this.mecanico = mecanico;
        inicializarComponentes();
        configurarEventos();
        cargarDatos();
        configurarRendererTablaEspera();

        // Registrarse como observador de órdenes
        GestorHilos.obtenerInstancia().registrarObservadorOrdenes(this);

        // Configurar para desregistrarse cuando se cierra la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GestorHilos.obtenerInstancia().eliminarObservadorOrdenes(MecanicoView.this);
            }
        });
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarComponentes() {
        // Configuración del JFrame
        setTitle("Taller Mecánico - Panel de Mecánico");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Panel principal con pestañas
        pestanas = new JTabbedPane();

        // Inicializar paneles principales
        inicializarPanelOrdenes();
        inicializarPanelActual();
        inicializarPanelCompletadas();

        // Agregar pestañas
        pestanas.addTab("Órdenes en Espera", panelOrdenes);
        pestanas.addTab("Orden Actual", panelActual);
        pestanas.addTab("Órdenes Completadas", panelCompletadas);

        // Panel de botones compartidos
        panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefrescar = new JButton("Refrescar");
        btnCerrarSesion = new JButton("Cerrar Sesión");
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnCerrarSesion);

        // Panel contenedor principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(pestanas, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
    }

    /**
     * Inicializa el panel de órdenes en espera
     */
    private void inicializarPanelOrdenes() {
        panelOrdenes = new JPanel(new BorderLayout());
        panelOrdenes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        lblTituloOrdenes = new JLabel("Órdenes de Trabajo en Espera");
        lblTituloOrdenes.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloOrdenes.setHorizontalAlignment(SwingConstants.CENTER);
        panelOrdenes.add(lblTituloOrdenes, BorderLayout.NORTH);

        // Tabla de órdenes
        String[] columnas = { "N° Orden", "Cliente", "Automóvil", "Servicio", "Fecha" };
        modeloTablaOrdenes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaOrdenes = new JTable(modeloTablaOrdenes);
        JScrollPane scrollTabla = new JScrollPane(tablaOrdenes);
        panelOrdenes.add(scrollTabla, BorderLayout.CENTER);

        // Botón asignar
        btnAsignar = new JButton("Asignar a Mí");
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(btnAsignar);
        panelOrdenes.add(panelBoton, BorderLayout.SOUTH);
    }

    /**
     * Inicializa el panel de orden actual
     */
    private void inicializarPanelActual() {
        panelActual = new JPanel(new BorderLayout());
        panelActual.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        lblTituloActual = new JLabel("Orden de Trabajo Actual");
        lblTituloActual.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloActual.setHorizontalAlignment(SwingConstants.CENTER);
        panelActual.add(lblTituloActual, BorderLayout.NORTH);

        // Panel de detalles
        panelDetalles = new JPanel();
        panelDetalles.setLayout(new BoxLayout(panelDetalles, BoxLayout.Y_AXIS));
        panelDetalles.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Etiquetas para detalles
        lblNoOrden = new JLabel("N° Orden: ");
        lblCliente = new JLabel("Cliente: ");
        lblAutomovil = new JLabel("Automóvil: ");
        lblServicio = new JLabel("Servicio: ");
        lblFecha = new JLabel("Fecha: ");

        // Aplicar formato a etiquetas
        Font fuenteEtiquetas = new Font("Arial", Font.PLAIN, 14);
        lblNoOrden.setFont(fuenteEtiquetas);
        lblCliente.setFont(fuenteEtiquetas);
        lblAutomovil.setFont(fuenteEtiquetas);
        lblServicio.setFont(fuenteEtiquetas);
        lblFecha.setFont(fuenteEtiquetas);

        // Agregar etiquetas al panel
        panelDetalles.add(lblNoOrden);
        panelDetalles.add(Box.createRigidArea(new Dimension(0, 10)));
        panelDetalles.add(lblCliente);
        panelDetalles.add(Box.createRigidArea(new Dimension(0, 10)));
        panelDetalles.add(lblAutomovil);
        panelDetalles.add(Box.createRigidArea(new Dimension(0, 10)));
        panelDetalles.add(lblServicio);
        panelDetalles.add(Box.createRigidArea(new Dimension(0, 10)));
        panelDetalles.add(lblFecha);

        panelActual.add(panelDetalles, BorderLayout.CENTER);

        // Botón completar orden
        btnCompletarOrden = new JButton("Completar Orden");
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(btnCompletarOrden);
        panelActual.add(panelBoton, BorderLayout.SOUTH);
    }

    /**
     * Inicializa el panel de órdenes completadas
     */
    private void inicializarPanelCompletadas() {
        panelCompletadas = new JPanel(new BorderLayout());
        panelCompletadas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        lblTituloCompletadas = new JLabel("Órdenes de Trabajo Completadas");
        lblTituloCompletadas.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloCompletadas.setHorizontalAlignment(SwingConstants.CENTER);
        panelCompletadas.add(lblTituloCompletadas, BorderLayout.NORTH);

        // Tabla de órdenes completadas
        String[] columnas = { "N° Orden", "Cliente", "Automóvil", "Servicio", "Fecha", "Estado" };
        modeloTablaCompletadas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaCompletadas = new JTable(modeloTablaCompletadas);
        JScrollPane scrollTabla = new JScrollPane(tablaCompletadas);
        panelCompletadas.add(scrollTabla, BorderLayout.CENTER);
    }

    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        // Botón Asignar
        btnAsignar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                asignarOrden();
            }
        });

        // Botón Completar Orden
        btnCompletarOrden.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                completarOrdenActual();
            }
        });

        // Botón Refrescar
        btnRefrescar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarDatos();
            }
        });

        // Botón Cerrar Sesión
        btnCerrarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarSesion();
            }
        });
    }

    /**
     * Carga los datos en las tablas y actualiza la vista
     */
    private void cargarDatos() {
        // Limpiar tablas
        modeloTablaOrdenes.setRowCount(0);
        modeloTablaCompletadas.setRowCount(0);

        // Cargar órdenes en espera
        actualizarTablaEspera();

        // Cargar orden actual
        actualizarOrdenActual();

        // Cargar órdenes completadas por este mecánico
        Vector<OrdenTrabajo> ordenesCompletadas = OrdenTrabajoController
                .obtenerOrdenesPorMecanico(mecanico.getIdentificador());
        for (OrdenTrabajo orden : ordenesCompletadas) {
            if ("listo".equals(orden.getEstado())) {
                Object[] fila = {
                        orden.getNumero(),
                        orden.getCliente().getNombreCompleto(),
                        orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca() + " "
                                + orden.getAutomovil().getModelo(),
                        orden.getServicio().getNombre(),
                        orden.getFecha(),
                        orden.isPagado() ? "Pagado" : "Pendiente de pago"
                };
                modeloTablaCompletadas.addRow(fila);
            }
        }
    }

    /**
     * Actualiza la información de la orden actual
     */
    private void actualizarOrdenActual() {
        OrdenTrabajo ordenActual = mecanico.getOrdenActual();

        if (ordenActual != null) {
            lblNoOrden.setText("N° Orden: " + ordenActual.getNumero());
            lblCliente.setText("Cliente: " + ordenActual.getCliente().getNombreCompleto());
            lblAutomovil.setText("Automóvil: " + ordenActual.getAutomovil().getPlaca() + " - " +
                    ordenActual.getAutomovil().getMarca() + " " + ordenActual.getAutomovil().getModelo());
            lblServicio.setText("Servicio: " + ordenActual.getServicio().getNombre());
            lblFecha.setText("Fecha: " + ordenActual.getFecha());

            btnCompletarOrden.setEnabled(true);
        } else {
            // No hay orden actual
            lblNoOrden.setText("N° Orden: No hay orden asignada");
            lblCliente.setText("Cliente: -");
            lblAutomovil.setText("Automóvil: -");
            lblServicio.setText("Servicio: -");
            lblFecha.setText("Fecha: -");

            btnCompletarOrden.setEnabled(false);
        }
    }

    /**
     * Actualiza la tabla de órdenes en espera
     */
    private void actualizarTablaEspera() {
        // Limpiar la tabla
        modeloTablaOrdenes.setRowCount(0);

        // Obtener órdenes en espera (ya ordenadas por prioridad)
        Vector<OrdenTrabajo> ordenesEspera = OrdenTrabajoController.obtenerColaEspera();

        // Agregar cada orden a la tabla
        for (OrdenTrabajo orden : ordenesEspera) {
            boolean esClienteOro = "oro".equals(orden.getCliente().getTipoCliente());

            // Nota: En un JTable normal no se pueden cambiar colores fácilmente
            // Se podría usar un TableCellRenderer personalizado para resaltar filas

            // Por ahora, agregamos un indicador en la primera columna
            modeloTablaOrdenes.addRow(new Object[] {
                    orden.getNumero(),
                    (esClienteOro ? "★ " : "") + orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca() + " "
                            + orden.getAutomovil().getModelo(),
                    orden.getServicio().getNombre(),
                    orden.getFecha()
            });
        }
    }

    /**
     * Configura un renderer personalizado para la tabla de espera
     * para resaltar clientes oro
     */
    private void configurarRendererTablaEspera() {
        tablaOrdenes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                // Verificar si es cliente oro
                int numOrden = (int) table.getValueAt(row, 0); // Asumiendo que el número de orden está en columna 0
                OrdenTrabajo orden = null;

                // Buscar la orden correspondiente
                for (OrdenTrabajo o : OrdenTrabajoController.obtenerColaEspera()) {
                    if (o.getNumero() == numOrden) {
                        orden = o;
                        break;
                    }
                }

                if (orden != null && "oro".equals(orden.getCliente().getTipoCliente())) {
                    // Cliente oro - fondo dorado claro
                    if (!isSelected) {
                        c.setBackground(new Color(255, 223, 0, 60)); // Dorado claro con transparencia
                    }
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    // Cliente normal
                    if (!isSelected) {
                        c.setBackground(table.getBackground());
                    }
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }

                return c;
            }
        });
    }

    /**
     * Asigna una orden seleccionada al mecánico actual
     */
    private void asignarOrden() {
        // Verificar si el mecánico está disponible
        if (!mecanico.isDisponible()) {
            JOptionPane.showMessageDialog(this,
                    "Ya tiene una orden asignada. Debe completar la orden actual antes de asignar una nueva.",
                    "Mecánico Ocupado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar si hay una fila seleccionada
        int filaSeleccionada = tablaOrdenes.getSelectedRow();
        if (filaSeleccionada >= 0) {
            int numeroOrden = (int) tablaOrdenes.getValueAt(filaSeleccionada, 0);

            // Asignar la orden al mecánico
            boolean asignado = OrdenTrabajoController.asignarOrdenAMecanico(numeroOrden, mecanico.getIdentificador());

            if (asignado) {
                JOptionPane.showMessageDialog(this,
                        "Orden #" + numeroOrden + " asignada correctamente",
                        "Asignación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);

                // Actualizar vistas
                cargarDatos();
                pestanas.setSelectedIndex(1); // Cambiar a la pestaña de orden actual
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo asignar la orden. Verifique que esté en estado de espera.",
                        "Error de Asignación",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una orden de la tabla",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Completa la orden actual del mecánico
     */
    private void completarOrdenActual() {
        OrdenTrabajo ordenActual = mecanico.getOrdenActual();

        if (ordenActual == null) {
            JOptionPane.showMessageDialog(this,
                    "No tiene ninguna orden asignada actualmente",
                    "Sin Orden Actual",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar si es un diagnóstico
        if (ordenActual.getServicio() != null && ordenActual.getServicio().esDiagnostico()) {
            // Si es diagnóstico, mostrar diálogo para elegir servicio
            mostrarDialogoDiagnostico(ordenActual);
        } else {
            // Si es un servicio normal, completar directamente
            completarOrden(ordenActual.getNumero());
        }
    }

    /**
     * Muestra un diálogo para elegir un servicio después del diagnóstico
     */
    private void mostrarDialogoDiagnostico(OrdenTrabajo ordenDiagnostico) {
        // Obtener servicios compatibles con el automóvil
        Vector<Servicio> serviciosCompatibles = new Vector<>();
        for (Servicio servicio : ServicioController.obtenerTodosLosServicios()) {
            if (!servicio.esDiagnostico() &&
                    ordenDiagnostico.getAutomovil().esCompatibleConServicio(servicio)) {
                serviciosCompatibles.add(servicio);
            }
        }

        // Verificar que haya servicios compatibles
        if (serviciosCompatibles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay servicios compatibles con este automóvil",
                    "Sin Servicios Disponibles",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Crear diálogo
        JDialog dialogoDiagnostico = new JDialog(this, "Resultado de Diagnóstico", true);
        dialogoDiagnostico.setSize(400, 300);
        dialogoDiagnostico.setLocationRelativeTo(this);

        // Crear panel principal
        JPanel panelDialogo = new JPanel();
        panelDialogo.setLayout(new BoxLayout(panelDialogo, BoxLayout.Y_AXIS));
        panelDialogo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Añadir etiqueta informativa
        JLabel lblInfo = new JLabel("Seleccione el servicio recomendado:");
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelDialogo.add(lblInfo);
        panelDialogo.add(Box.createRigidArea(new Dimension(0, 10)));

        // Crear combo para servicios
        JComboBox<Servicio> comboServicios = new JComboBox<>();
        for (Servicio servicio : serviciosCompatibles) {
            comboServicios.addItem(servicio);
        }
        comboServicios.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboServicios.getPreferredSize().height));
        comboServicios.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelDialogo.add(comboServicios);
        panelDialogo.add(Box.createRigidArea(new Dimension(0, 20)));

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAceptar = new JButton("Recomendar");

        btnCancelar.addActionListener(e -> dialogoDiagnostico.dispose());

        btnAceptar.addActionListener(e -> {
            Servicio servicioRecomendado = (Servicio) comboServicios.getSelectedItem();
            if (servicioRecomendado != null) {
                // Notificar al cliente (simulado)
                JOptionPane.showMessageDialog(dialogoDiagnostico,
                        "Se ha notificado al cliente sobre el servicio recomendado: " + servicioRecomendado.getNombre(),
                        "Notificación Enviada",
                        JOptionPane.INFORMATION_MESSAGE);

                // Actualizar servicio y completar diagnóstico
                ordenDiagnostico.setServicio(servicioRecomendado);
                completarOrden(ordenDiagnostico.getNumero());

                dialogoDiagnostico.dispose();
            }
        });

        panelBotones.add(btnCancelar);
        panelBotones.add(btnAceptar);
        panelBotones.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelDialogo.add(panelBotones);

        // Añadir panel al diálogo
        dialogoDiagnostico.setContentPane(panelDialogo);
        dialogoDiagnostico.setVisible(true);
    }

    /**
     * Completa una orden de trabajo
     */
    private void completarOrden(int numeroOrden) {
        boolean completado = OrdenTrabajoController.completarOrden(numeroOrden);

        if (completado) {
            JOptionPane.showMessageDialog(this,
                    "Orden #" + numeroOrden + " completada correctamente",
                    "Operación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar vistas
            cargarDatos();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo completar la orden. Verifique que esté en servicio.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cierra la sesión del mecánico
     */
    private void cerrarSesion() {
        GestorBitacora.registrarEvento(mecanico.getNombreUsuario(), "Cierre de sesión", true,
                "Mecánico cerró sesión: " + mecanico.getNombreCompleto());
        dispose();
        new LoginView(LoginView.TIPO_MECANICO).setVisible(true);
    }

    // Implementación del método de la interfaz ObservadorOrdenes
    @Override
    public void ordenesActualizadas(Vector<OrdenTrabajo> ordenesEspera,
            Vector<OrdenTrabajo> ordenesServicio,
            Vector<OrdenTrabajo> ordenesListas) {
        // Las actualizaciones de la UI deben hacerse en el EDT
        SwingUtilities.invokeLater(() -> {
            // Actualizar la tabla de órdenes en espera
            modeloTablaOrdenes.setRowCount(0);

            for (OrdenTrabajo orden : ordenesEspera) {
                boolean esClienteOro = "oro".equals(orden.getCliente().getTipoCliente());

                modeloTablaOrdenes.addRow(new Object[] {
                        orden.getNumero(),
                        (esClienteOro ? "★ " : "") + orden.getCliente().getNombreCompleto(),
                        orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca() + " "
                                + orden.getAutomovil().getModelo(),
                        orden.getServicio().getNombre(),
                        orden.getFecha()
                });
            }

            // Actualizar la orden actual si es necesario
            actualizarOrdenActual();

            // Actualizar tabla de órdenes completadas
            modeloTablaCompletadas.setRowCount(0);

            // Filtrar órdenes completadas por este mecánico
            for (OrdenTrabajo orden : ordenesListas) {
                if (mecanico.getIdentificador().equals(orden.getMecanico().getIdentificador())) {
                    modeloTablaCompletadas.addRow(new Object[] {
                            orden.getNumero(),
                            orden.getCliente().getNombreCompleto(),
                            orden.getAutomovil().getPlaca() + " - " + orden.getAutomovil().getMarca() + " "
                                    + orden.getAutomovil().getModelo(),
                            orden.getServicio().getNombre(),
                            orden.getFecha(),
                            orden.isPagado() ? "Pagado" : "Pendiente de pago"
                    });
                }
            }
        });
    }
}
