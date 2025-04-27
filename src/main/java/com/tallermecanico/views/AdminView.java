package com.tallermecanico.views;

import com.tallermecanico.controllers.*;
import com.tallermecanico.models.*;
import com.tallermecanico.models.personas.*;
import com.tallermecanico.utils.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

public class AdminView extends BaseView {
    private Empleado administrador;
    private JTabbedPane tabbedPane;
    private JTable tablaRepuestos;
    private DefaultTableModel modeloRepuestos;
    private JTable tablaServicios;
    private DefaultTableModel modeloServicios;
    private JTable tablaClientes;
    private DefaultTableModel modeloClientes;
    private JTable tablaProgreso;
    private DefaultTableModel modeloProgreso;
    private JTable tablaFacturas;
    private DefaultTableModel modeloFacturas;

    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Arial", Font.BOLD, 16);
    private static final Font FONT_NORMAL = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 12);

    private static final Color COLOR_PRIMARY = new Color(0, 120, 215);
    private static final Color COLOR_SECONDARY = new Color(230, 230, 230);
    private static final Color COLOR_LIGHT = Color.WHITE;
    private static final Color COLOR_TEXT = new Color(40, 40, 40);

    public AdminView(Empleado administrador) {
        super("Panel de Administración");
        this.administrador = administrador;
        inicializarComponentes();
        cargarDatos();
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    @Override
    protected void inicializarComponentes() {
        contentPanel.setBackground(COLOR_LIGHT);

        JPanel navBar = crearBarraNavegacion(
                administrador.getNombreCompleto(),
                "Administrador");
        contentPanel.add(navBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_NORMAL);
        tabbedPane.setBackground(COLOR_LIGHT);
        tabbedPane.setForeground(COLOR_PRIMARY);

        tabbedPane.addTab("Repuestos", inicializarPanelRepuestos());
        tabbedPane.addTab("Servicios", inicializarPanelServicios());
        tabbedPane.addTab("Clientes", inicializarPanelClientes());
        tabbedPane.addTab("Progreso de Automóviles", inicializarPanelProgreso());
        tabbedPane.addTab("Facturas", inicializarPanelFacturas());
        tabbedPane.addTab("Reportes", inicializarPanelReportes());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Menú para reiniciar el sistema
        JMenuBar menuBar = new JMenuBar();
        JMenu menuSistema = new JMenu("Opciones del Sistema");
        menuSistema.setFont(FONT_NORMAL);
        JMenuItem menuItemLimpiarDatos = new JMenuItem("Reiniciar Sistema");
        menuItemLimpiarDatos.addActionListener(e -> {
            int confirmar = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea eliminar TODOS los datos del sistema?\n" +
                            "Esta acción no se puede deshacer.",
                    "Confirmar reinicio",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmar == JOptionPane.YES_OPTION) {
                Serializador.limpiarDatos();
                JOptionPane.showMessageDialog(this,
                        "Todos los datos han sido eliminados.\nLa aplicación se reiniciará.",
                        "Sistema reiniciado",
                        JOptionPane.INFORMATION_MESSAGE);

                // Reiniciar la aplicación
                dispose();
                new LoginView().setVisible(true);
            }
        });
        menuSistema.add(menuItemLimpiarDatos);
        menuBar.add(menuSistema);
        setJMenuBar(menuBar);
    }

    // ------------------- REPUESTOS -------------------
    private JPanel inicializarPanelRepuestos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COLOR_LIGHT);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(COLOR_LIGHT);

        JLabel lblBuscar = crearEtiqueta("Buscar:");
        JTextField txtBuscar = crearCampoTexto();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        JButton btnBuscar = crearBotonSecundario("Buscar");
        btnBuscar.addActionListener(e -> buscarRepuesto(txtBuscar.getText()));

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(COLOR_LIGHT);

        JButton btnAgregar = crearBotonPrimario("Agregar");
        btnAgregar.addActionListener(e -> agregarRepuesto());

        JButton btnEditar = crearBotonSecundario("Editar");
        btnEditar.addActionListener(e -> editarRepuesto());

        JButton btnEliminar = crearBotonSecundario("Eliminar");
        btnEliminar.addActionListener(e -> eliminarRepuesto());

        JButton btnCargar = crearBotonSecundario("Cargar Archivo");
        btnCargar.addActionListener(e -> cargarArchivoRepuestos());

        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCargar);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Existencias", "Precio" };
        modeloRepuestos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaRepuestos = new JTable(modeloRepuestos);
        estilizarTabla(tablaRepuestos);

        JScrollPane scrollPane = new JScrollPane(tablaRepuestos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void buscarRepuesto(String texto) {
        modeloRepuestos.setRowCount(0);
        Vector<Repuesto> repuestos = RepuestoController.buscarRepuestos(texto);
        for (int i = 0; i < repuestos.size(); i++) {
            Repuesto r = repuestos.get(i);
            modeloRepuestos.addRow(new Object[] {
                    r.getId(), r.getNombre(), r.getMarca(), r.getModelo(), r.getExistencias(),
                    String.format("Q %.2f", r.getPrecio())
            });
        }
    }

    private void agregarRepuesto() {
        JTextField nombre = new JTextField();
        JTextField marca = new JTextField();
        JTextField modelo = new JTextField();
        JTextField existencias = new JTextField();
        JTextField precio = new JTextField();

        Object[] campos = {
                "Nombre:", nombre,
                "Marca:", marca,
                "Modelo:", modelo,
                "Existencias:", existencias,
                "Precio:", precio
        };

        int res = JOptionPane.showConfirmDialog(this, campos, "Agregar Repuesto", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String n = nombre.getText().trim();
                String m = marca.getText().trim();
                String mo = modelo.getText().trim();
                int ex = Integer.parseInt(existencias.getText().trim());
                double p = Double.parseDouble(precio.getText().trim());
                if (n.isEmpty() || m.isEmpty() || mo.isEmpty() || ex < 0 || p < 0)
                    throw new Exception();
                boolean ok = RepuestoController.registrarRepuesto(n, m, mo, ex, p) != null;
                if (ok) {
                    cargarDatos();
                    JOptionPane.showMessageDialog(this, "Repuesto agregado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al agregar repuesto.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editarRepuesto() {
        int fila = tablaRepuestos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un repuesto para editar.");
            return;
        }
        int id = (int) modeloRepuestos.getValueAt(fila, 0);
        Repuesto r = RepuestoController.buscarRepuestoPorId(id);
        if (r == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el repuesto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextField nombre = new JTextField(r.getNombre());
        JTextField marca = new JTextField(r.getMarca());
        JTextField modelo = new JTextField(r.getModelo());
        JTextField existencias = new JTextField(String.valueOf(r.getExistencias()));
        JTextField precio = new JTextField(String.valueOf(r.getPrecio()));

        Object[] campos = {
                "Nombre:", nombre,
                "Marca:", marca,
                "Modelo:", modelo,
                "Existencias:", existencias,
                "Precio:", precio
        };

        int res = JOptionPane.showConfirmDialog(this, campos, "Editar Repuesto", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String n = nombre.getText().trim();
                String m = marca.getText().trim();
                String mo = modelo.getText().trim();
                int ex = Integer.parseInt(existencias.getText().trim());
                double p = Double.parseDouble(precio.getText().trim());
                if (n.isEmpty() || m.isEmpty() || mo.isEmpty() || ex < 0 || p < 0)
                    throw new Exception();
                boolean ok = RepuestoController.actualizarRepuesto(id, n, m, mo, ex, p);
                if (ok) {
                    cargarDatos();
                    JOptionPane.showMessageDialog(this, "Repuesto editado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al editar repuesto.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarRepuesto() {
        int fila = tablaRepuestos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un repuesto para eliminar.");
            return;
        }
        int id = (int) modeloRepuestos.getValueAt(fila, 0);
        int res = JOptionPane.showConfirmDialog(this, "¿Eliminar repuesto seleccionado?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            boolean ok = RepuestoController.eliminarRepuesto(id);
            if (ok) {
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Repuesto eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar repuesto.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarArchivoRepuestos() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            int count = 0, errores = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                StringBuilder mensajeError = new StringBuilder("Errores encontrados:\n");

                while ((linea = br.readLine()) != null) {
                    try {
                        // Para archivos TMR, el formato es: ID-Nombre-Marca-Modelo-Existencias-Precio
                        String[] partes = linea.split("-");

                        if (partes.length >= 6) { // Al menos 6 partes (ID, Nombre, Marca, Modelo, Existencias, Precio)
                            // Ignorar el ID (partes[0]) y usar los demás campos
                            String n = partes[1].trim();
                            String m = partes[2].trim();
                            String mo = partes[3].trim();
                            int ex = Integer.parseInt(partes[4].trim());
                            double p = Double.parseDouble(partes[5].trim().replace(",", "."));

                            if (n.isEmpty() || m.isEmpty() || mo.isEmpty() || ex < 0 || p < 0)
                                throw new Exception("Datos vacíos o negativos");

                            boolean ok = RepuestoController.registrarRepuesto(n, m, mo, ex, p) != null;
                            if (ok)
                                count++;
                            else {
                                errores++;
                                mensajeError.append("- Error al registrar: ").append(linea).append("\n");
                            }
                        } else {
                            errores++;
                            mensajeError.append("- No hay suficientes partes: ").append(linea).append("\n");
                        }
                    } catch (Exception ex) {
                        errores++;
                        mensajeError.append("- Error procesando: ").append(linea).append(" (").append(ex.getMessage())
                                .append(")\n");
                    }
                }

                cargarDatos();

                // Si hubo errores, mostrar detalles
                if (errores > 0) {
                    // Limitar el mensaje para que no sea excesivamente largo
                    if (mensajeError.length() > 500)
                        mensajeError = new StringBuilder(
                                mensajeError.substring(0, 500) + "...\n(Truncado por longitud)");

                    Object[] opciones = { "Ver detalles", "Aceptar" };
                    int opcion = JOptionPane.showOptionDialog(
                            this,
                            "Carga finalizada.\nAgregados: " + count + "\nErrores: " + errores,
                            "Resultado de carga",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            opciones,
                            opciones[1]);

                    if (opcion == 0) { // Si elige "Ver detalles"
                        JTextArea textArea = new JTextArea(mensajeError.toString());
                        textArea.setEditable(false);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 300));
                        JOptionPane.showMessageDialog(this, scrollPane, "Detalles de errores",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Carga finalizada.\nAgregados: " + count + "\nErrores: " + errores);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ------------------- SERVICIOS -------------------
    private JPanel inicializarPanelServicios() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COLOR_LIGHT);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(COLOR_LIGHT);

        JLabel lblBuscar = crearEtiqueta("Buscar:");
        JTextField txtBuscar = crearCampoTexto();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        JButton btnBuscar = crearBotonSecundario("Buscar");
        btnBuscar.addActionListener(e -> buscarServicio(txtBuscar.getText()));

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(COLOR_LIGHT);

        JButton btnAgregar = crearBotonPrimario("Agregar");
        btnAgregar.addActionListener(e -> agregarServicio());

        JButton btnEditar = crearBotonSecundario("Editar");
        btnEditar.addActionListener(e -> editarServicio());

        JButton btnEliminar = crearBotonSecundario("Eliminar");
        btnEliminar.addActionListener(e -> eliminarServicio());

        JButton btnCargar = crearBotonSecundario("Cargar Archivo");
        btnCargar.addActionListener(e -> cargarArchivoServicios());

        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCargar);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        String[] columnas = { "ID", "Nombre", "Descripción", "Precio" };
        modeloServicios = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaServicios = new JTable(modeloServicios);
        estilizarTabla(tablaServicios);

        JScrollPane scrollPane = new JScrollPane(tablaServicios);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void buscarServicio(String texto) {
        modeloServicios.setRowCount(0);
        // Usa el método correcto que acepta un String para buscar
        Vector<Servicio> servicios = ServicioController.buscarServicios(texto);
        for (int i = 0; i < servicios.size(); i++) {
            Servicio s = servicios.get(i);
            modeloServicios.addRow(new Object[] {
                    s.getId(), s.getNombre(), s.getDescripcion(), String.format("Q %.2f", s.getPrecioTotal())
            });
        }
    }

    private void agregarServicio() {
        JTextField nombre = new JTextField();
        JTextField descripcion = new JTextField();
        JTextField precio = new JTextField();

        Object[] campos = {
                "Nombre:", nombre,
                "Descripción:", descripcion,
                "Precio:", precio
        };

        int res = JOptionPane.showConfirmDialog(this, campos, "Agregar Servicio", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String n = nombre.getText().trim();
                String d = descripcion.getText().trim();
                double p = Double.parseDouble(precio.getText().trim());
                if (n.isEmpty() || d.isEmpty() || p < 0)
                    throw new Exception();
                boolean ok = ServicioController.registrarServicio(n, d, d, p) != null;
                if (ok) {
                    cargarDatos();
                    JOptionPane.showMessageDialog(this, "Servicio agregado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al agregar servicio.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editarServicio() {
        int fila = tablaServicios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio para editar.");
            return;
        }
        int id = (int) modeloServicios.getValueAt(fila, 0);
        Servicio s = ServicioController.buscarServicioPorId(id);
        if (s == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el servicio.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextField nombre = new JTextField(s.getNombre());
        JTextField descripcion = new JTextField(s.getDescripcion());
        JTextField precio = new JTextField(String.valueOf(s.getPrecioTotal()));

        Object[] campos = {
                "Nombre:", nombre,
                "Descripción:", descripcion,
                "Precio:", precio
        };

        int res = JOptionPane.showConfirmDialog(this, campos, "Editar Servicio", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String n = nombre.getText().trim();
                String d = descripcion.getText().trim();
                double p = Double.parseDouble(precio.getText().trim());
                if (n.isEmpty() || d.isEmpty() || p < 0)
                    throw new Exception();
                boolean ok = ServicioController.actualizarServicio(id, n, d, d, p);
                if (ok) {
                    cargarDatos();
                    JOptionPane.showMessageDialog(this, "Servicio editado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al editar servicio.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarServicio() {
        int fila = tablaServicios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio para eliminar.");
            return;
        }
        int id = (int) modeloServicios.getValueAt(fila, 0);
        int res = JOptionPane.showConfirmDialog(this, "¿Eliminar servicio seleccionado?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            boolean ok = ServicioController.eliminarServicio(id);
            if (ok) {
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Servicio eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar servicio.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarArchivoServicios() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            int count = 0, errores = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] partes = linea.split(",");
                    if (partes.length != 3) {
                        errores++;
                        continue;
                    }
                    try {
                        String n = partes[0].trim();
                        String d = partes[1].trim();
                        double p = Double.parseDouble(partes[2].trim());
                        if (n.isEmpty() || d.isEmpty() || p < 0)
                            throw new Exception();
                        boolean ok = ServicioController.registrarServicio(n, d, d, p) != null;
                        if (ok)
                            count++;
                        else
                            errores++;
                    } catch (Exception ex) {
                        errores++;
                    }
                }
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Carga finalizada.\nAgregados: " + count + "\nErrores: " + errores);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ------------------- CLIENTES -------------------
    private JPanel inicializarPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COLOR_LIGHT);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(COLOR_LIGHT);

        JLabel lblBuscar = crearEtiqueta("Buscar:");
        JTextField txtBuscar = crearCampoTexto();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        JButton btnBuscar = crearBotonSecundario("Buscar");
        btnBuscar.addActionListener(e -> buscarCliente(txtBuscar.getText()));

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(COLOR_LIGHT);

        JButton btnAgregar = crearBotonPrimario("Agregar");
        btnAgregar.addActionListener(e -> agregarCliente());

        JButton btnEditar = crearBotonSecundario("Editar");
        btnEditar.addActionListener(e -> editarCliente());

        JButton btnEliminar = crearBotonSecundario("Eliminar");
        btnEliminar.addActionListener(e -> eliminarCliente());

        JButton btnCargar = crearBotonSecundario("Cargar Archivo");
        btnCargar.addActionListener(e -> cargarArchivoClientes());

        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCargar);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        String[] columnas = { "DPI", "Nombre", "Usuario", "Tipo", "Automóviles" };
        modeloClientes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaClientes = new JTable(modeloClientes);
        estilizarTabla(tablaClientes);

        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void buscarCliente(String texto) {
        modeloClientes.setRowCount(0);
        Vector<Cliente> clientes = ClienteController.buscarClientes(texto);
        for (int i = 0; i < clientes.size(); i++) {
            Cliente c = clientes.get(i);
            modeloClientes.addRow(new Object[] {
                    c.getIdentificador(), c.getNombreCompleto(), c.getNombreUsuario(), c.getTipoCliente(),
                    c.getAutomoviles().size()
            });
        }
    }

    private void agregarCliente() {
        // Implementa según tus modelos y controladores
        JOptionPane.showMessageDialog(this, "Funcionalidad de agregar cliente aquí.");
    }

    private void editarCliente() {
        // Implementa según tus modelos y controladores
        JOptionPane.showMessageDialog(this, "Funcionalidad de editar cliente aquí.");
    }

    private void eliminarCliente() {
        // Implementa según tus modelos y controladores
        JOptionPane.showMessageDialog(this, "Funcionalidad de eliminar cliente aquí.");
    }

    private void cargarArchivoClientes() {
        // Implementa según tus modelos y controladores
        JOptionPane.showMessageDialog(this, "Funcionalidad de carga masiva de clientes aquí.");
    }

    // ------------------- PROGRESO DE AUTOMÓVILES -------------------
    private JPanel inicializarPanelProgreso() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Crear tabla y modelo
        String[] columnas = { "Orden", "Cliente", "Vehículo", "Servicio", "Estado", "Fecha" };
        modeloProgreso = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProgreso = new JTable(modeloProgreso);
        JScrollPane scrollPane = new JScrollPane(tablaProgreso);

        // Configurar tabla
        tablaProgreso.setFillsViewportHeight(true);
        tablaProgreso.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        estilizarTabla(tablaProgreso);

        // Cargar datos
        cargarProgreso();

        // Añadir botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_LIGHT);

        JButton btnDetalles = crearBotonSecundario("Ver Detalles");
        btnDetalles.addActionListener(e -> verDetallesOrden());

        buttonPanel.add(btnDetalles);

        // Añadir componentes al panel
        panel.add(new JLabel("Órdenes de Trabajo y Progreso"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarProgreso() {
        modeloProgreso.setRowCount(0);
        Vector<OrdenTrabajo> ordenes = OrdenTrabajoController.obtenerTodasLasOrdenes();
        for (OrdenTrabajo orden : ordenes) {
            modeloProgreso.addRow(new Object[] {
                    orden.getId(),
                    orden.getCliente().getNombreCompleto(),
                    orden.getAutomovil().getMarca() + " " + orden.getAutomovil().getModelo(),
                    orden.getServicio().getNombre(),
                    orden.getEstado(),
                    orden.getFecha()
            });
        }
    }

    private void verDetallesOrden() {
        int fila = tablaProgreso.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden para ver detalles.");
            return;
        }

        int id = (int) modeloProgreso.getValueAt(fila, 0);
        OrdenTrabajo orden = OrdenTrabajoController.obtenerOrdenPorId(id);

        if (orden != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Orden #").append(orden.getId()).append("\n\n");
            sb.append("Cliente: ").append(orden.getCliente().getNombreCompleto()).append("\n");
            sb.append("Vehículo: ").append(orden.getAutomovil().getMarca()).append(" ");
            sb.append(orden.getAutomovil().getModelo()).append(" (").append(orden.getAutomovil().getPlaca())
                    .append(")\n");
            sb.append("Servicio: ").append(orden.getServicio().getNombre()).append("\n");
            sb.append("Estado: ").append(orden.getEstado()).append("\n");
            sb.append("Fecha: ").append(orden.getFecha()).append("\n");
            sb.append("Mecánico: ")
                    .append(orden.getMecanico() != null ? orden.getMecanico().getNombreCompleto() : "No asignado");

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "Detalles de Orden", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ------------------- FACTURAS -------------------
    private JPanel inicializarPanelFacturas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Crear tabla y modelo
        String[] columnas = { "#", "Cliente", "Vehículo", "Servicio", "Fecha", "Total", "Estado" };
        modeloFacturas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaFacturas = new JTable(modeloFacturas);
        JScrollPane scrollPane = new JScrollPane(tablaFacturas);

        // Configurar tabla
        tablaFacturas.setFillsViewportHeight(true);
        tablaFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        estilizarTabla(tablaFacturas);

        // Cargar datos
        cargarFacturas();

        // Añadir botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_LIGHT);

        JButton btnDetalles = crearBotonSecundario("Ver Detalles");
        btnDetalles.addActionListener(e -> verDetallesFactura());

        JButton btnImprimir = crearBotonSecundario("Imprimir");
        btnImprimir.addActionListener(e -> imprimirFactura());

        buttonPanel.add(btnDetalles);
        buttonPanel.add(btnImprimir);

        // Añadir componentes al panel
        panel.add(new JLabel("Facturas"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarFacturas() {
        modeloFacturas.setRowCount(0);
        Vector<Factura> facturas = FacturaController.obtenerTodasLasFacturas();
        for (Factura factura : facturas) {
            modeloFacturas.addRow(new Object[] {
                    factura.getNumero(),
                    factura.getCliente().getNombreCompleto(),
                    factura.getOrdenTrabajo().getAutomovil().getMarca() + " "
                            + factura.getOrdenTrabajo().getAutomovil().getModelo(),
                    factura.getOrdenTrabajo().getServicio().getNombre(),
                    factura.getFechaEmision(),
                    String.format("Q %.2f", factura.getTotal()),
                    factura.getEstado()
            });
        }
    }

    private void verDetallesFactura() {
        int fila = tablaFacturas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura para ver detalles.");
            return;
        }

        int numero = (int) modeloFacturas.getValueAt(fila, 0);
        Factura factura = FacturaController.buscarFacturaPorNumero(numero);

        if (factura != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Factura #").append(factura.getNumero()).append("\n\n");
            sb.append("Cliente: ").append(factura.getCliente().getNombreCompleto()).append("\n");
            sb.append("Vehículo: ").append(factura.getOrdenTrabajo().getAutomovil().getMarca()).append(" ");
            sb.append(factura.getOrdenTrabajo().getAutomovil().getModelo()).append(" (")
                    .append(factura.getOrdenTrabajo().getAutomovil().getPlaca()).append(")\n");
            sb.append("Servicio: ").append(factura.getOrdenTrabajo().getServicio().getNombre()).append("\n");
            sb.append("Fecha: ").append(factura.getFechaEmision()).append("\n");
            sb.append("Total: ").append(String.format("Q %.2f", factura.getTotal())).append("\n");
            sb.append("Estado: ").append(factura.getEstado());

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "Detalles de Factura", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void imprimirFactura() {
        int fila = tablaFacturas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura para imprimir.");
            return;
        }

        int numero = (int) modeloFacturas.getValueAt(fila, 0);
        Factura factura = FacturaController.buscarFacturaPorNumero(numero);

        if (factura != null) {
            // Implementa la lógica de impresión aquí
            JOptionPane.showMessageDialog(this, "Funcionalidad de impresión de factura aquí.");
        }
    }

    // ------------------- REPORTES -------------------
    private JPanel inicializarPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel para botones de reportes
        JPanel buttonsPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonsPanel.setBackground(COLOR_LIGHT);

        JButton btnReporteClientes = crearBotonPrimario("Reporte de Clientes");
        JButton btnReporteRepuestosMasUsados = crearBotonPrimario("Top 10 Repuestos Más Usados");
        JButton btnReporteRepuestosMasCaros = crearBotonPrimario("Top 10 Repuestos Más Caros");
        JButton btnReporteServiciosMasUsados = crearBotonPrimario("Top 10 Servicios Más Usados");
        JButton btnReporteAutomovilesRepetidos = crearBotonPrimario("Top 5 Automóviles Más Comunes");

        // Añadir listeners
        btnReporteClientes.addActionListener(e -> mostrarReporte("clientes"));
        btnReporteRepuestosMasUsados.addActionListener(e -> mostrarReporte("repuestos_usados"));
        btnReporteRepuestosMasCaros.addActionListener(e -> mostrarReporte("repuestos_caros"));
        btnReporteServiciosMasUsados.addActionListener(e -> mostrarReporte("servicios_usados"));
        btnReporteAutomovilesRepetidos.addActionListener(e -> mostrarReporte("autos_repetidos"));

        // Añadir botones al panel
        buttonsPanel.add(btnReporteClientes);
        buttonsPanel.add(btnReporteRepuestosMasUsados);
        buttonsPanel.add(btnReporteRepuestosMasCaros);
        buttonsPanel.add(btnReporteServiciosMasUsados);
        buttonsPanel.add(btnReporteAutomovilesRepetidos);

        // Añadir panel de botones al panel principal
        panel.add(new JLabel("Seleccione un reporte para generar:"), BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.CENTER);

        return panel;
    }

    private void mostrarReporte(String tipoReporte) {
        JPanel reportePanel;
        String titulo;

        switch (tipoReporte) {
            case "clientes":
                reportePanel = GeneradorReportes.generarReporteClientes();
                titulo = "Reporte de Clientes";
                break;
            case "repuestos_usados":
                reportePanel = GeneradorReportes.generarReporteRepuestosMasUsados();
                titulo = "Top 10 Repuestos Más Usados";
                break;
            case "repuestos_caros":
                reportePanel = GeneradorReportes.generarReporteRepuestosMasCaros();
                titulo = "Top 10 Repuestos Más Caros";
                break;
            case "servicios_usados":
                reportePanel = GeneradorReportes.generarReporteServiciosMasUsados();
                titulo = "Top 10 Servicios Más Usados";
                break;
            case "autos_repetidos":
                reportePanel = GeneradorReportes.generarReporteAutomovilesRepetidos();
                titulo = "Top 5 Automóviles Más Comunes";
                break;
            default:
                return;
        }

        // Crear ventana de reporte
        JFrame reporteFrame = new JFrame(titulo);
        reporteFrame.setSize(900, 600);
        reporteFrame.setLocationRelativeTo(this);
        reporteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        reporteFrame.setContentPane(reportePanel);
        reporteFrame.setVisible(true);
    }

    // ------------------- UTILIDADES -------------------
    protected JPanel crearBarraNavegacion(String nombreUsuario, String tipoUsuario) {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(COLOR_PRIMARY);
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblLogo = new JLabel("Taller Mecánico");
        lblLogo.setFont(FONT_TITLE);
        lblLogo.setForeground(Color.WHITE);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(lblLogo);

        JLabel lblUsuario = new JLabel(nombreUsuario + " ");
        lblUsuario.setFont(FONT_SUBTITLE);
        lblUsuario.setForeground(Color.WHITE);

        JLabel lblTipo = new JLabel("(" + tipoUsuario + ")");
        lblTipo.setFont(FONT_SMALL);
        lblTipo.setForeground(new Color(220, 220, 220));

        JButton btnLogout = new JButton("Cerrar sesión");
        btnLogout.setBackground(new Color(180, 30, 30));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setOpaque(true);
        btnLogout.addActionListener(e -> cerrarSesion());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(lblUsuario);
        rightPanel.add(lblTipo);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(btnLogout);

        navBar.add(leftPanel, BorderLayout.WEST);
        navBar.add(rightPanel, BorderLayout.EAST);

        return navBar;
    }

    private void cargarDatos() {
        // Repuestos
        modeloRepuestos.setRowCount(0);
        Vector<Repuesto> repuestos = RepuestoController.obtenerTodosLosRepuestos();
        for (int i = 0; i < repuestos.size(); i++) {
            Repuesto r = repuestos.get(i);
            modeloRepuestos.addRow(new Object[] {
                    r.getId(), r.getNombre(), r.getMarca(), r.getModelo(), r.getExistencias(),
                    String.format("Q %.2f", r.getPrecio())
            });
        }
        // Servicios
        modeloServicios.setRowCount(0);
        Vector<Servicio> servicios = ServicioController.obtenerTodosLosServicios();
        for (int i = 0; i < servicios.size(); i++) {
            Servicio s = servicios.get(i);
            modeloServicios.addRow(new Object[] {
                    s.getId(), s.getNombre(), s.getDescripcion(), String.format("Q %.2f", s.getPrecioTotal())
            });
        }
        // Clientes
        modeloClientes.setRowCount(0);
        Vector<Cliente> clientes = ClienteController.obtenerTodosLosClientes();
        for (int i = 0; i < clientes.size(); i++) {
            Cliente c = clientes.get(i);
            modeloClientes.addRow(new Object[] {
                    c.getIdentificador(), c.getNombreCompleto(), c.getNombreUsuario(), c.getTipoCliente(),
                    c.getAutomoviles().size()
            });
        }
        // Progreso
        cargarProgreso();
        // Facturas
        cargarFacturas();
    }

    protected void estilizarTabla(JTable tabla) {
        tabla.setForeground(COLOR_TEXT);
        tabla.setBackground(Color.WHITE);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setSelectionBackground(COLOR_PRIMARY);
        tabla.setGridColor(COLOR_PRIMARY);
        tabla.setRowHeight(28);

        JTableHeader header = tabla.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                lbl.setBackground(COLOR_PRIMARY);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(FONT_SUBTITLE);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                return lbl;
            }
        });
    }

    protected JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONT_NORMAL);
        label.setForeground(COLOR_TEXT);
        return label;
    }

    protected JTextField crearCampoTexto() {
        JTextField field = new JTextField();
        field.setFont(FONT_NORMAL);
        return field;
    }

    protected JButton crearBotonPrimario(String texto) {
        JButton button = new JButton(texto);
        button.setFont(FONT_NORMAL);
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    protected JButton crearBotonSecundario(String texto) {
        JButton button = new JButton(texto);
        button.setFont(FONT_NORMAL);
        button.setBackground(COLOR_SECONDARY);
        button.setForeground(COLOR_TEXT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    private void cerrarSesion() {
        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar cierre de sesión",
                JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            GestorBitacora.registrarEvento(
                    administrador.getNombreUsuario(),
                    "Cierre de sesión",
                    true,
                    "Administrador cerró sesión: " + administrador.getNombreCompleto());
            dispose();
            new LoginView().setVisible(true);
        }
    }
}