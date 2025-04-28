package com.tallermecanico.utils;

import com.tallermecanico.controllers.*;
import com.tallermecanico.models.*;
import com.tallermecanico.models.personas.Cliente;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.*;

/**
 * Clase para generar los reportes del sistema
 */
public class GeneradorReportes {

    /**
     * Aplica estilo consistente a las tablas de reportes
     */
    private static void estilizarTablaReporte(JTable tabla) {
        tabla.setForeground(new Color(40, 40, 40));
        tabla.setBackground(Color.WHITE);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setSelectionBackground(new Color(0, 120, 215));
        tabla.setGridColor(new Color(0, 120, 215));
        tabla.setRowHeight(28);

        JTableHeader header = tabla.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                lbl.setBackground(new Color(0, 120, 215));
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Arial", Font.BOLD, 16));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                return lbl;
            }
        });
    }

    // Reporte de clientes
    public static JPanel generarReporteClientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Obtener datos
        Vector<Cliente> clientes = DataController.getClientes();
        int clientesNormales = 0;
        int clientesOro = 0;

        for (Cliente cliente : clientes) {
            if (cliente.getTipoCliente().equals("oro")) {
                clientesOro++;
            } else {
                clientesNormales++;
            }
        }

        // Crear tabla de clientes
        String[] columnas = { "ID", "Nombre", "Usuario", "Tipo", "Automóviles" };
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Cliente cliente : clientes) {
            modelo.addRow(new Object[] {
                    cliente.getIdentificador(),
                    cliente.getNombreCompleto(),
                    cliente.getNombreUsuario(),
                    cliente.getTipoCliente(),
                    cliente.getAutomoviles().size()
            });
        }

        JTable tabla = new JTable(modelo);
        estilizarTablaReporte(tabla);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Crear gráfico de pastel
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Clientes Normales", clientesNormales);
        dataset.setValue("Clientes Oro", clientesOro);

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribución de Tipos de Cliente",
                dataset,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));

        // Panel para gráfico
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chartPanel, BorderLayout.CENTER);

        // Panel para botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        btnExportarPDF.addActionListener(
                e -> exportarReporteAPDF("ReporteClientes.pdf", "Reporte de Clientes", tabla, chart));
        buttonPanel.add(btnExportarPDF);

        // Añadir componentes al panel principal
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Reporte de top 10 repuestos más usados
    public static JPanel generarReporteRepuestosMasUsados() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Obtener y ordenar repuestos por uso
        Vector<Repuesto> repuestos = new Vector<>(DataController.getRepuestos());
        repuestos.sort((r1, r2) -> Integer.compare(r2.getVecesUsado(), r1.getVecesUsado()));

        // Crear tabla con top 10
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Precio", "Veces Usado" };
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int count = 0;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Repuesto repuesto : repuestos) {
            if (count < 10) {
                modelo.addRow(new Object[] {
                        repuesto.getId(),
                        repuesto.getNombre(),
                        repuesto.getMarca(),
                        repuesto.getModelo(),
                        String.format("Q %.2f", repuesto.getPrecio()),
                        repuesto.getVecesUsado()
                });

                dataset.addValue(repuesto.getVecesUsado(), "Veces Usado", repuesto.getNombre());
                count++;
            } else {
                break;
            }
        }

        JTable tabla = new JTable(modelo);
        estilizarTablaReporte(tabla);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Crear gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 10 Repuestos Más Usados",
                "Repuesto",
                "Veces Usado",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));

        // Panel para gráfico
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chartPanel, BorderLayout.CENTER);

        // Panel para botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        btnExportarPDF.addActionListener(
                e -> exportarReporteAPDF("ReporteRepuestosMasUsados.pdf", "Top 10 Repuestos Más Usados", tabla, chart));
        buttonPanel.add(btnExportarPDF);

        // Añadir componentes al panel principal
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Reporte de top 10 repuestos más caros
    public static JPanel generarReporteRepuestosMasCaros() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Obtener y ordenar repuestos por precio
        Vector<Repuesto> repuestos = new Vector<>(DataController.getRepuestos());
        repuestos.sort((r1, r2) -> Double.compare(r2.getPrecio(), r1.getPrecio()));

        // Crear tabla con top 10
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Existencias", "Precio" };
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int count = 0;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Repuesto repuesto : repuestos) {
            if (count < 10) {
                modelo.addRow(new Object[] {
                        repuesto.getId(),
                        repuesto.getNombre(),
                        repuesto.getMarca(),
                        repuesto.getModelo(),
                        repuesto.getExistencias(),
                        String.format("Q %.2f", repuesto.getPrecio())
                });

                dataset.addValue(repuesto.getPrecio(), "Precio", repuesto.getNombre());
                count++;
            } else {
                break;
            }
        }

        JTable tabla = new JTable(modelo);
        estilizarTablaReporte(tabla);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Crear gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 10 Repuestos Más Caros",
                "Repuesto",
                "Precio (Q)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));

        // Panel para gráfico
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chartPanel, BorderLayout.CENTER);

        // Panel para botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        btnExportarPDF.addActionListener(
                e -> exportarReporteAPDF("ReporteRepuestosMasCaros.pdf", "Top 10 Repuestos Más Caros", tabla, chart));
        buttonPanel.add(btnExportarPDF);

        // Añadir componentes al panel principal
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Reporte de top 10 servicios más usados
    public static JPanel generarReporteServiciosMasUsados() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Obtener y ordenar servicios por uso
        Vector<Servicio> servicios = new Vector<>(DataController.getServicios());
        servicios.sort((s1, s2) -> Integer.compare(s2.getVecesUsado(), s1.getVecesUsado()));

        // Crear tabla con top 10
        String[] columnas = { "ID", "Nombre", "Marca", "Modelo", "Precio Mano Obra", "Precio Total", "Veces Usado" };
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int count = 0;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Servicio servicio : servicios) {
            if (count < 10) {
                modelo.addRow(new Object[] {
                        servicio.getId(),
                        servicio.getNombre(),
                        servicio.getMarca(),
                        servicio.getModelo(),
                        String.format("Q %.2f", servicio.getPrecioManoObra()),
                        String.format("Q %.2f", servicio.getPrecioTotal()),
                        servicio.getVecesUsado()
                });

                dataset.addValue(servicio.getVecesUsado(), "Veces Usado", servicio.getNombre());
                count++;
            } else {
                break;
            }
        }

        JTable tabla = new JTable(modelo);
        estilizarTablaReporte(tabla);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Crear gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 10 Servicios Más Usados",
                "Servicio",
                "Veces Usado",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));

        // Panel para gráfico
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chartPanel, BorderLayout.CENTER);

        // Panel para botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        btnExportarPDF.addActionListener(
                e -> exportarReporteAPDF("ReporteServiciosMasUsados.pdf", "Top 10 Servicios Más Usados", tabla, chart));
        buttonPanel.add(btnExportarPDF);

        // Añadir componentes al panel principal
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Reporte de top 5 automóviles más repetidos
    public static JPanel generarReporteAutomovilesRepetidos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Crear clases para manejar los datos
        class AutoInfo {
            String clave;
            int cantidad;
            Vector<Cliente> clientes = new Vector<>();

            AutoInfo(String clave) {
                this.clave = clave;
                this.cantidad = 0;
            }

            void incrementar() {
                cantidad++;
            }

            void agregarCliente(Cliente cliente) {
                boolean existe = false;
                for (Cliente c : clientes) {
                    if (c.getIdentificador().equals(cliente.getIdentificador())) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    clientes.add(cliente);
                }
            }
        }

        // Usar Vector en vez de HashMap para contar automóviles
        Vector<AutoInfo> autoInfos = new Vector<>();

        // Procesar clientes y sus automóviles
        for (Cliente cliente : DataController.getClientes()) {
            for (Automovil auto : cliente.getAutomoviles()) {
                String clave = auto.getMarca() + " " + auto.getModelo();

                // Buscar o crear AutoInfo
                AutoInfo autoInfo = null;
                for (AutoInfo info : autoInfos) {
                    if (info.clave.equals(clave)) {
                        autoInfo = info;
                        break;
                    }
                }

                if (autoInfo == null) {
                    autoInfo = new AutoInfo(clave);
                    autoInfos.add(autoInfo);
                }

                autoInfo.incrementar();
                autoInfo.agregarCliente(cliente);
            }
        }

        // Ordenar el vector por cantidad (mayor a menor)
        for (int i = 0; i < autoInfos.size() - 1; i++) {
            for (int j = 0; j < autoInfos.size() - i - 1; j++) {
                if (autoInfos.get(j).cantidad < autoInfos.get(j + 1).cantidad) {
                    // Intercambiar elementos
                    AutoInfo temp = autoInfos.get(j);
                    autoInfos.set(j, autoInfos.get(j + 1));
                    autoInfos.set(j + 1, temp);
                }
            }
        }

        // Crear tabla con top 5
        String[] columnas = { "Auto", "Repeticiones", "Clientes" };
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Agregar solo los primeros 5 (o menos si no hay suficientes)
        int limiteAutos = Math.min(5, autoInfos.size());
        for (int i = 0; i < limiteAutos; i++) {
            AutoInfo info = autoInfos.get(i);

            StringBuilder nombresClientes = new StringBuilder();
            for (int j = 0; j < info.clientes.size(); j++) {
                if (j > 0) {
                    nombresClientes.append(", ");
                }
                nombresClientes.append(info.clientes.get(j).getNombreCompleto());
            }

            modelo.addRow(new Object[] {
                    info.clave,
                    info.cantidad,
                    nombresClientes.toString()
            });

            dataset.addValue(info.cantidad, "Cantidad", info.clave);
        }

        // Resto del código igual...
        JTable tabla = new JTable(modelo);
        estilizarTablaReporte(tabla);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Crear gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 5 Automóviles Más Repetidos",
                "Automóvil",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));

        // Panel para gráfico
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chartPanel, BorderLayout.CENTER);

        // Panel para botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        btnExportarPDF.addActionListener(
                e -> exportarReporteAPDF("ReporteAutosRepetidos.pdf", "Top 5 Automóviles Más Repetidos", tabla, chart));
        buttonPanel.add(btnExportarPDF);

        // Añadir componentes al panel principal
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Método para exportar reporte a PDF
    private static void exportarReporteAPDF(String nombreArchivo, String titulo, JTable tabla, JFreeChart chart) {
        try {
            ExportadorPDF.exportarReporte(nombreArchivo, titulo, tabla, chart);
            JOptionPane.showMessageDialog(null,
                    "Reporte exportado correctamente a: " + new File(nombreArchivo).getAbsolutePath(),
                    "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al exportar reporte: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}