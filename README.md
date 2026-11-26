# NóminaLibre

Estudiante: Carlos Daniel Ramírez Perilla
Variante asignada: Variante 3, Nómina NóminaLibre (patrón Observer)
Motor de base de datos: SQLite mediante JDBC
Asignatura: Programación 1 (docente Andrés Castaño)

Esta aplicación de escritorio hecha en Java Swing sirve para registrar trabajadores de planta, contratistas y empleados por horas, liquidar sus pagos mensuales y guardar el historial en una base de datos SQLite. El sistema calcula el pago base y el bono de antigüedad por polimorfismo según el tipo de contrato, valida los datos con excepciones propias y notifica cada liquidación con el patrón Observer para llevar el acumulado de contabilidad y lanzar alertas cuando un pago pasa de $8.000.000.

## Estructura del proyecto

El código está dividido en cinco paquetes dentro de src, además de la carpeta sql con el script de creación de tablas y la carpeta lib con el driver de SQLite:

- modelo: contiene la clase abstracta Trabajador, las subclases EmpleadoPlanta, Contratista y EmpleadoPorHoras, la interfaz Bonificable (implementada solo por planta y por horas) y la clase Pago.
- excepciones: incluye DocumentoInvalidoException, SalarioInvalidoException y HorasInvalidasException.
- patron: implementa el patrón Observer con PagoObserver, LiquidadorNomina, ContabilidadObserver y AlertaObserver.
- persistencia: maneja la conexión y el acceso a datos con ConexionSQLite, NominaDAO y NominaDAOImpl, usando PreparedStatement y try-with-resources.
- vista: contiene VentanaNomina, la interfaz en Swing para ver, registrar, editar, eliminar y ejecutar las consultas de negocio.
- sql/schema.sql: script SQL con la definición de las tablas trabajadores y pagos.

## Cómo ejecutar la aplicación en Apache NetBeans

1. Descarga o clona este repositorio en tu equipo.
2. Abre Apache NetBeans, ve a File > Open Project y selecciona la carpeta NominaLibre.
3. Haz clic derecho sobre el proyecto, entra a Properties > Libraries > Classpath y revisa que esté enlazado el archivo lib/sqlite-jdbc.jar. Si aparece alguna advertencia de ruta, bórralo de la lista y agrégalo de nuevo desde la carpeta lib del proyecto.
4. En Source Packages, abre el paquete vista, haz clic derecho sobre VentanaNomina.java y selecciona Run File (o presiona Shift + F6).

Al abrir la ventana por primera vez, el programa crea automáticamente el archivo nomina_libre.db con sus tablas en la raíz del proyecto.
