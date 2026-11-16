package proyectoib.g6;

import proyectoib.g6.algoritmos.Kruskal;
import proyectoib.g6.algoritmos.Prim;
import proyectoib.g6.modelo.Grafo;

/*
 * PRUEBA DE COMPLEJIDAD
 *
 * Mide cuánto tardan Prim y Kruskal al duplicar el tamaño
 * de la red
 * Se toma el mínimo de 5 ejecuciones después de una de
 * calentamiento.
 */
public class PruebaComplejidad {

    private static final int REPETICIONES = 5;

    public static void main(String[] args) {

        System.out.println("Tiempos en milisegundos (mínimo de "
            + REPETICIONES + " ejecuciones)\n");

        System.out.println("RED POCO DENSA (E = 3V aprox.)");
        medirDispersas(new int[] {1000, 2000, 4000, 8000});

        System.out.println("\nRED MUY DENSA (completa)");
        medirCompletas(new int[] {40, 80, 160, 320});
    }

    private static void medirDispersas(int[] tamanos) {

        encabezado();

        double[] anterior = new double[3];

        for (int i = 0; i < tamanos.length; i++) {

            int v = tamanos[i];

            long inicio = System.nanoTime();
            Grafo red = GrafosDePrueba.dispersa(v);
            double construir = (System.nanoTime() - inicio) / 1e6;

            fila(red, construir, anterior, i == 0);
        }
    }

    private static void medirCompletas(int[] tamanos) {

        encabezado();

        double[] anterior = new double[3];

        for (int i = 0; i < tamanos.length; i++) {

            int v = tamanos[i];

            long inicio = System.nanoTime();
            Grafo red = GrafosDePrueba.completa(v);
            double construir = (System.nanoTime() - inicio) / 1e6;

            fila(red, construir, anterior, i == 0);
        }
    }

    private static void fila(Grafo red, double construir,
                             double[] anterior, boolean primera) {

        final Grafo r = red;

        double prim = minimoMs(() -> new Prim().ejecutarPrim(r));
        double kruskal = minimoMs(() -> new Kruskal().ejecutarKruskal(r));

        System.out.printf(
            "%6d %9d | construir %9.1f %s | Prim %8.2f %s | Kruskal %8.2f %s%n",
            red.getNumVertices(), red.getNumAristas(),
            construir, factor(construir, anterior[0], primera),
            prim, factor(prim, anterior[1], primera),
            kruskal, factor(kruskal, anterior[2], primera)
        );

        anterior[0] = construir;
        anterior[1] = prim;
        anterior[2] = kruskal;
    }

    private static void encabezado() {
        System.out.println("     V         E |");
    }

    // Texto "(xN.N)" con cuántas veces creció respecto a la fila anterior.
    private static String factor(double actual, double anterior,
                                 boolean primera) {

        if (primera || anterior <= 0) {
            return "       ";
        }

        return String.format("(x%4.1f)", actual / anterior);
    }

    // Mínimo tiempo en ms de varias ejecuciones.
    private static double minimoMs(Runnable tarea) {

        tarea.run();

        double mejor = Double.MAX_VALUE;

        for (int i = 0; i < REPETICIONES; i++) {

            long inicio = System.nanoTime();
            tarea.run();
            double ms = (System.nanoTime() - inicio) / 1e6;

            if (ms < mejor) {
                mejor = ms;
            }
        }

        return mejor;
    }
}
