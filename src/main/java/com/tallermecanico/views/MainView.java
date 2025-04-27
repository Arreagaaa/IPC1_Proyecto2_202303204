package com.tallermecanico.views;

import javax.swing.*;
import java.awt.*;

/**
 * Vista principal de bienvenida al sistema
 */
public class MainView extends BaseView {

    public MainView() {
        super("Taller Mecánico - Bienvenido");
        inicializarComponentes();
    }

    /**
     * Inicializa y configura los componentes de la interfaz
     */
    @Override
    protected void inicializarComponentes() {
        // Panel principal de contenido
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setOpaque(false); // Fondo transparente para mostrar el wallpaper
        setContentPane(mainPanel);

        // Panel superior con logo y título
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Logo del taller
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon("resources/logo.png"); // Ruta al logo
            Image img = logo.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            logoLabel.setText("TALLER MECÁNICO");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 48));
            logoLabel.setForeground(new Color(0, 0, 128)); // Azul oscuro
        }
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(logoLabel, BorderLayout.CENTER);

        // Título principal
        JLabel titleLabel = new JLabel("Sistema de Gestión de Taller Mecánico", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 0, 128)); // Azul oscuro
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Descripción
        JTextArea descriptionArea = new JTextArea(
                "Bienvenido al sistema de gestión para el taller mecánico. Este software le " +
                        "permite administrar clientes, vehículos, servicios, repuestos, órdenes de " +
                        "trabajo y facturas de manera eficiente.\n\n" +
                        "Seleccione una opción para continuar:");
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setForeground(Color.DARK_GRAY);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(0, 50, 30, 50));

        // Panel para botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 100));

        // Botón de inicio de sesión
        JButton loginButton = crearBoton("Iniciar Sesión");
        loginButton.setPreferredSize(new Dimension(200, 50));
        loginButton.addActionListener(e -> openLoginView());

        // Botón de registro (solo para clientes)
        JButton registerButton = crearBoton("Registrarse como Cliente");
        registerButton.setPreferredSize(new Dimension(200, 50));
        registerButton.addActionListener(e -> openRegisterView());

        // Añadir botones al panel
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Panel para información adicional en el pie
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);

        JLabel footerLabel = new JLabel("© 2025 Taller Mecánico - Todos los derechos reservados", JLabel.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        footerPanel.add(footerLabel, BorderLayout.CENTER);

        // Agregar componentes al panel principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(titleLabel, BorderLayout.CENTER);

        // Panel central que contiene la descripción y botones
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(descriptionArea, BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Abre la vista de inicio de sesión
     */
    private void openLoginView() {
        LoginView loginView = new LoginView();
        loginView.setVisible(true);
        this.dispose(); // Cerrar la vista actual
    }

    /**
     * Abre la vista de registro para clientes
     */
    private void openRegisterView() {
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de registro en desarrollo. Por favor, utilice la opción 'Iniciar Sesión'.");
    }
}