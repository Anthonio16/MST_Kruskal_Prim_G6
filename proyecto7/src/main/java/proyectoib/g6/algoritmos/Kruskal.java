package proyectoib.g6.algoritmos;


import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;



/*
 * CLASE KRUSKAL
 *
 * Calcula el arbol de expansion minima
 * de un grafo no dirigido y ponderado.
 *
 * Utiliza:
 * - Arreglos tradicionales.
 * - Merge Sort implementado manualmente.
 * - Union-Find optimizado.
 *
 * No utiliza ArrayList, Collections.sort
 * ni librerias de algoritmos.
 */
public class Kruskal {

    /*
     * METODO PRINCIPAL: EJECUTAR KRUSKAL
     *
     * 1. Obtener las aristas del grafo.
     * 2. Ordenarlas de menor a mayor costo.
     * 3. Crear los conjuntos independientes.
     * 4. Seleccionar aristas sin formar ciclos.
     * 5. Calcular el costo total.
     * 6. Devolver el resultado.
     */
    public ResultadoMST ejecutarKruskal(Grafo grafo) {

        // PASO 1: Validar el grafo.
        if (grafo == null) {
            throw new IllegalArgumentException(
                "El grafo no puede ser nulo"
            );
        }

        int numVertices = grafo.getNumVertices();
        int numAristas = grafo.getNumAristas();

        // No se puede construir una red
        // si no existe ningun edificio.
        if (numVertices == 0) {

            return new ResultadoMST(
                new Arista[0], 0, 0, false
            );
        }

        // PASO 2: Copiar todas las aristas.
        //
        // Se utiliza una copia para no modificar
        // el orden original del grafo.
        Arista[] aristasOrdenadas =
            new Arista[numAristas];

        for (int i = 0; i < numAristas; i++) {
            aristasOrdenadas[i] = grafo.getArista(i);
        }

        // PASO 3: Ordenar las aristas
        // de menor a mayor costo.
        ordenarAristas(aristasOrdenadas);

        // PASO 4: Crear Union-Find.
        //
        // Inicialmente cada vertice pertenece
        // a un conjunto independiente.
        UnionFind conjuntos =
            new UnionFind(numVertices);

        // PASO 5: Preparar el arreglo del MST.
        //
        // Un MST de V vertices conectados
        // tiene exactamente V - 1 aristas.
        Arista[] aristasMST =
            new Arista[numVertices - 1];

        int seleccionadas = 0;
        double costoTotal = 0;

        // PASO 6: Recorrer las aristas ordenadas.
        int i = 0;

        while (i < numAristas &&
               seleccionadas < numVertices - 1) {

            Arista actual = aristasOrdenadas[i];

            int origen = actual.getOrigen();
            int destino = actual.getDestino();

            /*
             * Si union devuelve true:
             * los vertices estaban en conjuntos
             * diferentes y la arista no forma ciclo.
             */
            if (conjuntos.union(origen, destino)) {

                // Agregar la arista al MST.
                aristasMST[seleccionadas] = actual;

                // Acumular su costo.
                costoTotal =
                    costoTotal + actual.getCosto();

                seleccionadas++;
            }

            // Pasar a la siguiente arista.
            i++;
        }

        // PASO 7: Comprobar la conectividad.
        //
        // Si seleccionamos V - 1 aristas,
        // todos los vertices quedaron conectados.
        boolean conexo =
            (seleccionadas == numVertices - 1);

        // PASO 8: Devolver el resultado.
        return new ResultadoMST(
            aristasMST,
            seleccionadas,
            costoTotal,
            conexo
        );
    }

    /*
     * ORDENAR ARISTAS
     *
     * Inicia el algoritmo Merge Sort.
     *
     * Complejidad temporal: O(E log E).
     */
    private void ordenarAristas(Arista[] aristas) {

        if (aristas.length <= 1) {
            return;
        }

        // Arreglo auxiliar para mezclar
        // las partes ordenadas.
        Arista[] auxiliar =
            new Arista[aristas.length];

        mergeSort(
            aristas,
            auxiliar,
            0,
            aristas.length - 1
        );
    }

    /*
     * MERGE SORT
     *
     * Divide el arreglo en dos mitades
     * hasta llegar a segmentos individuales.
     *
     * Despues combina las partes ordenadas.
     */
    private void mergeSort(
            Arista[] aristas,
            Arista[] auxiliar,
            int inicio,
            int fin) {

        // Caso base:
        // Un segmento de un solo elemento
        // ya se encuentra ordenado.
        if (inicio >= fin) {
            return;
        }

        // Calcular el punto medio.
        int medio = inicio + (fin - inicio) / 2;

        // Ordenar la mitad izquierda.
        mergeSort(
            aristas, auxiliar, inicio, medio
        );

        // Ordenar la mitad derecha.
        mergeSort(
            aristas, auxiliar, medio + 1, fin
        );

        // Unir ambas mitades ordenadas.
        mezclar(
            aristas, auxiliar, inicio, medio, fin
        );
    }

    /*
     * MEZCLAR
     *
     * Combina dos segmentos previamente
     * ordenados en un solo segmento.
     *
     * Se comparan los costos de las aristas.
     */
    private void mezclar(
            Arista[] aristas,
            Arista[] auxiliar,
            int inicio,
            int medio,
            int fin) {

        // Posicion inicial de la mitad izquierda.
        int i = inicio;

        // Posicion inicial de la mitad derecha.
        int j = medio + 1;

        // Posicion donde se guardara
        // la siguiente arista ordenada.
        int k = inicio;

        /*
         * Comparar las dos mitades mientras
         * ambas tengan elementos disponibles.
         */
        while (i <= medio && j <= fin) {

            if (aristas[i].getCosto() <=
                aristas[j].getCosto()) {

                auxiliar[k] = aristas[i];
                i++;

            } else {

                auxiliar[k] = aristas[j];
                j++;
            }

            k++;
        }

        // Copiar las aristas restantes
        // de la mitad izquierda.
        while (i <= medio) {

            auxiliar[k] = aristas[i];

            i++;
            k++;
        }

        // Copiar las aristas restantes
        // de la mitad derecha.
        while (j <= fin) {

            auxiliar[k] = aristas[j];

            j++;
            k++;
        }

        // Copiar el segmento ordenado
        // al arreglo original de trabajo.
        for (int p = inicio; p <= fin; p++) {

            aristas[p] = auxiliar[p];
        }
    }
}

