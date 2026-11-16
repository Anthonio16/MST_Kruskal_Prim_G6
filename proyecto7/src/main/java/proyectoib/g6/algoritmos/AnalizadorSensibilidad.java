package proyectoib.g6.algoritmos;

import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.InformeSensibilidad;
import proyectoib.g6.modelo.InformeSensibilidad.TipoFallo;
import proyectoib.g6.modelo.ResultadoMST;

/*
 * CLASE ANALIZADOR SENSIBILIDAD
 *
 * Simula la falla de un enlace del MST
 * Otro enlace del grafo reconecta
 * las dos partes y el costo sube.
 * El enlace era un puente y no
 * hay alternativa ya no existe MST completo.
 */
public class AnalizadorSensibilidad {

    private final Grafo grafo;
    private final ResultadoMST mstOriginal;

    /*
     * CONSTRUCTOR
     *
     * Recibe el grafo original y calcula su MST.
     *
     * Complejidad: O(V + E log E)
     */
    public AnalizadorSensibilidad(Grafo grafo) {

        if (grafo == null) {
            throw new IllegalArgumentException(
                "El grafo no puede ser nulo"
            );
        }

        this.grafo = grafo;
        this.mstOriginal = new Kruskal().ejecutarKruskal(grafo);
    }

    /*
     * Devuelve el MST original, de donde se elige
     * el enlace que va a fallar.
     *
     * Si el grafo es desconectado es un bosque
     */
    public ResultadoMST getMstOriginal() {
        return mstOriginal;
    }

    /*
     * ANALIZAR POR POSICIÓN
     *
     * Recibe la posición del enlace dentro del MST
     * original.
     *
     * Complejidad: O(V + E log E)
     */
    public InformeSensibilidad analizar(int posicionEnMST) {

        if (posicionEnMST < 0 ||
            posicionEnMST >= mstOriginal.getNumAristas()) {

            throw new IllegalArgumentException(
                "El MST no tiene un enlace en la posición "
                + posicionEnMST
            );
        }

        Arista retirada = mstOriginal.getArista(posicionEnMST);

        // PASO 1: Copia del grafo sin el enlace.
        // El grafo original no se toca.
        Grafo copia = grafo.copiarSinArista(
            retirada.getOrigen(), retirada.getDestino()
        );

        // PASO 2: Recalcular el MST sobre la copia.
        ResultadoMST nuevo = new Kruskal().ejecutarKruskal(copia);

        // PASO 3: Contar los componentes antes y después.
        //
        // Un bosque de expansión mínima tiene
        // (vértices - componentes) aristas.
        int numVertices = grafo.getNumVertices();

        int componentesOriginal =
            numVertices - mstOriginal.getNumAristas();

        int componentesNuevo =
            numVertices - nuevo.getNumAristas();

        // PASO 4: Clasificar el fallo.
        //
        // Se compara la cantidad de componentes para que también funcione cuando
        // la red original ya estaba desconectada.
        TipoFallo tipo = TipoFallo.CON_REEMPLAZO;

        if (componentesNuevo > componentesOriginal) {
            tipo = TipoFallo.DESCONECTA_RED;
        }

        // PASO 5: Identificar el enlace de reemplazo.
        String nombreReemplazo = null;
        double costoReemplazo = 0;

        if (tipo == TipoFallo.CON_REEMPLAZO) {

            Arista reemplazo = buscarReemplazo(posicionEnMST);

            if (reemplazo != null) {
                nombreReemplazo = describir(reemplazo);
                costoReemplazo = reemplazo.getCosto();
            }
        }

        return new InformeSensibilidad(
            describir(retirada),
            retirada.getCosto(),
            tipo,
            componentesOriginal,
            componentesNuevo,
            mstOriginal.getCostoTotal(),
            nuevo.getCostoTotal(),
            nombreReemplazo,
            costoReemplazo,
            nuevo
        );
    }

    /*
     * ANALIZAR POR EDIFICIOS
     *
     * Recibe los ids de los dos edificios del enlace. 
     * El enlace debe pertenecer
     * al MST original y el orden no importa.
     *
     * Complejidad: O(V + E log E)
     */
    public InformeSensibilidad analizar(String origen,
                                        String destino) {

        int a = grafo.numVertice(origen);
        int b = grafo.numVertice(destino);

        if (a == -1 || b == -1) {
            throw new IllegalArgumentException(
                "Uno de los vértices no existe"
            );
        }

        for (int i = 0; i < mstOriginal.getNumAristas(); i++) {

            Arista e = mstOriginal.getArista(i);

            boolean directo =
                e.getOrigen() == a && e.getDestino() == b;

            boolean inverso =
                e.getOrigen() == b && e.getDestino() == a;

            if (directo || inverso) {
                return analizar(i);
            }
        }

        throw new IllegalArgumentException(
            "El enlace " + origen + " - " + destino
            + " no pertenece al MST original"
        );
    }

    /*
     * ANALIZAR TODOS LOS ENLACES
     *
     * Devuelve un informe por cada enlace del MST
     * original en el mismo orden del MST.
     *
     * Complejidad: O(V (V + E log E))
     */
    public InformeSensibilidad[] analizarTodos() {

        InformeSensibilidad[] informes =
            new InformeSensibilidad[mstOriginal.getNumAristas()];

        for (int i = 0; i < informes.length; i++) {
            informes[i] = analizar(i);
        }

        return informes;
    }

    /*
     * BUSCAR ENLACE DE REEMPLAZO
     *
     * Al quitar un enlace del MST, el árbol de su componente
     * se parte en dos zonas. El reemplazo es la arista más
     * barata del grafo que une esas dos zonas.
     *
     * Devuelve null si no existe ninguna.
     *
     * Complejidad: O(V + E α(V))
     */
    private Arista buscarReemplazo(int posicionRetirada) {

        Arista retirada = mstOriginal.getArista(posicionRetirada);

        UnionFind zonas = new UnionFind(grafo.getNumVertices());

        for (int i = 0; i < mstOriginal.getNumAristas(); i++) {

            if (i != posicionRetirada) {

                Arista e = mstOriginal.getArista(i);
                zonas.union(e.getOrigen(), e.getDestino());
            }
        }

        Arista mejor = null;

        for (int i = 0; i < grafo.getNumAristas(); i++) {

            Arista candidata = grafo.getArista(i);

            if (mismoEnlace(candidata, retirada)) {
                continue;
            }

            boolean unePartes =
                !zonas.estanConectados(
                    candidata.getOrigen(), candidata.getDestino()
                );

            if (unePartes &&
                (mejor == null ||
                 candidata.getCosto() < mejor.getCosto())) {

                mejor = candidata;
            }
        }

        return mejor;
    }

    /*
     * Indica si dos aristas unen los mismos vértices,
     * en cualquier dirección.
     *
     * Complejidad: O(1)
     */
    private boolean mismoEnlace(Arista a, Arista b) {

        boolean directo =
            a.getOrigen() == b.getOrigen() &&
            a.getDestino() == b.getDestino();

        boolean inverso =
            a.getOrigen() == b.getDestino() &&
            a.getDestino() == b.getOrigen();

        return directo || inverso;
    }

    /*
     * Complejidad: O(1)
     */
    private String describir(Arista a) {

        return etiqueta(a.getOrigen()) + " - "
             + etiqueta(a.getDestino());
    }

    private String etiqueta(int vertice) {

        String id = grafo.getVertice(vertice).nomVertice();
        String nombre = grafo.getVertice(vertice).getNombreEdificio();

        if (nombre == null || nombre.equals(id)) {
            return id;
        }

        return nombre + " (" + id + ")";
    }
}
