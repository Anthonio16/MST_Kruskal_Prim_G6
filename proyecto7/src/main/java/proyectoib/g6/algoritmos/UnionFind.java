package proyectoib.g6.algoritmos;



/*
 * CLASE UNION FIND
 *
 * Administra conjuntos disjuntos de vertices.
 *
 * Permite:
 * 1. Crear conjuntos independientes.
 * 2. Encontrar el representante de un conjunto.
 * 3. Unir dos conjuntos diferentes.
 * 4. Detectar si dos vertices ya estan conectados.
 *
 * OPTIMIZACIONES:
 * - Compresion de caminos.
 * - Union por rango.
 *
 * No utiliza librerias de colecciones.
 */
public class UnionFind {

    // Almacena el padre de cada vertice.
    private int[] padre;

    // Almacena el rango de cada conjunto.
    private int[] rango;

    /*
     * CONSTRUCTOR
     *
     * Recibe la cantidad de vertices del grafo.
     *
     * Inicialmente cada vertice pertenece
     * a su propio conjunto.
     */
    public UnionFind(int numVertices) {

        if (numVertices < 0) {
            throw new IllegalArgumentException(
                "La cantidad de vertices no puede ser negativa"
            );
        }

        padre = new int[numVertices];
        rango = new int[numVertices];

        // Crear un conjunto independiente
        // para cada vertice.
        for (int i = 0; i < numVertices; i++) {

            padre[i] = i;
            rango[i] = 0;
        }
    }

    /*
     * VALIDAR VERTICE
     *
     * Verifica que el numero recibido
     * corresponda a un vertice existente.
     */
    private void validarVertice(int vertice) {

        if (vertice < 0 || vertice >= padre.length) {
            throw new IllegalArgumentException(
                "El vertice no existe: " + vertice
            );
        }
    }

    /*
     * FIND: BUSCAR REPRESENTANTE
     *
     * Devuelve la raiz del conjunto
     * al que pertenece un vertice.
     *
     * COMPRESION DE CAMINOS:
     * Cuando encuentra la raiz, hace que
     * el vertice apunte directamente a ella.
     *
     * Esto acelera las busquedas futuras.
     */
    public int find(int vertice) {

        validarVertice(vertice);

        // Si el vertice es su propio padre,
        // entonces es el representante.
        if (padre[vertice] == vertice) {
            return vertice;
        }

        // Buscar la raiz y comprimir el camino.
        padre[vertice] = find(padre[vertice]);

        return padre[vertice];
    }

    /*
     * UNION: UNIR DOS CONJUNTOS
     *
     * Devuelve:
     * true  -> si se unieron dos conjuntos.
     * false -> si ya pertenecian al mismo.
     *
     * UNION POR RANGO:
     * El arbol con menor rango se coloca
     * debajo del arbol con mayor rango.
     */
    public boolean union(int verticeA, int verticeB) {

        // Encontrar los representantes.
        int raizA = find(verticeA);
        int raizB = find(verticeB);

        // Si tienen la misma raiz,
        // ya pertenecen al mismo conjunto.
        if (raizA == raizB) {
            return false;
        }

        // Caso 1:
        // El arbol A tiene menor rango.
        if (rango[raizA] < rango[raizB]) {

            padre[raizA] = raizB;

        }
        // Caso 2:
        // El arbol B tiene menor rango.
        else if (rango[raizA] > rango[raizB]) {

            padre[raizB] = raizA;

        }
        // Caso 3:
        // Ambos arboles tienen igual rango.
        else {

            padre[raizB] = raizA;

            // Solo aumenta el rango
            // cuando ambos eran iguales.
            rango[raizA]++;
        }

        return true;
    }

    /*
     * ESTAN CONECTADOS
     *
     * Comprueba si dos vertices pertenecen
     * al mismo conjunto.
     */
    public boolean estanConectados(
            int verticeA, int verticeB) {

        return find(verticeA) == find(verticeB);
    }

    /*
     * MOSTRAR CONJUNTOS
     *
     * Muestra para cada vertice:
     * - Su numero.
     * - Su representante actual.
     *
     * Se utiliza para comprobar el algoritmo.
     */
    public void mostrarConjuntos() {

        System.out.println("Vertice | Representante");
        System.out.println("-----------------------");

        for (int i = 0; i < padre.length; i++) {

            System.out.println(
                "   " + i + "    |      " + find(i)
            );
        }
    }
}

