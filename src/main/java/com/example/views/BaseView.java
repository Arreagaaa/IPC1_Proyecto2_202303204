package com.example.views;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase base para todas las vistas de la aplicación.
 * Proporciona estilos consistentes y funcionalidades comunes.
 */
public abstract class BaseView extends JFrame {

    // ===== CONFIGURACIÓN VISUAL =====
    // Paleta de colores
    protected static final Color PRIMARY_COLOR = new Color(25, 118, 210); // Azul primario
    protected static final Color SECONDARY_COLOR = new Color(13, 71, 161); // Azul secundario
    protected static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Gris fondo claro
    protected static final Color TEXT_COLOR = new Color(33, 33, 33); // Texto principal
    protected static final Color ACCENT_COLOR = new Color(230, 74, 25); // Acento naranja
    protected static final Color SUCCESS_COLOR = new Color(46, 125, 50); // Verde éxito
    protected static final Color WARNING_COLOR = new Color(251, 140, 0); // Naranja advertencia
    protected static final Color LIGHT_GRAY = new Color(224, 224, 224); // Gris claro para bordes

    // Fuentes
    protected static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    protected static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    protected static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    protected static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    protected static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    // Dimensiones estándar
    protected static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(900, 700);
    protected static final Dimension MODAL_WINDOW_SIZE = new Dimension(650, 500);
    protected static final Dimension LOGIN_WINDOW_SIZE = new Dimension(450, 350);
    protected static final Dimension BUTTON_SIZE = new Dimension(180, 40);
    protected static final int PANEL_SPACING = 15;

    // Iconos de la aplicación
    private static final String DEFAULT_ICON_PATH = "/images/app_icon.png";
    private static Map<String, ImageIcon> iconsCache = new HashMap<>();

    // Propiedades de configuración
    protected String backgroundImagePath;
    private Image backgroundImage;

    /**
     * Constructor base para todas las vistas
     * 
     * @param title Título de la ventana
     */
    public BaseView(String title) {
        super(title);
        initializeBaseComponents();
    }

