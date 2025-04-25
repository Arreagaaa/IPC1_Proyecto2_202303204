package com.example.views.mecanico;

import com.example.models.GestorOrdenes;
import com.example.models.OrdenTrabajo;
import com.example.models.personas.Mecanico;
import com.example.models.personas.Persona;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Vista para gestionar las órdenes de trabajo asignadas al mecánico
 */
public class OrdenesTrabajoView extends BaseView {
    
    private Persona usuario;
    private JPanel mainPanel;
    private JTable tablaOrdenes;
    private DefaultTableModel modeloTabla;
    private JLabel labelDetalles;
    private Timer timer;
    
    public OrdenesTrabajoView(Persona usuario) {
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
        JLabel titleLabel = createSubtitle("Órdenes de Trabajo Asignadas");
        titlePanel.add(titleLabel);
        
        // Panel principal dividido
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
        
        // Iniciar el timer para actualizar automáticamente
        timer = new Timer(5000, e -> cargarOrdenes());
        timer.start();
        
        // Cargar datos iniciales
        cargarOrdenes();
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Órdenes"));
        
        // Modelo de tabla
        String[] columnas = {"Nº Orden", "Cliente", "Automóvil", "Servicio", "Fecha", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaOrdenes = new JTable(modeloTabla);
        tablaOrdenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaOrdenes);
        
        // Listener para detectar selección
        tablaOrdenes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaOrdenes.getSelectedRow() != -1) {
                mostrarDetallesOrden(tablaOrdenes.getSelectedRow());
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setOpaque(false);
        
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(e -> cargarOrdenes());
        
        JButton completeButton = new JButton("Marcar como Completada");
        completeButton.addActionListener(e -> completarOrden());
        
        JButton startButton = new JButton("Comenzar Servicio");
        startButton.addActionListener(e -> comenzarServicio());
        
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(startButton);
        buttonsPanel.add(completeButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Detalles de la Orden"));
        panel.setPreferredSize(new Dimension(300, 0));
        
        labelDetalles = new JLabel("<html><body>Seleccione una orden para ver sus detalles</body></html>");
        labelDetalles.setVerticalAlignment(JLabel.TOP);
        
        JScrollPane scrollPane = new JScrollPane(labelDetalles);
        scrollPane.setBorder(null);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void cargarOrdenes() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);
        
        // Obtener mecánico
        Mecanico mecanico = (Mecanico) usuario;
        
        // Cargar ordenes asignadas al mecánico
        Vector<OrdenTrabajo> ordenesServicio = GestorOrdenes.getInstancia().getOrdenesServicio();
        
        for (OrdenTrabajo orden : ordenesServicio) {
            if (orden.getMecanico().getIdentificador().equals(mecanico.getIdentificador())) {
                // Formato de fecha
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String fechaStr = sdf.format(orden.getFecha());
                
                // Convertir primera letra del estado a mayúscula
                String estado = orden.getEstado();
                estado = estado.substring(0, 1).toUpperCase() + estado.substring(1);
                
                Object[] fila = {
                    orden.getNumeroOrden(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo() + " (" + orden.getAutomovil().getPlaca() + ")",
                    orden.getServicio().getNombre(),
                    fechaStr,
                    estado
                };
                modeloTabla.addRow(fila);
            }
        }
        
        // Si no hay filas seleccionadas y hay datos, seleccionar la primera
        if (tablaOrdenes.getSelectedRow() == -1 && tablaOrdenes.getRowCount() > 0) {
            tablaOrdenes.setRowSelectionInterval(0, 0);
            mostrarDetallesOrden(0);
        } else if (tablaOrdenes.getRowCount() > 0 && tablaOrdenes.getSelectedRow() != -1) {
            // Mantener la selección actual
            mostrarDetallesOrden(tablaOrdenes.getSelectedRow());
        } else {
            // No hay datos
            labelDetalles.setText("<html><body>No hay órdenes asignadas</body></html>");
        }
    }
    
    private void mostrarDetallesOrden(int rowIndex) {
        // Obtener número de orden
        int numeroOrden = (int) tablaOrdenes.getValueAt(rowIndex, 0);
        
        // Buscar la orden en el gestor
        OrdenTrabajo orden = null;
        for (OrdenTrabajo o : GestorOrdenes.getInstancia().getOrdenesServicio()) {
            if (o.getNumeroOrden() == numeroOrden) {
                orden = o;
                break;
            }
        }
        
        if (orden == null) {
            return;
        }
        
        // Formato de texto para mostrar detalles
        StringBuilder detalles = new StringBuilder();
        detalles.append("<html><body style='width: 250px'>");
        
        // Información de la orden
        detalles.append("<h2>Orden #").append(orden.getNumeroOrden()).append("</h2>");
        detalles.append("<p><b>Estado:</b> ");
        
        // Mostrar estado con color
        String estado = orden.getEstado();
        String colorEstado;
        if (estado.equals("espera")) {
            colorEstado = "orange";
        } else if (estado.equals("servicio")) {
            colorEstado = "blue";
        } else if (estado.equals("listo")) {
            colorEstado = "green";
        } else {
            colorEstado = "gray";
        }
        
        // Convertir primera letra a mayúscula
        estado = estado.substring(0, 1).toUpperCase() + estado.substring(1);
        detalles.append("<span style='color:").append(colorEstado).append("'>")
                .append(estado).append("</span></p>");
        
        // Información del cliente
        detalles.append("<h3>Cliente</h3>");
        detalles.append("<p>").append(orden.getCliente().getNombreCompleto()).append("<br>");
        detalles.append("DPI: ").append(orden.getCliente().getIdentificador()).append("</p>");
        
        // Información del automóvil
        detalles.append("<h3>Automóvil</h3>");
        detalles.append("<p>Marca: ").append(orden.getAutomovil().getMarca()).append("<br>");
        detalles.append("Modelo: ").append(orden.getAutomovil().getModelo()).append("<br>");
        detalles.append("Placa: ").append(orden.getAutomovil().getPlaca()).append("</p>");
        
        // Información del servicio
        detalles.append("<h3>Servicio Solicitado</h3>");
        detalles.append("<p>").append(orden.getServicio().getNombre()).append("<br>");
        detalles.append("<small>").append(orden.getServicio().getDescripcion()).append("</small></p>");
        
        // Fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        detalles.append("<p><b>Fecha de solicitud:</b><br>").append(sdf.format(orden.getFecha())).append("</p>");
        
        detalles.append("</body></html>");
        
        labelDetalles.setText(detalles.toString());
    }
    
    private void comenzarServicio() {
        if (tablaOrdenes.getSelectedRow() == -1) {
            showErrorMessage("Debe seleccionar una orden para comenzar");
            return;
        }
        
        // Obtener número de orden
        int numeroOrden = (int) tablaOrdenes.getValueAt(tablaOrdenes.getSelectedRow(), 0);
        
        // Buscar la orden en el gestor
        OrdenTrabajo orden = null;
        for (OrdenTrabajo o : GestorOrdenes.getInstancia().getOrdenesServicio()) {
            if (o.getNumeroOrden() == numeroOrden) {
                orden = o;
                break;
            }
        }
        
        if (orden == null) {
            showErrorMessage("No se encontró la orden");
            return;
        }
        
        // Verificar estado
        if (!orden.getEstado().equals("espera")) {
            showErrorMessage("Solo se pueden comenzar órdenes en estado 'En espera'");
            return;
        }
        
        // Cambiar estado
        orden.setEstado("servicio");
        
        // Guardar cambios
        Serializador.guardarDatos();
        
        // Mostrar mensaje de éxito
        showSuccessMessage("Orden marcada como 'En servicio'");
        
        // Actualizar vista
        cargarOrdenes();
    }
    
    private void completarOrden() {
        if (tablaOrdenes.getSelectedRow() == -1) {
            showErrorMessage("Debe seleccionar una orden para completar");
            return;
        }
        
        // Obtener número de orden
        int numeroOrden = (int) tablaOrdenes.getValueAt(tablaOrdenes.getSelectedRow(), 0);
        
        // Buscar la orden en el gestor
        OrdenTrabajo orden = null;
        for (OrdenTrabajo o : GestorOrdenes.getInstancia().getOrdenesServicio()) {
            if (o.getNumeroOrden() == numeroOrden) {
                orden = o;
                break;
            }
        }
        
        if (orden == null) {
            showErrorMessage("No se encontró la orden");
            return;
        }
        
        // Verificar estado
        if (!orden.getEstado().equals("servicio")) {
            showErrorMessage("Solo se pueden completar órdenes que están 'En servicio'");
            return;
        }
        
        // Confirmar acción
        if (!showConfirmDialog("¿Está seguro que desea marcar esta orden como completada?")) {
            return;
        }
        
        // Cambiar estado
        orden.setEstado("listo");
        
        // Guardar cambios
        Serializador.guardarDatos();
        
        // Mostrar mensaje de éxito
        showSuccessMessage("Orden completada con éxito");
        
        // Actualizar vista
        cargarOrdenes();
    }
    
    // Método para detener el timer cuando se cierra la ventana
    public void detenerActualizacion() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
    
    public JPanel getPanel() {
        return mainPanel;
    }
}
