package com.example.views.cliente;

import com.example.models.Automovil;
import com.example.models.GestorDatos;
import com.example.models.personas.Cliente;
import com.example.models.personas.Persona;
import com.example.utils.Serializador;
import com.example.views.BaseView;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Vector;

/**
 * Vista para que el cliente gestione sus automóviles
 */
public class MisAutomovilesView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JTable tablaAutomoviles;
    private DefaultTableModel modeloTabla;
    private JTextField txtPlaca;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtAnio;
    private JTextField txtColor;
    private JComboBox<String> cboTipo;
    private JLabel lblFoto;
    private JButton btnSeleccionarFoto;
    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;

    private String rutaFotoSeleccionada;
    private Automovil automovilSeleccionado;
    private boolean modoEdicion = false;

    public MisAutomovilesView(Persona usuario) {
        super("Mis Automóviles");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Mis Automóviles");
        titlePanel.add(titleLabel);

        // Panel principal dividido horizontalmente
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);

        // Panel izquierdo - Lista de automóviles
        JPanel leftPanel = createAutomovilesList();

        // Panel derecho - Formulario
        JPanel rightPanel = createAutomovilForm();

        // Configurar y agregar paneles al SplitPane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(450);
        splitPane.setOneTouchExpandable(true);

        // Agregar componentes al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarAutomoviles();
        configurarControles();
    }

    private JPanel createAutomovilesList() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Automóviles"));

        // Definir columnas para la tabla
        Vector<String> columnas = new Vector<>();
        columnas.add("Placa");
        columnas.add("Marca");
        columnas.add("Modelo");
        columnas.add("Año");
        columnas.add("Vista Previa");

        // Crear modelo de tabla
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4)
                    return ImageIcon.class;
                return Object.class;
            }
        };

        // Crear tabla
        tablaAutomoviles = new JTable(modeloTabla);
        tablaAutomoviles.setRowHeight(80); // Para las imágenes

        // Ajustar tamaño de columnas
        TableColumn columnaImagen = tablaAutomoviles.getColumnModel().getColumn(4);
        columnaImagen.setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tablaAutomoviles);

        // Detectar selección
        tablaAutomoviles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaAutomoviles.getSelectedRow() != -1) {
                mostrarAutomovilSeleccionado();
            }
        });

        // Panel de botones
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sortPanel.setOpaque(false);

        JButton btnOrdenarAsc = new JButton("Ordenar Ascendente (Placa)");
        JButton btnOrdenarDesc = new JButton("Ordenar Descendente (Placa)");

        btnOrdenarAsc.addActionListener(e -> ordenarAutomoviles(true));
        btnOrdenarDesc.addActionListener(e -> ordenarAutomoviles(false));

        sortPanel.add(btnOrdenarAsc);
        sortPanel.add(btnOrdenarDesc);

        // Estructura del panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(sortPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAutomovilForm() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Automóvil"));
        panel.setPreferredSize(new Dimension(350, 0));

        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Placa
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createLabel("Placa:"), gbc);

        gbc.gridy = 1;
        txtPlaca = new JTextField();
        formPanel.add(txtPlaca, gbc);

        // Marca
        gbc.gridy = 2;
        formPanel.add(createLabel("Marca:"), gbc);

        gbc.gridy = 3;
        txtMarca = new JTextField();
        formPanel.add(txtMarca, gbc);

        // Modelo
        gbc.gridy = 4;
        formPanel.add(createLabel("Modelo:"), gbc);

        gbc.gridy = 5;
        txtModelo = new JTextField();
        formPanel.add(txtModelo, gbc);

        // Año
        gbc.gridy = 6;
        formPanel.add(createLabel("Año:"), gbc);

        gbc.gridy = 7;
        txtAnio = new JTextField();
        formPanel.add(txtAnio, gbc);

        // Color
        gbc.gridy = 8;
        formPanel.add(createLabel("Color:"), gbc);

        gbc.gridy = 9;
        txtColor = new JTextField();
        formPanel.add(txtColor, gbc);

        // Tipo
        gbc.gridy = 10;
        formPanel.add(createLabel("Tipo:"), gbc);

        gbc.gridy = 11;
        String[] tipos = { "Sedan", "SUV", "Pick-up", "Hatchback", "Coupe", "Otro" };
        cboTipo = new JComboBox<>(tipos);
        formPanel.add(cboTipo, gbc);

        // Foto
        gbc.gridy = 12;
        formPanel.add(createLabel("Foto:"), gbc);

        gbc.gridy = 13;
        JPanel fotoPanel = new JPanel(new BorderLayout(5, 0));
        fotoPanel.setOpaque(false);

        lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(200, 150));
        lblFoto.setBorder(BorderFactory.createEtchedBorder());
        lblFoto.setHorizontalAlignment(JLabel.CENTER);

        btnSeleccionarFoto = new JButton("Seleccionar...");
        btnSeleccionarFoto.addActionListener(e -> seleccionarFoto());

        fotoPanel.add(lblFoto, BorderLayout.CENTER);
        fotoPanel.add(btnSeleccionarFoto, BorderLayout.SOUTH);

        formPanel.add(fotoPanel, gbc);

        // Panel de botones
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        buttonsPanel.setOpaque(false);

        btnAgregar = new JButton("Agregar");
        btnEditar = new JButton("Guardar Cambios");
        btnEliminar = new JButton("Eliminar");

        btnAgregar.addActionListener(e -> agregarAutomovil());
        btnEditar.addActionListener(e -> editarAutomovil());
        btnEliminar.addActionListener(e -> eliminarAutomovil());

        buttonsPanel.add(btnAgregar);
        buttonsPanel.add(btnEditar);
        buttonsPanel.add(btnEliminar);

        // Estructura del panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void configurarControles() {
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        limpiarFormulario();
    }

    private void cargarAutomoviles() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Obtener automóviles del cliente
        Cliente cliente = (Cliente) usuario;
        Vector<Automovil> automoviles = cliente.getAutomoviles();

        for (int i = 0; i < automoviles.size(); i++) {
            Automovil auto = automoviles.elementAt(i);

            // Crear miniatura para la tabla
            ImageIcon icono = null;
            if (auto.getRutaFoto() != null && !auto.getRutaFoto().isEmpty()) {
                try {
                    BufferedImage img = ImageIO.read(new File(auto.getRutaFoto()));
                    if (img != null) {
                        icono = new ImageIcon(img.getScaledInstance(80, 60, Image.SCALE_SMOOTH));
                    }
                } catch (IOException e) {
                    icono = new ImageIcon(new BufferedImage(80, 60, BufferedImage.TYPE_INT_RGB));
                }
            }

            if (icono == null) {
                icono = new ImageIcon(new BufferedImage(80, 60, BufferedImage.TYPE_INT_RGB));
            }

            Vector<Object> fila = new Vector<>();
            fila.add(auto.getPlaca());
            fila.add(auto.getMarca());
            fila.add(auto.getModelo());
            fila.add(auto.getAnio());
            fila.add(icono);

            modeloTabla.addRow(fila);
        }

        // Si hay filas, seleccionar la primera
        if (tablaAutomoviles.getRowCount() > 0) {
            tablaAutomoviles.setRowSelectionInterval(0, 0);
            mostrarAutomovilSeleccionado();
        }
    }

    private void mostrarAutomovilSeleccionado() {
        int rowIndex = tablaAutomoviles.getSelectedRow();
        if (rowIndex == -1)
            return;

        String placa = (String) modeloTabla.getValueAt(rowIndex, 0);

        // Obtener automóvil del cliente
        Cliente cliente = (Cliente) usuario;
        Vector<Automovil> automoviles = cliente.getAutomoviles();

        automovilSeleccionado = null;
        for (int i = 0; i < automoviles.size(); i++) {
            Automovil auto = automoviles.elementAt(i);
            if (auto.getPlaca().equals(placa)) {
                automovilSeleccionado = auto;
                break;
            }
        }

        if (automovilSeleccionado != null) {
            // Llenar formulario
            txtPlaca.setText(automovilSeleccionado.getPlaca());
            txtMarca.setText(automovilSeleccionado.getMarca());
            txtModelo.setText(automovilSeleccionado.getModelo());
            txtAnio.setText(String.valueOf(automovilSeleccionado.getAnio()));
            txtColor.setText(automovilSeleccionado.getColor());
            cboTipo.setSelectedItem(automovilSeleccionado.getTipo());

            // Mostrar foto
            mostrarFoto(automovilSeleccionado.getRutaFoto());
            rutaFotoSeleccionada = automovilSeleccionado.getRutaFoto();

            // Habilitar botones
            btnEditar.setEnabled(true);
            btnEliminar.setEnabled(true);

            // Deshabilitar campo de placa en edición
            txtPlaca.setEditable(false);
            modoEdicion = true;
        }
    }

    private void limpiarFormulario() {
        txtPlaca.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtAnio.setText("");
        txtColor.setText("");
        cboTipo.setSelectedIndex(0);
        lblFoto.setIcon(null);
        lblFoto.setText("Sin imagen");

        rutaFotoSeleccionada = null;
        automovilSeleccionado = null;

        txtPlaca.setEditable(true);
        modoEdicion = false;

        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    private void seleccionarFoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar foto del automóvil");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes JPG (*.jpg, *.jpeg)", "jpg", "jpeg"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".")).toLowerCase();

            if (!extension.equals(".jpg") && !extension.equals(".jpeg")) {
                showErrorMessage("La imagen debe estar en formato JPG/JPEG");
                return;
            }

            rutaFotoSeleccionada = selectedFile.getAbsolutePath();
            mostrarFoto(rutaFotoSeleccionada);
        }
    }

    private void mostrarFoto(String ruta) {
        if (ruta == null || ruta.isEmpty()) {
            lblFoto.setIcon(null);
            lblFoto.setText("Sin imagen");
            return;
        }

        try {
            BufferedImage img = ImageIO.read(new File(ruta));
            if (img != null) {
                ImageIcon icon = new ImageIcon(img.getScaledInstance(200, 150, Image.SCALE_SMOOTH));
                lblFoto.setIcon(icon);
                lblFoto.setText("");
            } else {
                lblFoto.setIcon(null);
                lblFoto.setText("Error al cargar imagen");
            }
        } catch (IOException e) {
            lblFoto.setIcon(null);
            lblFoto.setText("Error al cargar imagen");
        }
    }

    private void agregarAutomovil() {
        if (modoEdicion) {
            limpiarFormulario();
            return;
        }

        // Validar campos
        if (!validarCampos())
            return;

        try {
            // Verificar si la placa ya existe
            Cliente cliente = (Cliente) usuario;
            String placa = txtPlaca.getText().trim();

            for (int i = 0; i < cliente.getAutomoviles().size(); i++) {
                if (cliente.getAutomoviles().elementAt(i).getPlaca().equals(placa)) {
                    showErrorMessage("Ya existe un automóvil con esa placa");
                    return;
                }
            }

            int anio = Integer.parseInt(txtAnio.getText().trim());

            // Copiar la imagen a un directorio del proyecto si existe
            String rutaFotoFinal = rutaFotoSeleccionada;
            if (rutaFotoSeleccionada != null && !rutaFotoSeleccionada.isEmpty()) {
                rutaFotoFinal = copiarImagenADirectorioProyecto(rutaFotoSeleccionada, placa);
            }

            // Crear nuevo automóvil
            Automovil nuevoAuto = new Automovil(
                    placa,
                    txtMarca.getText().trim(),
                    txtModelo.getText().trim(),
                    anio,
                    txtColor.getText().trim(),
                    cboTipo.getSelectedItem().toString());

            if (rutaFotoFinal != null) {
                nuevoAuto.setRutaFoto(rutaFotoFinal);
            }

            // Agregar automóvil al cliente
            cliente.agregarAutomovil(nuevoAuto);

            // Agregar al gestor de datos global
            GestorDatos.getInstancia().agregarAutomovil(nuevoAuto);

            // Guardar cambios
            Serializador.guardarDatos();

            // Limpiar formulario y recargar tabla
            limpiarFormulario();
            cargarAutomoviles();

            showSuccessMessage("Automóvil agregado exitosamente");

        } catch (NumberFormatException e) {
            showErrorMessage("El año debe ser un número entero");
        } catch (Exception e) {
            showErrorMessage("Error al agregar el automóvil: " + e.getMessage());
        }
    }

    private void editarAutomovil() {
        if (automovilSeleccionado == null) {
            showErrorMessage("Debe seleccionar un automóvil para editar");
            return;
        }

        // Validar campos
        if (!validarCampos())
            return;

        try {
            int anio = Integer.parseInt(txtAnio.getText().trim());

            // Copiar la imagen a un directorio del proyecto si es nueva
            String rutaFotoFinal = automovilSeleccionado.getRutaFoto();
            if (rutaFotoSeleccionada != null && !rutaFotoSeleccionada.equals(automovilSeleccionado.getRutaFoto())) {
                rutaFotoFinal = copiarImagenADirectorioProyecto(rutaFotoSeleccionada, automovilSeleccionado.getPlaca());
            }

            // Actualizar datos del automóvil
            automovilSeleccionado.setMarca(txtMarca.getText().trim());
            automovilSeleccionado.setModelo(txtModelo.getText().trim());
            automovilSeleccionado.setAnio(anio);
            automovilSeleccionado.setColor(txtColor.getText().trim());
            automovilSeleccionado.setTipo(cboTipo.getSelectedItem().toString());

            if (rutaFotoFinal != null) {
                automovilSeleccionado.setRutaFoto(rutaFotoFinal);
            }

            // Guardar cambios
            Serializador.guardarDatos();

            // Recargar tabla
            cargarAutomoviles();

            showSuccessMessage("Automóvil actualizado exitosamente");

        } catch (NumberFormatException e) {
            showErrorMessage("El año debe ser un número entero");
        } catch (Exception e) {
            showErrorMessage("Error al actualizar el automóvil: " + e.getMessage());
        }
    }

    private void eliminarAutomovil() {
        if (automovilSeleccionado == null) {
            showErrorMessage("Debe seleccionar un automóvil para eliminar");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de eliminar este automóvil?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            Cliente cliente = (Cliente) usuario;

            // Eliminar automóvil del cliente
            cliente.eliminarAutomovil(automovilSeleccionado);

            // Eliminar automóvil del gestor de datos
            GestorDatos.getInstancia().eliminarAutomovil(automovilSeleccionado);

            // Guardar cambios
            Serializador.guardarDatos();

            // Limpiar formulario y recargar tabla
            limpiarFormulario();
            cargarAutomoviles();

            showSuccessMessage("Automóvil eliminado exitosamente");
        }
    }

    private void ordenarAutomoviles(boolean ascendente) {
        Cliente cliente = (Cliente) usuario;
        // Ordenar automóviles usando ShellSort por placa
        cliente.ordenarAutomoviles(ascendente);

        // Recargar tabla
        cargarAutomoviles();
    }

    private boolean validarCampos() {
        if (txtPlaca.getText().trim().isEmpty()) {
            showErrorMessage("La placa es obligatoria");
            txtPlaca.requestFocus();
            return false;
        }

        // Validar formato de placa (opcional, según requisitos específicos)
        // Ejemplo: 3 letras seguidas de 3 números
        String placa = txtPlaca.getText().trim();
        if (!placa.matches("[A-Z0-9]{3,7}")) {
            showErrorMessage("La placa debe tener un formato válido (3-7 caracteres alfanuméricos)");
            txtPlaca.requestFocus();
            return false;
        }

        if (txtMarca.getText().trim().isEmpty()) {
            showErrorMessage("La marca es obligatoria");
            txtMarca.requestFocus();
            return false;
        }

        if (txtModelo.getText().trim().isEmpty()) {
            showErrorMessage("El modelo es obligatorio");
            txtModelo.requestFocus();
            return false;
        }

        if (txtAnio.getText().trim().isEmpty()) {
            showErrorMessage("El año es obligatorio");
            txtAnio.requestFocus();
            return false;
        }

        try {
            int anio = Integer.parseInt(txtAnio.getText().trim());
            if (anio < 1900 || anio > 2024) { // Ajustar según requisitos específicos
                showErrorMessage("El año debe estar entre 1900 y 2024");
                txtAnio.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("El año debe ser un número válido");
            txtAnio.requestFocus();
            return false;
        }

        // Verificar que haya una foto seleccionada (ya que es requisito según
        // especificaciones)
        if (rutaFotoSeleccionada == null || rutaFotoSeleccionada.isEmpty()) {
            showErrorMessage("Debe seleccionar una foto del automóvil (formato .jpg)");
            return false;
        }

        return true;
    }

    private String copiarImagenADirectorioProyecto(String rutaOriginal, String placa) {
        try {
            // Crear directorio para imágenes si no existe
            Path directorioImagenes = Paths.get("imagenes_automoviles");
            if (!Files.exists(directorioImagenes)) {
                Files.createDirectories(directorioImagenes);
            }

            // Verificar que es una imagen JPG como requiere la especificación
            String extension = rutaOriginal.substring(rutaOriginal.lastIndexOf(".")).toLowerCase();
            if (!extension.equals(".jpg") && !extension.equals(".jpeg")) {
                // Convertir a JPG si no lo es
                BufferedImage originalImage = ImageIO.read(new File(rutaOriginal));

                // Nombre para el nuevo archivo jpg
                String nombreArchivo = "auto_" + placa.replace(" ", "_") + ".jpg";
                Path rutaDestino = directorioImagenes.resolve(nombreArchivo);

                // Guardar como JPG
                File outputFile = rutaDestino.toFile();
                ImageIO.write(originalImage, "jpg", outputFile);

                return rutaDestino.toString();
            } else {
                // Si ya es JPG, simplemente copiar
                String nombreArchivo = "auto_" + placa.replace(" ", "_") + extension;
                Path rutaDestino = directorioImagenes.resolve(nombreArchivo);

                // Copiar archivo
                Files.copy(Paths.get(rutaOriginal), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

                return rutaDestino.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Error al procesar la imagen: " + e.getMessage());
            return null;
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}