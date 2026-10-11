/**
 * Rotor DualBlade 72/78 mm para arrancada: 1500 g, qualidade 0,7, 70 min.
 *
 * @author Lucas Migliaccio
 */
public class RotorDualBladeArrancada extends Produto {

    /** Massa de liga consumida por unidade, em gramas. */
    public static final double MASSA_BRUTA = 1500.0;

    /** @param id identificador único do rotor */
    public RotorDualBladeArrancada(String id) {
        super(id, "DualBlade 72/78mm - 6 pas duplas", MASSA_BRUTA, 0.7);
    }

    /** {@inheritDoc} */
    @Override
    public void processar() {
        setStatus(StatusRotor.USINADO);
        System.out.println("     Pas duplas recortadas: pronto para os 201 metros.");
    }

    /** {@inheritDoc} */
    @Override
    public int calcularTempoProducao() { return 70; }

    /** {@inheritDoc} */
    @Override
    public TipoRotor getTipo() { return TipoRotor.ARRANCADA; }
}