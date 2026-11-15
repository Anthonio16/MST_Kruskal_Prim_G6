package proyectoib.g6.interfaz;

import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import proyectoib.g6.algoritmos.AnalizadorSensibilidad;
import proyectoib.g6.algoritmos.Kruskal;
import proyectoib.g6.algoritmos.Prim;
import proyectoib.g6.datos.CSVInvalido;
import proyectoib.g6.datos.LectorCSV;
import proyectoib.g6.modelo.Arista;
import proyectoib.g6.modelo.Grafo;
import proyectoib.g6.modelo.InformeSensibilidad;
import proyectoib.g6.modelo.InformeSensibilidad.TipoFallo;
import proyectoib.g6.modelo.ResultadoMST;
import proyectoib.g6.modelo.Vertice;

/*
 * Interfaz grafica para cargar redes desde CSV, calcular el arbol
 * de expansion minima con Kruskal o Prim 
 */
public class MainApp extends Application {

    // Area de dibujo.
    private static final double ancho = 660;
    private static final double alto = 480;

    // Limites para dibujar 
    private static final int maxNodos = 100;
    private static final int maxAristas = 400;

    // Cantidad maxima de enlaces
    private static final int maxLista = 50;

    // Rutas iniciales de los archivos
    private String rutaEdificios = "datos/edificios.csv";
    private String rutaConexiones = "datos/conexiones.csv";

    // Datos actuales de la red y resultados
    private Grafo red = null;
    private ResultadoMST mstActual = null;
    private AnalizadorSensibilidad analizador = null;
    private Arista enlaceFallido = null;
    private Arista enlaceReemplazo = null;

    private Pane lienzo;
    private TextArea cuadroTexto;
    private ComboBox<String> listaEnlaces;
    private Label lblEdificios;
    private Label lblConexiones;

    // Muestra la ventana principal.
    @Override
    public void start(final Stage escenario) {

        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-padding: 10;");

        raiz.setTop(crearPanelCarga(escenario));

        lienzo = new Pane();
        lienzo.setPrefSize(ancho, alto);
        lienzo.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC;");
        raiz.setCenter(lienzo);

        raiz.setRight(crearPanelControles());

        Scene escena = new Scene(raiz, 1020, 560);
        escenario.setTitle("Kruskal-Prim Grupo 6");
        escenario.setScene(escena);
        escenario.show();
    }

    // Barra superior para elegir y cargar los archivos CSV
    private HBox crearPanelCarga(final Stage escenario) {

        HBox barra = new HBox(10);
        barra.setStyle("-fx-padding: 0 0 10 0;");

        Button btnEdificios = new Button("Elegir edificios.csv");
        lblEdificios = new Label(rutaEdificios);

        Button btnConexiones = new Button("Elegir conexiones.csv");
        lblConexiones = new Label(rutaConexiones);

        Button btnCargar = new Button("Cargar Red");

        btnEdificios.setOnAction(e -> {

            File archivo = elegirArchivo(escenario, "Seleccionar edificios.csv");

            if (archivo != null) {
                rutaEdificios = archivo.getAbsolutePath();
                lblEdificios.setText(archivo.getName());
            }
        });

        btnConexiones.setOnAction(e -> {

            File archivo = elegirArchivo(escenario, "Seleccionar conexiones.csv");

            if (archivo != null) {
                rutaConexiones = archivo.getAbsolutePath();
                lblConexiones.setText(archivo.getName());
            }
        });

        btnCargar.setOnAction(e -> cargarDatosCSV());

        barra.getChildren().addAll(
                btnEdificios, lblEdificios,
                btnConexiones, lblConexiones,
                btnCargar
        );

        return barra;
    }