    /**
     * Inicializa los componentes base de la vista
     */
    private void initializeBaseComponents() {
        // Configuración básica de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WINDOW_SIZE);
        setLocationRelativeTo(null);
        setResizable(false); // Evitar redimensionar

        // Cargar icono de la aplicación
        loadAppIcon();

        // Implementar panel principal con posible imagen de fondo
        setContentPane(createBackgroundPanel());

        // Agregar listener para confirmar al cerrar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        // Aplicar look and feel moderno
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Error al configurar look and feel: " + e.getMessage());
        }
    }

    /**
     * Carga el icono de la aplicación desde los recursos
     */
    private void loadAppIcon() {
        try {
            ImageIcon icon = getIconFromResources(DEFAULT_ICON_PATH);
            if (icon != null) {
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la aplicación: " + e.getMessage());
        }
    }

    /**
     * Crea un panel con posible imagen de fondo
     */
    private JPanel createBackgroundPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Si hay imagen de fondo, la dibuja
                if (backgroundImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                    g2d.dispose();
                }
            }
        };
    }

    /**
     * Configura una imagen de fondo para la vista
     */
    protected void setBackgroundImage(String path) {
        try {
            ImageIcon imageIcon = getIconFromResources(path);
            if (imageIcon != null) {
                backgroundImage = imageIcon.getImage();
                backgroundImagePath = path;
                repaint();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de fondo: " + e.getMessage());
        }
    }

    /**
     * Obtiene un icono desde los recursos de la aplicación
     */
    protected ImageIcon getIconFromResources(String path) {
        if (iconsCache.containsKey(path)) {
            return iconsCache.get(path);
        }

        try {
            URL iconURL = getClass().getResource(path);
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                iconsCache.put(path, icon);
                return icon;
            }

            // Si no encuentra en resources, intenta buscar como archivo
            File file = new File(path);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(path);
                iconsCache.put(path, icon);
                return icon;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar icono: " + e.getMessage());
        }

        return null;
    }

    /**
     * Muestra confirmación antes de cerrar la aplicación
     */
    protected void confirmExit() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea salir de la aplicación?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    // ===== MÉTODOS PARA CREAR COMPONENTES VISUALES =====

    /**
     * Crea un panel transparente con margen
     */
    protected JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(
                PANEL_SPACING, PANEL_SPACING, PANEL_SPACING, PANEL_SPACING));
        return panel;
    }

    /**
     * Crea un panel con fondo sólido y borde elevado
     */
    protected JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY),
                BorderFactory.createEmptyBorder(PANEL_SPACING, PANEL_SPACING, PANEL_SPACING, PANEL_SPACING)));
        return panel;
    }

    /**
     * Crea un título centrado
     */
    protected JLabel createTitle(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    /**
     * Crea un subtítulo
     */
    protected JLabel createSubtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBTITLE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    /**
     * Crea una etiqueta normal
     */
    protected JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(REGULAR_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    /**
     * Crea un botón con estilo principal
     */
    protected JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR, Color.WHITE);
    }

    /**
     * Crea un botón con estilo secundario
     */
    protected JButton createSecondaryButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR, Color.WHITE);
    }

    /**
     * Crea un botón con estilo de acento
     */
    protected JButton createAccentButton(String text) {
        return createStyledButton(text, ACCENT_COLOR, Color.WHITE);
    }

    /**
     * Crea un botón con estilo de éxito
     */
    protected JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR, Color.WHITE);
    }

    /**
     * Crea un botón con icono
     */
    protected JButton createIconButton(String text, String iconPath) {
        JButton button = createPrimaryButton(text);

        ImageIcon icon = getIconFromResources(iconPath);
        if (icon != null) {
            button.setIcon(icon);
        }

        return button;
    }

    /**
     * Método base para crear botones estilizados
     */
    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(BUTTON_SIZE);

        // Efectos hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(getDarkerColor(background));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    /**
     * Crea un campo de texto estilizado
     */
    protected JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(REGULAR_FONT);
        return textField;
    }

    /**
     * Crea un campo de contraseña estilizado
     */
    protected JPasswordField createPasswordField(int columns) {
        JPasswordField passwordField = new JPasswordField(columns);
        passwordField.setFont(REGULAR_FONT);
        return passwordField;
    }

    /**
     * Crea un ComboBox estilizado
     */
    protected JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(REGULAR_FONT);
        return comboBox;
    }

    /**
     * Crea un panel de título para la parte superior de la ventana
     */
    protected JPanel createHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    /**
     * Crea una línea separadora horizontal
     */
    protected JSeparator createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(LIGHT_GRAY);
        return separator;
    }

    /**
     * Crea un borde redondeado
     */
    protected Border createRoundedBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15));
    }

    /**
     * Muestra un mensaje de error
     */
    protected void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje informativo
     */
    protected void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un mensaje de éxito
     */
    protected void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un diálogo de confirmación
     */
    protected boolean showConfirmDialog(String message) {
        int response = JOptionPane.showConfirmDialog(this, message, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * Retorna un color más oscuro para efectos hover
     */
    private Color getDarkerColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1], Math.max(0, hsb[2] - 0.1f));
    }

    /**
     * Crea un campo de texto estilizado con bordes redondeados y efectos
     */
    protected JTextField createStylishTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(REGULAR_FONT);
        textField.setPreferredSize(new Dimension(columns * 12, 30)); // Altura fija
        textField.setMargin(new Insets(2, 10, 2, 10)); // Padding interior
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        // Agregar efectos de focus
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                        BorderFactory.createEmptyBorder(4, 7, 4, 7)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LIGHT_GRAY, 1, true),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            }
        });

        return textField;
    }

    /**
     * Crea un campo de contraseña estilizado con bordes redondeados y efectos
     */
    protected JPasswordField createStylishPasswordField(int columns) {
        JPasswordField passwordField = new JPasswordField(columns);
        passwordField.setFont(REGULAR_FONT);
        passwordField.setPreferredSize(new Dimension(columns * 12, 30)); // Altura fija
        passwordField.setMargin(new Insets(2, 10, 2, 10)); // Padding interior
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        // Agregar efectos de focus
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                        BorderFactory.createEmptyBorder(4, 7, 4, 7)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LIGHT_GRAY, 1, true),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            }
        });

        return passwordField;
    }

    /**
     * Crea un panel de formulario con etiquetas alineadas a la derecha y campos a
     * la izquierda
     */
    protected JPanel createFormPanel(String[] labels, JComponent[] fields) {
        if (labels.length != fields.length) {
            throw new IllegalArgumentException("El número de etiquetas y campos debe ser igual");
        }

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.EAST;
        labelConstraints.insets = new Insets(8, 5, 8, 10);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(8, 0, 8, 5);

        for (int i = 0; i < labels.length; i++) {
            labelConstraints.gridx = 0;
            labelConstraints.gridy = i;
            JLabel label = createLabel(labels[i]);
            panel.add(label, labelConstraints);

            fieldConstraints.gridx = 1;
            fieldConstraints.gridy = i;
            panel.add(fields[i], fieldConstraints);
        }

        return panel;
    }
}