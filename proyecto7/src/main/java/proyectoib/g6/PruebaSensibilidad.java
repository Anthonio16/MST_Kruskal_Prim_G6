package proyectoib.g6;

import proyectoib.g6.algoritmos.AnalizadorSensibilidad;
import proyectoib.g6.algoritmos.VerificadorMST;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.InformeSensibilidad;
import proyectoib.g6.modelo.InformeSensibilidad.TipoFallo;

/*
 * PRUEBA DEL ANÁLISIS DE SENSIBILIDAD
 *
 * Se comprueba que el grafo original
 * no cambia después de simular las fallas.
 */
public class PruebaSensibilidad {

    private static int pruebas = 0;
    private static int fallos = 0;

    public static void main(String[] args) {

        System.out.println("--- 1. Enlace reemplazable ---");
        pruebaReemplazable();

        System.out.println("\n--- 2. Puente sin alternativa ---");
        pruebaPuente();

        System.out.println("\n--- 3. Costos iguales ---");
        pruebaCostosIguales();

        System.out.println("\n--- 4a. Red poco densa ---");
        pruebaRedEsparsa();

        System.out.println("\n--- 4b. Red muy densa ---");
        pruebaRedDensa();

        System.out.println("\n--- Enlaces inválidos ---");
        pruebaEnlacesInvalidos();

        System.out.println("\nPruebas: " + pruebas
            + " | Fallos: " + fallos);
    }

    // CASOS

    private static void pruebaReemplazable() {

        Grafo red = redCincoEdificios();
        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);

        // El MST es B-C(2), A-D(3), A-B(4), D-E(5) = 14.
        // Al quitar B-C, C se reconecta con C-E(7).
        InformeSensibilidad inf = an.analizar("B", "C");
        inf.mostrarInforme();

