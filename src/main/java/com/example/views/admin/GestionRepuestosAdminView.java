package com.example.views.admin;

import com.example.models.GestorDatos;
import com.example.models.Repuesto;
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
 * Vista para la gestión de repuestos por parte del administrador
 */
public class GestionRepuestosAdminView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JTable tablaRepuestos;
    private DefaultTableModel modeloTabla;

    // Componentes para el formulario
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtExistencias;
    private JTextField txtPrecio;

    private Repuesto repuestoSeleccionado;

    public GestionRepuestosAdminView(Persona usuario) {
        super("Gestión de Repuestos");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Gestión de Repuestos");
        titlePanel.add(titleLabel);

        // Panel de tabla (izquierda)
        JPanel tablePanel = createTablePanel();

        // Panel de formulario (derecha)
        JPanel formPanel = createFormPanel();

        // Agregar componentes al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.EAST);

        // Cargar datos iniciales
        cargarRepuestos();
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Panel de botones superior
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setOpaque(false);

        JButton btnRefresh = new JButton("Actualizar");
        JButton btnImportar = new JButton("Importar .tmr");

        btnRefresh.addActionListener(e -> cargarRepuestos());
        btnImportar.addActionListener(e -> importarDesdeArchivo());

        buttonsPanel.add(btnRefresh);
        buttonsPanel.add(btnImportar);

        // Tabla de repuestos
        String[] columnas = { "Código", "Nombre", "Marca", "Modelo", "Existencias", "Precio" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaRepuestos = new JTable(modeloTabla);
        tablaRepuestos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaRepuestos);

        // Detectar selección
        tablaRepuestos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaRepuestos.getSelectedRow() != -1) {
                mostrarRepuestoSeleccionado();
            }
        });

        // Estructura del panel
        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Repuesto"));
        panel.setPreferredSize(new Dimension(300, 0));

        // Panel para formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Código
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createLabel("Código:"), gbc);

        gbc.gridy = 1;
        txtCodigo = new JTextField();
        txtCodigo.setEditable(false); // Autogenerado
        formPanel.add(txtCodigo, gbc);

        // Nombre
        gbc.gridy = 2;
        formPanel.add(createLabel("Nombre:"), gbc);

        gbc.gridy = 3;
        txtNombre = new JTextField();
        formPanel.add(txtNombre, gbc);

        // Marca
        gbc.gridy = 4;
        formPanel.add(createLabel("Marca:"), gbc);

        gbc.gridy = 5;
        txtMarca = new JTextField();
        formPanel.add(txtMarca, gbc);

        // Modelo
        gbc.gridy = 6;
        formPanel.add(createLabel("Modelo:"), gbc);

        gbc.gridy = 7;
        txtModelo = new JTextField();
        formPanel.add(txtModelo, gbc);

        // Existencias
        gbc.gridy = 8;
        formPanel.add(createLabel("Existencias:"), gbc);

        gbc.gridy = 9;
        txtExistencias = new JTextField();
        formPanel.add(txtExistencias, gbc);

        // Precio
        gbc.gridy = 10;
        formPanel.add(createLabel("Precio:"), gbc);

        gbc.gridy = 11;
        txtPrecio = new JTextField();
        formPanel.add(txtPrecio, gbc);

        // Panel para botones
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        buttonsPanel.setOpaque(false);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");

        btnNuevo.addActionListener(e -> nuevoRepuesto());
        btnGuardar.addActionListener(e -> guardarRepuesto());
        btnEliminar.addActionListener(e -> eliminarRepuesto());

        buttonsPanel.add(btnNuevo);
        buttonsPanel.add(btnGuardar);
        buttonsPanel.add(btnEliminar);

        // Estructura del panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarRepuestos() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener todos los repuestos
        Vector<Repuesto> repuestos = GestorDatos.getInstancia().getRepuestos();

        for (Repuesto repuesto : repuestos) {
            Object[] fila = {
                    repuesto.getCodigo(),
                    repuesto.getNombre(),
                    repuesto.getMarca(),
                    repuesto.getModelo(),
                    repuesto.getExistencias(),
                    String.format("Q%.2f", repuesto.getPrecio())
            };
            modeloTabla.addRow(fila);
        }

        // Si hay filas, seleccionar la primera
        if (tablaRepuestos.getRowCount() > 0) {
            tablaRepuestos.setRowSelectionInterval(0, 0);
            mostrarRepuestoSeleccionado();
        } else {
            limpiarFormulario();
        }
    }

    private void mostrarRepuestoSeleccionado() {
        int rowIndex = tablaRepuestos.getSelectedRow();
        if (rowIndex == -1)
            return;

        String codigo = (String) modeloTabla.getValueAt(rowIndex, 0);

        repuestoSeleccionado = null;
        for (Repuesto repuesto : GestorDatos.getInstancia().getRepuestos()) {
            if (repuesto.getCodigo().equals(codigo)) {
                repuestoSeleccionado = repuesto;
                break;
            }
        }

        if (repuestoSeleccionado != null) {
            txtCodigo.setText(repuestoSeleccionado.getCodigo());
            txtNombre.setText(repuestoSeleccionado.getNombre());
            txtMarca.setText(repuestoSeleccionado.getMarca());
            txtModelo.setText(repuestoSeleccionado.getModelo());
            txtExistencias.setText(String.valueOf(repuestoSeleccionado.getExistencias()));
            txtPrecio.setText(String.valueOf(repuestoSeleccionado.getPrecio()));
        }
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtExistencias.setText("");
        txtPrecio.setText("");
        repuestoSeleccionado = null;
    }

    private void nuevoRepuesto() {
        limpiarFormulario();
        txtCodigo.setText("[Autogenerado]");
        repuestoSeleccionado = null;
        tablaRepuestos.clearSelection();
    }

    private void guardarRepuesto() {
        // Validar campos
        if (txtNombre.getText().trim().isEmpty()) {
            showErrorMessage("El nombre del repuesto es obligatorio");
            return;
        }

        try {
            int existencias = Integer.parseInt(txtExistencias.getText().trim());
            if (existencias < 0) {
                showErrorMessage("Las existencias no pueden ser negativas");
                return;
            }

            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio <= 0) {
                showErrorMessage("El precio debe ser mayor a cero");
                return;
            }

            if (repuestoSeleccionado == null) {
                // Crear nuevo repuesto
                Repuesto nuevoRepuesto = new Repuesto(
                        null, // Se asignará automáticamente
                        txtNombre.getText().trim(),
                        txtMarca.getText().trim(),
                        txtModelo.getText().trim(),
                        existencias,
                        precio);

                GestorDatos.getInstancia().agregarRepuesto(nuevoRepuesto);
                showSuccessMessage("Repuesto creado exitosamente");
            } else {
                // Actualizar repuesto existente
                repuestoSeleccionado.setNombre(txtNombre.getText().trim());
                repuestoSeleccionado.setMarca(txtMarca.getText().trim());
                repuestoSeleccionado.setModelo(txtModelo.getText().trim());
                repuestoSeleccionado.setExistencias(existencias);
                repuestoSeleccionado.setPrecio(precio);

                showSuccessMessage("Repuesto actualizado exitosamente");
            }

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar tabla
            cargarRepuestos();

        } catch (NumberFormatException e) {
            showErrorMessage("Las existencias y precio deben ser valores numéricos válidos");
        }
    }

    private void eliminarRepuesto() {
        if (repuestoSeleccionado == null) {
            showErrorMessage("Debe seleccionar un repuesto para eliminar");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de eliminar el repuesto " + repuestoSeleccionado.getNombre() + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            GestorDatos.getInstancia().eliminarRepuesto(repuestoSeleccionado);

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar tabla
            cargarRepuestos();

            showSuccessMessage("Repuesto eliminado exitosamente");
        }
    }

    private void importarDesdeArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de repuestos");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo de repuestos (.tmr)", "tmr"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                // Contar repuestos antes de importar
                int repuestosAntes = GestorDatos.getInstancia().getRepuestos().size();

                // Importar repuestos
                CargadorDatos.cargarRepuestosDesdeArchivo(file.getAbsolutePath());

                // Contar repuestos después de importar
                int repuestosDespues = GestorDatos.getInstancia().getRepuestos().size();
                int repuestosImportados = repuestosDespues - repuestosAntes;

                // Guardar cambios
                Serializador.guardarDatos();

                // Recargar tabla
                cargarRepuestos();

                showSuccessMessage("Se importaron " + repuestosImportados + " repuestos exitosamente");

            } catch (Exception e) {
                showErrorMessage("Error al importar repuestos: " + e.getMessage());
            }
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}