    // Crea el panel derecho con botones
    private VBox crearPanelControles() {

        VBox columna = new VBox(10);
        columna.setStyle("-fx-padding: 0 0 0 12;");
        columna.setPrefWidth(320);

        Label lblAlgoritmos = new Label("Calcular Arbol Minimo");
        Button btnKruskal = new Button("Ejecutar Kruskal");
        Button btnPrim = new Button("Ejecutar Prim");
        btnKruskal.setMaxWidth(Double.MAX_VALUE);
        btnPrim.setMaxWidth(Double.MAX_VALUE);

        Label lblFalla = new Label("Simular Falla en Enlace del MST - Kruskal");
        listaEnlaces = new ComboBox<String>();
        listaEnlaces.setPromptText("Seleccione enlace del MST");
        listaEnlaces.setMaxWidth(Double.MAX_VALUE);

        Button btnFalla = new Button("Analizar Falla");
        btnFalla.setMaxWidth(Double.MAX_VALUE);

        Label lblLeyenda = new Label( "Colores: Gris = Red | Verde = MST\n" + "Rojo = Falla | Azul = Reemplazo");
        lblLeyenda.setWrapText(true);

        cuadroTexto = new TextArea();
        cuadroTexto.setEditable(false);
        cuadroTexto.setWrapText(true);
        cuadroTexto.setPrefHeight(250);

        btnKruskal.setOnAction(e -> ejecutarAlgoritmo("KRUSKAL"));
        btnPrim.setOnAction(e -> ejecutarAlgoritmo("PRIM"));
        btnFalla.setOnAction(e -> simularFallaEnlace());

        columna.getChildren().addAll( lblAlgoritmos, btnKruskal, btnPrim, lblFalla, listaEnlaces, btnFalla,
                lblLeyenda, cuadroTexto
        );

        return columna;
    }

    // Abre el explorador de archivos y devuelve el archivo elegido
    private File elegirArchivo(Stage escenario, String titulo) {

        FileChooser selector = new FileChooser();
        selector.setTitle(titulo);

        return selector.showOpenDialog(escenario);
    }

    // Lee los CSV, limpia resultados anteriores, prepara sensibilidad y dibuja
    private void cargarDatosCSV() {

        try {

            red = LectorCSV.cargarGrafo(rutaEdificios, rutaConexiones);
            mstActual = null;
            enlaceFallido = null;
            enlaceReemplazo = null;

            analizador = new AnalizadorSensibilidad(red);
            llenarComboEnlaces();
            dibujarGrafo();

            cuadroTexto.setText( "--- Red cargada ---\n" + "Edificios: " + red.getNumVertices() + "\n" 
            + "Conexiones: " + red.getNumAristas() + "\n");

        } catch (CSVInvalido e) {

            mostrarAlerta("Error al leer CSV", e.getMessage());

        } catch (Exception e) {

            mostrarAlerta("Error", "No se pudo cargar la red: " + e.getMessage());
        }
    }

    // Carga los enlaces del MST base en la lista desplegable
    private void llenarComboEnlaces() {

        listaEnlaces.getItems().clear();
        ResultadoMST mst = analizador.getMstOriginal();

        for (int i = 0; i < mst.getNumAristas(); i++) {

            Arista a = mst.getArista(i);
            String origen = red.getVertice(a.getOrigen()).nomVertice();
            String destino = red.getVertice(a.getDestino()).nomVertice();

            listaEnlaces.getItems().add(
                    origen + " - " + destino + " | costo: " + a.getCosto());
        }
    }

    // Ejecuta Kruskal o Prim midiendo el tiempo del calculo
    private void ejecutarAlgoritmo(String algoritmo) {

        if (red == null) {
            mostrarAlerta("Aviso", "Primero debe cargar los archivos CSV.");
            return;
        }

        try {

            enlaceFallido = null;
            enlaceReemplazo = null;

            ResultadoMST resultado;
            long inicio = System.nanoTime();

            if (algoritmo.equals("KRUSKAL")) {
                resultado = new Kruskal().ejecutarKruskal(red);
            } else {
                resultado = new Prim().ejecutarPrim(red);
            }

            double tiempoMs = (System.nanoTime() - inicio) / 1000000.0;

            mstActual = resultado;
            dibujarGrafo();
            mostrarResultados(algoritmo, resultado, tiempoMs);

        } catch (Exception e) {

            mostrarAlerta("Error inesperado", "No se pudo ejecutar el algoritmo: " + e.getMessage());
        }
    }

