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

    private static final String NO_EXISTE = "NO existe un MST completo";

    private static int pruebas = 0;
    private static int fallos = 0;

    public static void main(String[] args) {
        ejecutar();
    }

    /*
     * Ejecuta todas las pruebas y devuelve
     * la cantidad de fallos.
     */
    public static int ejecutar() {

        pruebas = 0;
        fallos = 0;

        System.out.println("--- 1. Enlace reemplazable ---");
        pruebaReemplazable();

        System.out.println("\n--- 2. Puente sin alternativa ---");
        pruebaPuente();

        System.out.println("\n--- 3. Costos iguales ---");
        pruebaCostosIguales();

        System.out.println("\n--- 4a. Red poco densa (camino) ---");
        pruebaRedEsparsa();

        System.out.println("\n--- 4b. Red muy densa (completa) ---");
        pruebaRedDensa();

        System.out.println("\n--- 5a. Red ya desconectada: enlace con reemplazo ---");
        pruebaDesconectadaConReemplazo();

        System.out.println("\n--- 5b. Red ya desconectada: fragmenta aún más ---");
        pruebaDesconectadaFragmenta();

        System.out.println("\n--- 5c. Redes sin enlaces que analizar ---");
        pruebaSinEnlaces();

        System.out.println("\n--- 6. La copia es independiente ---");
        pruebaCopiaIndependiente();

        System.out.println("\n--- 7. 300 redes aleatorias ---");
        pruebaAleatorias();

        System.out.println("\n--- 8. Enlaces inválidos ---");
        pruebaEnlacesInvalidos();

        System.out.println("\nPruebas: " + pruebas
            + " | Fallos: " + fallos);

        return fallos;
    }

    // ---------------- CASOS ----------------

    private static void pruebaReemplazable() {

        Grafo red = GrafosDePrueba.cincoEdificios();
        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);

        // El MST es B-C(2), A-D(3), A-B(4), D-E(5) = 14.
        // Al quitar B-C, C se reconecta con C-E(7).
        InformeSensibilidad inf = an.analizar("B", "C");
        inf.mostrarInforme();

        verificar("Tipo: con reemplazo",
            inf.getTipo() == TipoFallo.CON_REEMPLAZO);
        verificar("La red sigue conectada", inf.sigueConectada());
        verificar("Existe un MST completo antes y después",
            inf.mstOriginalCompleto() && inf.existeMSTCompleto());
        verificar("El informe NO dice que falte el MST",
            !inf.resumen().contains(NO_EXISTE));
        verificar("Costo original 14", inf.getCostoOriginal() == 14);
        verificar("Costo nuevo 19", inf.getCostoNuevo() == 19);
        verificar("Variación +5", inf.getVariacion() == 5);
        verificar("El reemplazo cuesta 7", inf.getCostoReemplazo() == 7);
        verificar("Hay enlace de reemplazo",
            inf.getEnlaceReemplazo() != null);
        verificar("El grafo original no cambió",
            red.getNumAristas() == 7
            && an.getMstOriginal().getCostoTotal() == 14);
    }

    private static void pruebaPuente() {

        Grafo red = GrafosDePrueba.conPuente();
        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);

        // D-F es el único enlace hacia F.
        InformeSensibilidad inf = an.analizar("D", "F");
        System.out.println();
        inf.mostrarInforme();

        verificar("Costo original 20", inf.getCostoOriginal() == 20);
        verificar("Tipo: desconecta la red",
            inf.getTipo() == TipoFallo.DESCONECTA_RED);
        verificar("La red NO sigue conectada", !inf.sigueConectada());
        verificar("Antes había MST completo; después ya no",
            inf.mstOriginalCompleto() && !inf.existeMSTCompleto());
        verificar("Componentes: de 1 a 2",
            inf.getComponentesOriginal() == 1
            && inf.getComponentesNuevo() == 2);
        verificar("El informe dice que NO existe un MST completo",
            inf.resumen().contains(NO_EXISTE));
        verificar("El informe dice que DESCONECTA LA RED",
            inf.resumen().contains("DESCONECTA LA RED"));
        verificar("No hay enlace de reemplazo",
            inf.getEnlaceReemplazo() == null);
        verificar("Quedan 4 aristas, no 5",
            inf.getNuevoMST().getNumAristas() == 4);
        verificar("Costo de las aristas restantes 14",
            inf.getCostoNuevo() == 14);
        verificar("El grafo original no cambió",
            red.getNumAristas() == 8);

        // En la misma red, un enlace del MST que sí tiene
        // alternativa (B-C se reemplaza por C-E).
        InformeSensibilidad otro = an.analizar("B", "C");
        verificar("En la misma red, B-C sí tiene reemplazo",
            otro.getTipo() == TipoFallo.CON_REEMPLAZO);
    }

    private static void pruebaCostosIguales() {

        Grafo red = GrafosDePrueba.costosIguales();
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
        verificar("Variación 0 en todos (el reemplazo cuesta igual)",
            sinVariacion);
        verificar("La red siempre sigue conectada", conectadas);
    }

    private static void pruebaRedEsparsa() {

        // Camino de 8 vértices: 7 aristas de las 28 posibles.
        Grafo red = GrafosDePrueba.camino(8);

        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);
        InformeSensibilidad[] informes = an.analizarTodos();

        boolean todosPuentes = true;

        for (int i = 0; i < informes.length; i++) {

            boolean puente =
                informes[i].getTipo() == TipoFallo.DESCONECTA_RED
                && !informes[i].sigueConectada()
                && informes[i].getNuevoMST().getNumAristas() == 6
                && informes[i].resumen().contains(NO_EXISTE);

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

        // Red completa de 8 vértices: 28 aristas, costos distintos.
        Grafo red = GrafosDePrueba.completa(8);

        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);
        InformeSensibilidad[] informes = an.analizarTodos();

        boolean todosConReemplazo = true;
        boolean costoNoBaja = true;
        boolean costoCoherente = true;
        boolean sinAvisoDeFalta = true;

        for (int i = 0; i < informes.length; i++) {

            InformeSensibilidad inf = informes[i];

            if (inf.getTipo() != TipoFallo.CON_REEMPLAZO
                || !inf.sigueConectada()) {
                todosConReemplazo = false;
            }
            if (inf.getVariacion() < 0) {
                costoNoBaja = false;
            }
            if (inf.resumen().contains(NO_EXISTE)) {
                sinAvisoDeFalta = false;
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
        verificar("Ningún informe dice que falte el MST",
            sinAvisoDeFalta);
        verificar("El costo nunca baja al fallar un enlace",
            costoNoBaja);
        verificar("Costo nuevo = original - retirado + reemplazo",
            costoCoherente);
        verificar("Prim y Kruskal coinciden",
            VerificadorMST.verificar(red));
        verificar("El grafo original no cambió",
            red.getNumAristas() == 28);
    }

    private static void pruebaDesconectadaConReemplazo() {

        // {A,B,C,D}, {E,F,G} y {H}. Bosque: 5 aristas, costo 15.
        Grafo red = GrafosDePrueba.desconectadoVariado();
        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);

        verificar("El MST original es un bosque (no conexo)",
            !an.getMstOriginal().esConexo());

        // Al quitar B-C(2), la arista A-D(10) reconecta C y D con A.
        InformeSensibilidad inf = an.analizar("B", "C");
        inf.mostrarInforme();

        verificar("Tipo: con reemplazo",
            inf.getTipo() == TipoFallo.CON_REEMPLAZO);
        verificar("Componentes: 3 antes y 3 después",
            inf.getComponentesOriginal() == 3
            && inf.getComponentesNuevo() == 3);
        verificar("Nunca existió MST completo (ni antes ni después)",
            !inf.mstOriginalCompleto() && !inf.existeMSTCompleto());
        verificar("La red NO sigue conectada", !inf.sigueConectada());
        verificar("El informe dice que NO existe un MST completo",
            inf.resumen().contains(NO_EXISTE));
        verificar("El informe habla del bosque, no de un MST",
            inf.resumen().contains("bosque"));
        verificar("El reemplazo es A-D (costo 10)",
            inf.getCostoReemplazo() == 10
            && inf.getEnlaceReemplazo() != null);
        verificar("Costo del bosque: 15 -> 23 (+8)",
            inf.getCostoOriginal() == 15
            && inf.getCostoNuevo() == 23
            && inf.getVariacion() == 8);
        verificar("El grafo original no cambió",
            red.getNumAristas() == 6);
    }

    private static void pruebaDesconectadaFragmenta() {

        Grafo red = GrafosDePrueba.desconectadoVariado();
        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);

        // E-F(4) es un puente dentro del componente {E,F,G}.
        InformeSensibilidad inf = an.analizar("E", "F");
        System.out.println();
        inf.mostrarInforme();

        verificar("Tipo: desconecta (aumentan los componentes)",
            inf.getTipo() == TipoFallo.DESCONECTA_RED);
        verificar("Componentes: de 3 a 4",
            inf.getComponentesOriginal() == 3
            && inf.getComponentesNuevo() == 4);
        verificar("El informe dice FRAGMENTA AÚN MÁS LA RED",
            inf.resumen().contains("FRAGMENTA AÚN MÁS LA RED"));
        verificar("El informe NO dice 'desconecta la red' "
                + "(ya estaba desconectada)",
            !inf.resumen().contains("DESCONECTA LA RED"));
        verificar("El informe dice que NO existe un MST completo",
            inf.resumen().contains(NO_EXISTE));
        verificar("Sin enlace de reemplazo",
            inf.getEnlaceReemplazo() == null);
        verificar("Costo del bosque: 15 -> 11",
            inf.getCostoOriginal() == 15 && inf.getCostoNuevo() == 11);
    }

    private static void pruebaSinEnlaces() {

        // Un solo vértice: conexo, pero sin enlaces que fallar.
        Grafo uno = new Grafo();
        uno.nuevoVertice("A");

        AnalizadorSensibilidad anUno = new AnalizadorSensibilidad(uno);

        verificar("Un vértice: no hay enlaces que analizar",
            anUno.analizarTodos().length == 0);

        // Tres edificios sin ninguna conexión.
        Grafo sinConexiones = new Grafo();
        sinConexiones.nuevoVertice("A");
        sinConexiones.nuevoVertice("B");
        sinConexiones.nuevoVertice("C");

        AnalizadorSensibilidad anSin =
            new AnalizadorSensibilidad(sinConexiones);

        verificar("Sin conexiones: no hay enlaces que analizar",
            anSin.analizarTodos().length == 0);
        verificar("Sin conexiones: el MST original no es completo",
            !anSin.getMstOriginal().esConexo());

        // Grafo vacío.
        AnalizadorSensibilidad anVacio =
            new AnalizadorSensibilidad(new Grafo());

        verificar("Grafo vacío: no hay enlaces que analizar",
            anVacio.analizarTodos().length == 0);
    }

    private static void pruebaCopiaIndependiente() {

        Grafo red = GrafosDePrueba.cincoEdificios();
        red.nuevoVertice("F", "Parqueadero", 164, 264);
        red.nuevaArista("D", "F", 6);

        int posicionDF = red.numVertice("D");
        int posicionF = red.numVertice("F");

        Grafo copia = red.copiarSinArista(posicionDF, posicionF);

        verificar("La copia tiene los mismos vértices",
            copia.getNumVertices() == red.getNumVertices());
        verificar("La copia tiene una arista menos",
            copia.getNumAristas() == red.getNumAristas() - 1);
        verificar("El original conserva todas sus aristas",
            red.getNumAristas() == 8);
        verificar("La copia conserva nombre y coordenadas",
            copia.getVertice(posicionF).getNombreEdificio()
                .equals("Parqueadero")
            && copia.getVertice(posicionF).getX() == 164
            && copia.getVertice(posicionF).getY() == 264);
        verificar("Los vértices conservan su número",
            copia.getVertice(posicionF).getNumVertice() == posicionF
            && copia.numVertice("F") == posicionF);

        // Se modifica la copia: el original no debe cambiar.
        copia.nuevoVertice("Z");
        copia.nuevaArista("A", "Z", 1);

        verificar("Modificar la copia no cambia el original",
            red.getNumVertices() == 6
            && red.getNumAristas() == 8
            && red.numVertice("Z") == -1);

        // El enlace se puede pedir en cualquier dirección.
        Grafo inversa = red.copiarSinArista(posicionF, posicionDF);
        verificar("(F, D) equivale a (D, F)",
            inversa.getNumAristas() == 7);

        boolean noExiste = false;
        try {
            // A-F no es una arista del grafo.
            red.copiarSinArista(red.numVertice("A"), posicionF);
        } catch (IllegalArgumentException e) {
            noExiste = true;
        }
        verificar("Quitar una arista inexistente lanza excepción",
            noExiste);
    }

    private static void pruebaAleatorias() {

        int conexas = 0;
        int desconectadas = 0;
        int informesTotales = 0;
        int conReemplazo = 0;
        int desconectan = 0;

        boolean componentesOriginalOk = true;
        boolean componentesNuevoOk = true;
        boolean reemplazoCoherente = true;
        boolean costoReemplazoOk = true;
        boolean costoPuenteOk = true;
        boolean textoCoherente = true;
        boolean originalSinCambios = true;

        for (int semilla = 1; semilla <= 300; semilla++) {

            int n = 2 + semilla % 9;
            int porcentaje = 10 + (semilla * 7) % 60;

            // Costos entre 1 y 4: hay muchos empates.
            Grafo red = GrafosDePrueba.aleatoria(semilla, n, porcentaje, 4);

            int aristasAntes = red.getNumAristas();
            int componentes = GrafosDePrueba.componentes(red);

            if (componentes == 1) {
                conexas++;
            } else {
                desconectadas++;
            }

            AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);
            InformeSensibilidad[] informes = an.analizarTodos();

            for (int i = 0; i < informes.length; i++) {

                InformeSensibilidad inf = informes[i];
                informesTotales++;

                // Componentes calculados de forma independiente.
                Arista retirada = an.getMstOriginal().getArista(i);
                Grafo copia = red.copiarSinArista(
                    retirada.getOrigen(), retirada.getDestino());

                if (inf.getComponentesOriginal() != componentes) {
                    componentesOriginalOk = false;
                }
                if (inf.getComponentesNuevo()
                        != GrafosDePrueba.componentes(copia)) {
                    componentesNuevoOk = false;
                }

                boolean conReemp =
                    inf.getTipo() == TipoFallo.CON_REEMPLAZO;

                // Hay enlace de reemplazo si y solo si el tipo
                // es CON_REEMPLAZO.
                if (conReemp != (inf.getEnlaceReemplazo() != null)) {
                    reemplazoCoherente = false;
                }

                if (conReemp) {

                    conReemplazo++;

                    double esperado = inf.getCostoOriginal()
                        - inf.getCostoRetirado()
                        + inf.getCostoReemplazo();

                    if (Math.abs(esperado - inf.getCostoNuevo()) > 1e-9
                        || inf.getVariacion() < 0) {
                        costoReemplazoOk = false;
                    }

                } else {

                    desconectan++;

                    double esperado = inf.getCostoOriginal()
                        - inf.getCostoRetirado();

                    if (Math.abs(esperado - inf.getCostoNuevo()) > 1e-9) {
                        costoPuenteOk = false;
                    }
                }

                // El texto debe decir "NO existe un MST completo"
                // si y solo si la red quedó desconectada.
                boolean dice = inf.resumen().contains(NO_EXISTE);

                if (dice == inf.existeMSTCompleto()
                    || inf.existeMSTCompleto()
                       != (inf.getComponentesNuevo() == 1)) {
                    textoCoherente = false;
                }
            }

            if (red.getNumAristas() != aristasAntes) {
                originalSinCambios = false;
            }
        }

        System.out.println("  (info) redes conexas: " + conexas
            + " | desconectadas: " + desconectadas);
        System.out.println("  (info) fallos analizados: " + informesTotales
            + " (con reemplazo: " + conReemplazo
            + ", aumentan componentes: " + desconectan + ")");

        verificar("Hubo redes conexas y desconectadas",
            conexas > 0 && desconectadas > 0);
        verificar("Hubo fallos con reemplazo y fallos que desconectan",
            conReemplazo > 0 && desconectan > 0);
        verificar("Componentes originales = cálculo independiente",
            componentesOriginalOk);
        verificar("Componentes tras la falla = cálculo independiente",
            componentesNuevoOk);
        verificar("Hay enlace de reemplazo <=> tipo CON_REEMPLAZO",
            reemplazoCoherente);
        verificar("Con reemplazo: nuevo = original - retirado + reemplazo "
                + "y la variación nunca es negativa",
            costoReemplazoOk);
        verificar("Puente: nuevo = original - retirado", costoPuenteOk);
        verificar("El texto dice 'NO existe un MST completo' "
                + "solo cuando la red quedó desconectada",
            textoCoherente);
        verificar("Ningún grafo original cambió", originalSinCambios);
    }

    private static void pruebaEnlacesInvalidos() {

        Grafo red = GrafosDePrueba.cincoEdificios();
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

        boolean nulo = false;
        try {
            new AnalizadorSensibilidad(null);
        } catch (IllegalArgumentException e) {
            nulo = true;
        }
        verificar("Grafo nulo lanza excepción", nulo);
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
