package modelo;

import excepciones.DocumentoInvalidoException;
import java.time.LocalDate;
import java.time.Period;

public abstract class Trabajador {
    public static final double SALARIO_REFERENCIA = 1_400_000.0;

    private String documento;
    private String nombre;
    private LocalDate fechaIngreso;

    public Trabajador(String documento, String nombre, LocalDate fechaIngreso) throws DocumentoInvalidoException {
        setDocumento(documento);
        setNombre(nombre);
        setFechaIngreso(fechaIngreso);
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }

    public void setDocumento(String documento) throws DocumentoInvalidoException {
        if (documento == null || !documento.trim().matches("\\d{6,10}")) {
            throw new DocumentoInvalidoException("El documento debe tener entre 6 y 10 dígitos numéricos.");
        }
        this.documento = documento.trim();
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del trabajador es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        if (fechaIngreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria.");
        }
        this.fechaIngreso = fechaIngreso;
    }

    protected int calcularAniosCompletos(LocalDate fechaLiquidacion) {
        if (fechaLiquidacion == null || fechaLiquidacion.isBefore(fechaIngreso)) {
            return 0;
        }
        return Period.between(fechaIngreso, fechaLiquidacion).getYears();
    }

    public abstract String getTipo();
    public abstract double calcularPagoMes();
    public abstract double calcularBono(LocalDate fechaLiquidacion);

    public double calcularTotal(LocalDate fechaLiquidacion) {
        return calcularPagoMes() + calcularBono(fechaLiquidacion);
    }
}
