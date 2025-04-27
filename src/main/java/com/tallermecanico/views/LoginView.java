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
        String usuario = txtUsuario.getText();
        String contraseña = new String(txtPassword.getPassword());

        // Validar campos vacíos
        if (usuario.isEmpty() || contraseña.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor complete todos los campos",
                    "Campos requeridos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Buscar empleado
            Empleado empleado = EmpleadoController.buscarEmpleadoPorUsuario(usuario);

            if (empleado != null && empleado.getContrasena().equals(contraseña)) {
                System.out.println("Tipo de empleado encontrado: " + empleado.getClass().getName());

                GestorBitacora.registrarEvento(
                        usuario,
                        "Inicio de sesión",
                        true,
                        "Empleado inició sesión: " + empleado.getNombreCompleto());

                // Redirigir según el tipo de empleado
                if (empleado instanceof Administrador) {
                    System.out.println("Creando vista de administrador...");
                    AdminView adminView = new AdminView(empleado);
                    adminView.setVisible(true);
                    dispose();
                } else if (empleado instanceof Mecanico) {
                    System.out.println("Creando vista de mecánico...");
                    MecanicoView mecanicoView = new MecanicoView((Mecanico) empleado);
                    mecanicoView.setVisible(true);
                    dispose();
                } else {
                    System.err.println("ERROR: Tipo de empleado inesperado: " + empleado.getClass().getName());
                    // Corregir el tipo de empleado en memoria si es el admin por defecto
                    if ("admin".equals(empleado.getNombreUsuario())) {
                        Empleado nuevoAdmin = new Administrador(
                                empleado.getIdentificador(),
                                empleado.getNombre(),
                                empleado.getApellido(),
                                empleado.getNombreUsuario());

                        // Reemplazar en el vector de empleados
                        int index = empleados.indexOf(empleado);
                        if (index >= 0) {
                            empleados.set(index, nuevoAdmin);
                            DataController.guardarDatos();
                            // Reintentar con el nuevo objeto
                            AdminView adminView = new AdminView(nuevoAdmin);
                            adminView.setVisible(true);
                            dispose();
                            return;
                        }
                    }

                    JOptionPane.showMessageDialog(
                            this,
                            "Tipo de empleado no reconocido: " + empleado.getClass().getName(),
                            "Error de sistema",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Buscar cliente
                Cliente cliente = ClienteController.autenticarCliente(usuario, contraseña);
                if (cliente != null) {
                    GestorBitacora.registrarEvento(
                            usuario,
                            "Inicio de sesión",
                            true,
                            "Cliente inició sesión: " + cliente.getNombreCompleto());
                    ClienteView clienteView = new ClienteView(cliente);
                    clienteView.setVisible(true);
                    dispose();
                } else {
                    GestorBitacora.registrarEvento(
                            "Sistema",
                            "Intento de inicio de sesión fallido",
                            false,
                            "Usuario intentó iniciar sesión con credenciales incorrectas: " + usuario);

                    JOptionPane.showMessageDialog(
                            this,
                            "Usuario o contraseña incorrectos",
                            "Error de autenticación",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            // Capturar cualquier excepción para evitar que el programa se cierre
            e.printStackTrace();

            GestorBitacora.registrarEvento(
                    "Sistema",
                    "Error crítico de inicio de sesión",
                    false,
                    "Error al procesar inicio de sesión para usuario: " + usuario + " - " + e.getMessage());

            JOptionPane.showMessageDialog(
                    this,
                    "Ha ocurrido un error al procesar el inicio de sesión.\nError: " + e.getMessage(),
                    "Error de sistema",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}