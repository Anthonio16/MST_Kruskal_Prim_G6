package proyectoib.g6;

import proyectoib.g6.algoritmos.Kruskal;
import proyectoib.g6.algoritmos.Prim;
import proyectoib.g6.algoritmos.VerificadorMST;
import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;

/*
 * PRUEBA DE PRIM Y VERIFICACIÓN CRUZADA
 *
 * Cada comprobación imprime [OK] o [FALLO].
 */
public class PruebaPrim {

    private static int pruebas = 0;
    private static int fallos = 0;

    public static void main(String[] args) {

        System.out.println("--- Red conexa (5 edificios) ---");
        pruebaRedConexa();

        System.out.println("\n--- Red desconectada (un edificio aislado) ---");
        pruebaEdificioAislado();

        System.out.println("\n--- Red desconectada (dos grupos) ---");
        pruebaDosGrupos();

        System.out.println("\n--- Casos límite ---");
        pruebaCasosLimite();

        System.out.println("\n--- Costos iguales (empates) ---");
        pruebaEmpates();

        System.out.println("\n--- Verificación cruzada en redes densas ---");
        pruebaRedDensa();

        System.out.println("\nPruebas: " + pruebas
            + " | Fallos: " + fallos);
    }

    private static void pruebaRedConexa() {

        Grafo red = redCincoEdificios();

        ResultadoMST prim = new Prim().ejecutarPrim(red);
        ResultadoMST kruskal = new Kruskal().ejecutarKruskal(red);

        verificar("Prim: 4 aristas", prim.getNumAristas() == 4);
        verificar("Prim: costo total 14", prim.getCostoTotal() == 14);
        verificar("Prim: red conexa", prim.esConexo());
        verificar("Prim y Kruskal coinciden",
            VerificadorMST.coinciden(prim, kruskal));
    }

    private static void pruebaEdificioAislado() {

        // Igual que edificios.csv: F no tiene conexiones.
        Grafo red = redCincoEdificios();
        red.nuevoVertice("F");

        ResultadoMST prim = new Prim().ejecutarPrim(red);

        verificar("Prim: NO es conexo (no existe MST completo)",
            !prim.esConexo());
        verificar("Prim: conserva las 4 aristas posibles",
            prim.getNumAristas() == 4);
        verificar("Prim: costo de las aristas 14",
            prim.getCostoTotal() == 14);
        verificar("Prim y Kruskal coinciden",
            VerificadorMST.verificar(red));
    }

    private static void pruebaDosGrupos() {

        Grafo red = new Grafo();
        red.nuevoVertice("A");
        red.nuevoVertice("B");
        red.nuevoVertice("C");
        red.nuevoVertice("D");
        red.nuevaArista("A", "B", 1);
        red.nuevaArista("C", "D", 2);

        ResultadoMST prim = new Prim().ejecutarPrim(red);

        verificar("Prim: NO es conexo", !prim.esConexo());
        verificar("Prim: 2 aristas (una por grupo)",
            prim.getNumAristas() == 2);
        verificar("Prim: costo 3", prim.getCostoTotal() == 3);
        verificar("Prim y Kruskal coinciden",
            VerificadorMST.verificar(red));
    }

    private static void pruebaCasosLimite() {

        Grafo vacio = new Grafo();
        ResultadoMST r0 = new Prim().ejecutarPrim(vacio);

        verificar("Grafo vacío: 0 aristas, no conexo",
            r0.getNumAristas() == 0 && !r0.esConexo());
        verificar("Grafo vacío: Prim y Kruskal coinciden",
            VerificadorMST.verificar(vacio));

        Grafo uno = new Grafo();
        uno.nuevoVertice("A");
        ResultadoMST r1 = new Prim().ejecutarPrim(uno);

        verificar("Un vértice: 0 aristas, conexo",
            r1.getNumAristas() == 0 && r1.esConexo());
        verificar("Un vértice: Prim y Kruskal coinciden",
            VerificadorMST.verificar(uno));

        boolean lanzo = false;
        try {
            new Prim().ejecutarPrim(null);
        } catch (IllegalArgumentException e) {
            lanzo = true;
        }
        verificar("Grafo nulo lanza excepción", lanzo);
    }

    private static void pruebaEmpates() {

        // Cuatro edificios, todas las conexiones con costo 5.
        Grafo red = redCostosIguales();

        ResultadoMST prim = new Prim().ejecutarPrim(red);
        ResultadoMST kruskal = new Kruskal().ejecutarKruskal(red);

        verificar("Prim: costo 15", prim.getCostoTotal() == 15);
        verificar("Kruskal: costo 15", kruskal.getCostoTotal() == 15);
        verificar("Mismo costo total",
            VerificadorMST.coinciden(prim, kruskal));

        System.out.println("  (info) ¿Mismas aristas? "
            + (mismasAristas(prim, kruskal) ? "sí" : "no, y es válido"));
    }

    private static void pruebaRedDensa() {

        Grafo red = redCompleta(8);

        verificar("K8 (28 aristas): Prim y Kruskal coinciden",
            VerificadorMST.verificar(red));

        ResultadoMST prim = new Prim().ejecutarPrim(red);

        verificar("K8: el MST tiene 7 aristas",
            prim.getNumAristas() == 7);
        verificar("K8: conexo", prim.esConexo());
    }

    private static Grafo redCincoEdificios() {

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

    private static Grafo redCostosIguales() {

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

    // Red completa de n vértices con costos todos distintos.
    private static Grafo redCompleta(int n) {

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

    private static boolean mismasAristas(ResultadoMST a,
                                         ResultadoMST b) {

        if (a.getNumAristas() != b.getNumAristas()) {
            return false;
        }

        for (int i = 0; i < a.getNumAristas(); i++) {

            boolean encontrada = false;
            Arista x = a.getArista(i);

            for (int j = 0; j < b.getNumAristas(); j++) {

                Arista y = b.getArista(j);

                boolean directo = x.getOrigen() == y.getOrigen()
                    && x.getDestino() == y.getDestino();
                boolean inverso = x.getOrigen() == y.getDestino()
                    && x.getDestino() == y.getOrigen();

                if (directo || inverso) {
                    encontrada = true;
                }
            }

            if (!encontrada) {
                return false;
            }
        }

        return true;
    }

    private static void verificar(String descripcion,
                                  boolean condicion) {

        pruebas++;

        if (condicion) {
            System.out.println("  [OK]    " + descripcion);
        } else {
            fallos++;
            System.out.println("  [FALLO] " + descripcion);
        }
    }
}
