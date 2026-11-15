package proyectoib.g6.algoritmos;

import java.util.PriorityQueue;
import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;

/*
 * CLASE PRIM
 *
 * Calcula el árbol de expansión mínima de un
 * grafo no dirigido y ponderado.
 *
 * Complejidad: O(E log E).
 */
public class Prim {

    /*
     * NODO DE LA LISTA DE ADYACENCIA
     */
    private static class Enlace {

        final Arista arista;
        final int vecino;
        final Enlace siguiente;

        Enlace(Arista arista, int vecino, Enlace siguiente) {
            this.arista = arista;
            this.vecino = vecino;
            this.siguiente = siguiente;
        }
    }

    /*
     * ELEMENTO DE LA COLA DE PRIORIDAD
     */
    private static class Candidato {

        final Arista arista;
        final int destino;

        Candidato(Arista arista, int destino) {
            this.arista = arista;
            this.destino = destino;
        }
    }
  
    public ResultadoMST ejecutarPrim(Grafo grafo) {

        // PASO 1: Validar el grafo.
        if (grafo == null) {
            throw new IllegalArgumentException(
                "El grafo no puede ser nulo"
            );
        }

        int numVertices = grafo.getNumVertices();

        // Sin edificios
        if (numVertices == 0) {

            return new ResultadoMST(
                new Arista[0], 0, 0, false
            );
        }

        // PASO 2: Construir las listas de adyacencia.
        Enlace[] adyacencia = construirAdyacencia(grafo);

        // PASO 3: Preparar los datos del algoritmo.
        boolean[] visitado = new boolean[numVertices];

        // Cola ordenada por costo desde el menor.
        PriorityQueue<Candidato> cola = new PriorityQueue<>(
            (a, b) -> Double.compare(
                a.arista.getCosto(), b.arista.getCosto()
            )
        );

        // Un MST de V vértices tiene como máximo V - 1 aristas.
        Arista[] aristasMST = new Arista[numVertices - 1];

        int seleccionadas = 0;
        double costoTotal = 0;

        // PASO 4: Recorrer todos los vértices como posible
        for (int inicio = 0; inicio < numVertices; inicio++) {

            if (visitado[inicio]) {
                continue;
            }

            cola.clear();

            visitado[inicio] = true;
            agregarCandidatos(inicio, adyacencia, visitado, cola);

            // PASO 5: Elegir iterativamente el enlace de
            // menor costo que incorpore un vértice nuevo.
            while (!cola.isEmpty()) {

                Candidato mejor = cola.poll();
                if (visitado[mejor.destino]) {
                    continue;
                }

                visitado[mejor.destino] = true;

                aristasMST[seleccionadas] = mejor.arista;
                costoTotal = costoTotal + mejor.arista.getCosto();
                seleccionadas++;

                agregarCandidatos(
                    mejor.destino, adyacencia, visitado, cola
                );
            }
        }

        // PASO 6: Comprobar la conectividad.
        boolean conexo = (seleccionadas == numVertices - 1);

        return new ResultadoMST(
            aristasMST, seleccionadas, costoTotal, conexo
        );
    }

    /*
     * CONSTRUIR LISTAS DE ADYACENCIA
     */
    private Enlace[] construirAdyacencia(Grafo grafo) {

        Enlace[] adyacencia = new Enlace[grafo.getNumVertices()];

        for (int i = 0; i < grafo.getNumAristas(); i++) {

            Arista a = grafo.getArista(i);

            int origen = a.getOrigen();
            int destino = a.getDestino();

            adyacencia[origen] =
                new Enlace(a, destino, adyacencia[origen]);

            adyacencia[destino] =
                new Enlace(a, origen, adyacencia[destino]);
        }

        return adyacencia;
    }

    /*
     * AGREGAR CANDIDATOS
     *
     * Mete en la cola los enlaces del vértice que
     * llevan a vértices todavía no visitados.
     */
    private void agregarCandidatos(
            int vertice,
            Enlace[] adyacencia,
            boolean[] visitado,
            PriorityQueue<Candidato> cola) {

        Enlace actual = adyacencia[vertice];

        while (actual != null) {

            if (!visitado[actual.vecino]) {
                cola.add(
                    new Candidato(actual.arista, actual.vecino)
                );
            }

            actual = actual.siguiente;
        }
    }
}
