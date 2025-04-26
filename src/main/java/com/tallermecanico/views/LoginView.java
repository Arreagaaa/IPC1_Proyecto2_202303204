package com.tallermecanico.views;

import com.tallermecanico.controllers.ClienteController;
import com.tallermecanico.controllers.EmpleadoController;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.personas.Empleado;
import com.tallermecanico.models.personas.Mecanico;
import com.tallermecanico.utils.GestorBitacora;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Vista de login del sistema
 */
public class LoginView extends JFrame {

    // Constantes para los tipos de login
    public static final int TIPO_ADMIN = 1;
    public static final int TIPO_CLIENTE = 2;
    public static final int TIPO_MECANICO = 3;

    private int tipoLogin;

    // Componentes de la interfaz
    private JPanel panel;
    private JLabel lblTitulo;
    private JLabel lblUsuario;
    private JTextField txtUsuario;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnVolver;

    /**
     * Constructor por defecto (login de cliente)
     */
    public LoginView() {
        this(TIPO_CLIENTE);
    }

    /**
     * Constructor con tipo de login específico
     */
    public LoginView(int tipoLogin) {
        this.tipoLogin = tipoLogin;
        inicializarComponentes();
        configurarEventos();
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarComponentes() {
        // Configuración del JFrame
        setTitle("Taller Mecánico - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Panel principal
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        setContentPane(panel);

        // Título
        lblTitulo = new JLabel("Iniciar Sesión");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Campo de usuario
        lblUsuario = new JLabel("Usuario:");
        txtUsuario = new JTextField(20);
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtUsuario.getPreferredSize().height));
        panel.add(lblUsuario);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(txtUsuario);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Campo de contraseña
        lblPassword = new JLabel("Contraseña:");
        txtPassword = new JPasswordField(20);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtPassword.getPreferredSize().height));
        panel.add(lblPassword);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(txtPassword);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Botones
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver = new JButton("Volver");
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(btnIngresar);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(btnVolver);
    }

    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        // Botón Ingresar
        btnIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usuario = txtUsuario.getText();
                String password = new String(txtPassword.getPassword());

                if (usuario.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginView.this,
                            "Debe ingresar usuario y contraseña",
                            "Error de Autenticación",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                switch (tipoLogin) {
                    case TIPO_CLIENTE:
                        autenticarCliente(usuario, password);
                        break;
                    case TIPO_ADMIN:
                        autenticarEmpleado(usuario, password);
                        break;
                    case TIPO_MECANICO:
                        autenticarEmpleado(usuario, password);
                        break;
                    default:
                        JOptionPane.showMessageDialog(LoginView.this,
                                "Tipo de login no soportado",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Botón Volver
        btnVolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginView().setVisible(true);
            }
        });
    }

    /**
     * Autentica a un cliente
     */
    private void autenticarCliente(String usuario, String password) {
        Cliente cliente = ClienteController.autenticarCliente(usuario, password);

        if (cliente != null) {
            GestorBitacora.registrarEvento(usuario, "Inicio de sesión", true,
                    "Cliente inició sesión: " + cliente.getNombreCompleto());

            dispose();
            new ClienteView(cliente).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Credenciales incorrectas",
                    "Error de Autenticación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Autentica a un empleado
     */
    private void autenticarEmpleado(String usuario, String password) {
        Empleado empleado = EmpleadoController.autenticarEmpleado(usuario, password);

        if (empleado != null) {
            GestorBitacora.registrarEvento(usuario, "Inicio de sesión", true,
                    "Empleado inició sesión: " + empleado.getNombreCompleto());

            dispose();

            if (empleado.isAdmin()) {
                new AdminView(empleado).setVisible(true);
            } else if (empleado instanceof Mecanico) {
                new MecanicoView((Mecanico) empleado).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Tipo de empleado no soportado",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                new LoginView().setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Credenciales incorrectas",
                    "Error de Autenticación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}