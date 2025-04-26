package com.tallermecanico.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Vista del menú principal
 */
public class MenuView extends JFrame {

    // Botones del menú
    private JButton btnCliente;
    private JButton btnAdmin;
    private JButton btnMecanico;
    private JButton btnSalir;
    private JPanel panel;
    private JLabel lblTitulo;

    /**
     * Constructor
     */
    public MenuView() {
        inicializarComponentes();
        configurarEventos();
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarComponentes() {
        // Configuración del JFrame
        setTitle("Taller Mecánico - Menú Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);

        // Panel principal
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        setContentPane(panel);

        // Título
        lblTitulo = new JLabel("TALLER MECÁNICO");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Seleccione su tipo de usuario:");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 16));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblSubtitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Botón Cliente
        btnCliente = new JButton("Cliente");
        btnCliente.setMaximumSize(new Dimension(200, 40));
        btnCliente.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCliente.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(btnCliente);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Botón Administrador
        btnAdmin = new JButton("Administrador");
        btnAdmin.setMaximumSize(new Dimension(200, 40));
        btnAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAdmin.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(btnAdmin);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Botón Mecánico
        btnMecanico = new JButton("Mecánico");
        btnMecanico.setMaximumSize(new Dimension(200, 40));
        btnMecanico.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMecanico.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(btnMecanico);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Botón Salir
        btnSalir = new JButton("Salir");
        btnSalir.setMaximumSize(new Dimension(200, 40));
        btnSalir.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalir.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(btnSalir);
    }

    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        // Evento para botón Cliente
        btnCliente.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirLoginCliente();
            }
        });

        // Evento para botón Administrador
        btnAdmin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirLoginAdmin();
            }
        });

        // Evento para botón Mecánico
        btnMecanico.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirLoginMecanico();
            }
        });

        // Evento para botón Salir
        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarAplicacion();
            }
        });
    }

    /**
     * Abre la vista de login para clientes
     */
    private void abrirLoginCliente() {
        dispose(); // Cerrar ventana actual
        new LoginView(LoginView.TIPO_CLIENTE).setVisible(true);
    }

    /**
     * Abre la vista de login para administradores
     */
    private void abrirLoginAdmin() {
        dispose(); // Cerrar ventana actual
        new LoginView(LoginView.TIPO_ADMIN).setVisible(true);
    }

    /**
     * Abre la vista de login para mecánicos
     */
    private void abrirLoginMecanico() {
        dispose(); // Cerrar ventana actual
        new LoginView(LoginView.TIPO_MECANICO).setVisible(true);
    }

    /**
     * Cierra la aplicación
     */
    private void cerrarAplicacion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea salir de la aplicación?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}