package com.tallermecanico.controllers;

import com.tallermecanico.utils.MonitorOrdenesThread;

import java.util.Vector;

public class HiloController {
    private static Vector<Thread> hilos = new Vector<>();

    public static void agregarHilo(Thread hilo) {
        hilos.add(hilo);
        hilo.start();
    }

    public static void detenerTodosLosHilos() {
        for (Thread hilo : hilos) {
            if (hilo instanceof MonitorOrdenesThread) {
                ((MonitorOrdenesThread) hilo).detener();
            }
        }
        hilos.clear();
    }
}
