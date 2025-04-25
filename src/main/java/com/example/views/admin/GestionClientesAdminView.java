package com.example.views.admin;

import com.example.models.Automovil;
import com.example.models.GestorDatos;
import com.example.models.personas.Cliente;
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
 * Vista para la gestión de clientes por parte del administrador
 */
public class GestionClientesAdminView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;

    // Panel de clientes
    private JTable tablaClientes;
    private DefaultTableModel modeloTablaClientes;

    // Panel de automóviles
    private JTable tablaAutomoviles;
    private DefaultTableModel modeloTablaAutomoviles;

    // Datos seleccionados
    private Cliente clienteSeleccionado;
    private Automovil automovilSeleccionado;

    // Componentes del formulario de cliente
    private JTextField txtDPI;
    private JTextField txtNombre;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JComboBox<String> comboTipoCliente;

    // Componentes del formulario de automóvil
    private JTextField txtPlaca;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtAnio;
    private JTextField txtColor;
    private JTextField txtTipo;
    private JButton btnAgregarAuto;
    private JButton btnEditarAuto;
    private JButton btnEliminarAuto;

    public GestionClientesAdminView(Persona usuario) {
        super("Gestión de Clientes");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Gestión de Clientes");
        titlePanel.add(titleLabel);

        // Panel principal dividido horizontalmente
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);

        // Panel izquierdo - Lista de clientes y formulario de cliente
        JPanel leftPanel = createClientsPanel();

        // Panel derecho - Lista de automóviles y formulario de automóvil
        JPanel rightPanel = createVehiclesPanel();

        // Configurar y agregar paneles al SplitPane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setOneTouchExpandable(true);

        // Agregar componentes al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarClientes();
    }

    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Panel superior con botones
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);

        JButton btnRefresh = new JButton("Actualizar");
        JButton btnImportar = new JButton("Importar .tmca");

        btnRefresh.addActionListener(e -> cargarClientes());
        btnImportar.addActionListener(e -> importarDesdeArchivo());

        topPanel.add(btnRefresh);
        topPanel.add(btnImportar);

        // Panel central con tabla de clientes
        String[] columnas = { "DPI", "Nombre", "Usuario", "Tipo", "Servicios" };
        modeloTablaClientes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaClientes = new JTable(modeloTablaClientes);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaClientes);

        // Detectar selección
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaClientes.getSelectedRow() != -1) {
                mostrarClienteSeleccionado();
            }
        });

        // Panel inferior con formulario de cliente
        JPanel formPanel = createClientFormPanel();

        // Estructura del panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createClientFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // DPI
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createLabel("DPI:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtDPI = new JTextField(20);
        formPanel.add(txtDPI, gbc);

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Nombre Completo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNombre = new JTextField(20);
        formPanel.add(txtNombre, gbc);

        // Usuario
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtUsuario = new JTextField(20);
        formPanel.add(txtUsuario, gbc);

        // Contraseña
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Contraseña:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPassword = new JPasswordField(20);
        formPanel.add(txtPassword, gbc);

        // Tipo de cliente
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Tipo de Cliente:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        comboTipoCliente = new JComboBox<>(new String[] { "normal", "oro" });
        formPanel.add(comboTipoCliente, gbc);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");

        btnNuevo.addActionListener(e -> nuevoCliente());
        btnGuardar.addActionListener(e -> guardarCliente());
        btnEliminar.addActionListener(e -> eliminarCliente());

        buttonPanel.add(btnNuevo);
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnEliminar);

        // Estructura del panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Automóviles del Cliente"));

        // Panel central con tabla de automóviles
        String[] columnas = { "Placa", "Marca", "Modelo", "Año", "Color", "Tipo" };
        modeloTablaAutomoviles = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaAutomoviles = new JTable(modeloTablaAutomoviles);
        tablaAutomoviles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaAutomoviles);

        // Detectar selección
        tablaAutomoviles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaAutomoviles.getSelectedRow() != -1) {
                mostrarAutomovilSeleccionado();
            }
        });

        // Panel inferior con formulario de automóvil
        JPanel formPanel = createVehicleFormPanel();

        // Estructura del panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createVehicleFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Automóvil"));

        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Placa
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createLabel("Placa:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPlaca = new JTextField(15);
        formPanel.add(txtPlaca, gbc);

        // Marca
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Marca:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtMarca = new JTextField(15);
        formPanel.add(txtMarca, gbc);

        // Modelo
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Modelo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtModelo = new JTextField(15);
        formPanel.add(txtModelo, gbc);

        // Año
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Año:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtAnio = new JTextField(15);
        formPanel.add(txtAnio, gbc);

        // Color
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Color:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtColor = new JTextField(15);
        formPanel.add(txtColor, gbc);

        // Tipo
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        formPanel.add(createLabel("Tipo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtTipo = new JTextField(15);
        formPanel.add(txtTipo, gbc);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        btnAgregarAuto = new JButton("Agregar");
        btnEditarAuto = new JButton("Editar");
        btnEliminarAuto = new JButton("Eliminar");

        btnAgregarAuto.addActionListener(e -> agregarAutomovil());
        btnEditarAuto.addActionListener(e -> editarAutomovil());
        btnEliminarAuto.addActionListener(e -> eliminarAutomovil());

        // Deshabilitar botones hasta que se seleccione un cliente
        btnAgregarAuto.setEnabled(false);
        btnEditarAuto.setEnabled(false);
        btnEliminarAuto.setEnabled(false);

        buttonPanel.add(btnAgregarAuto);
        buttonPanel.add(btnEditarAuto);
        buttonPanel.add(btnEliminarAuto);

        // Estructura del panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarClientes() {
        // Limpiar tabla
        modeloTablaClientes.setRowCount(0);

        // Obtener todos los clientes
        Vector<Cliente> clientes = GestorDatos.getInstancia().getClientes();

        for (Cliente cliente : clientes) {
            Object[] fila = {
                    cliente.getIdentificador(),
                    cliente.getNombreCompleto(),
                    cliente.getUsuario(),
                    cliente.getTipoCliente().toUpperCase(),
                    cliente.getServiciosRealizados()
            };
            modeloTablaClientes.addRow(fila);
        }

        // Si hay filas, seleccionar la primera
        if (tablaClientes.getRowCount() > 0) {
            tablaClientes.setRowSelectionInterval(0, 0);
            mostrarClienteSeleccionado();
        } else {
            limpiarFormularioCliente();
            limpiarFormularioAutomovil();
            clienteSeleccionado = null;
            automovilSeleccionado = null;
        }
    }

    private void mostrarClienteSeleccionado() {
        int rowIndex = tablaClientes.getSelectedRow();
        if (rowIndex == -1)
            return;

        String dpi = (String) modeloTablaClientes.getValueAt(rowIndex, 0);

        clienteSeleccionado = null;
        for (Cliente cliente : GestorDatos.getInstancia().getClientes()) {
            if (cliente.getIdentificador().equals(dpi)) {
                clienteSeleccionado = cliente;
                break;
            }
        }

        if (clienteSeleccionado != null) {
            // Llenar formulario
            txtDPI.setText(clienteSeleccionado.getIdentificador());
            txtNombre.setText(clienteSeleccionado.getNombreCompleto());
            txtUsuario.setText(clienteSeleccionado.getUsuario());
            txtPassword.setText(clienteSeleccionado.getPassword());

            if (clienteSeleccionado.getTipoCliente().equals("oro")) {
                comboTipoCliente.setSelectedIndex(1);
            } else {
                comboTipoCliente.setSelectedIndex(0);
            }

            // Habilitar botones de automóvil
            btnAgregarAuto.setEnabled(true);

            // Cargar automóviles del cliente
            cargarAutomovilesCliente();
        }
    }

    private void cargarAutomovilesCliente() {
        // Limpiar tabla
        modeloTablaAutomoviles.setRowCount(0);

        if (clienteSeleccionado != null) {
            for (Automovil auto : clienteSeleccionado.getAutomoviles()) {
                Object[] fila = {
                        auto.getPlaca(),
                        auto.getMarca(),
                        auto.getModelo(),
                        auto.getAnio(),
                        auto.getColor(),
                        auto.getTipo()
                };
                modeloTablaAutomoviles.addRow(fila);
            }

            // Si hay filas, seleccionar la primera
            if (tablaAutomoviles.getRowCount() > 0) {
                tablaAutomoviles.setRowSelectionInterval(0, 0);
                mostrarAutomovilSeleccionado();
            } else {
                limpiarFormularioAutomovil();
                automovilSeleccionado = null;
                btnEditarAuto.setEnabled(false);
                btnEliminarAuto.setEnabled(false);
            }
        }
    }

    private void mostrarAutomovilSeleccionado() {
        int rowIndex = tablaAutomoviles.getSelectedRow();
        if (rowIndex == -1)
            return;

        String placa = (String) modeloTablaAutomoviles.getValueAt(rowIndex, 0);

        automovilSeleccionado = null;
        if (clienteSeleccionado != null) {
            for (Automovil auto : clienteSeleccionado.getAutomoviles()) {
                if (auto.getPlaca().equals(placa)) {
                    automovilSeleccionado = auto;
                    break;
                }
            }
        }

        if (automovilSeleccionado != null) {
            // Llenar formulario
            txtPlaca.setText(automovilSeleccionado.getPlaca());
            txtMarca.setText(automovilSeleccionado.getMarca());
            txtModelo.setText(automovilSeleccionado.getModelo());
            txtAnio.setText(String.valueOf(automovilSeleccionado.getAnio()));
            txtColor.setText(automovilSeleccionado.getColor());
            txtTipo.setText(automovilSeleccionado.getTipo());

            // Habilitar botones
            btnEditarAuto.setEnabled(true);
            btnEliminarAuto.setEnabled(true);
        }
    }

    private void limpiarFormularioCliente() {
        txtDPI.setText("");
        txtNombre.setText("");
        txtUsuario.setText("");
        txtPassword.setText("");
        comboTipoCliente.setSelectedIndex(0);
    }

    private void limpiarFormularioAutomovil() {
        txtPlaca.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtAnio.setText("");
        txtColor.setText("");
        txtTipo.setText("");
    }

    private void nuevoCliente() {
        limpiarFormularioCliente();
        limpiarFormularioAutomovil();
        clienteSeleccionado = null;
        automovilSeleccionado = null;
        tablaClientes.clearSelection();
        modeloTablaAutomoviles.setRowCount(0);

        // Deshabilitar botones de automóvil
        btnAgregarAuto.setEnabled(false);
        btnEditarAuto.setEnabled(false);
        btnEliminarAuto.setEnabled(false);
    }

    private void guardarCliente() {
        // Validar campos
        if (txtDPI.getText().trim().isEmpty() ||
                txtNombre.getText().trim().isEmpty() ||
                txtUsuario.getText().trim().isEmpty() ||
                txtPassword.getPassword().length == 0) {

            showErrorMessage("Todos los campos son obligatorios");
            return;
        }

        String dpi = txtDPI.getText().trim();
        String nombre = txtNombre.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        String tipoCliente = (String) comboTipoCliente.getSelectedItem();

        // Verificar si es un nuevo cliente o una edición
        if (clienteSeleccionado == null) {
            // Verificar si ya existe un cliente con ese DPI
            for (Cliente c : GestorDatos.getInstancia().getClientes()) {
                if (c.getIdentificador().equals(dpi)) {
                    showErrorMessage("Ya existe un cliente con ese DPI");
                    return;
                }
            }

            // Verificar si ya existe un usuario con ese nombre de usuario
            for (Persona p : GestorDatos.getInstancia().getPersonas()) {
                if (p.getUsuario().equals(usuario)) {
                    showErrorMessage("Ya existe un usuario con ese nombre de usuario");
                    return;
                }
            }

            // Crear nuevo cliente
            Cliente nuevoCliente = new Cliente(dpi, nombre, usuario, password);
            nuevoCliente.setTipoCliente(tipoCliente);

            GestorDatos.getInstancia().agregarPersona(nuevoCliente);
            showSuccessMessage("Cliente creado exitosamente");

            // Seleccionar el nuevo cliente
            clienteSeleccionado = nuevoCliente;

            // Habilitar botón para agregar automóviles
            btnAgregarAuto.setEnabled(true);

        } else {
            // Verificar si se está cambiando el DPI y si ya existe otro cliente con ese DPI
            if (!clienteSeleccionado.getIdentificador().equals(dpi)) {
                for (Cliente c : GestorDatos.getInstancia().getClientes()) {
                    if (c.getIdentificador().equals(dpi) && c != clienteSeleccionado) {
                        showErrorMessage("Ya existe un cliente con ese DPI");
                        return;
                    }
                }
            }

            // Verificar si se está cambiando el usuario y si ya existe otro con ese nombre
            if (!clienteSeleccionado.getUsuario().equals(usuario)) {
                for (Persona p : GestorDatos.getInstancia().getPersonas()) {
                    if (p.getUsuario().equals(usuario) && p != clienteSeleccionado) {
                        showErrorMessage("Ya existe un usuario con ese nombre de usuario");
                        return;
                    }
                }
            }

            // Actualizar cliente existente
            clienteSeleccionado.setIdentificador(dpi);
            clienteSeleccionado.setNombreCompleto(nombre);
            clienteSeleccionado.setUsuario(usuario);
            clienteSeleccionado.setPassword(password);
            clienteSeleccionado.setTipoCliente(tipoCliente);

            showSuccessMessage("Cliente actualizado exitosamente");
        }

        // Guardar cambios
        Serializador.guardarDatos();

        // Recargar tabla
        cargarClientes();
    }

    private void eliminarCliente() {
        if (clienteSeleccionado == null) {
            showErrorMessage("Debe seleccionar un cliente para eliminar");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de eliminar al cliente " + clienteSeleccionado.getNombreCompleto() + "?\n" +
                        "Se eliminarán también todos sus automóviles asociados.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            GestorDatos.getInstancia().eliminarPersona(clienteSeleccionado);

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar tabla
            cargarClientes();

            // Limpiar formularios
            limpiarFormularioCliente();
            limpiarFormularioAutomovil();
            clienteSeleccionado = null;
            automovilSeleccionado = null;

            // Deshabilitar botones de automóvil
            btnAgregarAuto.setEnabled(false);
            btnEditarAuto.setEnabled(false);
            btnEliminarAuto.setEnabled(false);

            showSuccessMessage("Cliente eliminado exitosamente");
        }
    }

    private void agregarAutomovil() {
        if (clienteSeleccionado == null) {
            showErrorMessage("Debe seleccionar o crear un cliente primero");
            return;
        }

        // Limpiar formulario
        limpiarFormularioAutomovil();
        automovilSeleccionado = null;
        tablaAutomoviles.clearSelection();

        // Deshabilitar botones
        btnEditarAuto.setEnabled(false);
        btnEliminarAuto.setEnabled(false);
    }

    private void editarAutomovil() {
        if (automovilSeleccionado == null) {
            showErrorMessage("Debe seleccionar un automóvil para editar");
            return;
        }

        // Validar campos
        if (txtPlaca.getText().trim().isEmpty() ||
                txtMarca.getText().trim().isEmpty() ||
                txtModelo.getText().trim().isEmpty() ||
                txtAnio.getText().trim().isEmpty() ||
                txtColor.getText().trim().isEmpty() ||
                txtTipo.getText().trim().isEmpty()) {

            showErrorMessage("Todos los campos son obligatorios");
            return;
        }

        try {
            int anio = Integer.parseInt(txtAnio.getText().trim());

            String placa = txtPlaca.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            String color = txtColor.getText().trim();
            String tipo = txtTipo.getText().trim();

            // Verificar si se está cambiando la placa y si ya existe otro con esa placa
            if (!automovilSeleccionado.getPlaca().equals(placa)) {
                for (Automovil a : GestorDatos.getInstancia().getAutomoviles()) {
                    if (a.getPlaca().equals(placa) && a != automovilSeleccionado) {
                        showErrorMessage("Ya existe un automóvil con esa placa");
                        return;
                    }
                }
            }

            // Actualizar automóvil
            automovilSeleccionado.setPlaca(placa);
            automovilSeleccionado.setMarca(marca);
            automovilSeleccionado.setModelo(modelo);
            automovilSeleccionado.setAnio(anio);
            automovilSeleccionado.setColor(color);
            automovilSeleccionado.setTipo(tipo);

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar automóviles
            cargarAutomovilesCliente();

            showSuccessMessage("Automóvil actualizado exitosamente");

        } catch (NumberFormatException e) {
            showErrorMessage("El año debe ser un número entero");
        }
    }

    private void eliminarAutomovil() {
        if (automovilSeleccionado == null) {
            showErrorMessage("Debe seleccionar un automóvil para eliminar");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de eliminar el automóvil " + automovilSeleccionado.getMarca() + " " +
                        automovilSeleccionado.getModelo() + " (" + automovilSeleccionado.getPlaca() + ")?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            // Eliminar automóvil del cliente
            clienteSeleccionado.eliminarAutomovil(automovilSeleccionado);

            // Eliminar automóvil del gestor
            GestorDatos.getInstancia().eliminarAutomovil(automovilSeleccionado);

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar automóviles
            cargarAutomovilesCliente();

            // Limpiar formulario
            limpiarFormularioAutomovil();
            automovilSeleccionado = null;

            // Deshabilitar botones
            btnEditarAuto.setEnabled(false);
            btnEliminarAuto.setEnabled(false);

            showSuccessMessage("Automóvil eliminado exitosamente");
        }
    }

    private void importarDesdeArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de clientes y automóviles");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo de clientes (.tmca)", "tmca"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                // Contar clientes antes de importar
                int clientesAntes = GestorDatos.getInstancia().getClientes().size();

                // Importar clientes y automóviles
                CargadorDatos.cargarClientesYAutomovilesDesdeArchivo(file.getAbsolutePath());

                // Contar clientes después de importar
                int clientesDespues = GestorDatos.getInstancia().getClientes().size();
                int clientesImportados = clientesDespues - clientesAntes;

                // Guardar cambios
                Serializador.guardarDatos();

                // Recargar tabla
                cargarClientes();

                showSuccessMessage("Se importaron " + clientesImportados + " clientes exitosamente");

            } catch (Exception e) {
                showErrorMessage("Error al importar clientes y automóviles: " + e.getMessage());
            }
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}