    // Muestra en el cuadro de texto el costo, conectividad, tiempo y enlaces
    private void mostrarResultados(String algoritmo,
                                   ResultadoMST resultado,
                                   double tiempoMs) {

        String estado = "NO - solo existe un bosque minimo";

        if (resultado.esConexo()) {
            estado = "SI - MST completo";
        }

        double costo = Math.round(resultado.getCostoTotal() * 100.0) / 100.0;
        double tiempo = Math.round(tiempoMs * 10000.0) / 10000.0;

        String texto = "--- Resultado " + algoritmo + " ---\n" + "Red conexa: " + estado + "\n"
                + "Enlaces del MST: " + resultado.getNumAristas() + "\n" + "Costo total: " + costo + "\n"
                + "Tiempo de ejecucion: " + tiempo + " ms\n" + "\nEnlaces seleccionados:\n";

        int limite = resultado.getNumAristas();

        if (limite > maxLista) {
            limite = maxLista;
        }

        for (int i = 0; i < limite; i++) {

            Arista a = resultado.getArista(i);
            String origen = red.getVertice(a.getOrigen()).nomVertice();
            String destino = red.getVertice(a.getDestino()).nomVertice();

            texto = texto + origen + " - " + destino + " | costo: " + a.getCosto() + "\n";
        }

        if (resultado.getNumAristas() > maxLista) {
            texto = texto + "... y " + (resultado.getNumAristas() - maxLista) + " enlaces mas\n";
        }

        cuadroTexto.setText(texto);
    }

    // Analiza la eliminacion del enlace elegido 
    private void simularFallaEnlace() {

        if (red == null || analizador == null) {
            mostrarAlerta("Aviso", "Primero cargue la red.");
            return;
        }

        if (listaEnlaces.getItems().isEmpty()) {
            mostrarAlerta("Aviso", "El MST no tiene enlaces para analizar.");
            return;
        }

        int pos = listaEnlaces.getSelectionModel().getSelectedIndex();

        if (pos < 0) {
            mostrarAlerta("Aviso", "Seleccione un enlace del MST para simular su falla.");
            return;
        }

        try {

            InformeSensibilidad informe = analizador.analizar(pos);
            ResultadoMST mstBase = analizador.getMstOriginal();

            mstActual = mstBase;
            enlaceFallido = mstBase.getArista(pos);
            enlaceReemplazo = null;

            if (informe.getTipo() == TipoFallo.CON_REEMPLAZO) {
                enlaceReemplazo = buscarAristaNueva(mstBase, informe.getNuevoMST());
            }

            dibujarGrafo();

            cuadroTexto.setText( "Se analizo el MST de Kruskal.\n\n" + informe.resumen());

        } catch (Exception e) {

            mostrarAlerta("Error", "No se pudo analizar el enlace: " + e.getMessage());
        }
    }

