package com.tallermecanico.views;

import com.tallermecanico.controllers.FacturaController;
import com.tallermecanico.models.Factura;
import com.tallermecanico.utils.GestorBitacora;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Vista para mostrar los detalles de una factura y permitir su pago
 */
public class FacturaView extends JDialog {

    private Factura factura;
    private JLabel lblNumeroFactura;
    private JLabel lblFechaEmision;
    private JLabel lblCliente;
    private JLabel lblAutomovil;
    private JLabel lblServicio;
    private JLabel lblEstado;
    private JTable tablaDetalles;
    private DefaultTableModel modeloTabla;
    private JLabel lblSubtotal;
    private JLabel lblDescuento;
    private JLabel lblTotal;
    private JComboBox<String> comboMetodoPago;
    private JButton btnPagar;
    private JButton btnCerrar;

    /**
     * Constructor
     * 
     * @param parent  Ventana padre
     * @param factura Factura a mostrar
     */
    public FacturaView(JFrame parent, Factura factura) {
        super(parent, "Detalle de Factura", true);
        this.factura = factura;

        inicializarComponentes();
        cargarDatos();
        configurarEventos();

        // Configurar diálogo
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));

        // Panel de cabecera
        JPanel panelCabecera = new JPanel(new GridLayout(6, 2, 10, 5));
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelCabecera.add(new JLabel("Número de Factura:"));
        lblNumeroFactura = new JLabel();
        panelCabecera.add(lblNumeroFactura);

        panelCabecera.add(new JLabel("Fecha de Emisión:"));
        lblFechaEmision = new JLabel();
        panelCabecera.add(lblFechaEmision);

        panelCabecera.add(new JLabel("Cliente:"));
        lblCliente = new JLabel();
        panelCabecera.add(lblCliente);

        panelCabecera.add(new JLabel("Automóvil:"));
        lblAutomovil = new JLabel();
        panelCabecera.add(lblAutomovil);

        panelCabecera.add(new JLabel("Servicio:"));
        lblServicio = new JLabel();
        panelCabecera.add(lblServicio);

        panelCabecera.add(new JLabel("Estado:"));
        lblEstado = new JLabel();
        panelCabecera.add(lblEstado);

        add(panelCabecera, BorderLayout.NORTH);

        // Tabla de detalles
        String[] columnas = { "Código", "Descripción", "Cantidad", "Precio Unitario", "Total" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDetalles = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaDetalles);
        add(scrollTabla, BorderLayout.CENTER);

        // Panel de totales
        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 10, 5));
        panelTotales.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelTotales.add(new JLabel("Subtotal:"));
        lblSubtotal = new JLabel();
        panelTotales.add(lblSubtotal);

        panelTotales.add(new JLabel("Descuento:"));
        lblDescuento = new JLabel();
        panelTotales.add(lblDescuento);

        panelTotales.add(new JLabel("TOTAL A PAGAR:"));
        lblTotal = new JLabel();
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        panelTotales.add(lblTotal);

        // Panel de acciones
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Opciones de pago
        panelAcciones.add(new JLabel("Método de Pago:"));
        comboMetodoPago = new JComboBox<>(new String[] {
                "Efectivo", "Tarjeta de Crédito", "Tarjeta de Débito", "Transferencia"
        });
        panelAcciones.add(comboMetodoPago);

        btnPagar = new JButton("Pagar Factura");
        btnCerrar = new JButton("Cerrar");

        panelAcciones.add(btnPagar);
        panelAcciones.add(btnCerrar);

        // Panel sur combinado (totales + acciones)
        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelTotales, BorderLayout.CENTER);
        panelSur.add(panelAcciones, BorderLayout.SOUTH);

        add(panelSur, BorderLayout.SOUTH);
    }

    /**
     * Carga los datos de la factura en la interfaz
     */
    private void cargarDatos() {
        DecimalFormat df = new DecimalFormat("#,##0.00");

        // Cabecera
        lblNumeroFactura.setText("F-" + factura.getNumero());
        lblFechaEmision.setText(factura.getFechaEmisionFormateada());
        lblCliente.setText(factura.getOrdenTrabajo().getCliente().getNombreCompleto() +
                " (" + factura.getOrdenTrabajo().getCliente().getTipoCliente() + ")");

        lblAutomovil.setText(factura.getOrdenTrabajo().getAutomovil().getPlaca() + " - " +
                factura.getOrdenTrabajo().getAutomovil().getMarca() + " " +
                factura.getOrdenTrabajo().getAutomovil().getModelo());

        lblServicio.setText(factura.getOrdenTrabajo().getServicio().getNombre());

        if (factura.isPagada()) {
            lblEstado.setText("PAGADA - " + factura.getFechaPagoFormateada() +
                    " (" + factura.getMetodoPago() + ")");
            lblEstado.setForeground(new Color(0, 128, 0));
        } else {
            lblEstado.setText("PENDIENTE DE PAGO");
            lblEstado.setForeground(Color.RED);
        }

        // Detalles
        modeloTabla.setRowCount(0);
        for (Factura.DetalleFactura detalle : factura.getDetalles()) {
            modeloTabla.addRow(new Object[] {
                    detalle.getCodigo(),
                    detalle.getDescripcion(),
                    detalle.getCantidad(),
                    "Q " + df.format(detalle.getPrecioUnitario()),
                    "Q " + df.format(detalle.calcularTotal())
            });
        }

        // Totales
        lblSubtotal.setText("Q " + df.format(factura.calcularSubtotal()));
        lblDescuento.setText("Q " + df.format(factura.calcularDescuento()));
        lblTotal.setText("Q " + df.format(factura.calcularTotal()));

        // Estado de los controles
        btnPagar.setEnabled(!factura.isPagada());
        comboMetodoPago.setEnabled(!factura.isPagada());
    }

    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        btnPagar.addActionListener(e -> realizarPago());
        btnCerrar.addActionListener(e -> dispose());
    }

    /**
     * Realiza el pago de la factura
     */
    private void realizarPago() {
        String metodoPago = (String) comboMetodoPago.getSelectedItem();

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Confirmar pago de Q" + String.format("%.2f", factura.calcularTotal()) +
                        " con " + metodoPago + "?",
                "Confirmar Pago",
                JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {
            boolean exito = FacturaController.registrarPago(factura.getNumero(), metodoPago);

            if (exito) {
                JOptionPane.showMessageDialog(this,
                        "Pago realizado correctamente.",
                        "Pago Exitoso",
                        JOptionPane.INFORMATION_MESSAGE);

                // Actualizar la vista
                cargarDatos();

                // Registrar en bitácora
                GestorBitacora.registrarEvento(
                        factura.getOrdenTrabajo().getCliente().getIdentificador(),
                        "Pago Factura",
                        true,
                        "Factura #" + factura.getNumero() + " pagada con " + metodoPago);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al procesar el pago. Intente nuevamente.",
                        "Error de Pago",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Método estático para mostrar y pagar una factura
     * 
     * @param parent      Ventana padre
     * @param numeroOrden Número de la orden de trabajo
     * @return true si se realizó el pago
     */
    public static boolean mostrarYPagarFactura(JFrame parent, int numeroOrden) {
        // Buscar la factura por número de orden
        Factura factura = FacturaController.buscarFacturaPorOrden(numeroOrden);

        if (factura == null) {
            JOptionPane.showMessageDialog(parent,
                    "No se encontró factura para la orden #" + numeroOrden,
                    "Factura no encontrada",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Estado inicial de pago
        boolean estadoInicial = factura.isPagada();

        // Mostrar la vista
        FacturaView vista = new FacturaView(parent, factura);
        vista.setVisible(true);

        // Retornar true si el estado cambió (se realizó el pago)
        return factura.isPagada() && !estadoInicial;
    }
}