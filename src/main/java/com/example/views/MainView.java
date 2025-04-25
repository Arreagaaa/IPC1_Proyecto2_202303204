package com.example.views;

import com.example.models.GestorDatos;
import com.example.models.personas.*;
import com.example.utils.Serializador;
import com.example.views.admin.MenuAdministradorView;
import com.example.views.cliente.MenuClienteView;
import com.example.views.mecanico.MenuMecanicoView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Vista principal que se adapta según el tipo de usuario
 */
public class MainView extends BaseView {

    private Persona usuarioActual;

    public MainView(Persona usuario) {
        super("Taller Mecánico - Panel Principal");
        this.usuarioActual = usuario;
        setSize(900, 650);
        setLocationRelativeTo(null);
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Crear la barra superior con información de usuario
        JPanel topBar = crearBarraSuperior();

        // Determinar qué menú mostrar según el tipo de usuario
        JPanel menuPanel = null;

        if (usuarioActual instanceof Administrador) {
            menuPanel = new MenuAdministradorView(usuarioActual).getPanel();
        } else if (usuarioActual instanceof Mecanico) {
            menuPanel = new MenuMecanicoView(usuarioActual).getPanel();
        } else if (usuarioActual instanceof Cliente) {
            menuPanel = new MenuClienteView(usuarioActual).getPanel();
        }

        // Panel de pie de página
        JPanel footerPanel = crearPiePagina();

        // Agregar componentes al panel principal
        mainPanel.add(topBar, BorderLayout.NORTH);
        if (menuPanel != null) {
            mainPanel.add(menuPanel, BorderLayout.CENTER);
        }
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Establecer el panel principal como contenido
        setContentPane(mainPanel);
    }

    private JPanel crearBarraSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Título de la aplicación
        JLabel titleLabel = new JLabel("SISTEMA DE GESTIÓN TALLER MECÁNICO");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        // Panel con información del usuario y botón de cerrar sesión
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);

        // Determinar tipo de usuario para mostrar
        String tipoUsuario = "";
        if (usuarioActual instanceof Administrador) {
            tipoUsuario = "Administrador";
        } else if (usuarioActual instanceof Mecanico) {
            tipoUsuario = "Mecánico";
        } else if (usuarioActual instanceof Cliente) {
            tipoUsuario = "Cliente";
            if (((Cliente) usuarioActual).getTipoCliente().equals("oro")) {
                tipoUsuario += " Oro";
            }
        }

        JLabel userLabel = new JLabel(tipoUsuario + ": " + usuarioActual.getNombreCompleto());
        userLabel.setFont(REGULAR_FONT);
        userLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Cerrar Sesión");
        logoutButton.setFont(SMALL_FONT);
        logoutButton.addActionListener(e -> cerrarSesion());

        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(15)); // Espacio entre etiqueta y botón
        userPanel.add(logoutButton);

        panel.add(userPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPiePagina() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel footerLabel = new JLabel("© 2025 Taller Mecánico - Todos los derechos reservados");
        footerLabel.setFont(SMALL_FONT);
        footerLabel.setForeground(new Color(100, 100, 100));

        panel.add(footerLabel);

        return panel;
    }

    private void cerrarSesion() {
        if (showConfirmDialog("¿Está seguro que desea cerrar sesión?")) {
            // Guardar datos antes de cerrar sesión
            Serializador.guardarDatos();

            // Abrir vista de login
            new LoginView().setVisible(true);

            // Cerrar esta ventana
            dispose();
        }
    }
}