package proyectoib.g6.datos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import proyectoib.g6.modelo.Grafo;

/*
 * CLASE CARGADOR GRAFO
 *
 * Lee los archivos CSV, valida sus datos
 * y construye un Grafo listo para usarse
 * con Kruskal, Prim o la interfaz.
 */
public class CargadorGrafo {

    /*
     * METODO PRINCIPAL: CARGAR LA RED
     * Si algo está mal lanza CSVInvalidoException
     * indicando archivo y línea del problema.
     */
    public static Grafo cargar(String rutaEdificios,
                               String rutaConexiones)
            throws CSVInvalidoException {

        Grafo grafo = new Grafo();

        cargarEdificios(grafo, rutaEdificios);
        cargarConexiones(grafo, rutaConexiones);

        return grafo;
    }

     // LEER EDIFICIOS
       
    private static void cargarEdificios(Grafo grafo,
                                        String ruta)
            throws CSVInvalidoException {

        String[] cabecera = {"id", "nombre", "x", "y"};

        try (BufferedReader lector = abrir(ruta)) {

            boolean cabeceraLeida = false;
            int numLinea = 0;
            String linea;

            while ((linea = lector.readLine()) != null) {

                numLinea++;

                if (numLinea == 1) {
                    linea = quitarBOM(linea);
                }

                // Ignorar líneas en blanco.
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] c = dividir(linea);

                // La primera línea con contenido
                // debe ser la cabecera.
                if (!cabeceraLeida) {
                    validarCabecera(c, cabecera, ruta, numLinea);
                    cabeceraLeida = true;
                    continue;
                }

                if (c.length != 4) {
                    throw new CSVInvalidoException(
                        ruta, numLinea,
                        "Se esperaban 4 columnas "
                        + "(id,nombre,x,y) pero hay " + c.length
                    );
                }

                String id = c[0];
                String nombre = c[1];

                if (id.isEmpty()) {
                    throw new CSVInvalidoException(
                        ruta, numLinea,
                        "El id del edificio está vacío"
                    );
                }

                if (nombre.isEmpty()) {
                    throw new CSVInvalidoException(
                        ruta, numLinea,
                        "El nombre del edificio '" + id
                        + "' está vacío"
                    );
                }

                if (grafo.numVertice(id) != -1) {
                    throw new CSVInvalidoException(
                        ruta, numLinea,
                        "El id '" + id + "' está repetido"
                    );
                }

                double x = leerNumero(c[2], "x", ruta, numLinea);
                double y = leerNumero(c[3], "y", ruta, numLinea);

                grafo.nuevoVertice(id, nombre, x, y);
            }

            if (!cabeceraLeida) {
                throw new CSVInvalidoException(
                    ruta, 0, "El archivo está vacío"
                );
            }

            if (grafo.getNumVertices() == 0) {
                throw new CSVInvalidoException(
                    ruta, 0, "No contiene ningún edificio"
                );
            }

        } catch (IOException e) {
            throw new CSVInvalidoException(
                ruta, 0,
                "Error al leer el archivo: " + e.getMessage()
            );
        }
    }

    // LEER CONEXIONES
    private static void cargarConexiones(Grafo grafo,
                                         String ruta)
            throws CSVInvalidoException {

        String[] cabecera = {"origen", "destino", "costo"};

        try (BufferedReader lector = abrir(ruta)) {

            boolean cabeceraLeida = false;
            int numLinea = 0;
            String linea;

            while ((linea = lector.readLine()) != null) {

                numLinea++;

                if (numLinea == 1) {
                    linea = quitarBOM(linea);
                }

                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] c = dividir(linea);

                if (!cabeceraLeida) {
                    validarCabecera(c, cabecera, ruta, numLinea);
                    cabeceraLeida = true;
                    continue;
                }

                if (c.length != 3) {
                    throw new CSVInvalidoException(
                        ruta, numLinea,
                        "Se esperaban 3 columnas "
                        + "(origen,destino,costo) pero hay "
                        + c.length
                    );
                }

                String origen = c[0];
                String destino = c[1];

                if (grafo.numVertice(origen) == -1) {
                    throw new CSVInvalidoException(
                        ruta, numLinea,
                        "El edificio '" + origen
                        + "' no existe en edificios.csv"
                    );
                }

                if (grafo.numVertice(destino) == -1) {
                    throw new CSVInvalidoException(
                        ruta, numLinea,
                        "El edificio '" + destino
                        + "' no existe en edificios.csv"
                    );
                }

                double costo =
                    leerNumero(c[2], "costo", ruta, numLinea);

                try {
                    grafo.nuevaArista(origen, destino, costo);

                } catch (IllegalArgumentException e) {
                    throw new CSVInvalidoException(
                        ruta, numLinea,
                        e.getMessage() + " (" + origen
                        + " - " + destino + ")"
                    );
                }
            }

            if (!cabeceraLeida) {
                throw new CSVInvalidoException(
                    ruta, 0, "El archivo está vacío"
                );
            }

        } catch (IOException e) {
            throw new CSVInvalidoException(
                ruta, 0,
                "Error al leer el archivo: " + e.getMessage()
            );
        }
    }


   // Abre el archivo en UTF-8
    private static BufferedReader abrir(String ruta)
            throws CSVInvalidoException {

        File archivo = new File(ruta);

        if (!archivo.isFile()) {
            throw new CSVInvalidoException(
                ruta, 0,
                "El archivo no existe: " + archivo.getAbsolutePath()
            );
        }

        try {
            return new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(archivo),
                    StandardCharsets.UTF_8
                )
            );

        } catch (IOException e) {
            throw new CSVInvalidoException(
                ruta, 0,
                "No se pudo abrir el archivo: " + e.getMessage()
            );
        }
    }

    private static String quitarBOM(String linea) {

        if (!linea.isEmpty() && linea.charAt(0) == '﻿') {
            return linea.substring(1);
        }

        return linea;
    }

    /*
     * Separa la línea por comas y elimina los
     * espacios alrededor de cada campo.
     *
     * El -1 conserva los campos vacíos del final
     * ("A,B," produce 3 campos, el último vacío).
     */
    private static String[] dividir(String linea) {

        String[] campos = linea.split(",", -1);

        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].trim();
        }

        return campos;
    }

    /*
     * Comprueba que la cabecera tenga exactamente
     * las columnas esperadas y en el mismo orden.
     */
    private static void validarCabecera(String[] encontrada,
                                        String[] esperada,
                                        String ruta,
                                        int numLinea)
            throws CSVInvalidoException {

        boolean correcta = encontrada.length == esperada.length;

        for (int i = 0; correcta && i < esperada.length; i++) {
            correcta = encontrada[i].equalsIgnoreCase(esperada[i]);
        }

        if (!correcta) {

            String texto = "";

            for (int i = 0; i < esperada.length; i++) {
                texto = texto + (i > 0 ? "," : "") + esperada[i];
            }

            throw new CSVInvalidoException(
                ruta, numLinea,
                "La cabecera debe ser: " + texto
            );
        }
    }

    /*
     * Solo acepta enteros o decimales con punto
     * y sin signo (4, 4.5, 120).
     */
    private static double leerNumero(String texto,
                                     String campo,
                                     String ruta,
                                     int numLinea)
            throws CSVInvalidoException {

        if (!texto.matches("\\d+(\\.\\d+)?")) {
            throw new CSVInvalidoException(
                ruta, numLinea,
                "'" + texto + "' no es válido en la columna "
                + campo + " (debe ser un número no negativo, "
                + "por ejemplo 4 o 4.5)"
            );
        }

        double valor = Double.parseDouble(texto);

        if (!Double.isFinite(valor)) {
            throw new CSVInvalidoException(
                ruta, numLinea,
                "El valor de la columna " + campo
                + " es demasiado grande"
            );
        }

        return valor;
    }
}
