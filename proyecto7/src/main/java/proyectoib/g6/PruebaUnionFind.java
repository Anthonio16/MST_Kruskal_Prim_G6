package proyectoib.g6;

import proyectoib.g6.algoritmos.UnionFind;

/*
 * PRUEBA UNION FIND
 *
 * Utiliza cinco vertices:
 *
 * A = 0
 * B = 1
 * C = 2
 * D = 3
 * E = 4
 */
public class PruebaUnionFind {

    public static void main(String[] args) {

        // Crear cinco conjuntos independientes.
        UnionFind conjuntos = new UnionFind(5);

        System.out.println("ESTADO INICIAL:");
        conjuntos.mostrarConjuntos();

        // Conectar los edificios A y B.
        conjuntos.union(0, 1);

        // Conectar los edificios C y D.
        conjuntos.union(2, 3);

        System.out.println("\nDESPUES DE LAS UNIONES:");
        conjuntos.mostrarConjuntos();

        // Comprobar si A y B estan conectados.
        System.out.println(
            "\nA y B conectados: " +
            conjuntos.estanConectados(0, 1)
        );

        // Comprobar si A y C estan conectados.
        System.out.println(
            "A y C conectados: " +
            conjuntos.estanConectados(0, 2)
        );

        // Unir los conjuntos de A y C.
        conjuntos.union(0, 2);

        System.out.println("\nDESPUES DE UNIR A Y C:");
        conjuntos.mostrarConjuntos();

        // Comprobar si B y D quedaron conectados.
        System.out.println(
            "\nB y D conectados: " +
            conjuntos.estanConectados(1, 3)
        );

        // Intentar una union repetida.
        boolean seUnieron = conjuntos.union(1, 3);

        System.out.println(
            "Union repetida realizada: " + seUnieron
        );
    }
}

