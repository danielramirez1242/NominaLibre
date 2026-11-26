package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionSQLite {
    private static final String URL = "jdbc:sqlite:nomina_libre.db";

    public static Connection conectar() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void inicializarTablas() throws SQLException {
        String sqlTrabajadores = """
            CREATE TABLE IF NOT EXISTS trabajadores (
                documento TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                fecha_ingreso TEXT NOT NULL,
                tipo TEXT NOT NULL,
                salario_base REAL DEFAULT 0,
                valor_contrato REAL DEFAULT 0,
                valor_hora REAL DEFAULT 0
            );
            """;
        String sqlPagos = """
            CREATE TABLE IF NOT EXISTS pagos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                documento_trabajador TEXT NOT NULL,
                mes TEXT NOT NULL,
                fecha_liquidacion TEXT NOT NULL,
                horas_trabajadas REAL DEFAULT 0,
                pago_base REAL NOT NULL,
                bono REAL NOT NULL,
                total REAL NOT NULL,
                FOREIGN KEY (documento_trabajador) REFERENCES trabajadores(documento) ON DELETE CASCADE
            );
            """;
        try (Connection conn = conectar(); Statement st = conn.createStatement()) {
            st.execute(sqlTrabajadores);
            st.execute(sqlPagos);
        }
    }
}
