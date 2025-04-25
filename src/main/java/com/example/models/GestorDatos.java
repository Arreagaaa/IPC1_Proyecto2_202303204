package com.example.models;

import com.example.models.personas.Administrador;
import com.example.models.personas.Cliente;
import com.example.models.personas.Mecanico;
import com.example.models.personas.Persona;

import java.io.Serializable;
import java.util.Vector;

/**
 * Gestor principal de datos del sistema
 * Implementa el patrón Singleton para asegurar una única instancia
 */
public class GestorDatos implements Serializable {

    private static final long serialVersionUID = 1L;

    // Instancia única (patrón Singleton)
    private static GestorDatos instancia;

    // Vectores de datos principales
    private Vector<Persona> personas;
    private Vector<Cliente> clientes;
    private Vector<Mecanico> mecanicos;
    private Vector<Administrador> administradores;
    private Vector<Repuesto> repuestos;
    private Vector<Servicio> servicios;
    private Vector<Automovil> automoviles;

    // Contadores para IDs autoincrementales
    private int contadorRepuestos;
    private int contadorServicios;

    /**
     * Constructor privado (Singleton)
     */
    private GestorDatos() {
        this.personas = new Vector<>();
        this.clientes = new Vector<>();
        this.mecanicos = new Vector<>();
        this.administradores = new Vector<>();
        this.repuestos = new Vector<>();
        this.servicios = new Vector<>();
        this.automoviles = new Vector<>();
        this.contadorRepuestos = 1;
        this.contadorServicios = 1;

        // Crear datos iniciales (admin por defecto)
        inicializarDatosPrueba();
    }

    /**
     * Obtiene la instancia única del gestor
     */
    public static GestorDatos getInstancia() {
        if (instancia == null) {
            instancia = new GestorDatos();
        }
        return instancia;
    }

    /**
     * Establece la instancia del gestor (usado en deserialización)
     */
    public static void setInstancia(GestorDatos nuevaInstancia) {
        instancia = nuevaInstancia;
    }

    /**
     * Crea datos iniciales para pruebas
     */
    private void inicializarDatosPrueba() {
        // Crear administrador por defecto si no hay ninguno
        if (administradores.isEmpty()) {
            Administrador adminDefault = new Administrador("admin123", "Administrador Default", "admin", "admin");
            administradores.add(adminDefault);
            personas.add(adminDefault);
        }

        // Crear mecánico de prueba si no hay ninguno
        if (mecanicos.isEmpty()) {
            Mecanico mecanicoDefault = new Mecanico("mec123", "Mecánico Default", "mecanico", "12345");
            mecanicos.add(mecanicoDefault);
            personas.add(mecanicoDefault);
        }
    }

    /**
     * MÉTODOS PARA GESTIONAR REPUESTOS
     */

    public Vector<Repuesto> getRepuestos() {
        return repuestos;
    }

    public void agregarRepuesto(Repuesto repuesto) {
        // Si no tiene código, asignar uno
        if (repuesto.getCodigo() == null || repuesto.getCodigo().isEmpty()) {
            repuesto.setCodigo("REP" + contadorRepuestos++);
        }
        repuestos.add(repuesto);
    }

    public void eliminarRepuesto(Repuesto repuesto) {
        repuestos.remove(repuesto);
    }

    public Repuesto buscarRepuestoPorCodigo(String codigo) {
        for (Repuesto repuesto : repuestos) {
            if (repuesto.getCodigo().equals(codigo)) {
                return repuesto;
            }
        }
        return null;
    }

    /**
     * MÉTODOS PARA GESTIONAR SERVICIOS
     */

    public Vector<Servicio> getServicios() {
        return servicios;
    }

    public void agregarServicio(Servicio servicio) {
        // Si no tiene código, asignar uno
        if (servicio.getCodigo() == null || servicio.getCodigo().isEmpty()) {
            servicio.setCodigo("SER" + contadorServicios++);
        }
        servicios.add(servicio);
    }

    public void eliminarServicio(Servicio servicio) {
        servicios.remove(servicio);
    }

    public Servicio buscarServicioPorCodigo(String codigo) {
        for (Servicio servicio : servicios) {
            if (servicio.getCodigo().equals(codigo)) {
                return servicio;
            }
        }
        return null;
    }

