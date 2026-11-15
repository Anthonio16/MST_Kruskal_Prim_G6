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
 *   las dos partes y el costo sube.
 * El enlace era un puente y no
 *   hay alternativa ya no existe MST completo.
 */
public class AnalizadorSensibilidad {

    private final Grafo grafo;
    private final ResultadoMST mstOriginal;

    /*
     * CONSTRUCTOR
     * Recibe el grafo original y calcula su MST.
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
     */
    public ResultadoMST getMstOriginal() {
        return mstOriginal;
    }

    /*
     * Recibe la posición del enlace dentro del MST
     * original.
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
        Grafo copia = grafo.copiarSinArista(
            retirada.getOrigen(), retirada.getDestino()
        );

        // PASO 2: Recalcular el MST sobre la copia.
        ResultadoMST nuevo = new Kruskal().ejecutarKruskal(copia);

        // PASO 3: Buscar el enlace que podría reemplazarlo.
        Arista reemplazo = buscarReemplazo(posicionEnMST);

        // PASO 4: Clasificar el fallo.
        TipoFallo tipo = TipoFallo.CON_REEMPLAZO;

        if (nuevo.getNumAristas() < mstOriginal.getNumAristas()) {
            tipo = TipoFallo.DESCONECTA_RED;
        }

        String nombreReemplazo = null;
        double costoReemplazo = 0;

        if (tipo == TipoFallo.CON_REEMPLAZO && reemplazo != null) {
            nombreReemplazo = describir(reemplazo);
            costoReemplazo = reemplazo.getCosto();
        }

        return new InformeSensibilidad(
            describir(retirada),
            retirada.getCosto(),
            tipo,
            nuevo.esConexo(),
            mstOriginal.esConexo(),
            mstOriginal.getCostoTotal(),
            nuevo.getCostoTotal(),
            nombreReemplazo,
            costoReemplazo,
            nuevo
        );
    }

    /*
     * Recibe los ids de los dos edificios del enlace el enlace debe pertenecer
     * al MST original y el orden no importa.
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
     * Devuelve un informe por cada enlace del MST
     * original en el mismo orden del MST.
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
     * Al quitar un enlace del MST, el árbol se parte en
     * dos zonas. El reemplazo es la arista más barata del
     * grafo que une esas dos zonas.
     *
     * Devuelve null si no existe ninguna.
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
     * Texto de un enlace con los nombres de sus edificios
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
