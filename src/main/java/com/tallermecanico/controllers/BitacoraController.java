package com.tallermecanico.controllers;

import java.util.Vector;

public class BitacoraController {
    private static Vector<String> bitacora = new Vector<>();

    public static void registrarEvento(String evento) {
        bitacora.add(evento);
    }

    public static Vector<String> getBitacora() {
        return bitacora;
    }
}
