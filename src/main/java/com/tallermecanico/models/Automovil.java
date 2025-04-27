package com.tallermecanico.models;

import com.tallermecanico.models.personas.Cliente;

import java.io.Serializable;

/**
 * Representa un automóvil registrado en el sistema
 */
public class Automovil implements Serializable {

    private static final long serialVersionUID = 1L;

    private String placa;
    private String marca;
    private String modelo;
    private String rutaFoto;
    private Cliente cliente; // Referencia al cliente propietario

    /**
     * Constructor por defecto
     */
    public Automovil() {
    }

    /**
     * Constructor con parámetros
     */
    public Automovil(String placa, String marca, String modelo, String rutaFoto) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.rutaFoto = rutaFoto;
    }

    // Getters y Setters

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = rutaFoto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Verifica si un servicio es compatible con este automóvil
     * 
     * @param servicio El servicio a verificar
     * @return true si la marca y modelo son compatibles
     */
    public boolean esCompatibleConServicio(Servicio servicio) {
        // Si el servicio es para cualquier marca/modelo
        if ("cualquiera".equalsIgnoreCase(servicio.getMarca()) ||
                "cualquiera".equalsIgnoreCase(servicio.getModelo())) {
            return true;
        }

        // Verificar coincidencia de marca y modelo
        return this.marca.equalsIgnoreCase(servicio.getMarca()) &&
                this.modelo.equalsIgnoreCase(servicio.getModelo());
    }

    @Override
    public String toString() {
        return placa + " - " + marca + " " + modelo;
    }

    public Object getDescripcion() {
        return marca + " " + modelo + " - " + placa;
    }
}
