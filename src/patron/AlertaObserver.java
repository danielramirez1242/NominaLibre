package patron;

import modelo.Pago;
import java.util.function.Consumer;

public class AlertaObserver implements PagoObserver {
    private static final double UMBRAL_ALERTA = 8_000_000.0;
    private final Consumer<String> notificadorUI;

    public AlertaObserver(Consumer<String> notificadorUI) {
        this.notificadorUI = notificadorUI;
    }

    @Override
    public void onPagoLiquidado(Pago pago) {
        if (pago.getTotal() > UMBRAL_ALERTA) {
            String msg = String.format("ALERTA: El pago de %s (%s) supera $8.000.000 -> Total: $%,.2f",
                    pago.getTrabajador().getNombre(),
                    pago.getTrabajador().getDocumento(),
                    pago.getTotal());
            notificadorUI.accept(msg);
        }
    }
}
