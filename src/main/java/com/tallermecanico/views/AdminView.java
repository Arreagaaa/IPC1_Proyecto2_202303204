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
    private static final Color COLOR_PRIMARY_DARK = new Color(0, 90, 160);

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
        // Inicializar todos los modelos de tabla primero
        modeloRepuestos = new DefaultTableModel();
        modeloServicios = new DefaultTableModel();
        modeloClientes = new DefaultTableModel();
        modeloProgreso = new DefaultTableModel();
        modeloFacturas = new DefaultTableModel();

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

        // Botón para carga masiva de archivos
        JButton btnCargaMasiva = crearBotonPrimario("Cargar Archivos Masivos");
        btnCargaMasiva.setBackground(new Color(100, 100, 100));
        btnCargaMasiva.setForeground(Color.WHITE);
        btnCargaMasiva.setFocusPainted(false);
        btnCargaMasiva.setBorderPainted(false);
        btnCargaMasiva.setOpaque(true);
        btnCargaMasiva.addActionListener(e -> {
            try {
                int option = JOptionPane.showConfirmDialog(this,
                        "Esta acción cargará archivos masivos de datos.\n¿Desea continuar?",
                        "Confirmación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    // Primero limpiar duplicados para evitar problemas
                    int duplicadosEliminados = RepuestoController.eliminarRepuestosDuplicados();

                    // Cargar archivos
                    File repuestosFile = new File("repuestos.tmr");
                    if (repuestosFile.exists()) {
                        int repuestosCargados = CargadorArchivos.cargarRepuestos(repuestosFile);

                        // Cargar otros datos
                        CargadorArchivos.cargarDatosIniciales();

                        // Mostrar resultado
                        JOptionPane.showMessageDialog(this,
                                "Carga masiva completada exitosamente.\n" +
                                        "- Repuestos cargados: " + repuestosCargados + "\n" +
                                        "- Duplicados eliminados: " + duplicadosEliminados,
                                "Operación exitosa", JOptionPane.INFORMATION_MESSAGE);

                        // Recargar datos en pantalla
                        cargarDatos();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se encontró el archivo de repuestos en:\n" + repuestosFile.getAbsolutePath(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error durante la carga masiva: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        JPanel panelMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelMenu.setOpaque(false);
        panelMenu.add(btnCargaMasiva);
        menuBar.add(panelMenu);
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
        btnBuscar.setForeground(COLOR_PRIMARY_DARK);

        btnBuscar.addActionListener(e -> buscarRepuesto(txtBuscar.getText()));

        searchPanel.add(lblBuscar);
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(COLOR_LIGHT);

        JButton btnAgregar = crearBotonPrimario("Agregar");
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setBackground(new Color(0, 120, 215));

        JButton btnEditar = crearBotonPrimario("Editar");
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setBackground(new Color(255, 140, 0));

        JButton btnEliminar = crearBotonPrimario("Eliminar");
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setBackground(new Color(200, 0, 0));

        JButton btnCargar = crearBotonPrimario("Cargar Archivo");
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setBackground(new Color(100, 100, 100));

        btnAgregar.addActionListener(e -> agregarRepuesto());
        btnEditar.addActionListener(e -> editarRepuesto());
        btnEliminar.addActionListener(e -> eliminarRepuesto());
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
                String exStr = existencias.getText().trim();
                String pStr = precio.getText().trim().replace(",", ".");
                if (n.isEmpty() || m.isEmpty() || mo.isEmpty() || exStr.isEmpty() || pStr.isEmpty())
                    throw new Exception("Todos los campos son obligatorios.");
                int ex = Integer.parseInt(exStr);
                double p = Double.parseDouble(pStr);
                if (ex < 0 || p < 0)
                    throw new Exception("Existencias y precio deben ser positivos.");
                Repuesto nuevoRepuesto = RepuestoController.registrarRepuesto(n, m, mo, ex, p);
                if (nuevoRepuesto != null) {
                    cargarDatos();
                    JOptionPane.showMessageDialog(this, "Repuesto agregado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al agregar repuesto.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Existencias y precio deben ser numéricos.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editarRepuesto() {
        int filaSeleccionada = tablaRepuestos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            try {
                String idRepuesto = tablaRepuestos.getValueAt(filaSeleccionada, 0).toString();
                String nombre = tablaRepuestos.getValueAt(filaSeleccionada, 1).toString();
                String marca = tablaRepuestos.getValueAt(filaSeleccionada, 2).toString();
                String modelo = tablaRepuestos.getValueAt(filaSeleccionada, 3).toString();
                int existencias = Integer.parseInt(tablaRepuestos.getValueAt(filaSeleccionada, 4).toString());
                String precioStr = tablaRepuestos.getValueAt(filaSeleccionada, 5).toString().replace("Q", "").trim();
                double precio = Double.parseDouble(precioStr.replace(",", "."));

                JTextField txtNombre = new JTextField(nombre);
                JTextField txtMarca = new JTextField(marca);
                JTextField txtModelo = new JTextField(modelo);
                JTextField txtExistencias = new JTextField(String.valueOf(existencias));
                JTextField txtPrecio = new JTextField(String.format("%.2f", precio));

                Object[] campos = {
                        "Nombre:", txtNombre,
                        "Marca:", txtMarca,
                        "Modelo:", txtModelo,
                        "Existencias:", txtExistencias,
                        "Precio:", txtPrecio
                };

                int res = JOptionPane.showConfirmDialog(this, campos, "Editar Repuesto", JOptionPane.OK_CANCEL_OPTION);
                if (res == JOptionPane.OK_OPTION) {
                    String nuevoNombre = txtNombre.getText().trim();
                    String nuevaMarca = txtMarca.getText().trim();
                    String nuevoModelo = txtModelo.getText().trim();
                    int nuevasExistencias = Integer.parseInt(txtExistencias.getText().trim());
                    double nuevoPrecio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));

                    if (nuevoNombre.isEmpty() || nuevaMarca.isEmpty() || nuevoModelo.isEmpty() ||
                            nuevasExistencias < 0 || nuevoPrecio < 0) {
                        throw new Exception("Datos inválidos");
                    }

                    boolean actualizado = RepuestoController.actualizarRepuesto(
                            idRepuesto, nuevoNombre, nuevaMarca, nuevoModelo, nuevasExistencias, nuevoPrecio);

                    if (actualizado) {
                        cargarDatos();
                        JOptionPane.showMessageDialog(this, "Repuesto actualizado correctamente.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al actualizar el repuesto.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al editar repuesto: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un repuesto para editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarRepuesto() {
        int filaSeleccionada = tablaRepuestos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            try {
                String idRepuesto = tablaRepuestos.getValueAt(filaSeleccionada, 0).toString();
                String nombre = tablaRepuestos.getValueAt(filaSeleccionada, 1).toString();

                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Está seguro de eliminar el repuesto '" + nombre + "'?",
                        "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    boolean eliminado = RepuestoController.eliminarRepuesto(idRepuesto);

                    if (eliminado) {
                        cargarDatos();
                        JOptionPane.showMessageDialog(this, "Repuesto eliminado correctamente.");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo eliminar el repuesto.\nPodría estar en uso en algún servicio.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un repuesto para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
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

                            Repuesto nuevoRepuesto = RepuestoController.registrarRepuesto(n, m, mo, ex, p);
                            boolean ok = nuevoRepuesto != null;
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
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setBackground(new Color(0, 120, 215));

        JButton btnEditar = crearBotonPrimario("Editar");
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setBackground(new Color(255, 140, 0));

        JButton btnEliminar = crearBotonPrimario("Eliminar");
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setBackground(new Color(200, 0, 0));

        JButton btnCargar = crearBotonPrimario("Cargar Archivo");
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setBackground(new Color(100, 100, 100));

        btnAgregar.addActionListener(e -> agregarServicio());
        btnEditar.addActionListener(e -> editarServicio());
        btnEliminar.addActionListener(e -> eliminarServicio());
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
                double p = Double.parseDouble(precio.getText().trim().replace(",", "."));
                if (n.isEmpty() || d.isEmpty() || p < 0)
                    throw new Exception("Datos vacíos o negativos");
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
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setBackground(new Color(0, 120, 215));

        JButton btnEditar = crearBotonPrimario("Editar");
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setBackground(new Color(255, 140, 0));

        JButton btnEliminar = crearBotonPrimario("Eliminar");
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setBackground(new Color(200, 0, 0));

        JButton btnCargar = crearBotonPrimario("Cargar Archivo");
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setBackground(new Color(100, 100, 100));

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

        // Panel de título y filtros
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COLOR_LIGHT);

        JLabel titleLabel = new JLabel("Gestión de Facturas");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_PRIMARY_DARK);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Panel de filtros
        JPanel filtrosPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filtrosPanel.setBackground(COLOR_LIGHT);

        JLabel lblFiltro = new JLabel("Filtrar por: ");
        JComboBox<String> comboEstado = new JComboBox<>(new String[] { "Todos", "Pendiente", "Pagada" });
        JTextField txtBuscar = new JTextField(15);
        JButton btnBuscar = crearBotonSecundario("Buscar");

        filtrosPanel.add(lblFiltro);
        filtrosPanel.add(comboEstado);
        filtrosPanel.add(txtBuscar);
        filtrosPanel.add(btnBuscar);

        // Añadir estadísticas de facturas
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(COLOR_LIGHT);

        JLabel lblTotalFacturas = new JLabel("Total facturas: 0");
        JLabel lblFacturasPendientes = new JLabel("Pendientes: 0");
        JLabel lblFacturasPagadas = new JLabel("Pagadas: 0");

        statsPanel.add(lblTotalFacturas);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(lblFacturasPendientes);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(lblFacturasPagadas);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statsPanel, BorderLayout.CENTER);
        headerPanel.add(filtrosPanel, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Tabla de facturas
        String[] columnas = { "ID", "Cliente", "Vehículo", "Servicio", "Total", "Fecha", "Estado", "Acciones" };
        DefaultTableModel modeloFacturas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Solo la columna de acciones es editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        JTable tablaFacturas = new JTable(modeloFacturas);
        JScrollPane scrollPane = new JScrollPane(tablaFacturas);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1));

        tablaFacturas.setFillsViewportHeight(true);
        estilizarTabla(tablaFacturas);

        // Configurar anchos de columna
        TableColumnModel columnModel = tablaFacturas.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40); // ID
        columnModel.getColumn(1).setPreferredWidth(150); // Cliente
        columnModel.getColumn(2).setPreferredWidth(120); // Vehículo
        columnModel.getColumn(3).setPreferredWidth(150); // Servicio
        columnModel.getColumn(4).setPreferredWidth(80); // Total
        columnModel.getColumn(5).setPreferredWidth(100); // Fecha
        columnModel.getColumn(6).setPreferredWidth(80); // Estado
        columnModel.getColumn(7).setPreferredWidth(170); // Acciones

        // Renderizador especial para la columna de estado
        tablaFacturas.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (value != null) {
                    String estado = value.toString();
                    if (estado.equals("Pendiente")) {
                        label.setForeground(new Color(214, 69, 65)); // Rojo
                    } else if (estado.equals("Pagada")) {
                        label.setForeground(new Color(46, 125, 50)); // Verde
                    }
                    label.setFont(new Font("Arial", Font.BOLD, 12));
                }
                return label;
            }
        });

        // Renderer y editor para los botones de acción
        tablaFacturas.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
                panel.setOpaque(true);

                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(table.getBackground());
                }

                // Botón para cambiar estado
                String estado = (String) table.getValueAt(row, 6);
                String textoCambio = estado.equals("Pendiente") ? "Marcar Pagada" : "Marcar Pendiente";
                Color colorBoton = estado.equals("Pendiente") ? new Color(46, 125, 50) : new Color(214, 69, 65);

                JButton btnCambiar = new JButton(textoCambio);
                btnCambiar.setFont(new Font("Arial", Font.BOLD, 10));
                btnCambiar.setForeground(Color.WHITE);
                btnCambiar.setBackground(colorBoton);
                btnCambiar.setBorderPainted(false);
                btnCambiar.setFocusPainted(false);

                // Botón para ver PDF
                JButton btnPDF = new JButton("Ver PDF");
                btnPDF.setFont(new Font("Arial", Font.BOLD, 10));
                btnPDF.setForeground(Color.WHITE);
                btnPDF.setBackground(new Color(66, 139, 202));
                btnPDF.setBorderPainted(false);
                btnPDF.setFocusPainted(false);

                panel.add(btnCambiar);
                panel.add(btnPDF);

                return panel;
            }
        });

        tablaFacturas.getColumnModel().getColumn(7).setCellEditor(new ButtonPanelEditor() {
            @Override
            public void onCambiarEstadoClick(int row) {
                try {
                    int facturaId = Integer.parseInt(modeloFacturas.getValueAt(row, 0).toString());
                    String estadoActual = (String) modeloFacturas.getValueAt(row, 6);

                    // Determinar el nuevo estado
                    String nuevoEstado = estadoActual.equals("Pendiente") ? "Pagada" : "Pendiente";

                    // Obtener la factura actual
                    Factura factura = FacturaController.obtenerFacturaPorId(facturaId);
                    if (factura != null) {
                        // Asegurar que el estado se actualice correctamente
                        factura.setEstado(nuevoEstado);

                        // Actualizar en la base de datos
                        boolean ok = FacturaController.actualizarFactura(factura);

                        if (ok) {
                            // Actualizar SOLO la celda de estado en la tabla
                            modeloFacturas.setValueAt(nuevoEstado, row, 6);

                            // Actualizar la vista completa si es necesario
                            JOptionPane.showMessageDialog(null,
                                    "Estado de factura actualizado: " + factura.getId() + " → " + nuevoEstado,
                                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

                            // Actualizar estadísticas
                            actualizarEstadisticasFacturas(lblTotalFacturas, lblFacturasPendientes, lblFacturasPagadas);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Error al actualizar estado de factura.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Error inesperado: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void onVerPDFClick(int row) {
                int facturaId = Integer.parseInt(modeloFacturas.getValueAt(row, 0).toString());
                Factura factura = FacturaController.obtenerFacturaPorId(facturaId);
                if (factura != null) {
                    GeneradorPDF.generarFacturaPDF(factura);
                    JOptionPane.showMessageDialog(null, "Factura PDF generada correctamente.");
                }
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con botón de actualizar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(COLOR_LIGHT);

        JButton btnActualizar = crearBotonPrimario("Actualizar");
        bottomPanel.add(btnActualizar);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Cargar datos iniciales
        cargarDatosFacturas(modeloFacturas);
        actualizarEstadisticasFacturas(lblTotalFacturas, lblFacturasPendientes, lblFacturasPagadas);

        // Configurar la búsqueda/filtro
        btnBuscar.addActionListener(e -> {
            String filtroEstado = comboEstado.getSelectedItem().toString();
            String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

            modeloFacturas.setRowCount(0);
            Vector<Factura> todasFacturas = FacturaController.obtenerTodasLasFacturas();

            for (Factura factura : todasFacturas) {
                // Aplicar filtro de estado
                if (!filtroEstado.equals("Todos") && !factura.getEstado().equals(filtroEstado)) {
                    continue;
                }

                // Aplicar filtro de búsqueda por texto
                boolean coincide = textoBusqueda.isEmpty() ||
                        factura.getCliente().getNombreCompleto().toLowerCase().contains(textoBusqueda) ||
                        factura.getAutomovil().getPlaca().toLowerCase().contains(textoBusqueda) ||
                        factura.getServicioAsociado().getNombre().toLowerCase().contains(textoBusqueda);

                if (coincide) {
                    modeloFacturas.addRow(new Object[] {
                            factura.getId(),
                            factura.getCliente().getNombreCompleto(),
                            factura.getAutomovil().getMarca() + " " + factura.getAutomovil().getModelo(),
                            factura.getServicioAsociado().getNombre(),
                            "Q" + String.format("%.2f", factura.getTotal()),
                            factura.getFechaEmision(),
                            factura.getEstado(),
                            "Acciones" // Placeholder para los botones
                    });
                }
            }
        });

        // Actualizar datos al hacer clic en el botón
        btnActualizar.addActionListener(e -> {
            cargarDatosFacturas(modeloFacturas);
            actualizarEstadisticasFacturas(lblTotalFacturas, lblFacturasPendientes, lblFacturasPagadas);
        });

        return panel;
    }

    /**
     * Carga los datos de todas las facturas en la tabla
     */
    private void cargarDatosFacturas(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        Vector<Factura> facturas = FacturaController.obtenerTodasLasFacturas();

        for (Factura factura : facturas) {
            modelo.addRow(new Object[] {
                    factura.getId(),
                    factura.getCliente().getNombreCompleto(),
                    factura.getAutomovil().getMarca() + " " + factura.getAutomovil().getModelo(),
                    factura.getServicioAsociado().getNombre(),
                    "Q" + String.format("%.2f", factura.getTotal()),
                    factura.getFechaEmision(),
                    factura.getEstado(),
                    "Acciones" // Placeholder para los botones
            });
        }
    }

    /**
     * Actualiza las etiquetas con estadísticas de facturas
     */
    private void actualizarEstadisticasFacturas(JLabel lblTotal, JLabel lblPendientes, JLabel lblPagadas) {
        Vector<Factura> facturas = FacturaController.obtenerTodasLasFacturas();
        int total = facturas.size();
        int pendientes = 0;
        int pagadas = 0;

        for (Factura factura : facturas) {
            if (factura.getEstado().equals("Pendiente")) {
                pendientes++;
            } else if (factura.getEstado().equals("Pagada")) {
                pagadas++;
            }
        }

        lblTotal.setText("Total facturas: " + total);
        lblPendientes.setText("Pendientes: " + pendientes);
        lblPagadas.setText("Pagadas: " + pagadas);
    }

    /**
     * Editor de celda personalizado para manejar panel con dos botones
     */
    private abstract class ButtonPanelEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnCambiar;
        private JButton btnPDF;

        public ButtonPanelEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));

            btnCambiar = new JButton();
            btnCambiar.setFont(new Font("Arial", Font.BOLD, 10));
            btnCambiar.setForeground(Color.WHITE);
            btnCambiar.setBorderPainted(false);
            btnCambiar.setFocusPainted(false);

            btnPDF = new JButton("Ver PDF");
            btnPDF.setFont(new Font("Arial", Font.BOLD, 10));
            btnPDF.setForeground(Color.WHITE);
            btnPDF.setBackground(new Color(66, 139, 202));
            btnPDF.setBorderPainted(false);
            btnPDF.setFocusPainted(false);

            panel.add(btnCambiar);
            panel.add(btnPDF);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            // Configurar el botón según el estado actual
            String estado = (String) table.getValueAt(row, 6);
            String textoCambio = estado.equals("Pendiente") ? "Marcar Pagada" : "Marcar Pendiente";
            Color colorBoton = estado.equals("Pendiente") ? new Color(46, 125, 50) : new Color(214, 69, 65);

            btnCambiar.setText(textoCambio);
            btnCambiar.setBackground(colorBoton);

            final int capturedRow = row;

            btnCambiar.addActionListener(e -> {
                onCambiarEstadoClick(capturedRow);
                fireEditingStopped();
            });

            btnPDF.addActionListener(e -> {
                onVerPDFClick(capturedRow);
                fireEditingStopped();
            });

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Acciones";
        }

        public abstract void onCambiarEstadoClick(int row);

        public abstract void onVerPDFClick(int row);
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
        try {
            // Repuestos
            if (modeloRepuestos != null) {
                modeloRepuestos.setRowCount(0);
                Vector<Repuesto> repuestos = RepuestoController.obtenerTodosLosRepuestos();
                if (repuestos != null) {
                    for (Repuesto r : repuestos) {
                        modeloRepuestos.addRow(new Object[] {
                                r.getId(), r.getNombre(), r.getMarca(), r.getModelo(), r.getExistencias(),
                                String.format("Q %.2f", r.getPrecio())
                        });
                    }
                }
            }

            // Servicios
            if (modeloServicios != null) {
                modeloServicios.setRowCount(0);
                Vector<Servicio> servicios = ServicioController.obtenerTodosLosServicios();
                if (servicios != null) {
                    for (Servicio s : servicios) {
                        modeloServicios.addRow(new Object[] {
                                s.getId(), s.getNombre(), s.getDescripcion(),
                                String.format("Q %.2f", s.getPrecioTotal())
                        });
                    }
                }
            }

            // Clientes
            if (modeloClientes != null) {
                modeloClientes.setRowCount(0);
                Vector<Cliente> clientes = ClienteController.obtenerTodosLosClientes();
                if (clientes != null) {
                    for (Cliente c : clientes) {
                        modeloClientes.addRow(new Object[] {
                                c.getIdentificador(), c.getNombreCompleto(), c.getNombreUsuario(), c.getTipoCliente(),
                                c.getAutomoviles() != null ? c.getAutomoviles().size() : 0
                        });
                    }
                }
            }

            // Progreso
            if (modeloProgreso != null) {
                cargarProgreso();
            }

            // Facturas
            if (modeloFacturas != null) {
                cargarFacturas();
            }

        } catch (Exception e) {
            System.out.println("Error al cargar datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarFacturas() {
        // Verificar que el modelo de facturas exista
        if (modeloFacturas == null) {
            System.out.println("ADVERTENCIA: modeloFacturas es null en cargarFacturas()");
            return; // Salir del método si no existe el modelo
        }

        modeloFacturas.setRowCount(0);
        Vector<Factura> facturas = FacturaController.obtenerTodasLasFacturas();
        if (facturas != null) {
            for (int i = 0; i < facturas.size(); i++) {
                Factura f = facturas.get(i);
                modeloFacturas.addRow(new Object[] {
                        f.getId(), f.getCliente().getNombreCompleto(), f.getAutomovil().getMarca() + " " +
                                f.getAutomovil().getModelo(),
                        f.getServicioAsociado().getNombre(),
                        String.format("Q %.2f", f.getTotal()), f.getFechaEmision(), f.getEstado()
                });
            }
        }
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
        JButton btn = new JButton(texto);
        btn.setBackground(COLOR_SECONDARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
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