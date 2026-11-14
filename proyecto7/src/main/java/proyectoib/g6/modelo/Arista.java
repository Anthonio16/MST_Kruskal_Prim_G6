package proyectoib.g6.modelo;

/*
 * CLASE ARISTA
 *
 * Representa una conexión entre dos vértices
 * de un grafo no dirigido y ponderado.
 *
 * Cada arista contiene:
 * - Vértice de origen.
 * - Vértice de destino.
 * - Costo de la conexión.
 */
public class Arista {

    private int origen;
    private int destino;
    private double costo;

    /*
     * CONSTRUCTOR
     *
     * Recibe los números de los dos vértices
     * y el costo de conectarlos.
     */
    public Arista(int origen, int destino, double costo) {

        this.origen = origen;
        this.destino = destino;
        this.costo = costo;
    }

    /*
     * Devuelve el número del vértice de origen.
     */
    public int getOrigen() {
        return origen;
    }

    /*
     * Devuelve el número del vértice de destino.
     */
    public int getDestino() {
        return destino;
    }

    /*
     * Devuelve el costo de la arista.
     */
    public double getCosto() {
        return costo;
    }

    /*
     * Muestra los datos de la arista.
     *
     * Ejemplo: (0 - 1) Costo: 4.0
     */
    public void mostrarArista() {

        System.out.println(
            "(" + origen + " - " + destino + ")" +
            " Costo: " + costo
        );
    }
}

