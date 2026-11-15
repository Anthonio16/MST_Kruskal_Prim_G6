package proyectoib.g6.experimentos;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Random;
import proyectoib.g6.algoritmos.Kruskal;
import proyectoib.g6.algoritmos.Prim;
import proyectoib.g6.algoritmos.VerificadorMST;
import proyectoib.g6.datos.CSVInvalido;
import proyectoib.g6.datos.LectorCSV;
import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;
import proyectoib.g6.modelo.Vertice;

/*
 * Compara el tiempo y la memoria de Kruskal y Prim
 * Guarda las redes  en datos/generados y la tabla en resultados
 */
public class MedidorRendimiento {

    // Ejecuciones sin medir 
    private static final int calentamiento = 10;

    // Ejecuciones medidas 
    private static final int repeticiones = 30;

    // Cantidad de copias retenidas en memoria
    private static final int copias = 30;

    // Carpetas donde se guardan 
    private static final String carpetaRedes = "datos/generados/";
    private static final String carpetaResultados = "resultados/";

    // Uso de memoria heap.
    private static final MemoryMXBean memoria =
            ManagementFactory.getMemoryMXBean();


    private static double controlCosto = 0;

    // Devuelve los bytes ocupados 
    private static long memoriaUsada() {
        return memoria.getHeapMemoryUsage().getUsed();
    }

    // Crea carpetas de salida y mide la red CSV 
    public static void main(String[] args) {

        new File(carpetaRedes).mkdirs();
        new File(carpetaResultados).mkdirs();

        PrintWriter archivo = null;

        try {

            archivo = new PrintWriter(carpetaResultados + "rendimiento.csv");

            archivo.println("tipo,nodos,aristas,densidad_pct,costo_mst," + "kruskal_ms,prim_ms,kruskal_kb,prim_kb,coinciden");

            // Medir la red real de los archivos CSV del proyecto.
            try {

                Grafo grafoCSV = LectorCSV.cargarGrafo( "datos/edificios.csv", "datos/conexiones.csv");

                medirRed("csv_proyecto", grafoCSV, archivo);

            } catch (CSVInvalido e) {

                System.out.println("No se pudo cargar los archivos CSV: " + e.getMessage());
            }

            // Generar, guardar y medir redes pequenas, medianas y grandes.
            int[] tamanos = {10, 50, 100, 200, 500, 1000};

            for (int i = 0; i < tamanos.length; i++) {

                int n = tamanos[i];

                Grafo disperso = generarGrafoDisperso(n);
                guardarCSV(disperso, "disperso_" + n);
                medirRed("disperso", disperso, archivo);

                Grafo denso = generarGrafoDenso(n, porcentajeDenso(n));
                guardarCSV(denso, "denso_" + n);
                medirRed("denso", denso, archivo);
            }

        } catch (IOException e) {

            System.out.println("No se pudo crear el archivo de resultados: " + e.getMessage());

        } finally {

            if (archivo != null) {
                archivo.close();
            }
        }
    }

    // Mide tiempo y memoria de Kruskal y Prim, imprime la tabla
    private static void medirRed(String tipo, Grafo grafo, PrintWriter archivo) {

        // Calentar la maquina virtual
        for (int i = 0; i < calentamiento; i++) {
            new Kruskal().ejecutarKruskal(grafo);
            new Prim().ejecutarPrim(grafo);
        }

        // Calcular tiempos medianos en milisegundos
        double kruskalMs = medirTiempo(grafo, true) / 1000000.0;
        double primMs = medirTiempo(grafo, false) / 1000000.0;

        // Calcular memoria promedio por ejecucion en kilobyte
        double kruskalKB = medirMemoria(grafo, true) / 1024.0;
        double primKB = medirMemoria(grafo, false) / 1024.0;

        // Verificar que ambos algoritmos obtengan el mismo costo y conectividad
        ResultadoMST resultadoK = new Kruskal().ejecutarKruskal(grafo);
        ResultadoMST resultadoP = new Prim().ejecutarPrim(grafo);

        boolean coinciden = VerificadorMST.coinciden(resultadoK, resultadoP);

        int n = grafo.getNumVertices();
        double densidad = 0;

        if (n > 1) {
            densidad = 100.0 * grafo.getNumAristas() / (n * (n - 1) / 2.0);
        }

        String estadoCoincidencia = "NO";
        if (coinciden) {
            estadoCoincidencia = "SI";
        }

        // Mostrar la tabla 
        System.out.printf("%nNodos: %d | Aristas: %d | Costo MST: %.2f%n", n, grafo.getNumAristas(), resultadoK.getCostoTotal());
        System.out.println("=============================================\n");
        System.out.printf("%-14s | %-11s | %-12s |%n%n", "Algoritmo", "Tiempo (ms)", "Memoria (KB)");
        System.out.println("---------------|-------------|--------------|\n");
        System.out.printf("%-14s | %11.3f | %12.2f |%n%n", "Kruskal", kruskalMs, kruskalKB);
        System.out.printf("%-14s | %11.3f | %12.2f |%n%n", "Prim", primMs, primKB);
        System.out.println("=============================================");

        archivo.println(tipo + "," + n + "," + grafo.getNumAristas() + ","
                + redondear(densidad) + "," + redondear(resultadoK.getCostoTotal()) + ","
                + redondear(kruskalMs) + "," + redondear(primMs) + ","
                + redondear(kruskalKB) + "," + redondear(primKB) + ","
                + estadoCoincidencia);
    }

    // Ejecuta los algoritmos
    private static ResultadoMST ejecutar(Grafo grafo, boolean esKruskal) {

        if (esKruskal) {
            return new Kruskal().ejecutarKruskal(grafo);
        }

        return new Prim().ejecutarPrim(grafo);
    }

