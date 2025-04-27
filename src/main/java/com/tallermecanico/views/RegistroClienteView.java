package com.tallermecanico.views;

import com.tallermecanico.controllers.ClienteController;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.utils.GestorBitacora;

import javax.swing.*;
import java.awt.*;

/**
 * Vista para registro de nuevos clientes
 */
public class RegistroClienteView extends BaseView {

    private JTextField txtDPI;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    public RegistroClienteView() {
        super("Registro de Cliente");
        inicializarComponentes();
    }

    @Override
    protected void inicializarComponentes() {
        // Configurar layout del panel de contenido (igual que LoginView)
        contentPanel.setLayout(new GridBagLayout());

        // Panel del formulario de registro
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200, 70), 1, true),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        formPanel.setMaximumSize(new Dimension(520, 650)); // Aumentado desde 450x600
        formPanel.setPreferredSize(new Dimension(520, 620)); // Aumentado desde 450x550

        // Título
        JLabel lblTitle = crearTitulo("Taller Mecánico");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblTitle);

        // Subtítulo
        JLabel lblSubtitle = crearSubtitulo("Registro de Cliente");
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblSubtitle);
        formPanel.add(Box.createVerticalStrut(30)); // Aumentado desde 20

        // Campo DPI
        JLabel lblDPI = crearEtiqueta("DPI (13 dígitos):");
        lblDPI.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblDPI);
        formPanel.add(Box.createVerticalStrut(5));

        txtDPI = crearCampoTexto();
        txtDPI.setMaximumSize(new Dimension(400, 35)); // Aumentado desde 300x35
        formPanel.add(txtDPI);
        formPanel.add(Box.createVerticalStrut(15)); // Aumentado desde 10

        // Campo Nombre
        JLabel lblNombre = crearEtiqueta("Nombre:");
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblNombre);
        formPanel.add(Box.createVerticalStrut(5));

        txtNombre = crearCampoTexto();
        txtNombre.setMaximumSize(new Dimension(400, 35)); // Aumentado desde 300x35
        formPanel.add(txtNombre);
        formPanel.add(Box.createVerticalStrut(15)); // Aumentado desde 10

        // Campo Apellido
        JLabel lblApellido = crearEtiqueta("Apellido:");
        lblApellido.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblApellido);
        formPanel.add(Box.createVerticalStrut(5));

        txtApellido = crearCampoTexto();
        txtApellido.setMaximumSize(new Dimension(400, 35)); // Aumentado desde 300x35
        formPanel.add(txtApellido);
        formPanel.add(Box.createVerticalStrut(15)); // Aumentado desde 10

        // Campo Usuario
        JLabel lblUsuario = crearEtiqueta("Usuario:");
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblUsuario);
        formPanel.add(Box.createVerticalStrut(5));

        txtUsuario = crearCampoTexto();
        txtUsuario.setMaximumSize(new Dimension(400, 35)); // Aumentado desde 300x35
        formPanel.add(txtUsuario);
        formPanel.add(Box.createVerticalStrut(15)); // Aumentado desde 10

        // Campo Contraseña
        JLabel lblPassword = crearEtiqueta("Contraseña:");
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblPassword);
        formPanel.add(Box.createVerticalStrut(5));

        txtPassword = crearCampoPassword();
        txtPassword.setMaximumSize(new Dimension(400, 35)); // Aumentado desde 300x35
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(15)); // Aumentado desde 10

        // Campo Confirmar Contraseña
        JLabel lblConfirmPassword = crearEtiqueta("Confirmar Contraseña:");
        lblConfirmPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblConfirmPassword);
        formPanel.add(Box.createVerticalStrut(5));

        txtConfirmPassword = crearCampoPassword();
        txtConfirmPassword.setMaximumSize(new Dimension(400, 35)); // Aumentado desde 300x35
        formPanel.add(txtConfirmPassword);
        formPanel.add(Box.createVerticalStrut(30)); // Aumentado desde 20

        // Panel de botones con mejor configuración - Modificado para mantener tamaños
        // consistentes
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 20, 0)); // Cambio a GridLayout para tamaños iguales
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(340, 40));
        buttonPanel.setPreferredSize(new Dimension(340, 40));

        // Botón Registrar mejorado
        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setBackground(new Color(0, 120, 215));
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setOpaque(true);
        btnRegistrar.addActionListener(e -> registrarCliente());

        // Botón Cancelar mejorado
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setBackground(new Color(160, 160, 160));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setOpaque(true);
        btnCancelar.addActionListener(e -> cancelar());

        buttonPanel.add(btnRegistrar);
        buttonPanel.add(btnCancelar);
        formPanel.add(buttonPanel);

        formPanel.add(Box.createVerticalStrut(25)); // Aumentado desde 20 (después de botones)

        // Enlace para volver al login (siguiendo el estilo de LoginView)
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        linkPanel.setOpaque(false);

        JLabel loginLink = new JLabel("¿Ya tienes cuenta? Inicia sesión aquí");
        loginLink.setFont(FONT_SMALL);
        loginLink.setForeground(new Color(135, 206, 250));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
                new LoginView().setVisible(true);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginLink.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginLink.setForeground(new Color(135, 206, 250));
            }
        });

        linkPanel.add(loginLink);
        formPanel.add(linkPanel);

        // Agregar el formulario al panel de contenido
        contentPanel.add(formPanel);

        // Configuraciones finales
        setSize(620, 720); // Aumentado desde 600x700
        setLocationRelativeTo(null);
    }

    /**
     * Registra un nuevo cliente
     */
    private void registrarCliente() {
        String dpi = txtDPI.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        // Validación básica
        if (dpi.isEmpty() || nombre.isEmpty() || apellido.isEmpty() ||
                usuario.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe completar todos los campos",
                    "Error de registro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar formato DPI (13 dígitos)
        if (!dpi.matches("\\d{13}")) {
            JOptionPane.showMessageDialog(this,
                    "El DPI debe contener exactamente 13 dígitos",
                    "Error de registro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar contraseñas
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Las contraseñas no coinciden",
                    "Error de registro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Registrar cliente
        Cliente nuevoCliente = ClienteController.registrarCliente(dpi, nombre, apellido, usuario, password);

        if (nuevoCliente != null) {
            GestorBitacora.registrarEvento("Sistema", "Registro de cliente", true,
                    "Cliente registrado con DPI: " + dpi);

            JOptionPane.showMessageDialog(this,
                    "Cliente registrado exitosamente",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);

            // Volver a la pantalla de login
            dispose();
            new LoginView().setVisible(true);
        } else {
            GestorBitacora.registrarEvento("Sistema", "Registro de cliente", false,
                    "Fallo al registrar cliente con DPI: " + dpi);

            JOptionPane.showMessageDialog(this,
                    "Error al registrar cliente. Verifique que el DPI o usuario no estén duplicados",
                    "Error de registro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cancelar registro y volver a pantalla de login
     */
    private void cancelar() {
        dispose();
        new LoginView().setVisible(true);
    }
}