package proyectoib.g6.interfaz;

import java.io.File;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
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
 * INTERFAZ GRAFICA DE LA RED DE FIBRA OPTICA
 *
 * Permite cargar los archivos CSV, ejecutar Kruskal o Prim
 * y simular la falla de un enlace del arbol minimo.
 */
public class MainApp extends Application {

    // Rutas por defecto de los archivos de entrada
    private String rutaEdificios = "datos/edificios.csv";
    private String rutaConexiones = "datos/conexiones.csv";

    // Estado actual de la red y de los resultados calculados
    private Grafo red = null;
    private ResultadoMST mstActual = null;
    private Arista enlaceFallido = null;
    private Arista enlaceReemplazo = null;

    // Componentes visuales principales
    private Pane panelGrafo;
    private TextArea areaResultados;
    private ComboBox<String> comboEnlacesMST;
    private Label lblRutaEdificios;
    private Label lblRutaConexiones;

    @Override
    public void start(final Stage escenario) {

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(10));

        // 1. Barra superior para elegir y cargar los archivos CSV
        HBox panelSuperior = crearPanelCarga(escenario);
        raiz.setTop(panelSuperior);

        // 2. Lienzo central donde se dibujan los edificios y conexiones
        panelGrafo = new Pane();
        panelGrafo.setPrefSize(660, 480);
        panelGrafo.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC;");
        raiz.setCenter(panelGrafo);

        // 3. Columna derecha con los botones de algoritmos y sensibilidad
        VBox panelDerecho = crearPanelControles();
        raiz.setRight(panelDerecho);

