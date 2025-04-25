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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Vista para registrar un nuevo automóvil
 */
public class RegistroAutomovilView extends BaseView {

    private Persona usuario;
    private JPanel mainPanel;
    private JTextField campoPlaca;
    private JTextField campoMarca;
    private JTextField campoModelo;
    private JTextField campoAnio;
    private JTextField campoColor;
    private JComboBox<String> campoTipo;
    private JLabel labelImagen;
    private String rutaImagen;

    public RegistroAutomovilView(Persona usuario) {
        super("Registro de Automóvil");
        this.usuario = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        mainPanel = createContentPanel();
        mainPanel.setLayout(new BorderLayout(0, 10));

        // Panel de título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createSubtitle("Registro de Nuevo Automóvil");
        titlePanel.add(titleLabel);

        // Panel principal de formulario
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Panel para campos de texto
        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        fieldsPanel.setOpaque(false);

        // Etiquetas y campos
        JLabel lblPlaca = createLabel("Placa:");
        campoPlaca = createStylishTextField(15);

        JLabel lblMarca = createLabel("Marca:");
        campoMarca = createStylishTextField(15);

        JLabel lblModelo = createLabel("Modelo:");
        campoModelo = createStylishTextField(15);

        JLabel lblAnio = createLabel("Año:");
        campoAnio = createStylishTextField(15);

        JLabel lblColor = createLabel("Color:");
        campoColor = createStylishTextField(15);

        JLabel lblTipo = createLabel("Tipo:");
        String[] tiposAuto = { "Sedan", "SUV", "Pickup", "Hatchback", "Otro" };
        campoTipo = new JComboBox<>(tiposAuto);

        // Agregar componentes al panel de campos
        fieldsPanel.add(lblPlaca);
        fieldsPanel.add(campoPlaca);
        fieldsPanel.add(lblMarca);
        fieldsPanel.add(campoMarca);
        fieldsPanel.add(lblModelo);
        fieldsPanel.add(campoModelo);
        fieldsPanel.add(lblAnio);
        fieldsPanel.add(campoAnio);
        fieldsPanel.add(lblColor);
        fieldsPanel.add(campoColor);
        fieldsPanel.add(lblTipo);
        fieldsPanel.add(campoTipo);

        // Panel para la imagen
        JPanel imagenPanel = new JPanel();
        imagenPanel.setLayout(new BoxLayout(imagenPanel, BoxLayout.Y_AXIS));
        imagenPanel.setOpaque(false);
        imagenPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JLabel lblImagenTitulo = createLabel("Imagen del Automóvil (formato JPG):");
        lblImagenTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Contenedor para la imagen
        JPanel imagePlaceholder = new JPanel();
        imagePlaceholder.setBackground(new Color(245, 245, 245));
        imagePlaceholder.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY));
        imagePlaceholder.setPreferredSize(new Dimension(300, 200));
        imagePlaceholder.setMaximumSize(new Dimension(300, 200));

        labelImagen = new JLabel("No se ha seleccionado imagen", JLabel.CENTER);
        labelImagen.setPreferredSize(new Dimension(280, 180));
        imagePlaceholder.add(labelImagen);

        // Botón para seleccionar imagen
        JButton btnSeleccionarImagen = new JButton("Seleccionar Imagen JPG");
        btnSeleccionarImagen.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSeleccionarImagen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seleccionarImagen();
            }
        });

        // Agregar componentes al panel de imagen
        imagenPanel.add(lblImagenTitulo);
        imagenPanel.add(Box.createVerticalStrut(10));
        imagenPanel.add(imagePlaceholder);
        imagenPanel.add(Box.createVerticalStrut(10));
        imagenPanel.add(btnSeleccionarImagen);

        // Panel de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setOpaque(false);

        JButton btnRegistrar = createPrimaryButton("Registrar Automóvil");
        JButton btnCancelar = createSecondaryButton("Cancelar");

        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarAutomovil();
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarVista();
            }
        });

        buttonsPanel.add(btnRegistrar);
        buttonsPanel.add(btnCancelar);

        // Agregar todo al panel de formulario
        formPanel.add(fieldsPanel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(imagenPanel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(buttonsPanel);

        // Agregar paneles al panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
    }

    private void seleccionarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Imagen del Automóvil");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes JPG (*.jpg, *.jpeg)", "jpg", "jpeg"));

        if (fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".")).toLowerCase();

            // Verificar que sea JPG como requiere la especificación
            if (!extension.equals(".jpg") && !extension.equals(".jpeg")) {
                showErrorMessage("Solo se permiten imágenes en formato JPG/JPEG");
                return;
            }

            rutaImagen = selectedFile.getAbsolutePath();

            try {
                // Cargar y mostrar la imagen seleccionada
                BufferedImage img = ImageIO.read(selectedFile);
                Image scaledImg = img.getScaledInstance(280, 180, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImg);

                labelImagen.setIcon(icon);
                labelImagen.setText("");
            } catch (IOException ex) {
                showErrorMessage("Error al cargar la imagen: " + ex.getMessage());
                labelImagen.setIcon(null);
                labelImagen.setText("Error al cargar la imagen");
                rutaImagen = null;
            }
        }
    }

    /**
     * Copia la imagen al directorio del proyecto y devuelve la ruta
     */
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
                // Si no es JPG, mostrar error (esto no debería ocurrir si la validación en
                // seleccionarImagen funciona)
                showErrorMessage("Solo se permiten imágenes en formato JPG/JPEG");
                return null;
            }

            // Generar nombre para el archivo en nuestro directorio
            String nombreArchivo = "auto_" + placa.replace(" ", "_") + extension;
            Path rutaDestino = directorioImagenes.resolve(nombreArchivo);

            // Copiar archivo
            Files.copy(Paths.get(rutaOriginal), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            return rutaDestino.toString();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Error al procesar la imagen: " + e.getMessage());
            return null;
        }
    }

    private void registrarAutomovil() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }

        String placa = campoPlaca.getText().trim();
        String marca = campoMarca.getText().trim();
        String modelo = campoModelo.getText().trim();
        String tipo = (String) campoTipo.getSelectedItem();
        String color = campoColor.getText().trim();
        int anio = Integer.parseInt(campoAnio.getText().trim());

        // Verificar si la placa ya existe
        Cliente cliente = (Cliente) usuario;
        for (int i = 0; i < cliente.getAutomoviles().size(); i++) {
            Automovil auto = cliente.getAutomoviles().elementAt(i);
            if (auto.getPlaca().equalsIgnoreCase(placa)) {
                showErrorMessage("Ya existe un automóvil registrado con esa placa");
                return;
            }
        }

        // Copiar imagen a directorio del proyecto
        String rutaImagenFinal = copiarImagenADirectorioProyecto(rutaImagen, placa);
        if (rutaImagenFinal == null) {
            return; // Error al copiar la imagen
        }

        // Crear y agregar el nuevo automóvil
        Automovil nuevoAuto = new Automovil(placa, marca, modelo, anio, color, tipo);
        nuevoAuto.setRutaFoto(rutaImagenFinal);

        cliente.agregarAutomovil(nuevoAuto);

        // También agregarlo al gestor de datos global
        GestorDatos.getInstancia().agregarAutomovil(nuevoAuto);

        // Ordenar automóviles usando ShellSort (como requiere la especificación)
        cliente.ordenarAutomoviles(true); // true para orden ascendente

        // Guardar cambios
        Serializador.guardarDatos();

        // Mostrar mensaje de éxito
        showSuccessMessage("Automóvil registrado exitosamente");

        // Cerrar vista
        cerrarVista();
    }

    private boolean validarCampos() {
        String placa = campoPlaca.getText().trim();
        if (placa.isEmpty()) {
            showErrorMessage("La placa es obligatoria");
            campoPlaca.requestFocus();
            return false;
        }

        // Validar formato de placa (3-7 caracteres alfanuméricos)
        if (!placa.matches("[A-Za-z0-9]{3,7}")) {
            showErrorMessage("La placa debe tener entre 3 y 7 caracteres alfanuméricos");
            campoPlaca.requestFocus();
            return false;
        }

        if (campoMarca.getText().trim().isEmpty()) {
            showErrorMessage("La marca es obligatoria");
            campoMarca.requestFocus();
            return false;
        }

        if (campoModelo.getText().trim().isEmpty()) {
            showErrorMessage("El modelo es obligatorio");
            campoModelo.requestFocus();
            return false;
        }

        if (campoAnio.getText().trim().isEmpty()) {
            showErrorMessage("El año es obligatorio");
            campoAnio.requestFocus();
            return false;
        }

        try {
            int anio = Integer.parseInt(campoAnio.getText().trim());
            if (anio < 1900 || anio > 2024) {
                showErrorMessage("El año debe estar entre 1900 y 2024");
                campoAnio.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("El año debe ser un número válido");
            campoAnio.requestFocus();
            return false;
        }

        if (campoColor.getText().trim().isEmpty()) {
            showErrorMessage("El color es obligatorio");
            campoColor.requestFocus();
            return false;
        }

        if (rutaImagen == null || rutaImagen.isEmpty()) {
            showErrorMessage("Debe seleccionar una imagen JPG para el automóvil");
            return false;
        }

        return true;
    }

    private void cerrarVista() {
        // Cerrar la ventana que contiene este panel
        SwingUtilities.getWindowAncestor(mainPanel).dispose();
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
