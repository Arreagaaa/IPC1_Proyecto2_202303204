package com.example.views.cliente;

import com.example.models.personas.Cliente;
import com.example.models.personas.Persona;
import com.example.views.BaseView;

import javax.swing.*;
import java.awt.*;

/**
 * Vista de menú para el cliente
 */
public class MenuClienteView extends BaseView {
    
    private Persona usuario;
    private JPanel mainPanel;
    
    public MenuClienteView(Persona usuario) {
        super("");
        this.usuario = usuario;
        inicializarComponentes();
    }
    
    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Panel de bienvenida con información del cliente
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Título de bienvenida
        JLabel welcomeLabel = createSubtitle("Bienvenido al Panel de Cliente");
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Información de tipo de cliente
        Cliente cliente = (Cliente) usuario;
        String tipoCliente = cliente.getTipoCliente().equals("oro") ? "Cliente Oro" : "Cliente Normal";
        JLabel typeLabel = createLabel("Tipo de Cliente: " + tipoCliente);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        welcomePanel.add(typeLabel);
        
        // Panel de opciones
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Botones principales
        JButton automobilesButton = createMenuButton("Mis Automóviles", "/images/automoviles.png");
        JButton solicitudButton = createMenuButton("Solicitar Servicio", "/images/solicitar.png");
        JButton progresoButton = createMenuButton("Progreso de Vehículos", "/images/progreso.png");
        JButton facturasButton = createMenuButton("Mis Facturas", "/images/facturas.png");
        
        // Asignar acciones
        automobilesButton.addActionListener(e -> abrirMisAutomoviles());
        solicitudButton.addActionListener(e -> abrirSolicitarServicio());
        progresoButton.addActionListener(e -> abrirProgresoVehiculos());
        facturasButton.addActionListener(e -> abrirMisFacturas());
        
        // Agregar botones al panel
        optionsPanel.add(automobilesButton);
        optionsPanel.add(solicitudButton);
        optionsPanel.add(progresoButton);
        optionsPanel.add(facturasButton);
        
        // Agregar paneles al panel principal
        mainPanel.add(welcomePanel, BorderLayout.NORTH);
        mainPanel.add(optionsPanel, BorderLayout.CENTER);
    }
    
    private JButton createMenuButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
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
    
    private void abrirMisAutomoviles() {
        JFrame frame = new JFrame("Mis Automóviles");
        frame.setContentPane(new MisAutomovilesView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void abrirSolicitarServicio() {
        JFrame frame = new JFrame("Solicitar Servicio");
        frame.setContentPane(new SolicitarServicioView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void abrirProgresoVehiculos() {
        JFrame frame = new JFrame("Progreso de Vehículos");
        frame.setContentPane(new ProgresoVehiculosView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void abrirMisFacturas() {
        JFrame frame = new JFrame("Mis Facturas");
        frame.setContentPane(new MisFacturasView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public JPanel getPanel() {
        return mainPanel;
    }
}
