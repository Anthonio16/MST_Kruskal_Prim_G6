package proyectoib.g6;

import proyectoib.g6.algoritmos.Kruskal;
import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;


/*
 * PRUEBA DEL ALGORITMO KRUSKAL
 *
 * Se utiliza la red de cinco edificios
 * definida en nuestros archivos CSV.
 *
 * Resultado esperado:
 * - 5 vertices
 * - 4 aristas seleccionadas
 * - Costo total: 14
 */
public class PruebaKruskal {

    public static void main(String[] args) {

        // PASO 1: Crear el grafo.
        Grafo red = new Grafo();

        // PASO 2: Registrar los edificios.
        red.nuevoVertice("A");
        red.nuevoVertice("B");
        red.nuevoVertice("C");
        red.nuevoVertice("D");
        red.nuevoVertice("E");

        // PASO 3: Agregar las conexiones.
        red.nuevaArista("A", "B", 4);
        red.nuevaArista("B", "C", 2);
        red.nuevaArista("A", "D", 3);
        red.nuevaArista("D", "E", 5);
        red.nuevaArista("A", "E", 9);
        red.nuevaArista("B", "E", 8);
        red.nuevaArista("C", "E", 7);

        // PASO 4: Crear el algoritmo.
        Kruskal algoritmo = new Kruskal();

        // PASO 5: Ejecutar Kruskal.
        ResultadoMST resultado =
            algoritmo.ejecutarKruskal(red);

        // PASO 6: Mostrar las aristas seleccionadas.
        System.out.println("=== RESULTADO KRUSKAL ===");

        for (int i = 0;
             i < resultado.getNumAristas();
             i++) {

            Arista a = resultado.getArista(i);

            String origen = red.getVertice(
                a.getOrigen()
            ).nomVertice();

            String destino = red.getVertice(
                a.getDestino()
            ).nomVertice();

            System.out.println(
                origen + " - " + destino +
                " | Costo: " + a.getCosto()
            );
        }

        // PASO 7: Mostrar los resultados finales.
        System.out.println(
            "\nAristas seleccionadas: " +
            resultado.getNumAristas()
        );

        System.out.println(
            "Costo total: " +
            resultado.getCostoTotal()
        );

        System.out.println(
            "Grafo conectado: " +
            resultado.esConexo()
        );
    }
}

