package com.example.views.admin;

import com.example.models.GestorDatos;
import com.example.models.Repuesto;
import com.example.models.Servicio;
import com.example.models.personas.Persona;
import com.example.utils.CargadorDatos;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Vector;

/**
 * Vista para la gestión de servicios por parte del administrador
 */
public class GestionServiciosAdminView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JTable tablaServicios;
    private DefaultTableModel modeloTablaServicios;
    private JTable tablaRepuestos;
    private DefaultTableModel modeloTablaRepuestos;

    private Servicio servicioSeleccionado;

    // Componentes para formulario
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtPrecioManoObra;
    private JTextField txtTiempoEstimado;
    private JTextArea txtDescripcion;

    public GestionServiciosAdminView(Persona usuario) {
        super("Gestión de Servicios");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Gestión de Servicios");
        titlePanel.add(titleLabel);

        // Panel principal dividido horizontalmente
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);

        // Panel izquierdo - Lista de servicios
        JPanel leftPanel = createServicesListPanel();

        // Panel derecho - Detalles del servicio y repuestos
        JPanel rightPanel = createServiceDetailPanel();

        // Configurar y agregar paneles al SplitPane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(400);
        splitPane.setOneTouchExpandable(true);

        // Agregar componentes al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarServicios();
    }

    private JPanel createServicesListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Panel de botones superior
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setOpaque(false);

        JButton btnRefresh = new JButton("Actualizar");
        JButton btnImportar = new JButton("Importar .tms");

        btnRefresh.addActionListener(e -> cargarServicios());
        btnImportar.addActionListener(e -> importarDesdeArchivo());

        buttonsPanel.add(btnRefresh);
        buttonsPanel.add(btnImportar);

        // Tabla de servicios
        Vector<String> columnas = new Vector<>();
        columnas.add("Código");
        columnas.add("Nombre");
        columnas.add("Marca");
        columnas.add("Modelo");
        columnas.add("Precio MO");
        columnas.add("Precio Total");

        modeloTablaServicios = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaServicios = new JTable(modeloTablaServicios);
        tablaServicios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaServicios);

        // Detectar selección
        tablaServicios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaServicios.getSelectedRow() != -1) {
                mostrarServicioSeleccionado();
            }
        });

        // Estructura del panel
        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de acción para servicios
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionsPanel.setOpaque(false);

        JButton btnNuevo = new JButton("Nuevo Servicio");
        JButton btnEliminar = new JButton("Eliminar Servicio");

        btnNuevo.addActionListener(e -> nuevoServicio());
        btnEliminar.addActionListener(e -> eliminarServicio());

        actionsPanel.add(btnNuevo);
        actionsPanel.add(btnEliminar);

        panel.add(actionsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createServiceDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Panel de formulario
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del Servicio"));

        // Campos de formulario
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Código
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(createLabel("Código:"), gbc);

        gbc.gridx = 1;
        txtCodigo = new JTextField();
        txtCodigo.setEditable(false); // Autogenerado
        fieldsPanel.add(txtCodigo, gbc);

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldsPanel.add(createLabel("Nombre:"), gbc);

        gbc.gridx = 1;
        txtNombre = new JTextField();
        fieldsPanel.add(txtNombre, gbc);

        // Marca
        gbc.gridx = 0;
        gbc.gridy = 2;
        fieldsPanel.add(createLabel("Marca:"), gbc);

        gbc.gridx = 1;
        txtMarca = new JTextField();
        fieldsPanel.add(txtMarca, gbc);

        // Modelo
        gbc.gridx = 0;
        gbc.gridy = 3;
        fieldsPanel.add(createLabel("Modelo:"), gbc);

        gbc.gridx = 1;
        txtModelo = new JTextField();
        fieldsPanel.add(txtModelo, gbc);

        // Precio mano de obra
        gbc.gridx = 0;
        gbc.gridy = 4;
        fieldsPanel.add(createLabel("Precio Mano de Obra:"), gbc);

        gbc.gridx = 1;
        txtPrecioManoObra = new JTextField();
        fieldsPanel.add(txtPrecioManoObra, gbc);

        // Tiempo estimado
        gbc.gridx = 0;
        gbc.gridy = 5;
        fieldsPanel.add(createLabel("Tiempo Estimado (hrs):"), gbc);

        gbc.gridx = 1;
        txtTiempoEstimado = new JTextField();
        fieldsPanel.add(txtTiempoEstimado, gbc);

        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        fieldsPanel.add(createLabel("Descripción:"), gbc);

        gbc.gridy = 7;
        gbc.gridheight = 2;
        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        fieldsPanel.add(scrollDescripcion, gbc);

        // Botón guardar
        gbc.gridy = 9;
        gbc.gridheight = 1;
        JButton btnGuardar = createPrimaryButton("Guardar Servicio");
        btnGuardar.addActionListener(e -> guardarServicio());
        fieldsPanel.add(btnGuardar, gbc);

        formPanel.add(fieldsPanel, BorderLayout.NORTH);

        // Panel de repuestos
        JPanel repuestosPanel = new JPanel(new BorderLayout(0, 10));
        repuestosPanel.setOpaque(false);
        repuestosPanel.setBorder(BorderFactory.createTitledBorder("Repuestos del Servicio"));

        // Tabla de repuestos
        Vector<String> columnasRepuestos = new Vector<>();
        columnasRepuestos.add("Código");
        columnasRepuestos.add("Nombre");
        columnasRepuestos.add("Marca");
        columnasRepuestos.add("Modelo");
        columnasRepuestos.add("Precio");

        modeloTablaRepuestos = new DefaultTableModel(columnasRepuestos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaRepuestos = new JTable(modeloTablaRepuestos);
        tablaRepuestos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollRepuestos = new JScrollPane(tablaRepuestos);

        repuestosPanel.add(scrollRepuestos, BorderLayout.CENTER);

        // Panel de botones para repuestos
        JPanel btnRepuestosPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRepuestosPanel.setOpaque(false);

        JButton btnAgregarRepuesto = new JButton("Agregar Repuesto");
        JButton btnEliminarRepuesto = new JButton("Eliminar Repuesto");

        btnAgregarRepuesto.addActionListener(e -> agregarRepuesto());
        btnEliminarRepuesto.addActionListener(e -> eliminarRepuesto());

        btnRepuestosPanel.add(btnAgregarRepuesto);
        btnRepuestosPanel.add(btnEliminarRepuesto);

        repuestosPanel.add(btnRepuestosPanel, BorderLayout.SOUTH);

        // Combinar todos los elementos
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(repuestosPanel, BorderLayout.CENTER);

        return panel;
    }

    private void cargarServicios() {
        // Limpiar tabla
        modeloTablaServicios.setRowCount(0);

        // Obtener todos los servicios
        Vector<Servicio> servicios = GestorDatos.getInstancia().getServicios();

        for (int i = 0; i < servicios.size(); i++) {
            Servicio servicio = servicios.elementAt(i);
            Object[] fila = {
                    servicio.getCodigo(),
                    servicio.getNombre(),
                    servicio.getMarca(),
                    servicio.getModelo(),
                    String.format("Q%.2f", servicio.getPrecioManoObra()),
                    String.format("Q%.2f", servicio.getPrecio())
            };
            modeloTablaServicios.addRow(fila);
        }

        // Si hay filas, seleccionar la primera
        if (tablaServicios.getRowCount() > 0) {
            tablaServicios.setRowSelectionInterval(0, 0);
            mostrarServicioSeleccionado();
        } else {
            limpiarFormulario();
        }
    }

    private void mostrarServicioSeleccionado() {
        int rowIndex = tablaServicios.getSelectedRow();
        if (rowIndex == -1)
            return;

        String codigo = (String) modeloTablaServicios.getValueAt(rowIndex, 0);

        servicioSeleccionado = null;
        Vector<Servicio> servicios = GestorDatos.getInstancia().getServicios();
        for (int i = 0; i < servicios.size(); i++) {
            Servicio servicio = servicios.elementAt(i);
            if (servicio.getCodigo().equals(codigo)) {
                servicioSeleccionado = servicio;
                break;
            }
        }

        if (servicioSeleccionado != null) {
            // Llenar formulario
            txtCodigo.setText(servicioSeleccionado.getCodigo());
            txtNombre.setText(servicioSeleccionado.getNombre());
            txtMarca.setText(servicioSeleccionado.getMarca());
            txtModelo.setText(servicioSeleccionado.getModelo());
            txtPrecioManoObra.setText(String.valueOf(servicioSeleccionado.getPrecioManoObra()));
            txtTiempoEstimado.setText(String.valueOf(servicioSeleccionado.getTiempoEstimado()));
            txtDescripcion.setText(servicioSeleccionado.getDescripcion());

            // Cargar repuestos
            cargarRepuestosServicio();
        }
    }

    private void cargarRepuestosServicio() {
        // Limpiar tabla de repuestos
        modeloTablaRepuestos.setRowCount(0);

        if (servicioSeleccionado != null) {
            Vector<Repuesto> repuestos = servicioSeleccionado.getRepuestos();
            for (int i = 0; i < repuestos.size(); i++) {
                Repuesto repuesto = repuestos.elementAt(i);
                Object[] fila = {
                        repuesto.getCodigo(),
                        repuesto.getNombre(),
                        repuesto.getMarca(),
                        repuesto.getModelo(),
                        String.format("Q%.2f", repuesto.getPrecio())
                };
                modeloTablaRepuestos.addRow(fila);
            }
        }
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtPrecioManoObra.setText("");
        txtTiempoEstimado.setText("");
        txtDescripcion.setText("");
        servicioSeleccionado = null;

        // Limpiar tabla de repuestos
        modeloTablaRepuestos.setRowCount(0);
    }

    private void nuevoServicio() {
        limpiarFormulario();
        txtCodigo.setText("[Autogenerado]");
        servicioSeleccionado = null;
        tablaServicios.clearSelection();
    }

    private void guardarServicio() {
        // Validar campos
        if (txtNombre.getText().trim().isEmpty()) {
            showErrorMessage("El nombre del servicio es obligatorio");
            return;
        }

        try {
            double precioManoObra = Double.parseDouble(txtPrecioManoObra.getText().trim());
            if (precioManoObra <= 0) {
                showErrorMessage("El precio de mano de obra debe ser mayor a cero");
                return;
            }

            int tiempoEstimado = Integer.parseInt(txtTiempoEstimado.getText().trim());
            if (tiempoEstimado <= 0) {
                showErrorMessage("El tiempo estimado debe ser mayor a cero");
                return;
            }

            String marca = txtMarca.getText().trim().isEmpty() ? "cualquiera" : txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim().isEmpty() ? "cualquiera" : txtModelo.getText().trim();
            String descripcion = txtDescripcion.getText().trim();

            if (servicioSeleccionado == null) {
                // Crear nuevo servicio
                Servicio nuevoServicio = new Servicio(
                        null, // Se asignará automáticamente
                        txtNombre.getText().trim(),
                        marca,
                        modelo,
                        precioManoObra,
                        tiempoEstimado,
                        descripcion);

                GestorDatos.getInstancia().agregarServicio(nuevoServicio);
                servicioSeleccionado = nuevoServicio;
                showSuccessMessage("Servicio creado exitosamente");
            } else {
                // Actualizar servicio existente
                servicioSeleccionado.setNombre(txtNombre.getText().trim());
                servicioSeleccionado.setMarca(marca);
                servicioSeleccionado.setModelo(modelo);
                servicioSeleccionado.setPrecioManoObra(precioManoObra);
                servicioSeleccionado.setTiempoEstimado(tiempoEstimado);
                servicioSeleccionado.setDescripcion(descripcion);

                showSuccessMessage("Servicio actualizado exitosamente");
            }

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar tabla
            cargarServicios();

        } catch (NumberFormatException e) {
            showErrorMessage("El precio y tiempo estimado deben ser valores numéricos válidos");
        }
    }

    private void eliminarServicio() {
        if (servicioSeleccionado == null) {
            showErrorMessage("Debe seleccionar un servicio para eliminar");
            return;
        }

        // Verificar si es el servicio de diagnóstico (no se debe eliminar)
        if (servicioSeleccionado.getNombre().equalsIgnoreCase("Diagnóstico")) {
            showErrorMessage("No se puede eliminar el servicio de Diagnóstico");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de eliminar el servicio " + servicioSeleccionado.getNombre() + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            GestorDatos.getInstancia().eliminarServicio(servicioSeleccionado);

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar tabla
            cargarServicios();

            // Limpiar formulario
            limpiarFormulario();

            showSuccessMessage("Servicio eliminado exitosamente");
        }
    }

    private void agregarRepuesto() {
        if (servicioSeleccionado == null) {
            showErrorMessage("Debe seleccionar o crear un servicio primero");
            return;
        }

        // Crear selector de repuestos disponibles
        Vector<Repuesto> repuestosDisponibles = GestorDatos.getInstancia().getRepuestos();

        if (repuestosDisponibles.isEmpty()) {
            showErrorMessage("No hay repuestos disponibles para agregar");
            return;
        }

        // Filtrar repuestos ya agregados y por compatibilidad
        Vector<Repuesto> repuestosFiltrados = new Vector<>();
        for (int i = 0; i < repuestosDisponibles.size(); i++) {
            Repuesto repuesto = repuestosDisponibles.elementAt(i);
            if (servicioSeleccionado.esRepuestoCompatible(repuesto)) {
                // Verificar que no esté ya agregado
                boolean yaAgregado = false;
                Vector<Repuesto> repuestosServicio = servicioSeleccionado.getRepuestos();
                for (int j = 0; j < repuestosServicio.size(); j++) {
                    Repuesto r = repuestosServicio.elementAt(j);
                    if (r.getCodigo().equals(repuesto.getCodigo())) {
                        yaAgregado = true;
                        break;
                    }
                }

                if (!yaAgregado) {
                    repuestosFiltrados.add(repuesto);
                }
            }
        }

        if (repuestosFiltrados.isEmpty()) {
            showErrorMessage("No hay repuestos compatibles disponibles");
            return;
        }

        // Crear diálogo para seleccionar repuesto
        Repuesto repuestoSeleccionado = (Repuesto) JOptionPane.showInputDialog(
                null,
                "Seleccione un repuesto para agregar:",
                "Agregar Repuesto",
                JOptionPane.QUESTION_MESSAGE,
                null,
                repuestosFiltrados.toArray(),
                repuestosFiltrados.get(0));

        if (repuestoSeleccionado != null) {
            // Agregar repuesto al servicio
            servicioSeleccionado.agregarRepuesto(repuestoSeleccionado);

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar repuestos
            cargarRepuestosServicio();

            // Actualizar precio total en la tabla de servicios
            cargarServicios();

            showSuccessMessage("Repuesto agregado exitosamente");
        }
    }

    private void eliminarRepuesto() {
        if (servicioSeleccionado == null) {
            showErrorMessage("Debe seleccionar un servicio primero");
            return;
        }

        int rowIndex = tablaRepuestos.getSelectedRow();
        if (rowIndex == -1) {
            showErrorMessage("Debe seleccionar un repuesto para eliminar");
            return;
        }

        String codigo = (String) modeloTablaRepuestos.getValueAt(rowIndex, 0);

        // Buscar el repuesto en el servicio
        Repuesto repuestoEliminar = null;
        Vector<Repuesto> repuestosServicio = servicioSeleccionado.getRepuestos();
        for (int i = 0; i < repuestosServicio.size(); i++) {
            Repuesto repuesto = repuestosServicio.elementAt(i);
            if (repuesto.getCodigo().equals(codigo)) {
                repuestoEliminar = repuesto;
                break;
            }
        }

        if (repuestoEliminar != null) {
            // Eliminar repuesto del servicio
            servicioSeleccionado.eliminarRepuesto(repuestoEliminar);

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar repuestos
            cargarRepuestosServicio();

            // Actualizar precio total en la tabla de servicios
            cargarServicios();

            showSuccessMessage("Repuesto eliminado del servicio");
        }
    }

    private void importarDesdeArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de servicios");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo de servicios (.tms)", "tms"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                // Contar servicios antes de importar
                int serviciosAntes = GestorDatos.getInstancia().getServicios().size();

                // Importar servicios
                CargadorDatos.cargarServiciosDesdeArchivo(file.getAbsolutePath());

                // Contar servicios después de importar
                int serviciosDespues = GestorDatos.getInstancia().getServicios().size();
                int serviciosImportados = serviciosDespues - serviciosAntes;

                // Guardar cambios
                Serializador.guardarDatos();

                // Recargar tabla
                cargarServicios();

                showSuccessMessage("Se importaron " + serviciosImportados + " servicios exitosamente");

            } catch (Exception e) {
                showErrorMessage("Error al importar servicios: " + e.getMessage());
            }
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}