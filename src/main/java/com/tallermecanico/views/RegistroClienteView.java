package com.tallermecanico.views;

import com.tallermecanico.controllers.ClienteController;
import com.tallermecanico.models.personas.Cliente;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Vista para registro de nuevos clientes
 */
public class RegistroClienteView extends JFrame {

    private JTextField txtDPI;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegistrar;
    private JButton btnCancelar;
    private JFrame ventanaAnterior;

    /**
     * Constructor
     */
    public RegistroClienteView(JFrame ventanaAnterior) {
        this.ventanaAnterior = ventanaAnterior;
        inicializarComponentes();
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarComponentes() {
        // Configuración del JFrame
        setTitle("Registro de Nuevo Cliente");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Título
        JLabel lblTitulo = new JLabel("REGISTRO DE CLIENTE", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Panel de formulario
        JPanel panelForm = new JPanel(new GridLayout(7, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // DPI
        panelForm.add(new JLabel("DPI:"));
        txtDPI = new JTextField();
        panelForm.add(txtDPI);

        // Nombre
        panelForm.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelForm.add(txtNombre);

        // Apellido
        panelForm.add(new JLabel("Apellido:"));
        txtApellido = new JTextField();
        panelForm.add(txtApellido);

        // Usuario
        panelForm.add(new JLabel("Usuario:"));
        txtUsuario = new JTextField();
        panelForm.add(txtUsuario);

        // Contraseña
        panelForm.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panelForm.add(txtPassword);

        // Confirmar Contraseña
        panelForm.add(new JLabel("Confirmar Contraseña:"));
        txtConfirmPassword = new JPasswordField();
        panelForm.add(txtConfirmPassword);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRegistrar = new JButton("Registrar");
        btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);

        panel.add(panelForm, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarCliente();
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelar();
            }
        });

        // Agregar panel al frame
        add(panel);
    }

    /**
     * Registra un nuevo cliente
     */
    private void registrarCliente() {
        String dpi = txtDPI.getText();
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        String usuario = txtUsuario.getText();
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
            JOptionPane.showMessageDialog(this,
                    "Cliente registrado exitosamente",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);

            // Volver a la pantalla de login
            cancelar();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al registrar cliente. Verifique que el DPI o usuario no estén duplicados",
                    "Error de registro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cancelar registro y volver a pantalla anterior
     */
    private void cancelar() {
        dispose();
        ventanaAnterior.setVisible(true);
    }
}