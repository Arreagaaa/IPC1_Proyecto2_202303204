package com.example.views.admin;

import com.example.models.personas.Persona;
import com.example.views.BaseView;

import javax.swing.*;
import java.awt.*;

/**
 * Vista de menú para el administrador
 */
public class MenuAdministradorView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;

    public MenuAdministradorView(Persona usuario) {
        super(""); // No es una ventana independiente
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout());

        // Título de bienvenida
        JPanel welcomePanel = new JPanel();
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel welcomeLabel = createSubtitle("Bienvenido al Panel de Administración");
        welcomePanel.add(welcomeLabel);

        // Panel de opciones con grid
        JPanel optionsPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Botones de opciones
        JButton clientesBtn = createMenuButton("Gestión de Clientes", "/images/clientes.png");
        JButton repuestosBtn = createMenuButton("Gestión de Repuestos", "/images/repuestos.png");
        JButton serviciosBtn = createMenuButton("Gestión de Servicios", "/images/servicios.png");
        JButton progresoBtn = createMenuButton("Progreso de Automóviles", "/images/progreso.png");
        JButton facturasBtn = createMenuButton("Facturación", "/images/facturas.png");
        JButton reportesBtn = createMenuButton("Reportes", "/images/reportes.png");

        // Asignar acciones a los botones
        clientesBtn.addActionListener(e -> abrirGestionClientes());
        repuestosBtn.addActionListener(e -> abrirGestionRepuestos());
        serviciosBtn.addActionListener(e -> abrirGestionServicios());
        progresoBtn.addActionListener(e -> abrirProgresoAutomoviles());
        facturasBtn.addActionListener(e -> abrirFacturacion());
        reportesBtn.addActionListener(e -> abrirReportes());

        // Agregar botones al panel
        optionsPanel.add(clientesBtn);
        optionsPanel.add(repuestosBtn);
        optionsPanel.add(serviciosBtn);
        optionsPanel.add(progresoBtn);
        optionsPanel.add(facturasBtn);
        optionsPanel.add(reportesBtn);

        // Agregar paneles al panel principal
        mainPanel.add(welcomePanel, BorderLayout.NORTH);
        mainPanel.add(optionsPanel, BorderLayout.CENTER);
    }

    // Método para crear botones de menú con íconos
    private JButton createMenuButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // Intentar cargar ícono
        ImageIcon icon = getIconFromResources(iconPath);
        if (icon != null) {
            button.setIcon(icon);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setIconTextGap(10);
        }

        // Efectos hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 240));
                button.setForeground(TEXT_COLOR);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    // Métodos para abrir las diferentes vistas de administración
    private void abrirGestionClientes() {
        JFrame frame = new JFrame("Gestión de Clientes");
        frame.setContentPane(new GestionClientesAdminView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void abrirGestionRepuestos() {
        JFrame frame = new JFrame("Gestión de Repuestos");
        frame.setContentPane(new GestionRepuestosAdminView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void abrirGestionServicios() {
        JFrame frame = new JFrame("Gestión de Servicios");
        frame.setContentPane(new GestionServiciosAdminView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void abrirProgresoAutomoviles() {
        JFrame frame = new JFrame("Progreso de Automóviles");
        frame.setContentPane(new ProgresoAutomivilesAdminView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void abrirFacturacion() {
        JFrame frame = new JFrame("Facturación");
        frame.setContentPane(new FacturacionAdminView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void abrirReportes() {
        JFrame frame = new JFrame("Reportes");
        frame.setContentPane(new ReportesAdminView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Método para obtener el panel principal
    public JPanel getPanel() {
        return mainPanel;
    }
}
