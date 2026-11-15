package proyectoib.g6.modelo;

/*
 * CLASE VERTICE
 *
 * Representa cada edificio de la red.
 * Cada vértice tiene un nombre y un número
 * que indica su posición dentro del grafo.
 */
public class Vertice {

    // Nombre o identificador del edificio.
    private String nombre;

    // Posición del vértice dentro del grafo.
    private int numVertice;

    private String nombreEdificio;
    private double x;
    private double y;

    /* Inicializa el vértice con su nombre.
     *
     * Se asigna -1 porque todavía no tiene
     * una posición dentro del grafo.
     */

    public Vertice(String nombre) {
        this(nombre, nombre, 0, 0);
    }

    /*
     * Constructor completo se usa al cargar
     * los edificios desde el archivo CSV.
     */
    public Vertice(String nombre, String nombreEdificio,
                   double x, double y) {
        this.nombre = nombre;
        this.nombreEdificio = nombreEdificio;
        this.x = x;
        this.y = y;
        this.numVertice = -1;
    }

    public String getNombreEdificio() {
        return nombreEdificio;
    }

    /*
     * Devuelve la coordenada X del edificio.
     */
    public double getX() {
        return x;
    }

    /*
     * Devuelve la coordenada Y del edificio.
     */
    public double getY() {
        return y;
    }

    /*
     * Devuelve el nombre del vértice.
     */
    public String nomVertice() {
        return nombre;
    }

    /*
     * Devuelve el número asignado al vértice.
     */
    public int getNumVertice() {
        return numVertice;
    }

    /*
     * Asigna la posición del vértice
     * dentro del grafo.
     */
    public void asigVert(int numero) {
        numVertice = numero;
    }

    /*
     * Compara dos vértices por su nombre.
     *
     * Devuelve true si tienen el mismo nombre
     * y false si sus nombres son diferentes.
     */
    public boolean igualA(Vertice otro) {

        if (otro == null) {
            return false;
        }

        return nombre.equals(otro.nombre);
    }
}