    // Ejecuta el algoritmo varias veces 
    private static long medirTiempo(Grafo grafo, boolean esKruskal) {

        long[] tiempos = new long[repeticiones];

        for (int i = 0; i < repeticiones; i++) {

            long inicio = System.nanoTime();
            ResultadoMST resultado = ejecutar(grafo, esKruskal);
            long fin = System.nanoTime();

            tiempos[i] = fin - inicio;
            controlCosto = controlCosto + resultado.getCostoTotal();
        }

        return calcularMediana(tiempos);
    }

    // Limpia la memoria, guarda varias ejecuciones y mide los bytes 
    private static long medirMemoria(Grafo grafo, boolean esKruskal) {

        System.gc();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
        }

        long antes = memoriaUsada();

        ResultadoMST[] resultados = new ResultadoMST[copias];
        for (int i = 0; i < copias; i++) {
            resultados[i] = ejecutar(grafo, esKruskal);
        }

        long despues = memoriaUsada();

        // Recorrer los resultados
        double sumaControl = 0;
        for (int i = 0; i < copias; i++) {
            sumaControl = sumaControl + resultados[i].getCostoTotal();
        }
        if (sumaControl == -1) {
            System.out.println("dummy");
        }

        long diferencia = Math.max(0, despues - antes);
        return diferencia / copias;
    }

    // Ordena los tiempos de menor a mayor
    private static long calcularMediana(long[] valores) {

        for (int i = 0; i < valores.length - 1; i++) {

            for (int j = 0; j < valores.length - 1 - i; j++) {

                if (valores[j] > valores[j + 1]) {

                    long auxiliar = valores[j];
                    valores[j] = valores[j + 1];
                    valores[j + 1] = auxiliar;
                }
            }
        }

        int medio = valores.length / 2;

        if (valores.length % 2 == 0) {
            return (valores[medio - 1] + valores[medio]) / 2;
        }

        return valores[medio];
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10000.0) / 10000.0;
    }

    private static int porcentajeDenso(int numNodos) {

        if (numNodos <= 200) {
            return 60;
        }

        if (numNodos <= 500) {
            return 20;
        }

        return 10;
    }

    // Crea los edificios 
    private static void crearVertices(Grafo grafo, int numNodos, Random aleatorio) {

        for (int i = 0; i < numNodos; i++) {

            grafo.nuevoVertice(
                "E" + i, "Edificio " + i,
                aleatorio.nextInt(600), aleatorio.nextInt(400)
            );
        }
    }
    
    // Genera una red conexa 
    public static Grafo generarGrafoDisperso(int numNodos) {

        Random aleatorio = new Random(42 + numNodos);
        Grafo grafo = new Grafo();
        boolean[][] conectada = new boolean[numNodos][numNodos];

        crearVertices(grafo, numNodos, aleatorio);

        // Conecta cada edificio 
        for (int i = 1; i < numNodos; i++) {

            int vecino = aleatorio.nextInt(i);
            double costo = 1 + aleatorio.nextInt(100);

            grafo.nuevaArista("E" + i, "E" + vecino, costo);
            conectada[i][vecino] = true;
            conectada[vecino][i] = true;
        }

        // Agregar aristas
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

    // Genera una red conexa
    public static Grafo generarGrafoDenso(int numNodos, int porcentaje) {

        Random aleatorio = new Random(142 + numNodos);
        Grafo grafo = new Grafo();
        boolean[][] conectada = new boolean[numNodos][numNodos];

        crearVertices(grafo, numNodos, aleatorio);

        // Asegurar conectividad 
        for (int i = 1; i < numNodos; i++) {

            int vecino = aleatorio.nextInt(i);
            double costo = 1 + aleatorio.nextInt(100);

            grafo.nuevaArista("E" + i, "E" + vecino, costo);
            conectada[i][vecino] = true;
            conectada[vecino][i] = true;
        }

        // Unir pares adicionales
        for (int i = 0; i < numNodos; i++) {

            for (int j = i + 1; j < numNodos; j++) {

                if (!conectada[i][j] && aleatorio.nextInt(100) < porcentaje) {

                    double costo = 1 + aleatorio.nextInt(100);

                    grafo.nuevaArista("E" + i, "E" + j, costo);
                    conectada[i][j] = true;
                    conectada[j][i] = true;
                }
            }
        }

        return grafo;
    }

    // Guarda los edificios y conexiones
    private static void guardarCSV(Grafo grafo, String nombre) {

        PrintWriter edificios = null;
        PrintWriter conexiones = null;

        try {

            edificios = new PrintWriter(carpetaRedes + nombre + "_edificios.csv");
            conexiones = new PrintWriter(carpetaRedes + nombre + "_conexiones.csv");

            edificios.println("id,nombre,x,y");

            for (int i = 0; i < grafo.getNumVertices(); i++) {

                Vertice v = grafo.getVertice(i);

                edificios.println(v.nomVertice() + "," + v.getNombreEdificio()
                        + "," + v.getX() + "," + v.getY());
            }

            conexiones.println("origen,destino,costo");

            for (int i = 0; i < grafo.getNumAristas(); i++) {

                Arista a = grafo.getArista(i);

                conexiones.println(grafo.getVertice(a.getOrigen()).nomVertice()
                        + "," + grafo.getVertice(a.getDestino()).nomVertice()
                        + "," + a.getCosto());
            }

        } catch (IOException e) {

            System.out.println("No se pudo guardar la red " + nombre + ": " + e.getMessage());

        } finally {

            if (edificios != null) {
                edificios.close();
            }

            if (conexiones != null) {
                conexiones.close();
            }
        }
    }
}