package patron;

import modelo.Pago;

public interface PagoObserver {
    void onPagoLiquidado(Pago pago);
}
