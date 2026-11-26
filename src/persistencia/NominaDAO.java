package persistencia;

import excepciones.DocumentoInvalidoException;
import modelo.Pago;
import modelo.Trabajador;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface NominaDAO {
    void guardarTrabajador(Trabajador t) throws SQLException, DocumentoInvalidoException;
    void actualizarTrabajador(Trabajador t) throws SQLException;
    void eliminarTrabajador(String documento) throws SQLException;
    List<Trabajador> listarTrabajadores() throws SQLException;
    Trabajador buscarTrabajador(String documento) throws SQLException;

    void guardarPago(Pago p) throws SQLException;
    void actualizarPago(Pago p) throws SQLException;
    void eliminarPago(int idPago) throws SQLException;
    List<Pago> listarPagos() throws SQLException;

    Map<String, Double> totalNominaPorTipoEnMes(String mes) throws SQLException;
    List<Trabajador> trabajadoresSinPagoEnMes(String mes) throws SQLException;
}
