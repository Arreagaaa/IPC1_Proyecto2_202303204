package com.tallermecanico.views;

import javax.swing.*;
import java.awt.*;

/**
 * Clase base para las vistas del sistema.
 * Define el diseño general y evita la repetición de código.
 */
public abstract class BaseView extends JFrame {
    public BaseView(String titulo) {
        // Configurar el título de la ventana
        setTitle(titulo);

        // Configurar el tamaño y la posición de la ventana
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Configurar el diseño general
        setLayout(new BorderLayout());

        // Agregar el wallpaper como fondo
        JLabel fondo = new JLabel(new ImageIcon("resources/wallpaper.jpg"));
        fondo.setLayout(new BorderLayout());
        setContentPane(fondo);

        // Agregar el logo en la parte superior
        JLabel logo = new JLabel(new ImageIcon("resources/logo.png"));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        add(logo, BorderLayout.NORTH);

        // Configurar el icono de la ventana
        setIconImage(new ImageIcon("resources/icon.png").getImage());
    }

    /**
     * Método abstracto para inicializar los componentes de la vista.
     * Cada vista debe implementar este método.
     */
    protected abstract void inicializarComponentes();

    /**
     * Crea un botón estilizado con diseño profesional.
     *
     * @param texto El texto del botón.
     * @return Un botón estilizado.
     */
    protected JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(new Color(173, 216, 230)); // Azul claro
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 2)); // Azul más oscuro
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }
}