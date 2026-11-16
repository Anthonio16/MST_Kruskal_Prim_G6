package proyectoib.g6.modelo;

import java.util.Locale;

/*
 * Resultado de simular la falla de un enlace
 * del MST original
 */
public class InformeSensibilidad {

    public enum TipoFallo {
        CON_REEMPLAZO,
        DESCONECTA_RED
    }

    private final String enlaceRetirado;
    private final double costoRetirado;
    private final TipoFallo tipo;
    private final int componentesOriginal;
    private final int componentesNuevo;
    private final double costoOriginal;
    private final double costoNuevo;

    // Solo tienen valor cuando el tipo es CON_REEMPLAZO.
    private final String enlaceReemplazo;
    private final double costoReemplazo;

    // MST recalculado sin el enlace.
    private final ResultadoMST nuevoMST;

    public InformeSensibilidad(String enlaceRetirado,
                               double costoRetirado,
                               TipoFallo tipo,
                               int componentesOriginal,
                               int componentesNuevo,
                               double costoOriginal,
                               double costoNuevo,
                               String enlaceReemplazo,
                               double costoReemplazo,
                               ResultadoMST nuevoMST) {

        this.enlaceRetirado = enlaceRetirado;
        this.costoRetirado = costoRetirado;
        this.tipo = tipo;
        this.componentesOriginal = componentesOriginal;
        this.componentesNuevo = componentesNuevo;
        this.costoOriginal = costoOriginal;
        this.costoNuevo = costoNuevo;
        this.enlaceReemplazo = enlaceReemplazo;
        this.costoReemplazo = costoReemplazo;
        this.nuevoMST = nuevoMST;
    }

    public String getEnlaceRetirado() {
        return enlaceRetirado;
    }

    public double getCostoRetirado() {
        return costoRetirado;
    }

    public TipoFallo getTipo() {
        return tipo;
    }

    /*
     * Cantidad de grupos de edificios conectados
     * antes de la falla
     */
    public int getComponentesOriginal() {
        return componentesOriginal;
    }

    /*
     * Cantidad de grupos de edificios conectados
     * después de la falla.
     */
    public int getComponentesNuevo() {
        return componentesNuevo;
    }

    /*
     * true si la red original era conexa, es decir,
     * si existía un MST completo antes de la falla.
     */
    public boolean mstOriginalCompleto() {
        return componentesOriginal == 1;
    }

    /*
     * true si después de la falla existe un MST completo
     * 
     */
    public boolean existeMSTCompleto() {
        return componentesNuevo == 1;
    }

    /*
     * Igual que existeMSTCompleto(): la red sigue
     * conectada después de la falla.
     */
    public boolean sigueConectada() {
        return existeMSTCompleto();
    }

    public double getCostoOriginal() {
        return costoOriginal;
    }

    /*
     * Costo del MST recalculado.
     *
     * Si no existe un MST completo
     * es el costo del bosque de expansión mínima.
     */
    public double getCostoNuevo() {
        return costoNuevo;
    }

    /*
     * Diferencia entre el costo nuevo y el original.
     *
     * Solo es comparable cuando el tipo es CON_REEMPLAZO
     * 
     * Si el tipo es DESCONECTA_RED solo refleja la arista
     * perdida.
     */
    public double getVariacion() {
        return costoNuevo - costoOriginal;
    }

    /*
     * Variación en porcentaje del costo original.
     * Devuelve NaN si el costo original es 0.
     */
    public double getVariacionPorcentual() {

        if (costoOriginal == 0) {
            return Double.NaN;
        }

        return getVariacion() / costoOriginal * 100;
    }

    /*
     * Enlace que sustituye al retirado.
     * Es null si el tipo es DESCONECTA_RED.
     */
    public String getEnlaceReemplazo() {
        return enlaceReemplazo;
    }

    public double getCostoReemplazo() {
        return costoReemplazo;
    }

    public ResultadoMST getNuevoMST() {
        return nuevoMST;
    }

    /*
     * Texto del informe listo para mostrar.
     *
     * Siempre indica si existe un MST completo antes
     * y después de la falla.
     */
    public String resumen() {

        StringBuilder s = new StringBuilder();

        // Con MST completo los costos son de un árbol;
        // sin él, de un bosque.
        String sujeto = mstOriginalCompleto()
            ? "del MST" : "del bosque";

        s.append("=== INFORME DE SENSIBILIDAD ===\n");
        s.append("Enlace retirado: ").append(enlaceRetirado)
         .append(" (costo ").append(costoRetirado).append(")\n");
        s.append("Red original: ")
         .append(estadoRed(componentesOriginal)).append("\n");
        s.append("Red tras la falla: ")
         .append(estadoRed(componentesNuevo)).append("\n");

        if (tipo == TipoFallo.CON_REEMPLAZO) {

            s.append("Tipo de fallo: CON REEMPLAZO "
                   + "(la cantidad de componentes no cambia)\n");
            s.append("Enlace de reemplazo: ")
             .append(enlaceReemplazo)
             .append(" (costo ").append(costoReemplazo)
             .append(")\n");
            s.append("Costo ").append(sujeto)
             .append(" original: ").append(costoOriginal)
             .append("\n");
            s.append("Costo ").append(sujeto)
             .append(" nuevo: ").append(costoNuevo).append("\n");
            s.append("Variación: ").append(signo(getVariacion()));

            if (!Double.isNaN(getVariacionPorcentual())) {
                s.append(String.format(
                    Locale.US, " (%+.1f%%)",
                    getVariacionPorcentual()
                ));
            }

            s.append("\n");

        } else {

            if (mstOriginalCompleto()) {
                s.append("Tipo de fallo: DESCONECTA LA RED "
                       + "(puente sin enlace alternativo)\n");
            } else {
                s.append("Tipo de fallo: FRAGMENTA AÚN MÁS LA RED "
                       + "(puente sin enlace alternativo; pasa de "
                       + componentesOriginal + " a "
                       + componentesNuevo + " componentes)\n");
            }

            s.append("Costo ").append(sujeto)
             .append(" original: ").append(costoOriginal)
             .append("\n");
            s.append("Costo de las aristas restantes: ")
             .append(costoNuevo)
             .append(" (parcial: no es un MST completo)\n");
            s.append("Variación: ").append(signo(getVariacion()))
             .append(" (solo refleja el enlace perdido; "
                   + "no es comparable)\n");
        }

        return s.toString();
    }

    /*
     * Imprime el informe en consola.
     */
    public void mostrarInforme() {
        System.out.print(resumen());
    }

    // Describe el estado de la red según sus componentes.
    private String estadoRed(int componentes) {

        if (componentes == 1) {
            return "CONEXA, existe un MST completo.";
        }

        return "DESCONECTADA (" + componentes
             + " componentes), NO existe un MST completo.";
    }

    // Agrega "+" a los valores positivos.
    private String signo(double valor) {

        if (valor > 0) {
            return "+" + valor;
        }

        return String.valueOf(valor);
    }
}
