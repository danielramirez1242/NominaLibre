package patron;

import modelo.Pago;
import java.util.ArrayList;
import java.util.List;

public class LiquidadorNomina {
    private final List<PagoObserver> observadores = new ArrayList<>();

    public void agregarObservador(PagoObserver obs) {
        if (!observadores.contains(obs)) {
            observadores.add(obs);
        }
    }

    public void quitarObservador(PagoObserver obs) {
        observadores.remove(obs);
    }

    public void notificar(Pago pago) {
        for (PagoObserver obs : new ArrayList<>(observadores)) {
            obs.onPagoLiquidado(pago);
        }
    }
}
