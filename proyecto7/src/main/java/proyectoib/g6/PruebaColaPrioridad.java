package proyectoib.g6;

import proyectoib.g6.estructuras.ColaPrioridad;

/*
 * PRUEBA DE LA COLA DE PRIORIDAD
 *
 * Cada comprobación imprime [OK] o [FALLO].
 */
public class PruebaColaPrioridad {

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

        System.out.println("--- Cola vacía ---");
        pruebaVacia();

        System.out.println("\n--- Orden de salida ---");
        pruebaOrden();

        System.out.println("\n--- Elementos repetidos ---");
        pruebaRepetidos();

        System.out.println("\n--- Crecimiento del arreglo ---");
        pruebaCrecimiento();

        System.out.println("\n--- Inserciones y extracciones mezcladas ---");
        pruebaMezcla();

        System.out.println("\n--- verMinimo y vaciar ---");
        pruebaVerYVaciar();

        System.out.println("\nPruebas: " + pruebas
            + " | Fallos: " + fallos);

        return fallos;
    }

    private static void pruebaVacia() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        verificar("Nueva cola: está vacía", cola.estaVacia());
        verificar("Nueva cola: tamaño 0", cola.getTamano() == 0);

        boolean extraerLanza = false;
        try {
            cola.extraerMinimo();
        } catch (IllegalStateException e) {
            extraerLanza = true;
        }
        verificar("extraerMinimo en cola vacía lanza excepción",
            extraerLanza);

        boolean verLanza = false;
        try {
            cola.verMinimo();
        } catch (IllegalStateException e) {
            verLanza = true;
        }
        verificar("verMinimo en cola vacía lanza excepción", verLanza);

        boolean nuloLanza = false;
        try {
            cola.insertar(null);
        } catch (IllegalArgumentException e) {
            nuloLanza = true;
        }
        verificar("insertar null lanza excepción", nuloLanza);
    }

    private static void pruebaOrden() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        int[] entrada = {5, 3, 8, 1, 9, 2, 7};

        for (int i = 0; i < entrada.length; i++) {
            cola.insertar(entrada[i]);
        }

        verificar("Tamaño 7 después de insertar",
            cola.getTamano() == 7);

        int[] esperado = {1, 2, 3, 5, 7, 8, 9};
        boolean ordenCorrecto = true;

        for (int i = 0; i < esperado.length; i++) {

            if (cola.extraerMinimo() != esperado[i]) {
                ordenCorrecto = false;
            }
        }

        verificar("Sale en orden 1,2,3,5,7,8,9", ordenCorrecto);
        verificar("Queda vacía al extraer todo", cola.estaVacia());

        // Entrada ya ordenada y entrada en orden inverso.
        ColaPrioridad<Integer> ascendente = new ColaPrioridad<>();
        ColaPrioridad<Integer> descendente = new ColaPrioridad<>();

        for (int i = 1; i <= 20; i++) {
            ascendente.insertar(i);
            descendente.insertar(21 - i);
        }

        boolean ambasOrdenadas = true;

        for (int i = 1; i <= 20; i++) {

            if (ascendente.extraerMinimo() != i ||
                descendente.extraerMinimo() != i) {
                ambasOrdenadas = false;
            }
        }

        verificar("Entrada ascendente y descendente salen ordenadas",
            ambasOrdenadas);
    }

    private static void pruebaRepetidos() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        int[] entrada = {4, 2, 4, 2, 4, 1, 1};

        for (int i = 0; i < entrada.length; i++) {
            cola.insertar(entrada[i]);
        }

        int[] esperado = {1, 1, 2, 2, 4, 4, 4};
        boolean correcto = true;

        for (int i = 0; i < esperado.length; i++) {

            if (cola.extraerMinimo() != esperado[i]) {
                correcto = false;
            }
        }

        verificar("Los repetidos salen todos, en orden", correcto);
    }

    private static void pruebaCrecimiento() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        long estado = 42;

        for (int i = 0; i < 1000; i++) {

            estado = estado * 6364136223846793005L
                   + 1442695040888963407L;

            cola.insertar((int) ((estado >>> 33) % 500));
        }

        verificar("Tamaño 1000 (capacidad inicial es 8)",
            cola.getTamano() == 1000);

        boolean noDecreciente = true;
        int anterior = Integer.MIN_VALUE;
        int sacados = 0;

        while (!cola.estaVacia()) {

            int actual = cola.extraerMinimo();

            if (actual < anterior) {
                noDecreciente = false;
            }

            anterior = actual;
            sacados++;
        }

        verificar("Se extraen los 1000 elementos", sacados == 1000);
        verificar("Salen en orden no decreciente", noDecreciente);
    }

    private static void pruebaMezcla() {

        // Se compara contra una "cola" de referencia muy simple
        // un arreglo donde el mínimo se busca recorriéndolo.
        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        int[] referencia = new int[600];
        int cantidad = 0;

        long estado = 7;
        boolean coincide = true;

        for (int paso = 0; paso < 600; paso++) {

            estado = estado * 6364136223846793005L
                   + 1442695040888963407L;

            int sorteo = (int) ((estado >>> 33) % 100);

            // 60% inserta, 40% extrae (si hay algo).
            if (sorteo < 60 || cantidad == 0) {

                int valor = (int) ((estado >>> 20) % 1000);

                cola.insertar(valor);
                referencia[cantidad] = valor;
                cantidad++;

            } else {

                int posicionMin = 0;

                for (int i = 1; i < cantidad; i++) {
                    if (referencia[i] < referencia[posicionMin]) {
                        posicionMin = i;
                    }
                }

                int minimo = referencia[posicionMin];

                referencia[posicionMin] = referencia[cantidad - 1];
                cantidad--;

                if (cola.extraerMinimo() != minimo) {
                    coincide = false;
                }
            }
        }

        verificar("Coincide con la referencia en 600 operaciones",
            coincide);
        verificar("Mismo tamaño que la referencia",
            cola.getTamano() == cantidad);
    }

    private static void pruebaVerYVaciar() {

        ColaPrioridad<Integer> cola = new ColaPrioridad<>();

        cola.insertar(9);
        cola.insertar(4);
        cola.insertar(6);

        verificar("verMinimo devuelve 4", cola.verMinimo() == 4);
        verificar("verMinimo no quita el elemento",
            cola.getTamano() == 3);

        cola.vaciar();

        verificar("vaciar deja la cola vacía",
            cola.estaVacia() && cola.getTamano() == 0);

        cola.insertar(3);
        cola.insertar(1);

        verificar("Se puede reutilizar después de vaciar",
            cola.extraerMinimo() == 1 && cola.extraerMinimo() == 3);
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
