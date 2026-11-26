package persistencia;

import excepciones.DocumentoInvalidoException;
import modelo.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class NominaDAOImpl implements NominaDAO {

    private Trabajador reconstruirTrabajador(ResultSet rs, double horasMes) throws SQLException {
        String doc = rs.getString("documento");
        String nombre = rs.getString("nombre");
        LocalDate ingreso = LocalDate.parse(rs.getString("fecha_ingreso"));
        String tipo = rs.getString("tipo");
        try {
            if ("PLANTA".equals(tipo)) {
                return new EmpleadoPlanta(doc, nombre, ingreso, rs.getDouble("salario_base"));
            } else if ("CONTRATISTA".equals(tipo)) {
                return new Contratista(doc, nombre, ingreso, rs.getDouble("valor_contrato"));
            } else {
                return new EmpleadoPorHoras(doc, nombre, ingreso, rs.getDouble("valor_hora"), horasMes);
            }
        } catch (Exception e) {
            throw new SQLException("Error reconstruyendo trabajador " + doc + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void guardarTrabajador(Trabajador t) throws SQLException, DocumentoInvalidoException {
        if (buscarTrabajador(t.getDocumento()) != null) {
            throw new DocumentoInvalidoException("Ya existe un trabajador registrado con el documento " + t.getDocumento());
        }
        String sql = "INSERT INTO trabajadores(documento, nombre, fecha_ingreso, tipo, salario_base, valor_contrato, valor_hora) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            llenarParametrosTrabajador(ps, t);
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizarTrabajador(Trabajador t) throws SQLException {
        String sql = "UPDATE trabajadores SET nombre=?, fecha_ingreso=?, tipo=?, salario_base=?, valor_contrato=?, valor_hora=? WHERE documento=?";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getFechaIngreso().toString());
            ps.setString(3, t.getTipo());
            ps.setDouble(4, (t instanceof EmpleadoPlanta ep) ? ep.getSalarioBase() : 0);
            ps.setDouble(5, (t instanceof Contratista c) ? c.getValorContrato() : 0);
            ps.setDouble(6, (t instanceof EmpleadoPorHoras eh) ? eh.getValorHora() : 0);
            ps.setString(7, t.getDocumento());
            ps.executeUpdate();
        }
    }

    private void llenarParametrosTrabajador(PreparedStatement ps, Trabajador t) throws SQLException {
        ps.setString(1, t.getDocumento());
        ps.setString(2, t.getNombre());
        ps.setString(3, t.getFechaIngreso().toString());
        ps.setString(4, t.getTipo());
        ps.setDouble(5, (t instanceof EmpleadoPlanta ep) ? ep.getSalarioBase() : 0);
        ps.setDouble(6, (t instanceof Contratista c) ? c.getValorContrato() : 0);
        ps.setDouble(7, (t instanceof EmpleadoPorHoras eh) ? eh.getValorHora() : 0);
    }

    @Override
    public void eliminarTrabajador(String documento) throws SQLException {
        String sql = "DELETE FROM trabajadores WHERE documento = ?";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documento);
            ps.executeUpdate();
        }
    }

    @Override
    public Trabajador buscarTrabajador(String documento) throws SQLException {
        String sql = "SELECT * FROM trabajadores WHERE documento = ?";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return reconstruirTrabajador(rs, 0);
                }
            }
        }
        return null;
    }

    @Override
    public List<Trabajador> listarTrabajadores() throws SQLException {
        List<Trabajador> lista = new ArrayList<>();
        String sql = "SELECT * FROM trabajadores ORDER BY nombre";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(reconstruirTrabajador(rs, 0));
            }
        }
        return lista;
    }

    @Override
    public void guardarPago(Pago p) throws SQLException {
        String sql = "INSERT INTO pagos(documento_trabajador, mes, fecha_liquidacion, horas_trabajadas, pago_base, bono, total) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getTrabajador().getDocumento());
            ps.setString(2, p.getMes());
            ps.setString(3, p.getFechaLiquidacion().toString());
            ps.setDouble(4, p.getHorasTrabajadas());
            ps.setDouble(5, p.getPagoBase());
            ps.setDouble(6, p.getBono());
            ps.setDouble(7, p.getTotal());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizarPago(Pago p) throws SQLException {
        String sql = "UPDATE pagos SET mes=?, fecha_liquidacion=?, horas_trabajadas=?, pago_base=?, bono=?, total=? WHERE id=?";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getMes());
            ps.setString(2, p.getFechaLiquidacion().toString());
            ps.setDouble(3, p.getHorasTrabajadas());
            ps.setDouble(4, p.getPagoBase());
            ps.setDouble(5, p.getBono());
            ps.setDouble(6, p.getTotal());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminarPago(int idPago) throws SQLException {
        String sql = "DELETE FROM pagos WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Pago> listarPagos() throws SQLException {
        List<Pago> pagos = new ArrayList<>();
        String sql = """
            SELECT p.id, p.mes, p.fecha_liquidacion, p.horas_trabajadas,
                   t.documento, t.nombre, t.fecha_ingreso, t.tipo,
                   t.salario_base, t.valor_contrato, t.valor_hora
            FROM pagos p
            INNER JOIN trabajadores t ON p.documento_trabajador = t.documento
            ORDER BY p.id DESC
            """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double horas = rs.getDouble("horas_trabajadas");
                Trabajador t = reconstruirTrabajador(rs, horas);
                Pago pago = new Pago(
                        rs.getInt("id"),
                        t,
                        rs.getString("mes"),
                        LocalDate.parse(rs.getString("fecha_liquidacion")),
                        horas
                );
                pagos.add(pago);
            }
        }
        return pagos;
    }

    @Override
    public Map<String, Double> totalNominaPorTipoEnMes(String mes) throws SQLException {
        Map<String, Double> totales = new LinkedHashMap<>();
        String sql = """
            SELECT t.tipo, SUM(p.total) AS total_tipo
            FROM pagos p
            INNER JOIN trabajadores t ON p.documento_trabajador = t.documento
            WHERE p.mes = ?
            GROUP BY t.tipo
            """;
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totales.put(rs.getString("tipo"), rs.getDouble("total_tipo"));
                }
            }
        }
        return totales;
    }

    @Override
    public List<Trabajador> trabajadoresSinPagoEnMes(String mes) throws SQLException {
        List<Trabajador> sinPago = new ArrayList<>();
        String sql = """
            SELECT * FROM trabajadores t
            WHERE NOT EXISTS (
                SELECT 1 FROM pagos p
                WHERE p.documento_trabajador = t.documento AND p.mes = ?
            )
            ORDER BY t.nombre
            """;
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sinPago.add(reconstruirTrabajador(rs, 0));
                }
            }
        }
        return sinPago;
    }
}
