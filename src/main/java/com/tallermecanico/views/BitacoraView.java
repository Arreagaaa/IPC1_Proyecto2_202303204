package com.tallermecanico.views;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class BitacoraView extends JFrame {
    private JTextArea textArea;

    public BitacoraView(Vector<String> bitacora) {
        setTitle("Bitácora del Sistema");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        actualizarBitacora(bitacora);
    }

    public void actualizarBitacora(Vector<String> bitacora) {
        StringBuilder contenido = new StringBuilder();
        for (String entrada : bitacora) {
            contenido.append(entrada).append("\n");
        }
        textArea.setText(contenido.toString());
    }
}
