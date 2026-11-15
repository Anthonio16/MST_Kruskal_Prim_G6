package proyectoib.g6.datos;

import java.io.File;

/*
 * Se lanza cuando un archivo CSV no existe,
 * está vacío o contiene datos incorrectos.
 */

public class CSVInvalidoException extends Exception {

    private final String archivo;
    private final int linea;

  //Constructor
    public CSVInvalidoException(String ruta,
                                int linea,
                                String detalle) {

        super(construirMensaje(ruta, linea, detalle));

        this.archivo = new File(ruta).getName();
        this.linea = linea;
    }

    private static String construirMensaje(
            String ruta, int linea, String detalle) {

        String nombre = new File(ruta).getName();

        if (linea > 0) {
            return "[" + nombre + ", línea " + linea + "] "
                   + detalle;
        }

        return "[" + nombre + "] " + detalle;
    }

    public String getArchivo() {
        return archivo;
    }

    public int getLinea() {
        return linea;
    }
}
