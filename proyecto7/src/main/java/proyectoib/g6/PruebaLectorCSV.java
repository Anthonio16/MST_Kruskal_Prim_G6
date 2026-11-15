package proyectoib.g6;

import proyectoib.g6.algoritmos.Kruskal;
import proyectoib.g6.datos.CSVInvalido;
import proyectoib.g6.datos.LectorCSV;
import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;
import proyectoib.g6.modelo.Vertice;

/*
 * CLASE PRUEBALECTORCSV
 *
 * Prueba la lectura de los archivos CSV, ejecuta Kruskal
 * y revisa que se capturen errores si un archivo no existe.
 */
public class PruebaLectorCSV {

    public static void main(String[] args) {

        String rutaEdificios = "datos/edificios.csv";
        String rutaConexiones = "datos/conexiones.csv";
        Grafo grafo = null;

        // 1. Probar carga de archivos CSV correctos
        try {
            grafo = LectorCSV.cargarGrafo(rutaEdificios, rutaConexiones);
        } catch (CSVInvalido e) {
            System.out.printf("Error al cargar CSV: %s%n", e.getMessage());
            return;
        }

        System.out.println("\n=== EDIFICIOS CARGADOS ===");
        for (int i = 0; i < grafo.getNumVertices(); i++) {
            Vertice v = grafo.getVertice(i);
            System.out.printf("%s - %s (%.1f, %.1f)%n", v.nomVertice(), v.getNombreEdificio(), v.getX(), v.getY());
        }

        // 2. Ejecutar Kruskal con el grafo cargado
        Kruskal kruskal = new Kruskal();
        ResultadoMST resultado = kruskal.ejecutarKruskal(grafo);

        System.out.println("\n=== RESULTADO KRUSKAL ===");
        for (int i = 0; i < resultado.getNumAristas(); i++) {
            Arista a = resultado.getArista(i);
            String origen = grafo.getVertice(a.getOrigen()).nomVertice();
            String destino = grafo.getVertice(a.getDestino()).nomVertice();
            System.out.printf("%s - %s | Costo: %.2f%n", origen, destino, a.getCosto());
        }

        System.out.printf("%nCosto total: %.2f%n", resultado.getCostoTotal());

        if (resultado.esConexo()) {
            System.out.println("El grafo esta conectado.");
        } else {
            System.out.println("El grafo NO esta conectado.");
        }

        // 3. Probar error cuando un archivo no existe
        System.out.println("\n=== PRUEBA DE ERROR ===");
        try {
            LectorCSV.cargarGrafo("prueba.csv", rutaConexiones);
        } catch (CSVInvalido e) {
            System.out.printf("Error detectado: %s%n", e.getMessage());
        }
    }
}