        verificar("Tipo: con reemplazo",
            inf.getTipo() == TipoFallo.CON_REEMPLAZO);
        verificar("La red sigue conectada", inf.sigueConectada());
        verificar("Costo original 14", inf.getCostoOriginal() == 14);
        verificar("Costo nuevo 19", inf.getCostoNuevo() == 19);
        verificar("Variación +5", inf.getVariacion() == 5);
        verificar("El reemplazo cuesta 7",
            inf.getCostoReemplazo() == 7);
        verificar("Hay enlace de reemplazo",
            inf.getEnlaceReemplazo() != null);
        verificar("El grafo original no cambió",
            red.getNumAristas() == 7
            && an.getMstOriginal().getCostoTotal() == 14);
    }

    private static void pruebaPuente() {

        Grafo red = redConPuente();
        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);

        // D-F es el único enlace hacia F.
        InformeSensibilidad inf = an.analizar("D", "F");
        System.out.println();
        inf.mostrarInforme();

        verificar("Costo original 20", inf.getCostoOriginal() == 20);
        verificar("Tipo: desconecta la red",
            inf.getTipo() == TipoFallo.DESCONECTA_RED);
        verificar("La red NO sigue conectada",
            !inf.sigueConectada());
        verificar("No hay enlace de reemplazo",
            inf.getEnlaceReemplazo() == null);
        verificar("Quedan 4 aristas, no 5",
            inf.getNuevoMST().getNumAristas() == 4);
        verificar("Costo de las aristas restantes 14",
            inf.getCostoNuevo() == 14);
        verificar("El grafo original no cambió",
            red.getNumAristas() == 8);

        // En la misma red, un enlace del MST que sí
        // tiene alternativa (B-C se reemplaza por C-E).
        InformeSensibilidad otro = an.analizar("B", "C");
        verificar("En la misma red, B-C sí tiene reemplazo",
            otro.getTipo() == TipoFallo.CON_REEMPLAZO);
    }

    private static void pruebaCostosIguales() {

        // Cuatro edificios y todas las conexiones con costo 5.
        Grafo red = redCostosIguales();
        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);

        verificar("MST original: costo 15",
            an.getMstOriginal().getCostoTotal() == 15);

        InformeSensibilidad[] informes = an.analizarTodos();
        verificar("Hay 3 enlaces en el MST", informes.length == 3);

        boolean todosReemplazables = true;
        boolean sinVariacion = true;
        boolean conectadas = true;

        for (int i = 0; i < informes.length; i++) {

            if (informes[i].getTipo() != TipoFallo.CON_REEMPLAZO) {
                todosReemplazables = false;
            }
            if (informes[i].getVariacion() != 0) {
                sinVariacion = false;
            }
            if (!informes[i].sigueConectada()) {
                conectadas = false;
            }
        }

        informes[0].mostrarInforme();

        verificar("Todos los fallos tienen reemplazo",
            todosReemplazables);
        verificar("Variación 0 en todos, el reemplazo cuesta igual",
            sinVariacion);
        verificar("La red siempre sigue conectada", conectadas);
    }

    private static void pruebaRedEsparsa() {

        // Árbol de 8 vértices
        Grafo red = new Grafo();

        for (int i = 0; i < 8; i++) {
            red.nuevoVertice("V" + i);
        }
        for (int i = 0; i < 7; i++) {
            red.nuevaArista("V" + i, "V" + (i + 1), i + 1);
        }

        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);
        InformeSensibilidad[] informes = an.analizarTodos();

        boolean todosPuentes = true;

        for (int i = 0; i < informes.length; i++) {

            boolean puente =
                informes[i].getTipo() == TipoFallo.DESCONECTA_RED
                && !informes[i].sigueConectada()
                && informes[i].getNuevoMST().getNumAristas() == 6;

            if (!puente) {
                todosPuentes = false;
            }
        }

        verificar("Los 7 enlaces del MST son puentes",
            informes.length == 7 && todosPuentes);
        verificar("Prim y Kruskal coinciden",
            VerificadorMST.verificar(red));
        verificar("El grafo original no cambió",
            red.getNumAristas() == 7);
    }

    private static void pruebaRedDensa() {

        // Red completa de 8 vértices
        Grafo red = new Grafo();

        for (int i = 0; i < 8; i++) {
            red.nuevoVertice("V" + i);
        }
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 8; j++) {
                red.nuevaArista("V" + i, "V" + j, i * 8 + j + 1);
            }
        }

        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);
        InformeSensibilidad[] informes = an.analizarTodos();

        boolean todosConReemplazo = true;
        boolean costoNoBaja = true;
        boolean costoCoherente = true;

        for (int i = 0; i < informes.length; i++) {

            InformeSensibilidad inf = informes[i];

            if (inf.getTipo() != TipoFallo.CON_REEMPLAZO
                || !inf.sigueConectada()) {
                todosConReemplazo = false;
            }
            if (inf.getVariacion() < 0) {
                costoNoBaja = false;
            }

            // costo nuevo = original - retirado + reemplazo
            double esperado = inf.getCostoOriginal()
                - inf.getCostoRetirado()
                + inf.getCostoReemplazo();

            if (Math.abs(esperado - inf.getCostoNuevo()) > 1e-9) {
                costoCoherente = false;
            }
        }

        verificar("Los 7 enlaces del MST tienen reemplazo",
            informes.length == 7 && todosConReemplazo);
        verificar("El costo nunca baja al fallar un enlace",
            costoNoBaja);
        verificar("Costo nuevo = original - retirado + reemplazo",
            costoCoherente);
        verificar("Prim y Kruskal coinciden",
            VerificadorMST.verificar(red));
        verificar("El grafo original no cambió",
            red.getNumAristas() == 28);
    }

    private static void pruebaEnlacesInvalidos() {

        Grafo red = redCincoEdificios();
        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);

        boolean fueraDelMST = false;
        try {
            // A-E existe en el grafo pero no está en el MST.
            an.analizar("A", "E");
        } catch (IllegalArgumentException e) {
            fueraDelMST = true;
        }
        verificar("Enlace fuera del MST lanza excepción",
            fueraDelMST);

        boolean noExiste = false;
        try {
            an.analizar("A", "Z");
        } catch (IllegalArgumentException e) {
            noExiste = true;
        }
        verificar("Edificio inexistente lanza excepción", noExiste);

        boolean posicionMala = false;
        try {
            an.analizar(99);
        } catch (IllegalArgumentException e) {
            posicionMala = true;
        }
        verificar("Posición inválida lanza excepción", posicionMala);
    }

    // REDES

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

    // Los cinco edificios más F conectado solo por D-F.
    private static Grafo redConPuente() {

        Grafo red = redCincoEdificios();
        red.nuevoVertice("F");
        red.nuevaArista("D", "F", 6);
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
