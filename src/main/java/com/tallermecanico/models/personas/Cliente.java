package com.tallermecanico.models.personas;

import com.tallermecanico.models.Automovil;

import java.util.Vector;

/**
 * Representa a un cliente del taller mecánico
 */
public class Cliente extends Persona {

    private static final long serialVersionUID = 1L;

    private String tipoCliente; // "normal" o "oro"
    private Vector<Automovil> automoviles;
    private int serviciosRealizados;
    private String email;
    private String telefono;

    /**
     * Constructor por defecto
     */
    public Cliente() {
        super();
        this.tipoCliente = "normal";
        this.automoviles = new Vector<>();
        this.serviciosRealizados = 0;
        this.email = "";
        this.telefono = "";
    }

    /**
     * Constructor con parámetros
     */
    public Cliente(String identificador, String nombre, String apellido,
            String nombreUsuario, String contrasena) {
        super(identificador, nombre, apellido, nombreUsuario, contrasena);
        this.tipoCliente = "normal";
        this.automoviles = new Vector<>();
        this.serviciosRealizados = 0;
        this.email = "";
        this.telefono = "";
    }

    /**
     * Constructor completo
     */
    public Cliente(String identificador, String nombre, String apellido,
            String nombreUsuario, String contrasena, String tipoCliente) {
        super(identificador, nombre, apellido, nombreUsuario, contrasena);
        this.tipoCliente = tipoCliente;
        this.automoviles = new Vector<>();
        this.serviciosRealizados = 0;
        this.email = "";
        this.telefono = "";
    }

    /**
     * Constructor completo con email y teléfono
     */
    public Cliente(String identificador, String nombre, String apellido,
            String nombreUsuario, String contrasena, String tipoCliente,
            String email, String telefono) {
        super(identificador, nombre, apellido, nombreUsuario, contrasena);
        this.tipoCliente = tipoCliente;
        this.automoviles = new Vector<>();
        this.serviciosRealizados = 0;
        this.email = email;
        this.telefono = telefono;
    }

    // Getters y Setters

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public Vector<Automovil> getAutomoviles() {
        return automoviles;
    }

    public void setAutomoviles(Vector<Automovil> automoviles) {
        this.automoviles = automoviles;
    }

    public int getServiciosRealizados() {
        return serviciosRealizados;
    }

    public void setServiciosRealizados(int serviciosRealizados) {
        this.serviciosRealizados = serviciosRealizados;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Agrega un automóvil a la lista del cliente
     * 
     * @return
     */
    public boolean agregarAutomovil(Automovil automovil) {
        if (automovil != null) {
            // Asignar referencia al cliente
            automovil.setCliente(this);
            automoviles.add(automovil);
        }
        return false;
    }

    /**
     * Elimina un automóvil de la lista del cliente
     */
    public boolean eliminarAutomovil(String placa) {
        for (int i = 0; i < automoviles.size(); i++) {
            if (automoviles.get(i).getPlaca().equals(placa)) {
                automoviles.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Busca un automóvil por su placa
     */
    public Automovil buscarAutomovil(String placa) {
        for (Automovil auto : automoviles) {
            if (auto.getPlaca().equals(placa)) {
                return auto;
            }
        }
        return null;
    }

    /**
     * Incrementa el contador de servicios realizados y verifica si debe
     * convertirse en cliente oro
     */
    public void incrementarServiciosRealizados() {
        serviciosRealizados++;

        // Si alcanza 4 servicios (según requerimiento), se convierte en cliente oro
        if (serviciosRealizados >= 4 && "normal".equals(tipoCliente)) {
            tipoCliente = "oro";
        }
    }

    /**
     * Ordenar automóviles por placa usando Shellsort
     * 
     * @param ascendente true para orden ascendente, false para descendente
     */
    public void ordenarAutomovilesPorPlaca(boolean ascendente) {
        int n = automoviles.size();

        // Algoritmo Shellsort
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                Automovil temp = automoviles.get(i);
                int j;

                if (ascendente) {
                    for (j = i; j >= gap
                            && automoviles.get(j - gap).getPlaca().compareTo(temp.getPlaca()) > 0; j -= gap) {
                        automoviles.set(j, automoviles.get(j - gap));
                    }
                } else {
                    for (j = i; j >= gap
                            && automoviles.get(j - gap).getPlaca().compareTo(temp.getPlaca()) < 0; j -= gap) {
                        automoviles.set(j, automoviles.get(j - gap));
                    }
                }
                automoviles.set(j, temp);
            }
        }
    }

    public Object getPassword() {
        return getContrasena();
    }
}
