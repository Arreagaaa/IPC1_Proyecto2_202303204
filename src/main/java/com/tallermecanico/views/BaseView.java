package com.tallermecanico.views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Clase base para todas las vistas de la aplicación
 * Proporciona métodos y estilos comunes para mantener consistencia
 */
public class BaseView extends JFrame {

    // Colores principales de la aplicación
    protected final Color COLOR_PRIMARY = new Color(25, 118, 210); // Azul principal
    protected final Color COLOR_SECONDARY = new Color(66, 165, 245); // Azul secundario
    protected final Color COLOR_BACKGROUND = new Color(245, 245, 245); // Fondo gris claro
    protected final Color COLOR_TEXT = new Color(33, 33, 33); // Texto casi negro
    protected final Color COLOR_ACCENT = new Color(255, 152, 0); // Naranja para acentos

    // Fuentes
    protected final Font FONT_TITLE = new Font("Arial", Font.BOLD, 24);
    protected final Font FONT_SUBTITLE = new Font("Arial", Font.BOLD, 18);
    protected final Font FONT_REGULAR = new Font("Arial", Font.PLAIN, 14);
    protected final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 12);

    /**
     * Constructor base para todas las vistas
     * 
     * @param title Título de la ventana
     */
    public BaseView(String title) {
        super(title);
        configureBaseFrame();
    }

    /**
     * Configura las propiedades básicas del frame
     */
    private void configureBaseFrame() {
        // Configuración general de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // Centrar en pantalla

        // Configurar panel principal con margen
        JPanel contentPane = new JPanel();
        contentPane.setBackground(COLOR_BACKGROUND);
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        // Crear ícono de la aplicación (si existe)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/img/app_icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // No mostrar error si no se encuentra el ícono
        }
    }

    /**
     * Crea un panel de contenido con estilo consistente
     * 
     * @return JPanel configurado
     */
    protected JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setLayout(new BorderLayout());
        return panel;
    }

    /**
     * Crea un título con estilo consistente
     * 
     * @param text Texto del título
     * @return JLabel configurado como título
     */
    protected JLabel createTitle(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_PRIMARY);
        return label;
    }

    /**
     * Crea un subtítulo con estilo consistente
     * 
     * @param text Texto del subtítulo
     * @return JLabel configurado como subtítulo
     */
    protected JLabel createSubtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(COLOR_TEXT);
        return label;
    }

    /**
     * Crea un botón primario con estilo consistente
     * 
     * @param text Texto del botón
     * @return JButton configurado con estilo primario
     */
    protected JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_REGULAR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Crea un botón secundario con estilo consistente
     * 
     * @param text Texto del botón
     * @return JButton configurado con estilo secundario
     */
    protected JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(COLOR_SECONDARY);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_REGULAR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Crea un campo de texto con estilo consistente
     * 
     * @param placeholder Texto de marcador de posición
     * @return JTextField configurado
     */
    protected JTextField createTextField(String placeholder) {
        JTextField textField = new JTextField(20);
        textField.setFont(FONT_REGULAR);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SECONDARY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return textField;
    }

    /**
     * Crea un campo de contraseña con estilo consistente
     * 
     * @param placeholder Texto de marcador de posición
     * @return JPasswordField configurado
     */
    protected JPasswordField createPasswordField(String placeholder) {
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(FONT_REGULAR);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SECONDARY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return passwordField;
    }

    /**
     * Muestra un mensaje de error
     * 
     * @param message Mensaje a mostrar
     */
    protected void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje de información
     * 
     * @param message Mensaje a mostrar
     */
    protected void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un mensaje de éxito
     * 
     * @param message Mensaje a mostrar
     */
    protected void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}