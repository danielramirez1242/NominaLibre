package modelo;

import excepciones.DocumentoInvalidoException;
import excepciones.SalarioInvalidoException;
import java.time.LocalDate;

public class Contratista extends Trabajador {
    private static final double RETENCION = 0.11;
    private double valorContrato;

    public Contratista(String documento, String nombre, LocalDate fechaIngreso, double valorContrato)
            throws DocumentoInvalidoException, SalarioInvalidoException {
        super(documento, nombre, fechaIngreso);
        setValorContrato(valorContrato);
    }

    public double getValorContrato() { return valorContrato; }

    public void setValorContrato(double valorContrato) throws SalarioInvalidoException {
        if (valorContrato < SALARIO_REFERENCIA) {
            throw new SalarioInvalidoException("El valor del contrato no puede ser menor al salario de referencia ($1.400.000).");
        }
        this.valorContrato = valorContrato;
    }

    @Override
    public String getTipo() { return "CONTRATISTA"; }

    @Override
    public double calcularPagoMes() {
        return valorContrato * (1.0 - RETENCION);
    }

    @Override
    public double calcularBono(LocalDate fechaLiquidacion) {
        return 0.0;
    }
}
