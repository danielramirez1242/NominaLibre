package modelo;

import excepciones.DocumentoInvalidoException;
import excepciones.HorasInvalidasException;
import java.time.LocalDate;

public class EmpleadoPorHoras extends Trabajador implements Bonificable {
    private double valorHora;
    private double horasTrabajadas;

    public EmpleadoPorHoras(String documento, String nombre, LocalDate fechaIngreso, double valorHora, double horasTrabajadas)
            throws DocumentoInvalidoException, HorasInvalidasException {
        super(documento, nombre, fechaIngreso);
        setValorHora(valorHora);
        setHorasTrabajadas(horasTrabajadas);
    }

    public double getValorHora() { return valorHora; }
    public double getHorasTrabajadas() { return horasTrabajadas; }

    public void setValorHora(double valorHora) throws HorasInvalidasException {
        if (valorHora <= 0) {
            throw new HorasInvalidasException("El valor de la hora debe ser mayor que 0.");
        }
        this.valorHora = valorHora;
    }

    public void setHorasTrabajadas(double horasTrabajadas) throws HorasInvalidasException {
        if (horasTrabajadas < 0 || horasTrabajadas > 300) {
            throw new HorasInvalidasException("Las horas trabajadas en el mes deben estar entre 0 y 300.");
        }
        this.horasTrabajadas = horasTrabajadas;
    }

    @Override
    public String getTipo() { return "POR_HORAS"; }

    @Override
    public double calcularPagoMes() {
        if (horasTrabajadas <= 160) {
            return horasTrabajadas * valorHora;
        }
        double horasExtra = horasTrabajadas - 160;
        return (160 * valorHora) + (horasExtra * valorHora * 1.25);
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