    // Dibuja los edificios y conexiones ajustados al tamanio
    private void dibujarGrafo() {

        lienzo.getChildren().clear();

        if (red == null || red.getNumVertices() == 0) {
            return;
        }

        if (red.getNumVertices() > maxNodos || red.getNumAristas() > maxAristas) {

            Text aviso = new Text(20, 30, "La red es muy grande para dibujarla: " + red.getNumVertices() + " edificios y "
                    + red.getNumAristas() + " conexiones.\n" + "Los resultados se ven en el cuadro de texto.");

            lienzo.getChildren().add(aviso);
            return;
        }

        int n = red.getNumVertices();

        // Buscar coordenadas 
        double minX = red.getVertice(0).getX();
        double maxX = minX;
        double minY = red.getVertice(0).getY();
        double maxY = minY;

        for (int i = 1; i < n; i++) {

            Vertice v = red.getVertice(i);

            if (v.getX() < minX) {
                minX = v.getX();
            }
            if (v.getX() > maxX) {
                maxX = v.getX();
            }
            if (v.getY() < minY) {
                minY = v.getY();
            }
            if (v.getY() > maxY) {
                maxY = v.getY();
            }
        }

        if (maxX == minX) {
            maxX = minX + 1;
        }
        if (maxY == minY) {
            maxY = minY + 1;
        }

        // Convertir coordenadas 
        double margen = 50.0;
        double anchoUtil = ancho - (2 * margen);
        double altoUtil = alto - (2 * margen);

        double[] posX = new double[n];
        double[] posY = new double[n];

        for (int i = 0; i < n; i++) {

            Vertice v = red.getVertice(i);
            posX[i] = margen + ((v.getX() - minX) / (maxX - minX)) * anchoUtil;
            posY[i] = margen + ((v.getY() - minY) / (maxY - minY)) * altoUtil;
        }

        // Dibujar las conexiones
        for (int i = 0; i < red.getNumAristas(); i++) {

            Arista a = red.getArista(i);
            int u = a.getOrigen();
            int v = a.getDestino();

            Line linea = new Line(posX[u], posY[u], posX[v], posY[v]);
            linea.setStroke(Color.LIGHTGRAY);
            linea.setStrokeWidth(1.5);

            if (mismaArista(a, enlaceFallido)) {
                linea.setStroke(Color.RED);
                linea.setStrokeWidth(3.5);
            } else if (mismaArista(a, enlaceReemplazo)) {
                linea.setStroke(Color.DODGERBLUE);
                linea.setStrokeWidth(3.5);
            } else if (estaEnMST(a)) {
                linea.setStroke(Color.SEAGREEN);
                linea.setStrokeWidth(3.0);
            }

            double medioX = (posX[u] + posX[v]) / 2.0;
            double medioY = (posY[u] + posY[v]) / 2.0;
            double costoLinea = Math.round(a.getCosto() * 10.0) / 10.0;

            Text textoCosto = new Text(medioX, medioY - 4, "" + costoLinea);
            textoCosto.setFill(Color.DIMGRAY);

            lienzo.getChildren().addAll(linea, textoCosto);
        }

        // Dibujar los edificios 
        for (int i = 0; i < n; i++) {

            Vertice v = red.getVertice(i);

            Circle circulo = new Circle(posX[i], posY[i], 14);
            circulo.setFill(Color.WHITE);
            circulo.setStroke(Color.DARKSLATEGRAY);
            circulo.setStrokeWidth(2.0);

            Text etiqueta = new Text(posX[i] - 10, posY[i] + 4, v.nomVertice());
            Text nombre = new Text(posX[i] - 25, posY[i] - 18, v.getNombreEdificio());

            lienzo.getChildren().addAll(circulo, etiqueta, nombre);
        }
    }

    // La arista pertenece al MST actual
    private boolean estaEnMST(Arista arista) {

        if (mstActual == null) {
            return false;
        }

        for (int i = 0; i < mstActual.getNumAristas(); i++) {

            if (mismaArista(arista, mstActual.getArista(i))) {
                return true;
            }
        }

        return false;
    }

    // Compara dos aristas  
    private boolean mismaArista(Arista a, Arista b) {

        if (a == null || b == null) {
            return false;
        }

        boolean directo = a.getOrigen() == b.getOrigen()
                && a.getDestino() == b.getDestino();

        boolean inverso = a.getOrigen() == b.getDestino()
                && a.getDestino() == b.getOrigen();

        return directo || inverso;
    }

    // Busca la arista que reemplaza al enlace eliminado.
    private Arista buscarAristaNueva(ResultadoMST anterior, ResultadoMST nuevo) {

        if (anterior == null || nuevo == null) {
            return null;
        }

        for (int i = 0; i < nuevo.getNumAristas(); i++) {

            Arista candidata = nuevo.getArista(i);
            boolean encontrada = false;

            for (int j = 0; j < anterior.getNumAristas(); j++) {

                if (mismaArista(candidata, anterior.getArista(j))) {
                    encontrada = true;
                }
            }

            if (!encontrada) {
                return candidata;
            }
        }

        return null;
    }

    // Muestra una ventana de aviso
    private void mostrarAlerta(String titulo, String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}