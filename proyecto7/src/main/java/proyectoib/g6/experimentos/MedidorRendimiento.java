package proyectoib.g6.experimentos;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Random;
import proyectoib.g6.algoritmos.Kruskal;
import proyectoib.g6.algoritmos.Prim;
import proyectoib.g6.algoritmos.VerificadorMST;
import proyectoib.g6.datos.LectorCSV;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;

/*
 * Compara el tiempo de ejecucion y el consumo de memoria
 * entre los algoritmos Kruskal y Prim usando archivos CSV
 * y redes generadas
 */
public class MedidorRendimiento {

    // Cantidad de repeticiones para calcular el tiempo promedio
    private static final int repeticiones = 10;

    // Cantidad de ejecuciones retenidas para medir el aumento de memoria
    private static final int copias = 30;

    // Generador con semilla fija para crear siempre los mismos grafos
    private static final Random aleatorio = new Random(42);

    // Consultar el uso de memoria heap
    private static final MemoryMXBean memoria =
            ManagementFactory.getMemoryMXBean();

    // Devuelve los bytes ocupados actualmente en la memoria 
    private static long memoriaUsada() {
        return memoria.getHeapMemoryUsage().getUsed();
    }

    // Ejecuta las pruebas con los archivos CSV y con redes diferentes
    public static void main(String[] args) {

        int[] tamanos = {10, 50, 100, 200, 300};
        Kruskal kruskal = new Kruskal();
        Prim prim = new Prim();

        // Calentamiento inicial 
        Grafo calentamiento = generarGrafoDisperso(20);
        for (int i = 0; i < 5; i++) {
            kruskal.ejecutarKruskal(calentamiento);
            prim.ejecutarPrim(calentamiento);
        }

        // Prueba con los archivos CSV 
        System.out.println("Archivo CSV");
        try {
            Grafo grafoCSV = LectorCSV.cargarGrafo( "datos/edificios.csv", "datos/conexiones.csv" );
            imprimirTablaComparativa(grafoCSV, kruskal, prim);
        } catch (Exception e) {
            System.out.println("No se pudo cargar los archivos CSV: " + e.getMessage());
        }

        // Pruebas con redes poco densas
        System.out.println("\nRedes densas - Dispersas");
        for (int i = 0; i < tamanos.length; i++) {
            Grafo grafoDisperso = generarGrafoDisperso(tamanos[i]);
            imprimirTablaComparativa(grafoDisperso, kruskal, prim);
        }

        // Pruebas con redes muy densas
        System.out.println("\nRedes my densas");
        for (int i = 0; i < tamanos.length; i++) {
            Grafo grafoDenso = generarGrafoDenso(tamanos[i]);
            imprimirTablaComparativa(grafoDenso, kruskal, prim);
        }
    }

    // Mide tiempo y memoria de Kruskal y Prim sobre un grafo e imprime la tabla
    private static void imprimirTablaComparativa(Grafo grafo, Kruskal kruskal, Prim prim) {

        // Calcular tiempos promedio en milisegundos
        double tiempoKruskalMs = medirTiempoKruskal(grafo, kruskal) / 1_000_000.0;
        double tiempoPrimMs = medirTiempoPrim(grafo, prim) / 1_000_000.0;

        // Calcular consumo de memoria promedio en kilobytes
        double memKruskalKB = medirMemoriaKruskal(grafo, kruskal) / 1024.0;
        double memPrimKB = medirMemoriaPrim(grafo, prim) / 1024.0;

        // Verificar que ambos arboles coincidan en costo
        ResultadoMST resK = kruskal.ejecutarKruskal(grafo);
        ResultadoMST resP = prim.ejecutarPrim(grafo);

        if (!VerificadorMST.coinciden(resK, resP)) {
            System.out.println("Advertencia: diferencia en el costo o conectividad del arbol.");
        }

        System.out.println("\nNodos: " + grafo.getNumVertices() + " | Aristas: " + grafo.getNumAristas() 
        + " | Costo MST: " + String.format("%.2f", resK.getCostoTotal()));
        System.out.println("=============================================");
        System.out.printf("%-14s | %-12s | %-12s%n", "Algoritmo", "Tiempo (ms)", "Memoria (KB)");
        System.out.println("---------------|--------------|--------------");
        System.out.printf("%-14s | %12.3f | %12.2f%n", "Kruskal", tiempoKruskalMs, memKruskalKB);
        System.out.printf("%-14s | %12.3f | %12.2f%n", "Prim", tiempoPrimMs, memPrimKB);
        System.out.println("=============================================");
    }

    // Ejecuta Kruskal y devuelve el tiempo promedio
    public static long medirTiempoKruskal(Grafo grafo, Kruskal kruskal) {

        long totalNs = 0;

        for (int i = 0; i < repeticiones; i++) {
            long inicio = System.nanoTime();
            kruskal.ejecutarKruskal(grafo);
            long fin = System.nanoTime();
            totalNs = totalNs + (fin - inicio);
        }

        return totalNs / repeticiones;
    }

