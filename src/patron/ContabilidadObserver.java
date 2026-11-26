package patron;

import modelo.Pago;

public class ContabilidadObserver implements PagoObserver {
    private double totalAcumuladoSesion = 0.0;

    @Override
    public void onPagoLiquidado(Pago pago) {
        totalAcumuladoSesion += pago.getTotal();
    }

    public double getTotalAcumuladoSesion() {
        return totalAcumuladoSesion;
    }

    public void reiniciar() {
        totalAcumuladoSesion = 0.0;
    }
}
