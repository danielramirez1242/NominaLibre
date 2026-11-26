package modelo;

import excepciones.DocumentoInvalidoException;
import excepciones.SalarioInvalidoException;
import java.time.LocalDate;

public class EmpleadoPlanta extends Trabajador implements Bonificable {
    private static final double AUXILIO_TRANSPORTE = 200_000.0;
    private double salarioBase;

    public EmpleadoPlanta(String documento, String nombre, LocalDate fechaIngreso, double salarioBase)
            throws DocumentoInvalidoException, SalarioInvalidoException {
        super(documento, nombre, fechaIngreso);
        setSalarioBase(salarioBase);
    }

    public double getSalarioBase() { return salarioBase; }

    public void setSalarioBase(double salarioBase) throws SalarioInvalidoException {
        if (salarioBase < SALARIO_REFERENCIA) {
            throw new SalarioInvalidoException("El salario base no puede ser menor al salario de referencia ($1.400.000).");
        }
        this.salarioBase = salarioBase;
    }

    @Override
    public String getTipo() { return "PLANTA"; }

    @Override
    public double calcularPagoMes() {
        double auxilio = (salarioBase <= 2 * SALARIO_REFERENCIA) ? AUXILIO_TRANSPORTE : 0.0;
        return salarioBase + auxilio;
    }

    @Override
    public double calcularBonoAntiguedad(LocalDate fechaLiquidacion) {
        int anios = calcularAniosCompletos(fechaLiquidacion);
        double porcentaje = Math.min(anios * 0.01, 0.10);
        return calcularPagoMes() * porcentaje;
    }

    @Override
    public double calcularBono(LocalDate fechaLiquidacion) {
        return calcularBonoAntiguedad(fechaLiquidacion);
    }
}
