package proyectoib.g6;

import proyectoib.g6.algoritmos.Kruskal;
import proyectoib.g6.datos.CSVInvalidoException;
import proyectoib.g6.datos.CargadorGrafo;
import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;

/*
 * PRUEBA DE LECTURA DE CSV
 *
 * Carga edificios.csv y conexiones.csv,
 * muestra el grafo y ejecuta Kruskal.
 */
public class PruebaCSV {

    public static void main(String[] args) {

        String rutaEdificios = "datos/edificios.csv";
        String rutaConexiones = "datos/conexiones.csv";

        if (args.length == 2) {
            rutaEdificios = args[0];
            rutaConexiones = args[1];
        }

        Grafo red;

        try {
            red = CargadorGrafo.cargar(
                rutaEdificios, rutaConexiones
            );

        } catch (CSVInvalidoException e) {
            System.out.println("ERROR AL CARGAR: " + e.getMessage());
            return;
        }

        // Mostrar los edificios con todos sus datos.
        System.out.println("=== EDIFICIOS CARGADOS ===");

        for (int i = 0; i < red.getNumVertices(); i++) {

            System.out.println(
                red.getVertice(i).nomVertice() + " - " +
                red.getVertice(i).getNombreEdificio() +
                " (" + red.getVertice(i).getX() + ", " +
                red.getVertice(i).getY() + ")"
            );
        }

        System.out.println();
        red.mostrarGrafo();

        // Ejecutar Kruskal con los datos cargados.
        Kruskal algoritmo = new Kruskal();
        ResultadoMST resultado = algoritmo.ejecutarKruskal(red);

        System.out.println("\n=== RESULTADO KRUSKAL ===");

        for (int i = 0; i < resultado.getNumAristas(); i++) {

            Arista a = resultado.getArista(i);

            System.out.println(
                red.getVertice(a.getOrigen()).nomVertice() +
                " - " +
                red.getVertice(a.getDestino()).nomVertice() +
                " | Costo: " + a.getCosto()
            );
        }

        System.out.println(
            "\nAristas seleccionadas: " +
            resultado.getNumAristas()
        );
        System.out.println(
            "Costo total: " + resultado.getCostoTotal()
        );
        System.out.println(
            "Grafo conectado: " + resultado.esConexo()
        );
    }
}
