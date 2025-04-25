package com.example.models;

import com.example.models.personas.Cliente;
import com.example.models.personas.Mecanico;
import com.example.utils.Serializador;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import javax.swing.Timer;

/**
 * Gestor de órdenes de servicio del sistema
 * Implementa el patrón Singleton
 */
public class GestorOrdenes implements Serializable {

    private static final long serialVersionUID = 1L;

    // Instancia única (patrón Singleton)
    private static GestorOrdenes instancia;

    // Colecciones de datos
    private Vector<OrdenTrabajo> ordenesServicio;
    private Vector<OrdenTrabajo> colaEspera;
    private Vector<OrdenTrabajo> ordenesListas;

    // Contador para generar números de orden
    private int contadorOrdenes;

    /**
     * Constructor privado (Singleton)
     */
    private GestorOrdenes() {
        this.ordenesServicio = new Vector<>();
        this.colaEspera = new Vector<>();
        this.ordenesListas = new Vector<>();
        this.contadorOrdenes = 1;
    }

    /**
     * Obtiene la instancia única del gestor
     */
    public static GestorOrdenes getInstancia() {
        if (instancia == null) {
            instancia = new GestorOrdenes();
        }
        return instancia;
    }

    /**
     * Establece la instancia del gestor (usado en deserialización)
     */
    public static void setInstancia(GestorOrdenes nuevaInstancia) {
        instancia = nuevaInstancia;
    }

    /**
     * Crea una nueva orden de trabajo
     */
    public OrdenTrabajo crearOrden(Cliente cliente, Automovil automovil, Servicio servicio) {
        // Buscar un mecánico disponible
        Mecanico mecanico = GestorDatos.getInstancia().obtenerMecanicoDisponible();

        // Crear la orden con el siguiente número disponible
        OrdenTrabajo nuevaOrden = new OrdenTrabajo(
                contadorOrdenes++,
                cliente,
                mecanico,
                automovil,
                servicio,
                "espera", // Estado inicial
                new Date() // Fecha actual
        );

        ordenesServicio.add(nuevaOrden);

        // Si no hay mecánico disponible, la orden va a la cola de espera
        if (mecanico == null) {
            agregarOrden(nuevaOrden);
            return nuevaOrden;
        }

        // Si hay mecánico disponible, asignarle la orden
        mecanico.asignarOrden(nuevaOrden);

        // Iniciar el servicio del vehículo
        iniciarServicioVehiculo(nuevaOrden);

        return nuevaOrden;
    }

    /**
     * Inicia el servicio de un vehículo
     */
    public void iniciarServicioVehiculo(OrdenTrabajo orden) {
        if (orden.getEstado().equals("espera")) {
            // Cambiar estado de la orden
            orden.setEstado("servicio");

            // Iniciar el servicio del automóvil (simulación con hilos)
            int tiempoEstimado = orden.getServicio().getTiempoEstimado();
            orden.getAutomovil().iniciarServicio(tiempoEstimado);

            // Si es diagnóstico, determinar servicio real aleatoriamente
            if (orden.getServicio().getNombre().equalsIgnoreCase("Diagnóstico")) {
                programarDiagnosticoAleatorio(orden);
            }
        }
    }

