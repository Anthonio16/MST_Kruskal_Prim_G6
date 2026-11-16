package proyectoib.g6;

import proyectoib.g6.algoritmos.UnionFind;
import proyectoib.g6.modelo.Grafo;

/*
 * GRAFOS DE PRUEBA
 *
 * Redes reutilizadas por las clases de prueba, para
 * no repetir el mismo código en cada una.
 */
public class GrafosDePrueba {

    /*
     * Cinco edificios conectados siete conexiones.
     * MST: B-C(2), A-D(3), A-B(4), D-E(5) = 14.
     */
    public static Grafo cincoEdificios() {

        Grafo red = new Grafo();
        red.nuevoVertice("A");
        red.nuevoVertice("B");
        red.nuevoVertice("C");
        red.nuevoVertice("D");
        red.nuevoVertice("E");
        red.nuevaArista("A", "B", 4);
        red.nuevaArista("B", "C", 2);
        red.nuevaArista("A", "D", 3);
        red.nuevaArista("D", "E", 5);
        red.nuevaArista("A", "E", 9);
        red.nuevaArista("B", "E", 8);
        red.nuevaArista("C", "E", 7);
        return red;
    }

    /*
     * Los cinco edificios más F conectado solo por D-F (6).
     * MST 20. D-F es un puente.
     */
    public static Grafo conPuente() {

        Grafo red = cincoEdificios();
        red.nuevoVertice("F");
        red.nuevaArista("D", "F", 6);
        return red;
    }

    /*
     * Los cinco edificios y F sin ninguna conexión,
     * igual que edificios.csv con conexiones.csv.
     */
    public static Grafo conEdificioAislado() {

        Grafo red = cincoEdificios();
        red.nuevoVertice("F");
        return red;
    }

    /*
     * Dos grupos sin conexión entre sí
     * A-B (1) y C-D (2).
     */
    public static Grafo dosGrupos() {

        Grafo red = new Grafo();
        red.nuevoVertice("A");
        red.nuevoVertice("B");
        red.nuevoVertice("C");
        red.nuevoVertice("D");
        red.nuevaArista("A", "B", 1);
        red.nuevaArista("C", "D", 2);
        return red;
    }

    /*
     * Bosque de expansión mínima: A-B, B-C, C-D, E-F, F-G
     */
    public static Grafo desconectadoVariado() {

        Grafo red = new Grafo();

        String[] nombres = {"A", "B", "C", "D", "E", "F", "G", "H"};

        for (int i = 0; i < nombres.length; i++) {
            red.nuevoVertice(nombres[i]);
        }

        red.nuevaArista("A", "B", 1);
        red.nuevaArista("B", "C", 2);
        red.nuevaArista("C", "D", 3);
        red.nuevaArista("A", "D", 10);
        red.nuevaArista("E", "F", 4);
        red.nuevaArista("F", "G", 5);
        return red;
    }

    /*
     * Cuatro edificios todas las conexiones con costo 5.
     * Hay muchos MST distintos todos de costo 15.
     */
    public static Grafo costosIguales() {

        Grafo red = new Grafo();
        red.nuevoVertice("A");
        red.nuevoVertice("B");
        red.nuevoVertice("C");
        red.nuevoVertice("D");
        red.nuevaArista("A", "B", 5);
        red.nuevaArista("A", "C", 5);
        red.nuevaArista("A", "D", 5);
        red.nuevaArista("B", "C", 5);
        red.nuevaArista("B", "D", 5);
        red.nuevaArista("C", "D", 5);
        return red;
    }

    /*
     * Red completa de n vértices todas
     * las parejas conectadas con costos todos distintos
     */
    public static Grafo completa(int n) {

        Grafo red = new Grafo();

        for (int i = 0; i < n; i++) {
            red.nuevoVertice("V" + i);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                red.nuevaArista("V" + i, "V" + j, i * n + j + 1);
            }
        }

        return red;
    }

    /*
     * Camino de n vértices con costos
     * 1, 2, 3 etc. Tiene n-1 aristas y todas
     * son puentes.
     */
    public static Grafo camino(int n) {

        Grafo red = new Grafo();

        for (int i = 0; i < n; i++) {
            red.nuevoVertice("V" + i);
        }

        for (int i = 0; i < n - 1; i++) {
            red.nuevaArista("V" + i, "V" + (i + 1), i + 1);
        }

        return red;
    }

    /*
     * Red conexa y poco densa de n vértices con 3n aristas
     */
    public static Grafo dispersa(int n) {

        Grafo red = camino(n);

        long estado = 12345;
        int agregadas = 0;

        while (agregadas < 2 * n) {

            estado = siguiente(estado);
            int a = (int) ((estado >>> 33) % n);

            estado = siguiente(estado);
            int b = (int) ((estado >>> 33) % n);

            estado = siguiente(estado);
            int costo = 1 + (int) ((estado >>> 33) % 100);

            if (a != b) {

                try {
                    red.nuevaArista("V" + a, "V" + b, costo);
                    agregadas++;

                } catch (IllegalArgumentException e) {
                    // Conexión repetida: se intenta otra.
                }
            }
        }

        return red;
    }

    public static Grafo aleatoria(long semilla, int n,
                                  int porcentaje, int costoMax) {

        Grafo red = new Grafo();

        for (int i = 0; i < n; i++) {
            red.nuevoVertice("V" + i);
        }

        long estado = semilla * 7919 + 17;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {

                estado = siguiente(estado);
                int sorteo = (int) ((estado >>> 33) % 100);

                if (sorteo < porcentaje) {

                    estado = siguiente(estado);
                    int costo = 1 + (int) ((estado >>> 33) % costoMax);

                    red.nuevaArista("V" + i, "V" + j, costo);
                }
            }
        }

        return red;
    }

    /*
     * Cuenta los componentes conexos de forma independiente
     * a Prim y a Kruskal para verificar sus resultados
     */
    public static int componentes(Grafo grafo) {

        UnionFind conjuntos = new UnionFind(grafo.getNumVertices());

        int componentes = grafo.getNumVertices();

        for (int i = 0; i < grafo.getNumAristas(); i++) {

            if (conjuntos.union(grafo.getArista(i).getOrigen(),
                                grafo.getArista(i).getDestino())) {
                componentes--;
            }
        }

        return componentes;
    }
    private static long siguiente(long x) {
        return x * 6364136223846793005L + 1442695040888963407L;
    }
}
