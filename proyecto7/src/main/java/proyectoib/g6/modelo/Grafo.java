package proyectoib.g6.modelo;



/*
 * CLASE GRAFO
 *
 * Representa una red de edificios y conexiones.
 *
 * Tipo de grafo:
 * - No dirigido.
 * - Ponderado.
 *
 * Se utilizan arreglos que crecen manualmente,
 * sin ArrayList ni otras colecciones.
 */
public class Grafo {

    // Arreglo donde se almacenan los edificios.
    private Vertice[] vertices;

    // Arreglo donde se almacenan las conexiones.
    private Arista[] aristas;

    // Cantidad de vértices registrados.
    private int numVertices;

    // Cantidad de aristas registradas.
    private int numAristas;

    /*
     * CONSTRUCTOR
     *
     * Se crea espacio inicial para 5 vértices
     * y 10 aristas.
     *
     * Estos valores representan la capacidad,
     * no el límite máximo del grafo.
     */
    public Grafo() {

        vertices = new Vertice[5];
        aristas = new Arista[10];

        numVertices = 0;
        numAristas = 0;
    }

    /*
     * BUSCAR UN VÉRTICE
     *
     * Recibe el identificador de un edificio.
     *
     * Devuelve su posición si existe.
     * Devuelve -1 si no existe.
     *
     * Complejidad: O(V)
     */
    public int numVertice(String nombre) {

        int posicion = -1;
        int i = 0;

        while (i < numVertices && posicion == -1) {

            if (vertices[i].nomVertice().equals(nombre)) {
                posicion = i;
            }

            i++;
        }

        return posicion;
    }

    /*
     * AMPLIAR EL ARREGLO DE VÉRTICES
     *
     * Cuando el arreglo está lleno:
     * 1. Crear otro con el doble de capacidad.
     * 2. Copiar los vértices existentes.
     * 3. Sustituir el arreglo anterior.
     */
    private void ampliarVertices() {

        Vertice[] nuevo =
            new Vertice[vertices.length * 2];

        for (int i = 0; i < numVertices; i++) {
            nuevo[i] = vertices[i];
        }

        vertices = nuevo;
    }

    /*
     * AMPLIAR EL ARREGLO DE ARISTAS
     *
     * Se aplica el mismo procedimiento
     * de crecimiento manual.
     */
    private void ampliarAristas() {

        Arista[] nuevo =
            new Arista[aristas.length * 2];

        for (int i = 0; i < numAristas; i++) {
            nuevo[i] = aristas[i];
        }

        aristas = nuevo;
    }

    /*
     * AGREGAR UN NUEVO VÉRTICE
     *
     * 1. Comprobar que el nombre sea válido.
     * 2. Verificar que no exista.
     * 3. Ampliar el arreglo si está lleno.
     * 4. Crear y numerar el vértice.
     */

     public void nuevoVertice(String nombre) {
        nuevoVertice(nombre, nombre, 0, 0);
    }
    
    public void nuevoVertice(String nombre, String nombreEdificio, double x, double y) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El nombre del vértice no puede estar vacío"
            );
        }

        if (numVertice(nombre) != -1) {
            throw new IllegalArgumentException(
                "El vértice ya existe: " + nombre
            );
        }

        if (numVertices == vertices.length) {
            ampliarVertices();
        }

        Vertice nuevo = new Vertice(nombre, nombre Edificio, x, y);

        nuevo.asigVert(numVertices);

        vertices[numVertices] = nuevo;

        numVertices++;
    }

    /*
     * AGREGAR UNA NUEVA ARISTA
     *
     * Recibe los nombres de los edificios
     * y el costo de la conexión.
     *
     * Como el grafo es no dirigido,
     * A-B y B-A son la misma conexión.
     */
    public void nuevaArista(
            String origen, String destino, double costo) {

        // Buscar la posición de ambos vértices.
        int va = numVertice(origen);
        int vb = numVertice(destino);

        // Verificar que los dos edificios existan.
        if (va == -1 || vb == -1) {
            throw new IllegalArgumentException(
                "Uno de los vértices no existe"
            );
        }

        // Evitar conexiones de un edificio consigo mismo.
        if (va == vb) {
            throw new IllegalArgumentException(
                "No se permiten lazos"
            );
        }

        // El costo debe ser válido y no negativo.
        if (!Double.isFinite(costo) || costo < 0) {
            throw new IllegalArgumentException(
                "El costo debe ser un número no negativo"
            );
        }

        // Comprobar que la conexión no esté repetida.
        for (int i = 0; i < numAristas; i++) {

            Arista actual = aristas[i];

            boolean mismaDireccion =
                actual.getOrigen() == va &&
                actual.getDestino() == vb;

            boolean direccionInversa =
                actual.getOrigen() == vb &&
                actual.getDestino() == va;

            if (mismaDireccion || direccionInversa) {
                throw new IllegalArgumentException(
                    "La conexión ya existe"
                );
            }
        }

        // Ampliar el arreglo si está lleno.
        if (numAristas == aristas.length) {
            ampliarAristas();
        }

        // Crear y almacenar la nueva conexión.
        aristas[numAristas] =
            new Arista(va, vb, costo);

        numAristas++;
    }

    /*
     * DEVOLVER LA CANTIDAD DE VÉRTICES
     */
    public int getNumVertices() {
        return numVertices;
    }

    /*
     * DEVOLVER LA CANTIDAD DE ARISTAS
     */
    public int getNumAristas() {
        return numAristas;
    }

    /*
     * DEVOLVER UN VÉRTICE POR SU POSICIÓN
     */
    public Vertice getVertice(int posicion) {

        if (posicion < 0 || posicion >= numVertices) {
            throw new IndexOutOfBoundsException(
                "Posición de vértice inválida"
            );
        }

        return vertices[posicion];
    }

    /*
     * DEVOLVER UNA ARISTA POR SU POSICIÓN
     */
    public Arista getArista(int posicion) {

        if (posicion < 0 || posicion >= numAristas) {
            throw new IndexOutOfBoundsException(
                "Posición de arista inválida"
            );
        }

        return aristas[posicion];
    }

    /*
     * MOSTRAR EL GRAFO
     *
     * Imprime los edificios y todas
     * las conexiones registradas.
     */
    public void mostrarGrafo() {

        System.out.println("=== VERTICES ===");

        for (int i = 0; i < numVertices; i++) {

            System.out.println(
                i + " - " + vertices[i].nomVertice()
            );
        }

        System.out.println("\n=== ARISTAS ===");

        for (int i = 0; i < numAristas; i++) {

            Arista a = aristas[i];

            String origen =
                vertices[a.getOrigen()].nomVertice();

            String destino =
                vertices[a.getDestino()].nomVertice();

            System.out.println(
                origen + " - " + destino +
                " | Costo: " + a.getCosto()
            );
        }
    }
}

