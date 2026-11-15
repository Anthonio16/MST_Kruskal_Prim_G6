package proyectoib.g6.algoritmos;

import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.ResultadoMST;

/*
 * Verificación cruzada ejecuta Prim y Kruskal
 * sobre el mismo grafo y comprueba que coincidan.
 *
 * Se comparan
 *  El costo total.
 *  Si la red es conexa o no.
 */
public class VerificadorMST {

    // Margen para comparar números decimales porque
    // sumar en distinto orden puede dar diferencias
    // minúsculas.
    private static final double TOLERANCIA = 1e-9;

    /*
     * Ejecuta Prim y Kruskal sobre el grafo y
     * devuelve true si sus resultados coinciden.
     */
    public static boolean verificar(Grafo grafo) {

        ResultadoMST prim = new Prim().ejecutarPrim(grafo);
        ResultadoMST kruskal = new Kruskal().ejecutarKruskal(grafo);

        return coinciden(prim, kruskal);
    }

    /*
     * Compara dos resultados ya calculados.
     */
    public static boolean coinciden(ResultadoMST a,
                                    ResultadoMST b) {

        if (a.esConexo() != b.esConexo()) {
            return false;
        }

        double diferencia =
            Math.abs(a.getCostoTotal() - b.getCostoTotal());

        double escala = Math.max(
            1.0,
            Math.max(
                Math.abs(a.getCostoTotal()),
                Math.abs(b.getCostoTotal())
            )
        );

        return diferencia <= TOLERANCIA * escala;
    }
}