    // Ejecuta Prim y devuelve el tiempo
    public static long medirTiempoPrim(Grafo grafo, Prim prim) {

        long totalNs = 0;

        for (int i = 0; i < repeticiones; i++) {
            long inicio = System.nanoTime();
            prim.ejecutarPrim(grafo);
            long fin = System.nanoTime();
            totalNs = totalNs + (fin - inicio);
        }

        return totalNs / repeticiones;
    }

    // Limpia memoria , ejecuta Kruskal y devuelve la memoria consumida
    public static long medirMemoriaKruskal(Grafo grafo, Kruskal kruskal) {

        System.gc();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
        }

        long antes = memoriaUsada();

        ResultadoMST[] resultados = new ResultadoMST[copias];
        for (int i = 0; i < copias; i++) {
            resultados[i] = kruskal.ejecutarKruskal(grafo);
        }

        long despues = memoriaUsada();

        double sumaControl = 0;
        for (int i = 0; i < copias; i++) {
            sumaControl = sumaControl + resultados[i].getCostoTotal();
        }
        if (sumaControl == Double.NEGATIVE_INFINITY) {
            System.out.println("control");
        }

        long diferencia = Math.max(0, despues - antes);
        return diferencia / copias;
    }

    // Limpia memoria, ejecuta Prim y devuelve la memoria consumida
    public static long medirMemoriaPrim(Grafo grafo, Prim prim) {

        System.gc();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
        }

        long antes = memoriaUsada();

        ResultadoMST[] resultados = new ResultadoMST[copias];
        for (int i = 0; i < copias; i++) {
            resultados[i] = prim.ejecutarPrim(grafo);
        }

        long despues = memoriaUsada();

        double sumaControl = 0;
        for (int i = 0; i < copias; i++) {
            sumaControl = sumaControl + resultados[i].getCostoTotal();
        }
        if (sumaControl == Double.NEGATIVE_INFINITY) {
            System.out.println("control");
        }

        long diferencia = Math.max(0, despues - antes);
        return diferencia / copias;
    }

    // Crea un grafo conexo con pocas conexiones
    public static Grafo generarGrafoDisperso(int numNodos) {

        Grafo grafo = new Grafo();
        boolean[][] conectada = new boolean[numNodos][numNodos];

        // Crear los vertices del grafo
        for (int i = 0; i < numNodos; i++) {
            grafo.nuevoVertice("E" + i);
        }

        // Unir cada edificio 
        for (int i = 1; i < numNodos; i++) {
            int vecino = aleatorio.nextInt(i);
            double costo = 1 + aleatorio.nextInt(100);

            grafo.nuevaArista("E" + i, "E" + vecino, costo);
            conectada[i][vecino] = true;
            conectada[vecino][i] = true;
        }

        // Agregar pocas conexiones 
        int extras = numNodos / 2;
        int agregadas = 0;
        int intentos = 0;

        while (agregadas < extras && intentos < numNodos * 5) {
            int a = aleatorio.nextInt(numNodos);
            int b = aleatorio.nextInt(numNodos);

            if (a != b && !conectada[a][b]) {
                double costo = 1 + aleatorio.nextInt(100);
                grafo.nuevaArista("E" + a, "E" + b, costo);
                conectada[a][b] = true;
                conectada[b][a] = true;
                agregadas++;
            }
            intentos++;
        }

        return grafo;
    }

    // Crea un grafo conexo 
    public static Grafo generarGrafoDenso(int numNodos) {

        Grafo grafo = new Grafo();
        boolean[][] conectada = new boolean[numNodos][numNodos];

        // Crear los vertices del grafo
        for (int i = 0; i < numNodos; i++) {
            grafo.nuevoVertice("E" + i);
        }

        // Asegurar conectividad entre todos los edificios
        for (int i = 1; i < numNodos; i++) {
            int vecino = aleatorio.nextInt(i);
            double costo = 1 + aleatorio.nextInt(100);

            grafo.nuevaArista("E" + i, "E" + vecino, costo);
            conectada[i][vecino] = true;
            conectada[vecino][i] = true;
        }

        // Conectar gran parte de los pares
        for (int i = 0; i < numNodos; i++) {
            for (int j = i + 1; j < numNodos; j++) {
                if (!conectada[i][j] && aleatorio.nextInt(100) < 60) {
                    double costo = 1 + aleatorio.nextInt(100);
                    grafo.nuevaArista("E" + i, "E" + j, costo);
                    conectada[i][j] = true;
                    conectada[j][i] = true;
                }
            }
        }

        return grafo;
    }
}
