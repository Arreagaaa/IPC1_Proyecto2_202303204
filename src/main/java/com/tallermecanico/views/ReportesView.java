package com.tallermecanico.views;

import com.tallermecanico.controllers.ClienteController;
import com.tallermecanico.controllers.RepuestoController;
import com.tallermecanico.controllers.ServicioController;
import com.tallermecanico.controllers.AutomovilController;
import com.tallermecanico.models.Repuesto;
import com.tallermecanico.models.Servicio;
import com.tallermecanico.models.personas.Cliente;
import com.tallermecanico.models.Automovil;
import com.tallermecanico.utils.GestorBitacora;
import com.tallermecanico.utils.GeneradorPDF;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class ReportesView extends BaseView {

    private static final Color COLOR_TEXT_SECONDARY = null;

    public ReportesView() {
        super("Generación de Reportes");
        inicializarComponentes();
    }

    @Override
    protected void inicializarComponentes() {
        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setOpaque(false);

        // Título
        JLabel lblTitulo = crearTitulo("Seleccione un reporte para generar:");
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelPrincipal.add(lblTitulo);

        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 20))); // Espaciador

        // Botones para generar reportes
        panelPrincipal.add(crearBotonConDescripcion("Clientes por Tipo",
                "Ver clientes separados por tipo (Oro y Normal).",
                () -> generarReporteClientesPorTipo()));

        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10))); // Espaciador

        panelPrincipal.add(crearBotonConDescripcion("TOP 10 Repuestos Más Usados",
                "Ver los repuestos más utilizados.",
                () -> generarReporteRepuestosMasUsados()));

        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10))); // Espaciador

        panelPrincipal.add(crearBotonConDescripcion("TOP 10 Repuestos Más Caros",
                "Ver los repuestos más costosos.",
                () -> generarReporteRepuestosMasCaros()));

        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10))); // Espaciador

        panelPrincipal.add(crearBotonConDescripcion("TOP 10 Servicios Más Usados",
                "Ver los servicios más solicitados.",
                () -> generarReporteServiciosMasUsados()));

        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10))); // Espaciador

        panelPrincipal.add(crearBotonConDescripcion("Los 5 Automóviles Más Repetidos",
                "Ver los automóviles más comunes.",
                () -> generarReporteAutomovilesMasRepetidos()));

        // Botón de Volver
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 30))); // Espaciador
        JButton btnVolver = crearBoton("Volver");
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> {
            dispose();
            new AdminView(null).setVisible(true);
        });
        panelPrincipal.add(btnVolver);

        // Agregar panel principal al contenido
        JScrollPane scrollPane = new JScrollPane(panelPrincipal);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel crearBotonConDescripcion(String textoBoton, String descripcion, Runnable accion) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JButton boton = crearBoton(textoBoton);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.addActionListener(e -> accion.run());

        JLabel lblDescripcion = new JLabel(descripcion, JLabel.CENTER);
        lblDescripcion.setFont(FONT_SMALL);
        lblDescripcion.setForeground(COLOR_TEXT_SECONDARY);
        lblDescripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(boton);
        panel.add(lblDescripcion);

        return panel;
    }

    // Implementación de los métodos para generar reportes

    private void generarReporteClientesPorTipo() {
        GestorBitacora.registrarEvento("Sistema", "Generación de Reporte", true,
                "Reporte de clientes por tipo generado");

        // Panel para mostrar el reporte
        JDialog dialog = new JDialog(this, "Reporte de Clientes por Tipo", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Título
        JLabel titulo = crearTitulo("Clientes por Tipo");
        mainPanel.add(titulo, BorderLayout.NORTH);

        // 2. Panel central con tabla y gráfico
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // 2.1 Tabla de clientes
        Vector<Cliente> clientesOro = ClienteController.obtenerClientesPorTipo("oro");
        Vector<Cliente> clientesNormales = ClienteController.obtenerClientesPorTipo("normal");

        String[] columnas = { "Identificador", "Nombre Completo", "Usuario", "Tipo" };
        Object[][] datos = new Object[clientesOro.size() + clientesNormales.size()][4];

        int fila = 0;
        // Agregar clientes oro
        for (Cliente c : clientesOro) {
            datos[fila][0] = c.getIdentificador();
            datos[fila][1] = c.getNombreCompleto();
            datos[fila][2] = c.getNombreUsuario();
            datos[fila][3] = "ORO";
            fila++;
        }

        // Agregar clientes normales
        for (Cliente c : clientesNormales) {
            datos[fila][0] = c.getIdentificador();
            datos[fila][1] = c.getNombreCompleto();
            datos[fila][2] = c.getNombreUsuario();
            datos[fila][3] = "Normal";
            fila++;
        }

        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        centerPanel.add(scrollTabla, BorderLayout.CENTER);

        // 2.2 Gráfico de pastel
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Clientes Oro", clientesOro.size());
        dataset.setValue("Clientes Normales", clientesNormales.size());

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribución de Clientes",
                dataset,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(300, 300));
        centerPanel.add(chartPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGenerarPDF = crearBoton("Generar PDF");
        btnGenerarPDF.addActionListener(e -> {
            GeneradorPDF.generarReporteClientesPorTipo(clientesOro, clientesNormales);
            JOptionPane.showMessageDialog(dialog, "PDF generado correctamente",
                    "Generar PDF", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnCerrar = crearBoton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGenerarPDF);
        buttonPanel.add(btnCerrar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void generarReporteRepuestosMasUsados() {
        GestorBitacora.registrarEvento("Sistema", "Generación de Reporte", true,
                "Reporte de TOP 10 repuestos más usados generado");

        // Obtener datos
        Vector<Repuesto> repuestos = RepuestoController.obtenerRepuestosMasUtilizados(10);

        // Panel para mostrar el reporte
        JDialog dialog = new JDialog(this, "TOP 10 Repuestos Más Usados", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Título
        JLabel titulo = crearTitulo("TOP 10 Repuestos Más Usados");
        mainPanel.add(titulo, BorderLayout.NORTH);

        // 2. Panel central con tabla y gráfico
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // 2.1 Tabla de repuestos
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Existencias", "Precio", "Veces Usado" };
        Object[][] datos = new Object[repuestos.size()][7];

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < repuestos.size(); i++) {
            Repuesto r = repuestos.get(i);
            datos[i][0] = r.getId();
            datos[i][1] = r.getNombre();
            datos[i][2] = r.getMarca();
            datos[i][3] = r.getModelo();
            datos[i][4] = r.getExistencias();
            datos[i][5] = String.format("%.2f", r.getPrecio());
            datos[i][6] = r.getVecesUsado();

            dataset.addValue(r.getVecesUsado(), "Veces Usado", r.getNombre());
        }

        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        centerPanel.add(scrollTabla, BorderLayout.CENTER);

        // 2.2 Gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "TOP 10 Repuestos Más Usados",
                "Repuestos",
                "Veces Usado",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        centerPanel.add(chartPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGenerarPDF = crearBoton("Generar PDF");
        btnGenerarPDF.addActionListener(e -> {
            GeneradorPDF.generarReporteRepuestosMasUsados(repuestos);
            JOptionPane.showMessageDialog(dialog, "PDF generado correctamente",
                    "Generar PDF", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnCerrar = crearBoton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGenerarPDF);
        buttonPanel.add(btnCerrar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void generarReporteRepuestosMasCaros() {
        GestorBitacora.registrarEvento("Sistema", "Generación de Reporte", true,
                "Reporte de TOP 10 repuestos más caros generado");

        // Obtener datos - este método debe ordenar por precio
        Vector<Repuesto> repuestos = RepuestoController.obtenerRepuestosMasCaros(10);

        // Panel para mostrar el reporte
        JDialog dialog = new JDialog(this, "TOP 10 Repuestos Más Caros", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Título
        JLabel titulo = crearTitulo("TOP 10 Repuestos Más Caros");
        mainPanel.add(titulo, BorderLayout.NORTH);

        // 2. Panel central con tabla y gráfico
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // 2.1 Tabla de repuestos
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Existencias", "Precio" };
        Object[][] datos = new Object[repuestos.size()][6];

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < repuestos.size(); i++) {
            Repuesto r = repuestos.get(i);
            datos[i][0] = r.getId();
            datos[i][1] = r.getNombre();
            datos[i][2] = r.getMarca();
            datos[i][3] = r.getModelo();
            datos[i][4] = r.getExistencias();
            datos[i][5] = String.format("%.2f", r.getPrecio());

            dataset.addValue(r.getPrecio(), "Precio", r.getNombre());
        }

        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        centerPanel.add(scrollTabla, BorderLayout.CENTER);

        // 2.2 Gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "TOP 10 Repuestos Más Caros",
                "Repuestos",
                "Precio",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        centerPanel.add(chartPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGenerarPDF = crearBoton("Generar PDF");
        btnGenerarPDF.addActionListener(e -> {
            GeneradorPDF.generarReporteRepuestosMasCaros(repuestos);
            JOptionPane.showMessageDialog(dialog, "PDF generado correctamente",
                    "Generar PDF", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnCerrar = crearBoton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGenerarPDF);
        buttonPanel.add(btnCerrar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void generarReporteServiciosMasUsados() {
        GestorBitacora.registrarEvento("Sistema", "Generación de Reporte", true,
                "Reporte de TOP 10 servicios más usados generado");

        // Obtener datos
        Vector<Servicio> servicios = ServicioController.obtenerServiciosMasUtilizados(10);

        // Panel para mostrar el reporte
        JDialog dialog = new JDialog(this, "TOP 10 Servicios Más Usados", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Título
        JLabel titulo = crearTitulo("TOP 10 Servicios Más Usados");
        mainPanel.add(titulo, BorderLayout.NORTH);

        // 2. Panel central con tabla y gráfico
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // 2.1 Tabla de servicios
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Precio MO", "Precio Total", "Veces Usado" };
        Object[][] datos = new Object[servicios.size()][7];

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < servicios.size(); i++) {
            Servicio s = servicios.get(i);
            datos[i][0] = s.getId();
            datos[i][1] = s.getNombre();
            datos[i][2] = s.getMarca();
            datos[i][3] = s.getModelo();
            datos[i][4] = String.format("%.2f", s.getPrecioManoObra());
            datos[i][5] = String.format("%.2f", s.getPrecioTotal());
            datos[i][6] = s.getVecesUsado();

            dataset.addValue(s.getVecesUsado(), "Veces Usado", s.getNombre());
        }

        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        centerPanel.add(scrollTabla, BorderLayout.CENTER);

        // 2.2 Gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "TOP 10 Servicios Más Usados",
                "Servicios",
                "Veces Usado",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        centerPanel.add(chartPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGenerarPDF = crearBoton("Generar PDF");
        btnGenerarPDF.addActionListener(e -> {
            GeneradorPDF.generarReporteServiciosMasUsados(servicios);
            JOptionPane.showMessageDialog(dialog, "PDF generado correctamente",
                    "Generar PDF", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnCerrar = crearBoton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGenerarPDF);
        buttonPanel.add(btnCerrar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void generarReporteAutomovilesMasRepetidos() {
        GestorBitacora.registrarEvento("Sistema", "Generación de Reporte", true,
                "Reporte de los 5 automóviles más repetidos generado");

        // Obtener datos - necesitamos implementar esto
        Map<String, Integer> conteoModelos = new HashMap<>();
        Map<String, Automovil> ejemplares = new HashMap<>();
        Map<String, Vector<Cliente>> clientesPorModelo = new HashMap<>();

        // Contar ocurrencias de cada modelo de automóvil
        for (Cliente cliente : ClienteController.obtenerTodosLosClientes()) {
            for (Automovil auto : cliente.getAutomoviles()) {
                String clave = auto.getMarca() + " " + auto.getModelo();

                // Contar ocurrencia
                conteoModelos.put(clave, conteoModelos.getOrDefault(clave, 0) + 1);

                // Guardar un ejemplar
                if (!ejemplares.containsKey(clave)) {
                    ejemplares.put(clave, auto);
                }

                // Agregar cliente a la lista
                if (!clientesPorModelo.containsKey(clave)) {
                    clientesPorModelo.put(clave, new Vector<>());
                }
                clientesPorModelo.get(clave).add(cliente);
            }
        }

        // Ordenar y obtener los 5 más comunes
        Vector<Map.Entry<String, Integer>> entradas = new Vector<>(conteoModelos.entrySet());
        entradas.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Tomar solo los 5 primeros
        int cantidad = Math.min(5, entradas.size());
        Vector<Map.Entry<String, Integer>> top5 = new Vector<>();
        for (int i = 0; i < cantidad; i++) {
            top5.add(entradas.get(i));
        }

        // Panel para mostrar el reporte
        JDialog dialog = new JDialog(this, "5 Automóviles Más Repetidos", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Título
        JLabel titulo = crearTitulo("Los 5 Automóviles Más Repetidos");
        mainPanel.add(titulo, BorderLayout.NORTH);

        // 2. Panel central con tabla y gráfico
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // 2.1 Tabla
        String[] columnas = { "Modelo", "Placa (ejemplo)", "Cliente", "Cantidad" };
        Object[][] datos = new Object[top5.size()][4];

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < top5.size(); i++) {
            Map.Entry<String, Integer> entry = top5.get(i);
            String modelo = entry.getKey();
            int cantidad_autos = entry.getValue();
            Automovil ejemplar = ejemplares.get(modelo);
            Cliente primerCliente = clientesPorModelo.get(modelo).get(0);

            datos[i][0] = modelo;
            datos[i][1] = ejemplar.getPlaca();
            datos[i][2] = primerCliente.getNombreCompleto();
            datos[i][3] = cantidad_autos;

            dataset.addValue(cantidad_autos, "Cantidad", modelo);
        }

        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        centerPanel.add(scrollTabla, BorderLayout.CENTER);

        // 2.2 Gráfico comparativo (mayormente los dos más comunes)
        if (top5.size() >= 2) {
            String modelo1 = top5.get(0).getKey();
            String modelo2 = top5.get(1).getKey();

            DefaultCategoryDataset datasetComp = new DefaultCategoryDataset();
            datasetComp.addValue(top5.get(0).getValue(), modelo1, "Más común");
            datasetComp.addValue(top5.get(1).getValue(), modelo2, "Segundo más común");

            JFreeChart chartComp = ChartFactory.createBarChart(
                    "Comparativa de los 2 más comunes",
                    "Modelos",
                    "Cantidad",
                    datasetComp,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);

            ChartPanel chartPanel = new ChartPanel(chartComp);
            chartPanel.setPreferredSize(new Dimension(400, 300));
            centerPanel.add(chartPanel, BorderLayout.SOUTH);
        }

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGenerarPDF = crearBoton("Generar PDF");
        btnGenerarPDF.addActionListener(e -> {
            GeneradorPDF.generarReporteAutomovilesMasRepetidos(top5, ejemplares, clientesPorModelo);
            JOptionPane.showMessageDialog(dialog, "PDF generado correctamente",
                    "Generar PDF", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnCerrar = crearBoton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGenerarPDF);
        buttonPanel.add(btnCerrar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}