        Scene escena = new Scene(raiz, 1020, 560);
        escenario.setTitle("Kruskal-Prim Grupo 6");
        escenario.setScene(escena);
        escenario.show();
    }

    // PANEL DE CARGA

    private HBox crearPanelCarga(final Stage escenario) {

        HBox barra = new HBox(10);
        barra.setPadding(new Insets(0, 0, 10, 0));

        Button btnEdificios = new Button("Elegir edificios.csv");
        lblRutaEdificios = new Label(rutaEdificios);

        Button btnConexiones = new Button("Elegir conexiones.csv");
        lblRutaConexiones = new Label(rutaConexiones);

        Button btnCargar = new Button("Cargar Red");

        // Abrir explorador de archivos para edificios.csv
        btnEdificios.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser selector = new FileChooser();
                selector.setTitle("Seleccionar edificios.csv");
                File archivo = selector.showOpenDialog(escenario);
                if (archivo != null) {
                    rutaEdificios = archivo.getAbsolutePath();
                    lblRutaEdificios.setText(archivo.getName());
                }
            }
        });

        // Abrir explorador de archivos para conexiones.csv
        btnConexiones.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser selector = new FileChooser();
                selector.setTitle("Seleccionar conexiones.csv");
                File archivo = selector.showOpenDialog(escenario);
                if (archivo != null) {
                    rutaConexiones = archivo.getAbsolutePath();
                    lblRutaConexiones.setText(archivo.getName());
                }
            }
        });

        // Leer los CSV seleccionados y mostrar la red en pantalla
        btnCargar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                cargarDatosCSV();
            }
        });

        barra.getChildren().addAll(
                btnEdificios, lblRutaEdificios,
                btnConexiones, lblRutaConexiones,
                btnCargar
        );

        return barra;
    }

    // PANEL DE CONTROLES

    private VBox crearPanelControles() {

        VBox columna = new VBox(10);
        columna.setPadding(new Insets(0, 0, 0, 12));
        columna.setPrefWidth(320);

        Label lblAlgoritmos = new Label(" Calcular Arbol Minimo");
        Button btnKruskal = new Button("Ejecutar Kruskal");
        Button btnPrim = new Button("Ejecutar Prim");
        btnKruskal.setMaxWidth(Double.MAX_VALUE);
        btnPrim.setMaxWidth(Double.MAX_VALUE);

        Label lblSensibilidad = new Label(" Simular Falla en Enlace del MST");
        comboEnlacesMST = new ComboBox<String>();
        comboEnlacesMST.setPromptText("Seleccione enlace del MST");
        comboEnlacesMST.setMaxWidth(Double.MAX_VALUE);

        Button btnSimularFalla = new Button("Analizar Falla");
        btnSimularFalla.setMaxWidth(Double.MAX_VALUE);

        Label lblLeyenda = new Label(
                "Colores: Gris = Red | Verde = MST \n" + " Rojo = Falla | Azul = Reemplazo" );
        lblLeyenda.setWrapText(true);

        // Cuadro de texto para mostrar costos, tiempos e informes
        areaResultados = new TextArea();
        areaResultados.setEditable(false);
        areaResultados.setWrapText(true);
        areaResultados.setPrefHeight(250);

        btnKruskal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ejecutarAlgoritmo("KRUSKAL");
            }
        });

        btnPrim.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ejecutarAlgoritmo("PRIM");
            }
        });

        btnSimularFalla.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                simularFallaEnlace();
            }
        });

        columna.getChildren().addAll(
                lblAlgoritmos, btnKruskal, btnPrim,
                lblSensibilidad, comboEnlacesMST, btnSimularFalla,
                lblLeyenda, areaResultados
        );

        return columna;
    }

    // OPERACIONES DEL GRAFO

    private void cargarDatosCSV() {

        try {
            // Reiniciar resultados anteriores al cargar una red nueva
            red = LectorCSV.cargarGrafo(rutaEdificios, rutaConexiones);
            mstActual = null;
            enlaceFallido = null;
            enlaceReemplazo = null;
            comboEnlacesMST.getItems().clear();

            dibujarGrafo();

            String texto = "--- Red cargada ---\n" + "Edificios: " + red.getNumVertices() + "\n" 
            + "Conexiones: " + red.getNumAristas() + "\n";

            areaResultados.setText(texto);
            System.out.println(" Red cargada: " + red.getNumVertices() + " edificios.");

        } catch (CSVInvalido e) {
            System.out.println(" Error al cargar CSV: " + e.getMessage());
            mostrarAlerta("Error al leer CSV", e.getMessage());
        }
    }

    private void ejecutarAlgoritmo(String nombreAlgoritmo) {

        if (red == null) {
            mostrarAlerta("Aviso", "Primero debe cargar los archivos CSV.");
            return;
        }

        // Limpiar simulaciones de falla previas
        enlaceFallido = null;
        enlaceReemplazo = null;

        // Medir tiempo de ejecucion del algoritmo elegido
        long inicio = System.nanoTime();

        if (nombreAlgoritmo.equals("KRUSKAL")) {
            mstActual = new Kruskal().ejecutarKruskal(red);
        } else {
            mstActual = new Prim().ejecutarPrim(red);
        }

        long fin = System.nanoTime();
        double tiempoMs = (fin - inicio) / 1000000.0;

        // Llenar la lista desplegable con los enlaces que quedaron en el MST
        comboEnlacesMST.getItems().clear();
        for (int i = 0; i < mstActual.getNumAristas(); i++) {
            Arista a = mstActual.getArista(i);
            String idOrigen = red.getVertice(a.getOrigen()).nomVertice();
            String idDestino = red.getVertice(a.getDestino()).nomVertice();
            comboEnlacesMST.getItems().add(idOrigen + " - " + idDestino);
        }

        dibujarGrafo();

        String estadoRed = "NO (Red desconectada)";
        if (mstActual.esConexo()) {
            estadoRed = "SI (MST completo)";
        }

        String texto = "--- Resultado " + nombreAlgoritmo + " ---\n"
                + "Red conexa: " + estadoRed + "\n"
                + "Enlaces del MST: " + mstActual.getNumAristas() + "\n"
                + String.format("Costo total: %.2f%n", mstActual.getCostoTotal())
                + String.format("Tiempo: %.4f ms%n", tiempoMs);

        System.out.printf("[OK] %s | Costo: %.2f | Tiempo: %.4f ms%n",
                nombreAlgoritmo, mstActual.getCostoTotal(), tiempoMs);

        areaResultados.setText(texto);
    }

    private void simularFallaEnlace() {

        if (red == null || mstActual == null) {
            mostrarAlerta("Aviso", "Primero cargue la red y ejecute Kruskal o Prim.");
            return;
        }

        int pos = comboEnlacesMST.getSelectionModel().getSelectedIndex();

        if (pos < 0) {
            mostrarAlerta("Aviso", "Seleccione un enlace del MST para simular su falla.");
            return;
        }

        // Obtener los edificios del enlace seleccionado y analizar su falla
        Arista elegida = mstActual.getArista(pos);
        String idOrigen = red.getVertice(elegida.getOrigen()).nomVertice();
        String idDestino = red.getVertice(elegida.getDestino()).nomVertice();

        AnalizadorSensibilidad an = new AnalizadorSensibilidad(red);
        InformeSensibilidad inf = an.analizar(idOrigen, idDestino);

        // Guardar enlace caido y buscar si entro uno nuevo en el MST
        enlaceFallido = elegida;
        enlaceReemplazo = buscarAristaNueva(mstActual, inf.getNuevoMST());

        dibujarGrafo();

        String texto = "--- Analisis de Sensibilidad ---\n"
                + "Enlace retirado: " + idOrigen + " - " + idDestino + "\n"
                + String.format("Costo original: %.2f%n", inf.getCostoOriginal());

        if (inf.getTipo() == TipoFallo.CON_REEMPLAZO && enlaceReemplazo != null) {
            String repOrigen = red.getVertice(enlaceReemplazo.getOrigen()).nomVertice();
            String repDestino = red.getVertice(enlaceReemplazo.getDestino()).nomVertice();

            texto = texto
                    + "Estado: Conexion con reemplazo \n"
                    + "Enlace nuevo: " + repOrigen + " - " + repDestino
                    + String.format(" (Costo: %.2f)%n", inf.getCostoReemplazo())
                    + String.format("Costo nuevo: %.2f%n", inf.getCostoNuevo())
                    + String.format("Variacion: +%.2f%n", inf.getVariacion());
        } else {
            texto = texto
                    + "Estado: Red desconectada\n"
                    + "No existe enlace alternativo para reconectar.\n";
        }

        System.out.println("[OK] Falla simulada en enlace " + idOrigen + " - " + idDestino);
        areaResultados.setText(texto);
    }

    // DIBUJO EN PANTALLA

    private void dibujarGrafo() {

        panelGrafo.getChildren().clear();

        if (red == null || red.getNumVertices() == 0) {
            return;
        }

        int n = red.getNumVertices();

        // Buscar coordenadas minimas y maximas para escalar el grafo al panel
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

        // Evitar division por cero si todos los nodos estan alineados
        if (maxX == minX) {
            maxX = minX + 1;
        }
        if (maxY == minY) {
            maxY = minY + 1;
        }

        double margen = 50.0;
        double anchoUtil = 660.0 - (2 * margen);
        double altoUtil = 480.0 - (2 * margen);

        // Convertir coordenadas del CSV a pixeles dentro del panel
        double[] posX = new double[n];
        double[] posY = new double[n];

        for (int i = 0; i < n; i++) {
            Vertice v = red.getVertice(i);
            posX[i] = margen + ((v.getX() - minX) / (maxX - minX)) * anchoUtil;
            posY[i] = margen + ((v.getY() - minY) / (maxY - minY)) * altoUtil;
        }

        // 1. Dibujar todas las conexiones y pintar segun su estado
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

            // Dibujar el costo en el punto medio de la linea
            double medioX = (posX[u] + posX[v]) / 2.0;
            double medioY = (posY[u] + posY[v]) / 2.0;
            Text textoCosto = new Text(medioX, medioY - 4, String.format("%.1f", a.getCosto()));
            textoCosto.setFill(Color.DIMGRAY);

            panelGrafo.getChildren().addAll(linea, textoCosto);
        }

        // 2. Dibujar los edificios encima de las lineas
        for (int i = 0; i < n; i++) {
            Vertice v = red.getVertice(i);

            Circle circulo = new Circle(posX[i], posY[i], 14);
            circulo.setFill(Color.WHITE);
            circulo.setStroke(Color.DARKSLATEGRAY);
            circulo.setStrokeWidth(2.0);

            Text etiqueta = new Text(posX[i] - 10, posY[i] + 4, v.nomVertice());
            Text nombre = new Text(posX[i] - 25, posY[i] - 18, v.getNombreEdificio());

            panelGrafo.getChildren().addAll(circulo, etiqueta, nombre);
        }
    }

    // METODOS AUXILIARES

    // Revisa si una arista forma parte del MST calculado
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

    // Compara dos aristas sin importar el orden de sus extremos
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

    // Busca la arista que entro al nuevo MST despues de simular una falla
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

    // Muestra una ventana emergente de aviso o error
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