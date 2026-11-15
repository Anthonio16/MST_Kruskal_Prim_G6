Proyecto Integrador IB — Grupo 6 - Kruskal y Prim

Aplicación en Java y JavaFX para calcular el Árbol de Expansión Mínima (MST) mediante los algoritmos de Kruskal y Prim, analizar fallas de enlaces y comparar su rendimiento.

1. Versión de Java y Dependencias

Entorno de desarrollo: Eclipse IDE for Java Developers.

Versión de Java: JDK 17 o superior.

Dependencias:

JavaFX SDK 17 o superior, necesario para ejecutar la interfaz gráfica.

Bibliotecas estándar de Java para el modelo, algoritmos y medición de rendimiento.

2. Formato de los Archivos CSV de Entrada

El sistema requiere dos archivos separados por comas con encabezado en la primera fila:

datos/edificios.csv - Vértices

Columnas: id,nombre,x,y - identificador único, nombre del edificio y coordenadas para graficar.

id,nombre,x,y
A,Biblioteca,100,120
B,Laboratorio,260,90
C,Rectorado,420,150


datos/conexiones.csv - Aristas

Columnas: origen,destino,costo - identificadores existentes en edificios.csv y peso mayor o igual a 0.

origen,destino,costo
A,B,4.5
B,C,3.0
A,C,8.0


El lector rechaza archivos vacíos, columnas faltantes, costos negativos, bucles y enlaces duplicados.

3. Instrucciones de Compilación y Ejecución en Eclipse

Importar el proyecto: 
Abra Eclipse, vaya a File > Import > Existing Projects into Workspace (o Maven > Existing Maven Projects) y seleccione la carpeta proyecto7/.

Configurar JavaFX: Si no usa Maven, haga clic derecho sobre el proyecto > Build Path > Configure Build Path... > Libraries > Classpath > Add External JARs... y agregue los archivos .jar de la carpeta lib de su JavaFX SDK.

Compilar: Eclipse compila el proyecto automáticamente al guardar (Project > Build Automatically).

Ejecutar la interfaz gráfica:

En el Package Explorer, abra src/main/java/proyectoib/g6/interfaz/Lanzador.java.

Haga clic derecho sobre el archivo y seleccione Run As > Java Application.

4. Cómo Reproducir un Experimento Básico

En el Package Explorer de Eclipse, ubique src/main/java/proyectoib/g6/experimentos/MedidorRendimiento.java.

Haga clic derecho sobre la clase y seleccione Run As > Java Application (no requiere JavaFX).

El programa medirá Kruskal y Prim (con 10 corridas de calentamiento, mediana de 30 repeticiones de tiempo y memoria con MemoryMXBean) sobre la red base y sobre redes dispersas y densas de 10 a 1000 nodos.

Al finalizar, presione F5 (Refresh) sobre el proyecto en Eclipse para ver los datasets generados en datos/generados/ y la tabla comparativa en resultados/rendimiento.csv.
