package com.example.models.personas;

import com.example.models.Automovil;

import java.io.Serializable;
import java.util.Vector;

/**
 * Modelo que representa un cliente del taller
 */
public class Cliente extends Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tipoCliente; // "normal" o "oro"
    private Vector<Automovil> automoviles;
    private int serviciosRealizados;

    /**
     * Constructor para crear un nuevo cliente
     */
    public Cliente(String identificador, String nombreCompleto, String usuario, String password) {
        super(identificador, nombreCompleto, usuario, password);
        this.tipoCliente = "normal";
        this.automoviles = new Vector<>();
        this.serviciosRealizados = 0;
    }

    /**
     * Obtiene el tipo de cliente (normal/oro)
     */
    public String getTipoCliente() {
        return tipoCliente;
    }

    /**
     * Establece el tipo de cliente
     */
    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    /**
     * Obtiene los automóviles del cliente
     */
    public Vector<Automovil> getAutomoviles() {
        return automoviles;
    }

    /**
     * Agrega un automóvil al cliente
     */
    public void agregarAutomovil(Automovil automovil) {
        automoviles.add(automovil);
        // Ordenar automóviles por placa
        ordenarAutomoviles();
    }

    /**
     * Elimina un automóvil del cliente
     */
    public void eliminarAutomovil(Automovil automovil) {
        automoviles.remove(automovil);
    }

    /**
     * Obtiene el contador de servicios realizados
     */
    public int getServiciosRealizados() {
        return serviciosRealizados;
    }

    /**
     * Establece el contador de servicios realizados
     */
    public void setServiciosRealizados(int serviciosRealizados) {
        this.serviciosRealizados = serviciosRealizados;
    }

    /**
     * Incrementa el contador de servicios y verifica promoción a oro
     */
    public void incrementarServiciosRealizados() {
        this.serviciosRealizados++;

        // Verificar promoción a cliente oro (5 servicios)
        if (this.serviciosRealizados >= 5 && tipoCliente.equals("normal")) {
            tipoCliente = "oro";
        }
    }

    /**
     * Método ShellSort para ordenar automóviles por placa
     */
    private void ordenarAutomoviles() {
        int n = automoviles.size();

        // Definir gaps
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                Automovil temp = automoviles.get(i);
                int j;

                for (j = i; j >= gap && automoviles.get(j - gap).getPlaca().compareTo(temp.getPlaca()) > 0; j -= gap) {
                    automoviles.set(j, automoviles.get(j - gap));
                }

                automoviles.set(j, temp);
            }
        }
    }

    /**
     * Ordena los automóviles utilizando el algoritmo ShellSort por placa
     * 
     * @param ascendente true para ordenar ascendente, false para descendente
     */
    public void ordenarAutomoviles(boolean ascendente) {
        // Implementación del algoritmo ShellSort
        int n = automoviles.size();

        // Generar la secuencia de intervalos
        for (int gap = n / 2; gap > 0; gap /= 2) {
            // Hacer insertion sort para cada intervalo
            for (int i = gap; i < n; i++) {
                Automovil temp = automoviles.elementAt(i);

                int j;
                // Comparación según orden ascendente o descendente
                if (ascendente) {
                    // Ordenamiento ascendente
                    for (j = i; j >= gap
                            && automoviles.elementAt(j - gap).getPlaca().compareTo(temp.getPlaca()) > 0; j -= gap) {
                        automoviles.setElementAt(automoviles.elementAt(j - gap), j);
                    }
                } else {
                    // Ordenamiento descendente
                    for (j = i; j >= gap
                            && automoviles.elementAt(j - gap).getPlaca().compareTo(temp.getPlaca()) < 0; j -= gap) {
                        automoviles.setElementAt(automoviles.elementAt(j - gap), j);
                    }
                }

                // Colocar el elemento en su posición correcta
                automoviles.setElementAt(temp, j);
            }
        }
    }

    /**
     * Busca un automóvil por placa
     */
    public Automovil buscarAutomovilPorPlaca(String placa) {
        for (Automovil auto : automoviles) {
            if (auto.getPlaca().equalsIgnoreCase(placa)) {
                return auto;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Cliente: " + getNombreCompleto() + " (DPI: " + getIdentificador() +
                ", Tipo: " + tipoCliente + ")";
    }
}