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
     * Complejidad: O(V) cada vez que se amplía. Como la
     * capacidad se duplica, el costo amortizado por
     * inserción es O(1).
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
     *
     * Complejidad: O(E) cada vez que se amplía;
     * amortizado O(1) por inserción.
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
     */
    public void nuevoVertice(String nombre) {
        nuevoVertice(nombre, nombre, 0, 0);
    }

    /*
     * AGREGAR UN VÉRTICE CON TODOS SUS DATOS
     *
     * Se usa al cargar el CSV de edificios:
     * id, nombre del edificio y
     * coordenadas para la interfaz.
     *
     * Complejidad: O(V), por la búsqueda de duplicados.
     */
    public void nuevoVertice(String nombre,
                             String nombreEdificio,
                             double x, double y) {

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

        Vertice nuevo =
            new Vertice(nombre, nombreEdificio, x, y);

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
     *
     * Complejidad: O(V + E). Buscar los dos edificios
     * cuesta O(V) y comprobar que la conexión no esté
     * repetida recorre las E aristas. Cargar E conexiones
     * cuesta entonces O(E (V + E)) en total.
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
     *
     * Complejidad: O(1)
     */
    public int getNumVertices() {
        return numVertices;
    }

    /*
     * DEVOLVER LA CANTIDAD DE ARISTAS
     *
     * Complejidad: O(1)
     */
    public int getNumAristas() {
        return numAristas;
    }

    /*
     * DEVOLVER UN VÉRTICE POR SU POSICIÓN
     *
     * Complejidad: O(1)
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
     *
     * Complejidad: O(1)
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
     * COPIAR EL GRAFO SIN UNA ARISTA
     *
     * Crea un grafo nuevo e independiente con los
     * mismos vértices y todas las aristas
     * excepto la que une origen y destino.
     *
     * Complejidad: O(V + E)
     */
    public Grafo copiarSinArista(int origen, int destino) {

        boolean existe = false;

        for (int i = 0; i < numAristas; i++) {

            if (mismoPar(aristas[i], origen, destino)) {
                existe = true;
            }
        }

        if (!existe) {
            throw new IllegalArgumentException(
                "La arista a retirar no existe"
            );
        }

        Grafo copia = new Grafo();

        // Arreglos del mismo tamaño que los originales.
        // Los datos ya fueron validados al crear este grafo,
        // por eso se copian directamente sin volver a
        // comprobar duplicados.
        copia.vertices = new Vertice[vertices.length];
        copia.aristas = new Arista[aristas.length];

        // Se crean objetos nuevos porque
        // Vertice se puede modificar.
        for (int i = 0; i < numVertices; i++) {

            Vertice v = vertices[i];

            Vertice nuevo = new Vertice(
                v.nomVertice(), v.getNombreEdificio(),
                v.getX(), v.getY()
            );

            nuevo.asigVert(i);

            copia.vertices[i] = nuevo;
        }

        copia.numVertices = numVertices;

        // Arista no tiene métodos que la modifiquen,
        // por lo que se pueden compartir sin riesgo.
        int copiadas = 0;

        for (int i = 0; i < numAristas; i++) {

            if (!mismoPar(aristas[i], origen, destino)) {

                copia.aristas[copiadas] = aristas[i];
                copiadas++;
            }
        }

        copia.numAristas = copiadas;

        return copia;
    }

    /*
     * Indica si una arista une los vértices
     * a y b, en cualquier dirección.
     *
     * Complejidad: O(1)
     */
    private boolean mismoPar(Arista arista, int a, int b) {

        boolean directo =
            arista.getOrigen() == a &&
            arista.getDestino() == b;

        boolean inverso =
            arista.getOrigen() == b &&
            arista.getDestino() == a;

        return directo || inverso;
    }

    /*
     * MOSTRAR EL GRAFO
     *
     * Imprime los edificios y todas
     * las conexiones registradas.
     *
     * Complejidad: O(V + E)
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
