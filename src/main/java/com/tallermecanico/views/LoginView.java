package com.tallermecanico.views;

import com.tallermecanico.controllers.ClienteController;
import com.tallermecanico.controllers.DataController;
import com.tallermecanico.controllers.EmpleadoController;
import com.tallermecanico.models.personas.Administrador;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Empleado;
import com.tallermecanico.models.personas.Mecanico;
import com.tallermecanico.utils.GestorBitacora;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * Vista de inicio de sesión del sistema
 */
public class LoginView extends BaseView {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private List<Empleado> empleados;

    public LoginView() {
        super("Inicio de Sesión");
        inicializarComponentes();
    }

    @Override
    protected void inicializarComponentes() {
        // Configurar layout del panel de contenido
        contentPanel.setLayout(new GridBagLayout());

        // Panel del formulario de login
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200, 70), 1, true),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        formPanel.setMaximumSize(new Dimension(400, 500));
        formPanel.setPreferredSize(new Dimension(400, 450));

        // Logo del taller (si existe)
        try {
            ImageIcon logoIcon = new ImageIcon("src\\main\\resources\\servicio_icon.png");
            Image img = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(img));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            formPanel.add(logoLabel);
            formPanel.add(Box.createVerticalStrut(20));
        } catch (Exception e) {
            // Si no existe el logo, no mostrar nada
        }

        // Título
        JLabel lblTitle = crearTitulo("Taller Mecánico");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblTitle);

        // Subtítulo
        JLabel lblSubtitle = crearSubtitulo("Inicio de Sesión");
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblSubtitle);
        formPanel.add(Box.createVerticalStrut(30));

        // Etiqueta de usuario
        JLabel lblUsuario = crearEtiqueta("Usuario:");
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblUsuario);
        formPanel.add(Box.createVerticalStrut(5));

        // Campo de usuario
        txtUsuario = crearCampoTexto();
        txtUsuario.setMaximumSize(new Dimension(300, 35));
        formPanel.add(txtUsuario);
        formPanel.add(Box.createVerticalStrut(15));

        // Etiqueta de contraseña
        JLabel lblPassword = crearEtiqueta("Contraseña:");
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblPassword);
        formPanel.add(Box.createVerticalStrut(5));

        // Campo de contraseña
        txtPassword = crearCampoPassword();
        txtPassword.setMaximumSize(new Dimension(300, 35));
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(25));

        // Botón de inicio de sesión
        JButton btnLogin = crearBotonPrimario("Iniciar Sesión");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(200, 40));
        btnLogin.addActionListener(e -> iniciarSesion());
        formPanel.add(btnLogin);
        formPanel.add(Box.createVerticalStrut(20));

        // Enlace para registrarse (si aplica)
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        linkPanel.setOpaque(false);

        JLabel registerLink = new JLabel("¿Eres cliente nuevo? Regístrate aquí");
        registerLink.setFont(FONT_SMALL);
        registerLink.setForeground(new Color(135, 206, 250));
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
                new RegistroClienteView().setVisible(true);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerLink.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerLink.setForeground(new Color(135, 206, 250));
            }
        });

        linkPanel.add(registerLink);
        formPanel.add(linkPanel);

        // Agregar el formulario al panel de contenido
        contentPanel.add(formPanel);
    }

    /**
     * Realiza el proceso de inicio de sesión
     */
    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese usuario y contraseña.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // PRIMERO: Verificar si hay ADMIN por defecto
        DataController.verificarAdministradorPorDefecto();

        // SEGUNDO: Obtener empleados de nuevo (ahora que verificamos defaults)
        empleados = DataController.getEmpleados();

        // TERCERO: Comprobar que no sea null
        if (empleados == null) {
            System.out.println("¡CUIDADO! Lista de empleados es NULL. Creando lista vacía.");
            empleados = new Vector<>();
        }

        // Ahora sí buscar en empleados (no debería ser null)
        Object usuarioEncontrado = null; // Usar Object en lugar de Usuario

        // Debug para ver cuántos empleados hay
        System.out.println("Empleados encontrados: " + empleados.size());

        for (Empleado empleado : empleados) {
            if (empleado.getNombreUsuario().equals(usuario) && empleado.getPassword().equals(password)) {
                usuarioEncontrado = empleado;
                System.out.println(
                        "Empleado encontrado: " + empleado.getNombreCompleto() + ", Tipo: " + empleado.getTipo());
                break;
            }
        }

        // Buscar en clientes si no se encontró en empleados
        if (usuarioEncontrado == null) {
            Vector<Cliente> clientes = DataController.getClientes();
            if (clientes != null) {
                for (Cliente cliente : clientes) {
                    if (cliente.getNombreUsuario().equals(usuario) && cliente.getPassword().equals(password)) {
                        usuarioEncontrado = cliente;
                        break;
                    }
                }
            }
        }

        if (usuarioEncontrado != null) {
            dispose(); // Cerrar ventana de login

            // Verificar tipo de usuario y abrir la vista correspondiente
            if (usuarioEncontrado instanceof Administrador) {
                System.out.println("Creando vista de administrador...");
                new AdminView((Empleado) usuarioEncontrado).setVisible(true);
            } else if (usuarioEncontrado instanceof Mecanico) {
                System.out.println("Creando vista de mecánico...");
                new MecanicoView((Empleado) usuarioEncontrado).setVisible(true);
            } else if (usuarioEncontrado instanceof Cliente) {
                System.out.println("Creando vista de cliente...");
                new ClienteView((Cliente) usuarioEncontrado).setVisible(true);
            } else if (usuarioEncontrado instanceof Empleado) {
                // Si es un empleado genérico, verificar su tipo manualmente
                Empleado emp = (Empleado) usuarioEncontrado;
                String tipo = emp.getTipo().toLowerCase();

                if (tipo.equals("admin") || tipo.equals("administrador")) {
                    System.out.println("Creando vista de administrador (por tipo)...");
                    // Convertir a Administrador si es necesario o usar como Empleado genérico
                    new AdminView(emp).setVisible(true);
                } else if (tipo.equals("mecanico")) {
                    System.out.println("Creando vista de mecánico (por tipo)...");
                    new MecanicoView(emp).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Tipo de empleado no reconocido: " + tipo,
                            "Error de inicio de sesión",
                            JOptionPane.ERROR_MESSAGE);
                    new LoginView().setVisible(true);
                }
            } else {
                System.out.println("ERROR: Tipo de usuario no reconocido: " + usuarioEncontrado.getClass().getName());
                JOptionPane.showMessageDialog(null,
                        "Tipo de usuario no reconocido",
                        "Error de inicio de sesión",
                        JOptionPane.ERROR_MESSAGE);
                new LoginView().setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}