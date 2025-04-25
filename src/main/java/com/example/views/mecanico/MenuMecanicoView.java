package com.example.views.mecanico;

import com.example.models.personas.Persona;
import com.example.views.BaseView;

import javax.swing.*;
import java.awt.*;

/**
 * Vista de menú para el mecánico
 */
public class MenuMecanicoView extends BaseView {
    
    private Persona usuario;
    private JPanel mainPanel;
    
    public MenuMecanicoView(Persona usuario) {
        super("");
        this.usuario = usuario;
        inicializarComponentes();
    }
    
    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Panel de bienvenida
        JPanel welcomePanel = new JPanel();
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel welcomeLabel = createSubtitle("Bienvenido al Panel de Mecánico");
        welcomePanel.add(welcomeLabel);
        
        // Panel de opciones
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Botones principales
        JButton ordenesButton = createMenuButton("Órdenes de Trabajo", "/images/ordenes.png");
        JButton diagnosticoButton = createMenuButton("Realizar Diagnóstico", "/images/diagnostico.png");
        JButton historialButton = createMenuButton("Historial de Servicios", "/images/historial.png");
        
        // Asignar acciones
        ordenesButton.addActionListener(e -> abrirOrdenesTrabajoView());
        diagnosticoButton.addActionListener(e -> abrirDiagnosticoView());
        historialButton.addActionListener(e -> abrirHistorialServiciosView());
        
        // Agregar botones al panel
        optionsPanel.add(ordenesButton);
        optionsPanel.add(diagnosticoButton);
        optionsPanel.add(historialButton);
        
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
    
    private void abrirOrdenesTrabajoView() {
        JFrame frame = new JFrame("Órdenes de Trabajo");
        frame.setContentPane(new OrdenesTrabajoView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void abrirDiagnosticoView() {
        JFrame frame = new JFrame("Realizar Diagnóstico");
        frame.setContentPane(new DiagnosticoView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void abrirHistorialServiciosView() {
        JFrame frame = new JFrame("Historial de Servicios");
        frame.setContentPane(new HistorialServiciosView(usuario).getPanel());
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public JPanel getPanel() {
        return mainPanel;
    }
}
