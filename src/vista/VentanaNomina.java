package vista;

import excepciones.*;
import modelo.*;
import patron.*;
import persistencia.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class VentanaNomina extends JFrame {
    private final NominaDAO dao = new NominaDAOImpl();
    private final LiquidadorNomina liquidador = new LiquidadorNomina();
    private final ContabilidadObserver contabilidadObserver = new ContabilidadObserver();
    private final AlertaObserver alertaObserver;

    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"PLANTA", "CONTRATISTA", "POR_HORAS"});
    private final JTextField txtDocumento = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtFechaIngreso = new JTextField("2020-01-15");
    private final JTextField txtSalarioBase = new JTextField();
    private final JTextField txtValorContrato = new JTextField();
    private final JTextField txtValorHora = new JTextField();
    private final JTextField txtHorasMes = new JTextField("0");
    private final JTextField txtFechaLiquidacion = new JTextField("2026-11-30");
    private final JCheckBox chkAlerta = new JCheckBox("Activar Alerta (> $8.000.000)", true);
    private final JLabel lblAcumulado = new JLabel("Total liquidado en sesión: $0.00");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Documento", "Nombre", "Tipo", "Mes", "Fecha Liq.", "Horas", "Pago Base", "Bono", "Total"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tablaPagos = new JTable(modeloTabla);

    private final JTextField txtMesConsulta = new JTextField("2026-11", 8);
    private final JTextArea txtConsultas = new JTextArea(6, 40);

    private Integer idPagoSeleccionado = null;

    public VentanaNomina() {
        setTitle("NóminaLibre - Parcial 3er Corte");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        alertaObserver = new AlertaObserver(msg ->
                JOptionPane.showMessageDialog(this, msg, "Alerta de Nómina", JOptionPane.WARNING_MESSAGE));
        liquidador.agregarObservador(contabilidadObserver);
        liquidador.agregarObservador(alertaObserver);

        try {
            ConexionSQLite.inicializarTablas();
        } catch (SQLException e) {
            mostrarError("Error inicializando la base de datos: " + e.getMessage());
        }

        add(crearPanelFormulario(), BorderLayout.WEST);
        add(crearPanelCentro(), BorderLayout.CENTER);
        add(crearPanelConsultas(), BorderLayout.SOUTH);

        actualizarCamposPorTipo();
        cargarTablaPagos();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Registro y Liquidación"));
        panel.setPreferredSize(new Dimension(340, 0));

        JPanel campos = new JPanel(new GridLayout(0, 2, 5, 5));
        campos.add(new JLabel("Tipo:")); campos.add(cbTipo);
        campos.add(new JLabel("Documento (6-10 díg):")); campos.add(txtDocumento);
        campos.add(new JLabel("Nombre:")); campos.add(txtNombre);
        campos.add(new JLabel("Fecha Ingreso (YYYY-MM-DD):")); campos.add(txtFechaIngreso);
        campos.add(new JLabel("Salario Base (Planta):")); campos.add(txtSalarioBase);
        campos.add(new JLabel("Valor Contrato (Contratista):")); campos.add(txtValorContrato);
        campos.add(new JLabel("Valor Hora (Por Horas):")); campos.add(txtValorHora);
        campos.add(new JLabel("Horas Mes (0-300):")); campos.add(txtHorasMes);
        campos.add(new JLabel("Fecha Liquidación:")); campos.add(txtFechaLiquidacion);

        cbTipo.addActionListener(e -> actualizarCamposPorTipo());
        chkAlerta.addActionListener(e -> {
            if (chkAlerta.isSelected()) liquidador.agregarObservador(alertaObserver);
            else liquidador.quitarObservador(alertaObserver);
        });

        JPanel botones = new JPanel(new GridLayout(0, 1, 5, 5));
        JButton btnGuardarNuevo = new JButton("Registrar Trabajador y Liquidar");
        JButton btnLiquidarExistente = new JButton("Liquidar Nuevo Mes (Doc. Existente)");
        JButton btnEditar = new JButton("Guardar Cambios (Editar Seleccionado)");
        JButton btnEliminar = new JButton("Eliminar Pago Seleccionado");
        JButton btnLimpiar = new JButton("Limpiar Formulario");

        btnGuardarNuevo.addActionListener(e -> registrarYLiquidar(true));
        btnLiquidarExistente.addActionListener(e -> registrarYLiquidar(false));
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        botones.add(chkAlerta);
        botones.add(btnGuardarNuevo);
        botones.add(btnLiquidarExistente);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(lblAcumulado);

        panel.add(campos, BorderLayout.NORTH);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelCentro() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pagos Liquidados (Clic en una fila para Editar/Eliminar)"));
        tablaPagos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPagos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaPagos.getSelectedRow() != -1) {
                cargarSeleccionEnFormulario();
            }
        });
        panel.add(new JScrollPane(tablaPagos), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelConsultas() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Consultas de Negocio (R4 y R9)"));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(new JLabel("Mes (YYYY-MM):"));
        barra.add(txtMesConsulta);

        JButton btnConsultaA = new JButton("a) Total Nómina por Tipo");
        JButton btnConsultaB = new JButton("b) Trabajadores Sin Pago");
        btnConsultaA.addActionListener(e -> ejecutarConsultaTotalPorTipo());
        btnConsultaB.addActionListener(e -> ejecutarConsultaSinPago());

        barra.add(btnConsultaA);
        barra.add(btnConsultaB);

        txtConsultas.setEditable(false);
        panel.add(barra, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtConsultas), BorderLayout.CENTER);
        return panel;
    }

    private void actualizarCamposPorTipo() {
        String tipo = (String) cbTipo.getSelectedItem();
        txtSalarioBase.setEnabled("PLANTA".equals(tipo));
        txtValorContrato.setEnabled("CONTRATISTA".equals(tipo));
        txtValorHora.setEnabled("POR_HORAS".equals(tipo));
        txtHorasMes.setEnabled("POR_HORAS".equals(tipo));
    }

    private Trabajador construirTrabajadorDesdeFormulario()
            throws DocumentoInvalidoException, SalarioInvalidoException, HorasInvalidasException {
        String tipo = (String) cbTipo.getSelectedItem();
        String doc = txtDocumento.getText().trim();
        String nombre = txtNombre.getText().trim();
        LocalDate ingreso = LocalDate.parse(txtFechaIngreso.getText().trim());

        if ("PLANTA".equals(tipo)) {
            return new EmpleadoPlanta(doc, nombre, ingreso, parseDouble(txtSalarioBase, "Salario Base"));
        } else if ("CONTRATISTA".equals(tipo)) {
            return new Contratista(doc, nombre, ingreso, parseDouble(txtValorContrato, "Valor Contrato"));
        } else {
            return new EmpleadoPorHoras(doc, nombre, ingreso,
                    parseDouble(txtValorHora, "Valor Hora"),
                    parseDouble(txtHorasMes, "Horas Mes"));
        }
    }

    private double parseDouble(JTextField campo, String nombreCampo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) throw new IllegalArgumentException("El campo '" + nombreCampo + "' es obligatorio.");
        return Double.parseDouble(texto);
    }

    private void registrarYLiquidar(boolean esNuevoTrabajador) {
        try {
            LocalDate fechaLiq = LocalDate.parse(txtFechaLiquidacion.getText().trim());
            String mes = fechaLiq.toString().substring(0, 7);
            Trabajador t;
            double horas = 0;

            if (esNuevoTrabajador) {
                t = construirTrabajadorDesdeFormulario();
                if (t instanceof EmpleadoPorHoras eh) horas = eh.getHorasTrabajadas();
                dao.guardarTrabajador(t);
            } else {
                String doc = txtDocumento.getText().trim();
                t = dao.buscarTrabajador(doc);
                if (t == null) {
                    throw new DocumentoInvalidoException("No existe un trabajador con documento " + doc);
                }
                if (t instanceof EmpleadoPorHoras eh) {
                    horas = parseDouble(txtHorasMes, "Horas Mes");
                    eh.setHorasTrabajadas(horas);
                }
            }

            Pago pago = new Pago(0, t, mes, fechaLiq, horas);
            dao.guardarPago(pago);
            liquidador.notificar(pago);
            lblAcumulado.setText(String.format("Total liquidado en sesión: $%,.2f",
                    contabilidadObserver.getTotalAcumuladoSesion()));

            cargarTablaPagos();
            JOptionPane.showMessageDialog(this, "Pago liquidado y guardado correctamente.");
        } catch (DateTimeParseException ex) {
            mostrarError("Formato de fecha inválido. Use YYYY-MM-DD.");
        } catch (NumberFormatException ex) {
            mostrarError("Formato numérico inválido en uno de los campos.");
        } catch (DocumentoInvalidoException | SalarioInvalidoException | HorasInvalidasException | IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        } catch (SQLException ex) {
            mostrarError("Error de base de datos: " + ex.getMessage());
        }
    }

    private void editarSeleccionado() {
        if (idPagoSeleccionado == null) {
            mostrarError("Seleccione primero una fila de la tabla para editar.");
            return;
        }
        try {
            LocalDate fechaLiq = LocalDate.parse(txtFechaLiquidacion.getText().trim());
            String mes = fechaLiq.toString().substring(0, 7);
            Trabajador t = construirTrabajadorDesdeFormulario();
            double horas = (t instanceof EmpleadoPorHoras eh) ? eh.getHorasTrabajadas() : 0;

            dao.actualizarTrabajador(t);
            Pago pagoActualizado = new Pago(idPagoSeleccionado, t, mes, fechaLiq, horas);
            dao.actualizarPago(pagoActualizado);

            cargarTablaPagos();
            JOptionPane.showMessageDialog(this, "Registro actualizado correctamente.");
        } catch (Exception ex) {
            mostrarError("Error al editar: " + ex.getMessage());
        }
    }

    private void eliminarSeleccionado() {
        if (idPagoSeleccionado == null) {
            mostrarError("Seleccione primero una fila de la tabla para eliminar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el pago ID " + idPagoSeleccionado + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dao.eliminarPago(idPagoSeleccionado);
                limpiarFormulario();
                cargarTablaPagos();
            } catch (SQLException ex) {
                mostrarError("Error al eliminar: " + ex.getMessage());
            }
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tablaPagos.getSelectedRow();
        idPagoSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
        String doc = (String) modeloTabla.getValueAt(fila, 1);
        String fechaLiq = (String) modeloTabla.getValueAt(fila, 5);
        double horas = Double.parseDouble(modeloTabla.getValueAt(fila, 6).toString());

        try {
            Trabajador t = dao.buscarTrabajador(doc);
            if (t != null) {
                cbTipo.setSelectedItem(t.getTipo());
                txtDocumento.setText(t.getDocumento());
                txtDocumento.setEditable(false);
                txtNombre.setText(t.getNombre());
                txtFechaIngreso.setText(t.getFechaIngreso().toString());
                txtFechaLiquidacion.setText(fechaLiq);
                txtHorasMes.setText(String.valueOf(horas));
                if (t instanceof EmpleadoPlanta ep) txtSalarioBase.setText(String.valueOf(ep.getSalarioBase()));
                if (t instanceof Contratista c) txtValorContrato.setText(String.valueOf(c.getValorContrato()));
                if (t instanceof EmpleadoPorHoras eh) txtValorHora.setText(String.valueOf(eh.getValorHora()));
                actualizarCamposPorTipo();
            }
        } catch (SQLException ex) {
            mostrarError("Error cargando registro: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        idPagoSeleccionado = null;
        txtDocumento.setEditable(true);
        txtDocumento.setText("");
        txtNombre.setText("");
        txtSalarioBase.setText("");
        txtValorContrato.setText("");
        txtValorHora.setText("");
        txtHorasMes.setText("0");
        tablaPagos.clearSelection();
    }

    private void cargarTablaPagos() {
        modeloTabla.setRowCount(0);
        try {
            List<Pago> pagos = dao.listarPagos();
            for (Pago p : pagos) {
                modeloTabla.addRow(new Object[]{
                        p.getId(),
                        p.getTrabajador().getDocumento(),
                        p.getTrabajador().getNombre(),
                        p.getTrabajador().getTipo(),
                        p.getMes(),
                        p.getFechaLiquidacion().toString(),
                        p.getHorasTrabajadas(),
                        String.format("$%,.2f", p.getPagoBase()),
                        String.format("$%,.2f", p.getBono()),
                        String.format("$%,.2f", p.getTotal())
                });
            }
        } catch (SQLException ex) {
            mostrarError("Error cargando tabla: " + ex.getMessage());
        }
    }

    private void ejecutarConsultaTotalPorTipo() {
        String mes = txtMesConsulta.getText().trim();
        try {
            Map<String, Double> res = dao.totalNominaPorTipoEnMes(mes);
            StringBuilder sb = new StringBuilder("=== Total nómina por tipo en " + mes + " ===\n");
            if (res.isEmpty()) sb.append("No hay pagos registrados para este mes.");
            else res.forEach((tipo, total) -> sb.append(String.format("- %s: $%,.2f\n", tipo, total)));
            txtConsultas.setText(sb.toString());
        } catch (SQLException ex) {
            mostrarError("Error en consulta: " + ex.getMessage());
        }
    }

    private void ejecutarConsultaSinPago() {
        String mes = txtMesConsulta.getText().trim();
        try {
            List<Trabajador> sinPago = dao.trabajadoresSinPagoEnMes(mes);
            StringBuilder sb = new StringBuilder("=== Trabajadores sin pago en " + mes + " ===\n");
            if (sinPago.isEmpty()) sb.append("Todos los trabajadores tienen pago liquidado en este mes.");
            else for (Trabajador t : sinPago) {
                sb.append(String.format("- %s (%s) [%s]\n", t.getNombre(), t.getDocumento(), t.getTipo()));
            }
            txtConsultas.setText(sb.toString());
        } catch (SQLException ex) {
            mostrarError("Error en consulta: " + ex.getMessage());
        }
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaNomina().setVisible(true));
    }
}
