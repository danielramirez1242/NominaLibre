package modelo;

import java.time.LocalDate;

public class Pago {
    private int id;
    private Trabajador trabajador;
    private String mes;
    private LocalDate fechaLiquidacion;
    private double horasTrabajadas;
    private double pagoBase;
    private double bono;
    private double total;

    public Pago(int id, Trabajador trabajador, String mes, LocalDate fechaLiquidacion, double horasTrabajadas) {
        this.id = id;
        this.trabajador = trabajador;
        this.mes = mes;
        this.fechaLiquidacion = fechaLiquidacion;
        this.horasTrabajadas = horasTrabajadas;
        this.pagoBase = trabajador.calcularPagoMes();
        this.bono = trabajador.calcularBono(fechaLiquidacion);
        this.total = trabajador.calcularTotal(fechaLiquidacion);
    }

    public int getId() { return id; }
    public Trabajador getTrabajador() { return trabajador; }
    public String getMes() { return mes; }
    public LocalDate getFechaLiquidacion() { return fechaLiquidacion; }
    public double getHorasTrabajadas() { return horasTrabajadas; }
    public double getPagoBase() { return pagoBase; }
    public double getBono() { return bono; }
    public double getTotal() { return total; }
}
