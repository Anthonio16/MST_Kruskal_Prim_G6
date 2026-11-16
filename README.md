# **Proyecto Integrador IB — Grupo 6 - Kruskal y Prim**

Aplicación en Java y JavaFX para calcular el Árbol de Expansión Mínima (MST) mediante los algoritmos de Kruskal y Prim, analizar fallas de enlaces y comparar su rendimiento.

## **1. Versión de Java y Dependencias**

Versión de Java: JDK 22 o superior.

IDE: Eclipse

Dependencias:

JavaFX - javafx-controls, versión 24 o superior gestionado automáticamente mediante Maven (pom.xml)

Bibliotecas estándar de Java para el modelo, algoritmos y medición de rendimiento

## **2. Formato de los Archivos CSV de Entrada**

El sistema requiere dos archivos separados por comas con encabezado en la primera fila:

datos/edificios.csv — Vértices

Columnas: id,nombre,x,y - identificador único, nombre del edificio y coordenadas para graficar

id,nombre,x,y

A,Biblioteca,100,120

B,Laboratorio,260,90

C,Rectorado,420,150

datos/conexiones.csv — Aristas

Columnas: origen,destino,costo - identificadores existentes en edificios.csv y peso mayor o igual a 0

origen,destino,costo

A,B,4.5

B,C,3.0

A,C,8.0

El lector de archivos CSV rechaza archivos vacíos, columnas faltantes, costos negativos, bucles y enlaces duplicados

## **3. Instrucciones de Compilación y Ejecución en Eclipse**

Importar el proyecto:

En Eclipse, vaya a File > Import > Maven > Existing Maven Projects.

Seleccione la carpeta del proyecto donde se encuentra el archivo pom.xml y haga clic en Finish.

Haga clic derecho sobre el proyecto en el Package Explorer y seleccione Run

Ejecutar la Interfaz Gráfica:

Haga clic derecho sobre el proyecto, seleccione el package proyectoib.g6.interfaz, seleccione la clase Lanzador.java y seleccione Run

## **4. Cómo Reproducir un Experimento Básico**

En el Package Explorer de Eclipse, abra el paquete proyectoib.g6.experimentos.

Haga clic derecho sobre la clase MedidorRendimiento.java y seleccione Run

El programa medirá Kruskal y Prim, con 10 corridas de calentamiento, mediana de 30 repeticiones de tiempo y memoria con MemoryMXBean sobre la red base y sobre redes dispersas y densas de 10 a 1000 nodos.

Al finalizar, los datasets generados quedarán guardados en datos/generados/ y la tabla comparativa se escribirá en resultados/rendimiento.csv.
