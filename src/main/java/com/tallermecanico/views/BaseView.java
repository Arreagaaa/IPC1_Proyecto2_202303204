package com.tallermecanico.views;

import com.tallermecanico.utils.GestorBitacora;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Clase base para todas las vistas del sistema.
 * Define el diseño general y proporciona métodos útiles para mantener
 * consistencia.
 */
public abstract class BaseView extends JFrame {
    // Colores del tema
    protected static final Color COLOR_PRIMARY = new Color(24, 53, 93);
    protected static final Color COLOR_SECONDARY = new Color(75, 101, 132);
    protected static final Color COLOR_ACCENT = new Color(232, 65, 24);
    protected static final Color COLOR_LIGHT = new Color(240, 245, 249);
    protected static final Color COLOR_DARK = new Color(33, 33, 33);
    protected static final Color COLOR_SUCCESS = new Color(46, 125, 50);
    protected static final Color COLOR_WARNING = new Color(251, 140, 0);
    protected static final Color COLOR_ERROR = new Color(198, 40, 40);
    protected static final Color COLOR_PRIMARY_DARK = new Color(20, 40, 70);

    // Fuentes
    protected static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    protected static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    protected static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    protected static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // Imágenes y componentes base
    protected Image backgroundImage;
    protected JPanel backgroundPanel;
    protected JPanel contentPanel;

