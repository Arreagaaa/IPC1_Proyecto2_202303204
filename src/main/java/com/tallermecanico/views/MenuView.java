package com.tallermecanico.views;

import javax.swing.*;
import java.awt.*;

/**
 * Vista del menú principal
 */
public class MenuView extends BaseView {

    /**
     * Constructor
     */
    public MenuView() {
        super("Menú Principal");
        inicializarComponentes();
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    @Override
    protected void inicializarComponentes() {
        // Configurar el panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridLayout(3, 2, 10, 10));
        panelPrincipal.setOpaque(false); // Fondo transparente para mostrar el wallpaper

        // Crear botones estilizados
        JButton btnClientes = crearBoton("Clientes");
        JButton btnRepuestos = crearBoton("Repuestos");
        JButton btnServicios = crearBoton("Servicios");
        JButton btnOrdenes = crearBoton("Órdenes de Trabajo");
        JButton btnReportes = crearBoton("Reportes");
        JButton btnSalir = crearBoton("Salir");

        // Agregar botones al panel
        panelPrincipal.add(btnClientes);
        panelPrincipal.add(btnRepuestos);
        panelPrincipal.add(btnServicios);
        panelPrincipal.add(btnOrdenes);
        panelPrincipal.add(btnReportes);
        panelPrincipal.add(btnSalir);

        // Agregar el panel principal al centro de la ventana
        add(panelPrincipal, BorderLayout.CENTER);

        // Configurar acción del botón Salir
        btnSalir.addActionListener(e -> System.exit(0));
    }

    /**
     * Crea un botón estilizado
     *
     * @param texto Texto del botón
     * @return Botón estilizado
     */
    protected JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setBackground(new Color(173, 216, 230)); // Azul claro
        boton.setForeground(Color.BLACK);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        return boton;
    }
}