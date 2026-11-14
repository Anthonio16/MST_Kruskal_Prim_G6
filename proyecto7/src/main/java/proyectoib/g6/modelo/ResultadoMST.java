package proyectoib.g6.modelo;



/*
 * CLASE RESULTADO MST
 *
 * Almacena el resultado obtenido al ejecutar
 * Kruskal o Prim sobre un grafo.
 *
 * Guarda las aristas seleccionadas, su cantidad,
 * el costo total y el estado de conectividad.
 */
public class ResultadoMST {

    private Arista[] aristasMST;
    private int numAristas;
    private double costoTotal;
    private boolean conexo;

    /*
     * CONSTRUCTOR
     *
     * Recibe el resultado calculado por
     * uno de los algoritmos MST.
     */
    public ResultadoMST(Arista[] aristas,
                        int cantidad,
                        double costo,
                        boolean conexo) {

        // Copiar las aristas para conservar
        // solamente las seleccionadas.
        aristasMST = new Arista[cantidad];

        for (int i = 0; i < cantidad; i++) {
            aristasMST[i] = aristas[i];
        }

        numAristas = cantidad;
        costoTotal = costo;
        this.conexo = conexo;
    }

    /*
     * Devuelve la cantidad de aristas del resultado.
     */
    public int getNumAristas() {
        return numAristas;
    }

    /*
     * Devuelve una arista seleccionada.
     */
    public Arista getArista(int posicion) {

        if (posicion < 0 || posicion >= numAristas) {
            throw new IndexOutOfBoundsException(
                "Posicion de arista invalida"
            );
        }

        return aristasMST[posicion];
    }

    /*
     * Devuelve el costo total de las conexiones.
     */
    public double getCostoTotal() {
        return costoTotal;
    }

    /*
     * Indica si todos los vertices quedaron
     * conectados mediante el resultado.
     */
    public boolean esConexo() {
        return conexo;
    }
}

