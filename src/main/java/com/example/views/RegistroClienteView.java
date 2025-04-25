package com.example.views;

import com.example.models.GestorDatos;
import com.example.models.personas.Cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Vista para el registro de nuevos clientes
 */
public class RegistroClienteView extends BaseView {
    private JTextField campoDPI;
    private JTextField campoNombre;
    private JTextField campoUsuario;
    private JPasswordField campoContraseña;

    public RegistroClienteView() {
        super("Taller Mecánico - Registro de Cliente");
        setSize(550, 450);
        setResizable(false); // No redimensionable
        inicializarComponentes();
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        // Panel principal con diseño BorderLayout
        JPanel mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(0, 15));

        // Panel de cabecera con título
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("REGISTRO DE NUEVO CLIENTE", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Etiquetas y campos
        JLabel dpiLabel = createLabel("DPI:");
        JLabel nombreLabel = createLabel("Nombre Completo:");
        JLabel usuarioLabel = createLabel("Usuario:");
        JLabel contraseñaLabel = createLabel("Contraseña:");

        campoDPI = createStylishTextField(15);
        campoNombre = createStylishTextField(15);
        campoUsuario = createStylishTextField(15);
        campoContraseña = createStylishPasswordField(15);

        // Ubicar componentes en el GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(dpiLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(campoDPI, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(nombreLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(campoNombre, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(usuarioLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(campoUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(contraseñaLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(campoContraseña, gbc);

        // Panel de botones
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setOpaque(false);

        JButton registerButton = createSuccessButton("Registrarse");
        registerButton.setPreferredSize(new Dimension(150, 40));

        JButton backButton = createAccentButton("Volver");
        backButton.setPreferredSize(new Dimension(150, 40));

        // Acción para el botón registrar
        registerButton.addActionListener(e -> registrarCliente());

        // Acción para el botón volver
        backButton.addActionListener(e -> volverLogin());

        // Permitir registro con tecla Enter
        campoContraseña.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    registrarCliente();
                }
            }
        });

        buttonsPanel.add(registerButton);
        buttonsPanel.add(backButton);

        // Nota informativa
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        JLabel infoLabel = new JLabel("* Al registrarse comenzará como Cliente Normal");
        infoLabel.setFont(SMALL_FONT);
        infoLabel.setForeground(new Color(102, 102, 102));
        infoPanel.add(infoLabel);

        // Panel inferior con botones e información
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(buttonsPanel, BorderLayout.CENTER);
        southPanel.add(infoPanel, BorderLayout.SOUTH);

        // Agregar los paneles al panel principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        // Establecer el panel principal como contenido
        setContentPane(mainPanel);
    }

    private void registrarCliente() {
        // Obtener datos del formulario
        String dpi = campoDPI.getText().trim();
        String nombre = campoNombre.getText().trim();
        String usuario = campoUsuario.getText().trim();
        String contraseña = new String(campoContraseña.getPassword());

        // Validar que no haya campos vacíos
        if (dpi.isEmpty() || nombre.isEmpty() || usuario.isEmpty() || contraseña.isEmpty()) {
            showErrorMessage("Por favor complete todos los campos");
            return;
        }

        // Validar formato del DPI (debe ser numérico y tener longitud correcta)
        if (!dpi.matches("\\d+") || dpi.length() < 5 || dpi.length() > 13) {
            showErrorMessage("El DPI debe ser un número entre 5 y 13 dígitos");
            return;
        }

        // Verificar si el DPI o usuario ya existen
        for (Cliente cliente : GestorDatos.getInstancia().getClientes()) {
            if (cliente.getIdentificador().equals(dpi)) {
                showErrorMessage("Ya existe un cliente con ese DPI");
                return;
            }
            if (cliente.getUsuario().equals(usuario)) {
                showErrorMessage("El nombre de usuario ya está en uso");
                return;
            }
        }

        // Crear nuevo cliente (tipo por defecto: normal)
        Cliente nuevoCliente = new Cliente(dpi, nombre, usuario, contraseña);
        nuevoCliente.setTipoCliente("normal");

        // Agregar cliente al gestor
        GestorDatos.getInstancia().agregarCliente(nuevoCliente);

        // Mostrar mensaje de éxito
        showSuccessMessage("Cliente registrado exitosamente");

        // Volver a la vista de login
        volverLogin();
    }

    private void volverLogin() {
        new LoginView().setVisible(true);
        dispose();
    }
}
