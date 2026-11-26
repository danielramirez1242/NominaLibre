package modelo;

import java.time.LocalDate;

public interface Bonificable {
    double calcularBonoAntiguedad(LocalDate fechaLiquidacion);
}