    /**
     * Constructor base para todas las vistas
     * 
     * @param titulo Título de la ventana
     */
    public BaseView(String titulo) {
        // Configuración básica de la ventana
        setTitle(titulo + " - Taller Mecánico");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Cargar imagen de fondo
        try {
            File imgFile = new File("src\\main\\resources\\wallpaper.jpg");
            if (imgFile.exists()) {
                backgroundImage = ImageIO.read(imgFile);
            } else {
                System.err.println("No se encontró el archivo de imagen: " + imgFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Establecer icono de la aplicación
        try {
            ImageIcon icon = new ImageIcon("src\\main\\resources\\servicio_icon.png");
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la aplicación");
        }

        // Panel principal con fondo
        backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    // Dibujar imagen de fondo
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

                    // Superponer un color semi-transparente para mejorar legibilidad
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(10, 25, 50, 220));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    // Fondo degradado como alternativa
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(
                            0, 0, COLOR_PRIMARY,
                            0, getHeight(), COLOR_SECONDARY);
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // Panel de contenido principal
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Método abstracto que debe implementar cada vista para inicializar sus
     * componentes
     */
    protected abstract void inicializarComponentes();

    /**
     * Crea una barra de navegación con información del usuario y botón de cierre de
     * sesión
     * 
     * @param nombreUsuario Nombre completo del usuario
     * @param tipoUsuario   Tipo o rol del usuario (Administrador, Mecánico,
     *                      Cliente)
     * @return Panel con la barra de navegación
     */
    protected JPanel crearBarraNavegacion(String nombreUsuario, String tipoUsuario) {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(COLOR_PRIMARY);
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Logo y título
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setOpaque(false);

        try {
            ImageIcon logoIcon = new ImageIcon("src\\main\\resources\\servicio_icon.png");
            Image img = logoIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(img));
            logoPanel.add(logoLabel);
        } catch (Exception e) {
            // Si no se encuentra el logo, no mostrar nada
        }

        JLabel titleLabel = new JLabel("Taller Mecánico");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_LIGHT);
        logoPanel.add(titleLabel);

        // Información del usuario
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel(nombreUsuario);
        userLabel.setFont(FONT_NORMAL);
        userLabel.setForeground(COLOR_LIGHT);

        JLabel roleLabel = new JLabel("(" + tipoUsuario + ")");
        roleLabel.setFont(FONT_SMALL);
        roleLabel.setForeground(new Color(200, 200, 200));

        JButton logoutButton = new JButton("Cerrar Sesión");
        logoutButton.setBackground(COLOR_ERROR);
        logoutButton.setForeground(COLOR_LIGHT);
        logoutButton.setFont(FONT_SMALL);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efectos hover
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(239, 83, 80));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(COLOR_ERROR);
            }
        });

        // Acción de cierre de sesión
        logoutButton.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro que desea cerrar sesión?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (opcion == JOptionPane.YES_OPTION) {
                GestorBitacora.registrarEvento(
                        nombreUsuario,
                        "Cierre de sesión",
                        true,
                        "Usuario cerró sesión: " + nombreUsuario);
                dispose();
                new LoginView().setVisible(true);
            }
        });

        userPanel.add(userLabel);
        userPanel.add(roleLabel);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(logoutButton);

        navBar.add(logoPanel, BorderLayout.WEST);
        navBar.add(userPanel, BorderLayout.EAST);

        return navBar;
    }

    /**
     * Crea un botón estilizado según el tema de la aplicación
     * 
     * @param texto Texto del botón
     * @return Botón estilizado
     */
    protected JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(COLOR_SECONDARY);
        boton.setForeground(COLOR_LIGHT);
        boton.setFont(FONT_NORMAL);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        // Efectos hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(95, 125, 160));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_SECONDARY);
            }
        });

        return boton;
    }

    /**
     * Crea un botón de acción primaria (destacado)
     * 
     * @param texto Texto del botón
     * @return Botón estilizado
     */
    protected JButton crearBotonPrimario(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(COLOR_ACCENT);
        boton.setForeground(COLOR_LIGHT);
        boton.setFont(FONT_NORMAL);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        // Efectos hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(239, 108, 82));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_ACCENT);
            }
        });

        return boton;
    }

    /**
     * Crea una etiqueta de título
     * 
     * @param texto Texto de la etiqueta
     * @return Etiqueta estilizada
     */
    protected JLabel crearTitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_LIGHT);
        return label;
    }

    /**
     * Crea una etiqueta de subtítulo
     * 
     * @param texto Texto de la etiqueta
     * @return Etiqueta estilizada
     */
    protected JLabel crearSubtitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(COLOR_LIGHT);
        return label;
    }

    /**
     * Crea una etiqueta normal
     * 
     * @param texto Texto de la etiqueta
     * @return Etiqueta estilizada
     */
    protected JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONT_NORMAL);
        label.setForeground(COLOR_LIGHT);
        return label;
    }

    /**
     * Crea un panel para formularios con estilo consistente
     * 
     * @return Panel estilizado
     */
    protected JPanel crearPanelFormulario() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200, 50), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        return panel;
    }

    /**
     * Crea un panel de tarjeta (card) para mostrar información
     * 
     * @param titulo Título de la tarjeta
     * @return Panel estilizado
     */
    protected JPanel crearTarjeta(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 65, 90, 180));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200, 30), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = crearSubtitulo(titulo);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Crea un campo de texto estilizado
     * 
     * @return Campo de texto estilizado
     */
    protected JTextField crearCampoTexto() {
        JTextField textField = new JTextField();
        textField.setFont(FONT_NORMAL);
        textField.setBackground(COLOR_LIGHT);
        textField.setForeground(COLOR_DARK);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SECONDARY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        return textField;
    }

    /**
     * Crea un campo de contraseña estilizado
     * 
     * @return Campo de contraseña estilizado
     */
    protected JPasswordField crearCampoPassword() {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(FONT_NORMAL);
        passwordField.setBackground(COLOR_LIGHT);
        passwordField.setForeground(COLOR_DARK);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SECONDARY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        return passwordField;
    }

    /**
     * Estiliza una tabla para que tenga un aspecto consistente
     * 
     * @param tabla Tabla a estilizar
     */
    protected void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(30);
        tabla.setIntercellSpacing(new Dimension(5, 5));
        tabla.setSelectionBackground(new Color(75, 110, 175));
        tabla.setSelectionForeground(COLOR_LIGHT);
        tabla.setFillsViewportHeight(true);
        tabla.setFont(FONT_NORMAL);
        tabla.setGridColor(new Color(200, 200, 200, 50));
        tabla.setBorder(BorderFactory.createEmptyBorder());

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(COLOR_LIGHT);
        header.setFont(FONT_NORMAL);
        header.setBorder(BorderFactory.createEmptyBorder());
    }

    /**
     * Configura una tabla con el estilo estándar de la aplicación
     */
    protected void configureTable(JTable table) {
        table.getTableHeader().setBackground(COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(225, 234, 245));
        table.setSelectionForeground(COLOR_PRIMARY_DARK);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFocusable(false);
        table.setIntercellSpacing(new Dimension(10, 5));

        // Centrar el contenido de las celdas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != table.getColumnCount() - 1) { // Excepto la última columna (acciones)
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    /**
     * Clase para renderizar botones en tablas
     */
    protected class ButtonRenderer extends JButton implements TableCellRenderer {
        private String text;

        public ButtonRenderer(String text) {
            this.text = text;
            setOpaque(true);
            setFont(new Font("Arial", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(text);
            setForeground(Color.WHITE);
            setBackground(COLOR_PRIMARY);
            return this;
        }
    }

    /**
     * Clase para manejar eventos de botones en tablas
     */
    protected abstract class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String text;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkbox, String text) {
            super(checkbox);
            this.text = text;
            button = new JButton(text);
            button.setOpaque(true);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setBackground(COLOR_PRIMARY);

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                buttonClicked();
            }
            isPushed = false;
            return text;
        }

        public abstract void buttonClicked();
    }
}