package com.example.views;

import com.example.models.GestorDatos;
import com.example.models.personas.Persona;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Vista para iniciar sesión en el sistema
 */
public class LoginView extends BaseView {
    private JTextField campoUsuario;
    private JPasswordField campoContraseña;

    public LoginView() {
        super("Taller Mecánico - Iniciar Sesión");
        setSize(450, 380);
        setResizable(false); // No redimensionable
        inicializarComponentes();
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        // Panel principal con fondo blanco y bordes
        JPanel mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(0, 15));

        // Panel de logo y título
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Título principal
        JLabel titleLabel = new JLabel("TALLER MECÁNICO", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Panel para formulario
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        // Título del formulario
        JLabel loginTitle = createSubtitle("Iniciar Sesión");
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campos de texto estilizados
        campoUsuario = createStylishTextField(15);
        campoContraseña = createStylishPasswordField(15);

        // Panel para usuario
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setOpaque(false);
        userPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        JLabel userLabel = createLabel("Usuario:");
        userPanel.add(userLabel, BorderLayout.WEST);
        userPanel.add(campoUsuario, BorderLayout.CENTER);

        // Panel para contraseña
        JPanel passPanel = new JPanel(new BorderLayout(10, 0));
        passPanel.setOpaque(false);
        passPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        JLabel passLabel = createLabel("Contraseña:");
        passPanel.add(passLabel, BorderLayout.WEST);
        passPanel.add(campoContraseña, BorderLayout.CENTER);

        // Agregar componentes al formulario
        formPanel.add(loginTitle);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(userPanel);
        formPanel.add(passPanel);

        // Panel para botones con espacio uniforme
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setOpaque(false);

        // Botones estilizados
        JButton loginButton = createPrimaryButton("Iniciar Sesión");
        loginButton.setPreferredSize(new Dimension(150, 40));

        JButton registerButton = createSecondaryButton("Registrarse");
        registerButton.setPreferredSize(new Dimension(150, 40));

        // Agregar acción a botones
        loginButton.addActionListener(e -> iniciarSesion());
        registerButton.addActionListener(e -> abrirRegistro());

        // Permitir inicio de sesión con tecla Enter
        campoContraseña.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    iniciarSesion();
                }
            }
        });

        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);

        // Panel de información para credenciales por defecto
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        JLabel infoLabel = new JLabel("Admin por defecto: usuario 'admin', contraseña 'admin123'");
        infoLabel.setFont(SMALL_FONT);
        infoLabel.setForeground(new Color(102, 102, 102));
        infoPanel.add(infoLabel);

        // Agregar todo al panel principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel sur con botones e información
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(buttonsPanel, BorderLayout.CENTER);
        southPanel.add(infoPanel, BorderLayout.SOUTH);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        // Establecer el panel principal como contenido
        setContentPane(mainPanel);
    }

    private void iniciarSesion() {
        String usuario = campoUsuario.getText();
        String contraseña = new String(campoContraseña.getPassword());

        if (usuario.isEmpty() || contraseña.isEmpty()) {
            showErrorMessage("Por favor complete todos los campos");
            return;
        }

        Object usuarioAutenticado = GestorDatos.getInstancia().autenticarUsuario(usuario, contraseña);

        if (usuarioAutenticado == null) {
            showErrorMessage("Usuario o contraseña incorrectos");
            return;
        }

        // Abrir la vista principal con el usuario autenticado
        new MainView((Persona) usuarioAutenticado).setVisible(true);
        dispose(); // Cerrar ventana de login
    }

    private void abrirRegistro() {
        new RegistroClienteView().setVisible(true);
        dispose(); // Cerrar ventana de login
    }
}