    /**
     * MÉTODOS PARA GESTIONAR PERSONAS
     */

    public Vector<Persona> getPersonas() {
        return personas;
    }

    public void agregarPersona(Persona persona) {
        personas.add(persona);

        // Agregar a la lista específica según el tipo
        if (persona instanceof Cliente) {
            clientes.add((Cliente) persona);
            // Ordenar clientes por DPI (método burbuja)
            ordenarClientesPorDPI();
        } else if (persona instanceof Mecanico) {
            mecanicos.add((Mecanico) persona);
        } else if (persona instanceof Administrador) {
            administradores.add((Administrador) persona);
        }
    }

    public void eliminarPersona(Persona persona) {
        personas.remove(persona);

        // Eliminar de la lista específica según el tipo
        if (persona instanceof Cliente) {
            clientes.remove(persona);
        } else if (persona instanceof Mecanico) {
            mecanicos.remove(persona);
        } else if (persona instanceof Administrador) {
            administradores.remove(persona);
        }
    }

    public Persona buscarPersonaPorUsuario(String usuario) {
        for (Persona persona : personas) {
            if (persona.getUsuario().equals(usuario)) {
                return persona;
            }
        }
        return null;
    }

    public Persona buscarPersonaPorId(String id) {
        for (Persona persona : personas) {
            if (persona.getIdentificador().equals(id)) {
                return persona;
            }
        }
        return null;
    }

    /**
     * MÉTODOS PARA GESTIONAR CLIENTES
     */

    public Vector<Cliente> getClientes() {
        return clientes;
    }

    public Cliente buscarClientePorDPI(String dpi) {
        for (Cliente cliente : clientes) {
            if (cliente.getIdentificador().equals(dpi)) {
                return cliente;
            }
        }
        return null;
    }

    /**
     * Método burbuja para ordenar clientes por DPI
     */
    private void ordenarClientesPorDPI() {
        int n = clientes.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                String dpi1 = clientes.get(j).getIdentificador();
                String dpi2 = clientes.get(j + 1).getIdentificador();

                if (dpi1.compareTo(dpi2) > 0) {
                    // Intercambiar
                    Cliente temp = clientes.get(j);
                    clientes.set(j, clientes.get(j + 1));
                    clientes.set(j + 1, temp);
                }
            }
        }
    }

    /**
     * MÉTODOS PARA GESTIONAR MECÁNICOS
     */

    public Vector<Mecanico> getMecanicos() {
        return mecanicos;
    }

    public Mecanico buscarMecanicoPorDPI(String dpi) {
        for (Mecanico mecanico : mecanicos) {
            if (mecanico.getIdentificador().equals(dpi)) {
                return mecanico;
            }
        }
        return null;
    }

    /**
     * Encuentra un mecánico disponible
     */
    public Mecanico obtenerMecanicoDisponible() {
        for (Mecanico mecanico : mecanicos) {
            if (mecanico.estaDisponible()) {
                return mecanico;
            }
        }
        return null; // No hay mecánicos disponibles
    }

    /**
     * MÉTODOS PARA GESTIONAR ADMINISTRADORES
     */

    public Vector<Administrador> getAdministradores() {
        return administradores;
    }

    /**
     * MÉTODOS PARA GESTIONAR AUTOMÓVILES
     */

    public Vector<Automovil> getAutomoviles() {
        return automoviles;
    }

    public void agregarAutomovil(Automovil automovil) {
        automoviles.add(automovil);
    }

    public Automovil buscarAutomovilPorPlaca(String placa) {
        for (Automovil automovil : automoviles) {
            if (automovil.getPlaca().equals(placa)) {
                return automovil;
            }
        }
        return null;
    }

    public void eliminarAutomovil(Automovil automovil) {
        if (automovil != null) {
            // Eliminar de la lista global de automóviles
            automoviles.remove(automovil);

            // También eliminar de cualquier cliente que lo tenga
            for (Cliente cliente : getClientes()) {
                cliente.eliminarAutomovil(automovil);
            }
        }
    }

    /**
     * Método para autenticar usuario en el sistema
     */
    public Persona autenticarUsuario(String usuario, String password) {
        for (Persona persona : personas) {
            if (persona.getUsuario().equals(usuario) && persona.verificarPassword(password)) {
                return persona;
            }
        }
        return null; // Autenticación fallida
    }
}
