package proyectoib.g6.datos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import proyectoib.g6.modelo.Grafo;

/*
 * CLASE LECTORCSV
 *
 * Lee los archivos edificios.csv y conexiones.csv
 * y guarda los datos dentro de un objeto Grafo.
 */
public class LectorCSV {

    public static Grafo cargarGrafo(String rutaEdificios, String rutaConexiones)
            throws CSVInvalido{

        Grafo grafo = new Grafo();

        leerEdificios(rutaEdificios, grafo);
        leerConexiones(rutaConexiones, grafo);

        System.out.printf("Grafo cargado con %d edificios y %d conexiones.%n", grafo.getNumVertices(), grafo.getNumAristas());

        return grafo;
    }

    private static void leerEdificios(String ruta, Grafo grafo)
            throws CSVInvalido {

        File archivo = new File(ruta);

        if (!archivo.exists()) {
            throw new CSVInvalido(ruta, 0, "El archivo no existe");
        }

        Scanner lector = null;
        int numLinea = 0;

        try {
            lector = new Scanner(archivo, "UTF-8");

            while (lector.hasNextLine()) {
                numLinea = numLinea + 1;
                String linea = lector.nextLine();
                linea = linea.trim();

                // Quitar caracter oculto si el archivo viene de Excel
                if (linea.startsWith("\uFEFF")) {
                    linea = linea.substring(1);
                }

                if (linea.isEmpty()) {
                    continue;
                }

                String[] datos = linea.split(",", -1);

                // Saltar la primera fila si es el encabezado
                if (numLinea == 1 && datos[0].trim().equalsIgnoreCase("id")) {
                    continue;
                }

                if (datos.length != 4) {
                    throw new CSVInvalido(ruta, numLinea,
                            "La fila debe tener 4 columnas: id, nombre, x, y");
                }

                String id = datos[0].trim();
                String nombre = datos[1].trim();

                if (nombre.isEmpty()) {
                    throw new CSVInvalido(ruta, numLinea,
                            "El nombre del edificio no puede estar vacio");
                }

                double x = Double.parseDouble(datos[2].trim());
                double y = Double.parseDouble(datos[3].trim());

                grafo.nuevoVertice(id, nombre, x, y);
            }

            if (grafo.getNumVertices() == 0) {
                throw new CSVInvalido(ruta, 0, "El archivo esta vacio");
            }

        } catch (FileNotFoundException e) {
            throw new CSVInvalido(ruta, 0, "No se pudo abrir el archivo");
        } catch (NumberFormatException e) {
            throw new CSVInvalido(ruta, numLinea,
                    "Las coordenadas x, y deben ser numeros");
        } catch (IllegalArgumentException e) {
            throw new CSVInvalido(ruta, numLinea, e.getMessage());
        } finally {
            if (lector != null) {
                lector.close();
            }
        }
    }

    private static void leerConexiones(String ruta, Grafo grafo)
            throws CSVInvalido {

        File archivo = new File(ruta);

        if (!archivo.exists()) {
            throw new CSVInvalido(ruta, 0, "El archivo no existe");
        }

        Scanner lector = null;
        int numLinea = 0;

        try {
            lector = new Scanner(archivo, "UTF-8");

            while (lector.hasNextLine()) {
                numLinea = numLinea + 1;
                String linea = lector.nextLine();
                linea = linea.trim();

                if (linea.startsWith("\uFEFF")) {
                    linea = linea.substring(1);
                }

                if (linea.isEmpty()) {
                    continue;
                }

                String[] datos = linea.split(",", -1);

                // Saltar la primera fila si es el encabezado
                if (numLinea == 1 && datos[0].trim().equalsIgnoreCase("origen")) {
                    continue;
                }

                if (datos.length != 3) {
                    throw new CSVInvalido(ruta, numLinea,
                            "La fila debe tener 3 columnas: origen, destino, costo");
                }

                String origen = datos[0].trim();
                String destino = datos[1].trim();
                double costo = Double.parseDouble(datos[2].trim());

                // Grafo revisa si los edificios existen o si el costo es negativo
                grafo.nuevaArista(origen, destino, costo);
            }

        } catch (FileNotFoundException e) {
            throw new CSVInvalido(ruta, 0, "No se pudo abrir el archivo");
        } catch (NumberFormatException e) {
            throw new CSVInvalido(ruta, numLinea, "El costo debe ser un numero");
        } catch (IllegalArgumentException e) {
            throw new CSVInvalido(ruta, numLinea, e.getMessage());
        } finally {
            if (lector != null) {
                lector.close();
            }
        }
    }
}