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
    private final boolean sigueConectada;
    private final boolean redOriginalConexa;
    private final double costoOriginal;
    private final double costoNuevo;

    // Solo tiene valor cuando el tipo es CON_REEMPLAZO.
    private final String enlaceReemplazo;
    private final double costoReemplazo;

    // MST recalculado sin el enlace.
    private final ResultadoMST nuevoMST;

    public InformeSensibilidad(String enlaceRetirado,
                               double costoRetirado,
                               TipoFallo tipo,
                               boolean sigueConectada,
                               boolean redOriginalConexa,
                               double costoOriginal,
                               double costoNuevo,
                               String enlaceReemplazo,
                               double costoReemplazo,
                               ResultadoMST nuevoMST) {

        this.enlaceRetirado = enlaceRetirado;
        this.costoRetirado = costoRetirado;
        this.tipo = tipo;
        this.sigueConectada = sigueConectada;
        this.redOriginalConexa = redOriginalConexa;
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
     * true si después de la falla todos los
     * vértices siguen conectados entre sí.
     */
    public boolean sigueConectada() {
        return sigueConectada;
    }

    public double getCostoOriginal() {
        return costoOriginal;
    }

    /*
     * Costo del MST recalculado.
     *
     * Si el tipo es DESCONECTA_RED es el costo de las
     * aristas restantes no de un MST completo.
     */
    public double getCostoNuevo() {
        return costoNuevo;
    }

  
    // Diferencia entre el costo nuevo y el original.
    public double getVariacion() {
        return costoNuevo - costoOriginal;
    }

    /*
     * Variación en porcentaje del costo original
     * devuelve NaN si el costo original es 0.
     */
    public double getVariacionPorcentual() {

        if (costoOriginal == 0) {
            return Double.NaN;
        }

        return getVariacion() / costoOriginal * 100;
    }

    /*
     * Enlace que sustituye al retirado
     * es null si la red se desconecta
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
     * Texto del informe para mostrar.
     */
    public String resumen() {

        StringBuilder s = new StringBuilder();

        s.append("=== INFORME DE SENSIBILIDAD ===\n");
        s.append("Enlace retirado: ").append(enlaceRetirado)
         .append(" (costo ").append(costoRetirado).append(")\n");

        if (!redOriginalConexa) {
            s.append("La red original ya estaba "
                   + "desconectada no tenía MST completo.\n");
        }

        if (tipo == TipoFallo.CON_REEMPLAZO) {

            s.append("Tipo de fallo: CON REEMPLAZO\n");
            s.append("Enlace de reemplazo: ")
             .append(enlaceReemplazo)
             .append(" (costo ").append(costoReemplazo)
             .append(")\n");
            s.append("Red conectada: ")
             .append(sigueConectada ? "SI" : "NO").append("\n");
            s.append("Costo original: ").append(costoOriginal)
             .append("\n");
            s.append("Costo nuevo: ").append(costoNuevo)
             .append("\n");
            s.append("Variación: ").append(signo(getVariacion()));

            if (!Double.isNaN(getVariacionPorcentual())) {
                s.append(String.format(
                    Locale.US, " (%+.1f%%)",
                    getVariacionPorcentual()
                ));
            }

            s.append("\n");

        } else {

            s.append("Tipo de fallo: DESCONECTA LA RED "
                   + "no hay enlace alternativo\n");
            s.append("Red conectada: NO. "
                   + "No existe un MST completo.\n");
            s.append("Costo original: ").append(costoOriginal)
             .append("\n");
            s.append("Costo de las aristas restantes: ")
             .append(costoNuevo)
             .append(" parcial, no comparable con el original\n");
            s.append("Variación: ").append(signo(getVariacion()))
             .append(" solo refleja el enlace perdido\n");
        }

        return s.toString();
    }

    /*
     * Imprime el informe en consola.
     */
    public void mostrarInforme() {
        System.out.print(resumen());
    }

    // Agrega "+" a los valores positivos.
    private String signo(double valor) {

        if (valor > 0) {
            return "+" + valor;
        }

        return String.valueOf(valor);
    }
}