    /**
     * Programa un diagnóstico aleatorio para determinar servicio real
     */
    private void programarDiagnosticoAleatorio(final OrdenTrabajo orden) {
        // Simular que el diagnóstico toma tiempo
        Thread diagnosticoThread = new Thread(() -> {
            try {
                // Esperar brevemente para simular diagnóstico
                Thread.sleep(5000);

                // Seleccionar servicio aleatorio que no sea diagnóstico
                Servicio servicioReal = seleccionarServicioAleatorio(orden.getAutomovil());

                // Notificar al cliente y esperar confirmación (simulado aquí)
                boolean clienteAcepta = true; // En un caso real, esto vendría del cliente

                if (clienteAcepta) {
                    // Actualizar la orden con el servicio real
                    orden.setServicio(servicioReal);

                    // Reiniciar el servicio con el nuevo tiempo estimado
                    orden.getAutomovil().detenerServicio();
                    orden.getAutomovil().iniciarServicio(servicioReal.getTiempoEstimado());
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        diagnosticoThread.setDaemon(true);
        diagnosticoThread.start();
    }

    /**
     * Selecciona un servicio aleatorio compatible con el automóvil
     */
    private Servicio seleccionarServicioAleatorio(Automovil automovil) {
        Vector<Servicio> serviciosCompatibles = new Vector<>();

        // Obtener todos los servicios compatibles que no sean diagnóstico
        for (Servicio servicio : GestorDatos.getInstancia().getServicios()) {
            if (!servicio.getNombre().equalsIgnoreCase("Diagnóstico")) {
                // Aquí verificaríamos compatibilidad marca/modelo
                serviciosCompatibles.add(servicio);
            }
        }

        // Si no hay servicios compatibles, devolver un servicio genérico
        if (serviciosCompatibles.isEmpty()) {
            return new Servicio("SER00", "Revisión General", 500, 2, "Revisión general del vehículo");
        }

        // Seleccionar aleatoriamente
        Random random = new Random();
        int indiceAleatorio = random.nextInt(serviciosCompatibles.size());
        return serviciosCompatibles.get(indiceAleatorio);
    }

    /**
     * Genera un servicio recomendado aleatoriamente para una orden de diagnóstico
     * 
     * @param orden Orden de trabajo con servicio de diagnóstico
     * @return true si se encontró un servicio compatible
     */
    public boolean generarDiagnosticoAleatorio(OrdenTrabajo orden) {
        if (!"Diagnóstico".equalsIgnoreCase(orden.getServicio().getNombre())) {
            return false; // No es una orden de diagnóstico
        }

        // Obtener automóvil
        Automovil auto = orden.getAutomovil();

        // Filtrar servicios compatibles (excepto diagnóstico)
        Vector<Servicio> serviciosCompatibles = new Vector<>();
        for (Servicio servicio : GestorDatos.getInstancia().getServicios()) {
            if (!"Diagnóstico".equalsIgnoreCase(servicio.getNombre())) {
                // Verificar compatibilidad marca/modelo
                if (servicio.getMarca().equalsIgnoreCase("cualquiera") ||
                        servicio.getMarca().equalsIgnoreCase(auto.getMarca())) {
                    if (servicio.getModelo().equalsIgnoreCase("cualquiera") ||
                            servicio.getModelo().equalsIgnoreCase(auto.getModelo())) {
                        serviciosCompatibles.add(servicio);
                    }
                }
            }
        }

        if (!serviciosCompatibles.isEmpty()) {
            // Seleccionar servicio aleatorio
            int index = (int) (Math.random() * serviciosCompatibles.size());
            Servicio servicioRecomendado = serviciosCompatibles.elementAt(index);

            orden.setServicioRecomendado(servicioRecomendado);
            orden.setServicioRecomendadoGestionado(false);
        }

        return true;
    }

    /**
     * Agrega una orden a la cola de espera
     */
    public void agregarOrden(OrdenTrabajo orden) {
        // Verificar tipo de cliente para prioridad
        if (orden.getCliente().getTipoCliente().equals("oro")) {
            // Cliente oro va al frente de la cola
            Vector<OrdenTrabajo> nuevaCola = new Vector<>();
            nuevaCola.add(orden);
            nuevaCola.addAll(colaEspera);
            colaEspera = nuevaCola;
        } else {
            // Cliente normal va al final de la cola
            colaEspera.add(orden);
        }

        // Intentar asignar mecánicos disponibles
        asignarMecanicosDisponibles();
    }

    /**
     * Asigna mecánicos disponibles a las órdenes en espera
     */
    public void asignarMecanicosDisponibles() {
        if (colaEspera.isEmpty()) {
            return;
        }

        // Buscar mecánicos disponibles
        Vector<Mecanico> mecanicos = GestorDatos.getInstancia().getMecanicos();
        for (Mecanico mecanico : mecanicos) {
            if (mecanico.estaDisponible() && !colaEspera.isEmpty()) {
                // Asignar orden a mecánico disponible
                OrdenTrabajo orden = colaEspera.remove(0); // Simular poll() con Vector
                orden.setMecanico(mecanico);
                orden.setEstado("servicio");
                mecanico.setDisponible(false);
                mecanico.asignarOrden(orden);
                ordenesServicio.add(orden);

                // Iniciar hilo para procesar la orden
                iniciarProcesoOrden(orden);
            }
        }
    }

    /**
     * Inicia el proceso de una orden
     */
    private void iniciarProcesoOrden(OrdenTrabajo orden) {
        // Código existente

        // Si es diagnóstico, programar la generación de recomendación
        if ("Diagnóstico".equalsIgnoreCase(orden.getServicio().getNombre())) {
            // Usar Timer para simular tiempo de diagnóstico (5 segundos)
            Timer timer = new Timer(5000, e -> {
                generarDiagnosticoAleatorio(orden);
                Serializador.guardarOrdenes();
            });
            timer.setRepeats(false);
            timer.start();
        }

        // Resto del código existente
    }

    /**
     * Finaliza una orden de trabajo
     */
    public void finalizarOrden(OrdenTrabajo orden) {
        if (orden.getEstado().equals("servicio")) {
            orden.setEstado("listo");

            // Liberar al mecánico
            Mecanico mecanico = orden.getMecanico();
            if (mecanico != null) {
                mecanico.liberarOrden(orden);

                // Procesar siguiente orden si hay en cola
                asignarMecanicosDisponibles();
            }

            // Añadir a órdenes listas
            ordenesListas.add(orden);

            // Actualizar contador de servicios del cliente
            actualizarServiciosCliente(orden.getCliente());
        }
    }

    /**
     * Actualiza el contador de servicios del cliente y verifica si debe
     * promocionarse a Oro
     */
    private void actualizarServiciosCliente(Cliente cliente) {
        cliente.incrementarServiciosRealizados();

        // Verificar promoción a cliente oro (5 servicios)
        if (cliente.getServiciosRealizados() >= 5 && !cliente.getTipoCliente().equals("oro")) {
            cliente.setTipoCliente("oro");
        }
    }

    /**
     * Busca una orden por su número
     */
    public OrdenTrabajo buscarOrdenPorNumero(int numeroOrden) {
        for (OrdenTrabajo orden : ordenesServicio) {
            if (orden.getNumeroOrden() == numeroOrden) {
                return orden;
            }
        }
        return null;
    }

    /**
     * Obtiene todas las órdenes de un cliente
     */
    public Vector<OrdenTrabajo> getOrdenesPorCliente(Cliente cliente) {
        Vector<OrdenTrabajo> ordenesCliente = new Vector<>();

        for (OrdenTrabajo orden : ordenesServicio) {
            if (orden.getCliente().getIdentificador().equals(cliente.getIdentificador())) {
                ordenesCliente.add(orden);
            }
        }

        return ordenesCliente;
    }

    /**
     * Obtiene todas las órdenes asignadas a un mecánico
     */
    public Vector<OrdenTrabajo> getOrdenesPorMecanico(Mecanico mecanico) {
        Vector<OrdenTrabajo> ordenesMecanico = new Vector<>();

        for (OrdenTrabajo orden : ordenesServicio) {
            if (orden.getMecanico() != null &&
                    orden.getMecanico().getIdentificador().equals(mecanico.getIdentificador())) {
                ordenesMecanico.add(orden);
            }
        }

        return ordenesMecanico;
    }

    /**
     * Obtiene todas las órdenes de servicio
     */
    public Vector<OrdenTrabajo> getOrdenesServicio() {
        return ordenesServicio;
    }

    /**
     * Obtiene las órdenes en cola de espera
     */
    public Vector<OrdenTrabajo> getColaEspera() {
        return colaEspera;
    }

    /**
     * Obtiene las órdenes finalizadas
     */
    public Vector<OrdenTrabajo> getOrdenesListas() {
        return ordenesListas;
    }

    /**
     * Obtiene el siguiente número de orden disponible
     */
    public int getNextNumeroOrden() {
        return contadorOrdenes;
    }
}
