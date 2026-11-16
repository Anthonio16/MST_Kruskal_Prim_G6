package proyectoib.g6.estructuras;

/*
 * Se usa en Prim para sacar, en cada paso,
 * la arista candidata de menor costo.
 *
 * El arreglo crece manualmente
 * igual que los arreglos de Grafo.
 *
 * COMPLEJIDAD (n = elementos guardados):
 * - insertar:      O(log n)  (amortizado, por el crecimiento)
 * - extraerMinimo: O(log n)
 * - verMinimo:     O(1)
 * - estaVacia, getTamano: O(1)
 * - vaciar:        O(n)
 * - Memoria:       O(n)
 */
public class ColaPrioridad<T extends Comparable<T>> {

    // Capacidad con la que se crea el arreglo.
    private static final int CAPACIDAD_INICIAL = 8;

    // Arreglo que representa el árbol del montículo.
    private Object[] datos;

    // Cantidad de elementos guardados.
    private int tamano;

    /*
     * CONSTRUCTOR
     *
     * Crea una cola vacía.
     */
    public ColaPrioridad() {

        datos = new Object[CAPACIDAD_INICIAL];
        tamano = 0;
    }

    /*
     * Indica si la cola no tiene elementos.
     *
     * Complejidad: O(1)
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /*
     * Devuelve la cantidad de elementos guardados.
     *
     * Complejidad: O(1)
     */
    public int getTamano() {
        return tamano;
    }

    /*
     * INSERTAR UN ELEMENTO
     */
    public void insertar(T elemento) {

        if (elemento == null) {
            throw new IllegalArgumentException(
                "No se puede insertar un elemento nulo"
            );
        }

        if (tamano == datos.length) {
            ampliar();
        }

        datos[tamano] = elemento;
        tamano++;

        subir(tamano - 1);
    }

    /*
     * VER EL MÍNIMO SIN SACARLO
     *
     * Complejidad: O(1)
     */
    public T verMinimo() {

        validarNoVacia();

        return elemento(0);
    }

    /*
     * EXTRAER EL MÍNIMO
     *
     * Complejidad: O(log n)
     */
    public T extraerMinimo() {

        validarNoVacia();

        T minimo = elemento(0);

        tamano--;

        datos[0] = datos[tamano];
        datos[tamano] = null;

        if (tamano > 0) {
            bajar(0);
        }

        return minimo;
    }

    /*
     * VACIAR LA COLA
     *
     * Complejidad: O(n)
     */
    public void vaciar() {

        for (int i = 0; i < tamano; i++) {
            datos[i] = null;
        }

        tamano = 0;
    }

    /*
     * SUBIR
     *
     * Mientras el elemento sea menor que su padre,
     * los intercambia
     */
    private void subir(int posicion) {

        boolean sigue = true;

        while (posicion > 0 && sigue) {

            int padre = (posicion - 1) / 2;

            if (comparar(posicion, padre) < 0) {

                intercambiar(posicion, padre);
                posicion = padre;

            } else {
                sigue = false;
            }
        }
    }

    /*
     * BAJAR
     *
     * Intercambia el elemento con el menor de sus hijos
     * mientras alguno de ellos sea menor que él.
     */
    private void bajar(int posicion) {

        boolean sigue = true;

        while (sigue) {

            int izquierdo = 2 * posicion + 1;
            int derecho = izquierdo + 1;
            int menor = posicion;

            if (izquierdo < tamano &&
                comparar(izquierdo, menor) < 0) {
                menor = izquierdo;
            }

            if (derecho < tamano &&
                comparar(derecho, menor) < 0) {
                menor = derecho;
            }

            if (menor != posicion) {

                intercambiar(posicion, menor);
                posicion = menor;

            } else {
                sigue = false;
            }
        }
    }

    /*
     * AMPLIAR
     *
     * Crea un arreglo del doble de capacidad y copia
     * los elementos cuesta O(n) pero ocurre pocas
     * veces
     */
    private void ampliar() {

        Object[] nuevo = new Object[datos.length * 2];

        for (int i = 0; i < tamano; i++) {
            nuevo[i] = datos[i];
        }

        datos = nuevo;
    }

    private void validarNoVacia() {

        if (tamano == 0) {
            throw new IllegalStateException(
                "La cola de prioridad está vacía"
            );
        }
    }

    // Compara los elementos de dos posiciones.
    private int comparar(int a, int b) {
        return elemento(a).compareTo(elemento(b));
    }

    private void intercambiar(int a, int b) {

        Object auxiliar = datos[a];
        datos[a] = datos[b];
        datos[b] = auxiliar;
    }

    // Los elementos solo entran por insertar(T), por lo
    // que la conversión a T es segura.
    @SuppressWarnings("unchecked")
    private T elemento(int posicion) {
        return (T) datos[posicion];
    }
}
