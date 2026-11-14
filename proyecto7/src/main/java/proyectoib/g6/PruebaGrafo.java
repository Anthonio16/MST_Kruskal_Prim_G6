package proyectoib.g6;

import proyectoib.g6.modelo.Grafo;

/*
 * PRUEBA DEL GRAFO
 *
 * Se crean los cinco edificios y las siete
 * conexiones de nuestros archivos CSV.
 *
 * Por ahora los datos se ingresan manualmente.
 * Después el integrante 3 los cargará desde CSV.
 */
public class PruebaGrafo {

    public static void main(String[] args) {

        // Crear un grafo vacío.
        Grafo red = new Grafo();

        // Agregar los cinco edificios.
        red.nuevoVertice("A");
        red.nuevoVertice("B");
        red.nuevoVertice("C");
        red.nuevoVertice("D");
        red.nuevoVertice("E");
        red.nuevoVertice("F");

        // Registrar las conexiones y sus costos.
        red.nuevaArista("A", "B", 4);
        red.nuevaArista("B", "C", 2);
        red.nuevaArista("A", "D", 3);
        red.nuevaArista("D", "E", 5);
        red.nuevaArista("A", "E", 9);
        red.nuevaArista("B", "E", 8);
        red.nuevaArista("C", "E", 7);
        red.nuevaArista("D", "F", 6);

        // Mostrar la red.
        red.mostrarGrafo();

        System.out.println(
            "\nTotal de edificios: " +
            red.getNumVertices()
        );

        System.out.println(
            "Total de conexiones: " +
            red.getNumAristas()
        );
    }
}
