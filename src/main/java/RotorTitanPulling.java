/**
 * Rotor Titan 90/100 mm para diesel pulling: 2500 g, qualidade 0,9, 95 min.
 *
 * @author Lucas Migliaccio
 */
public class RotorTitanPulling extends Produto {

    /** Massa de liga consumida por unidade, em gramas. */
    public static final double MASSA_BRUTA = 2500.0;

    /** @param id identificador único do rotor */
    public RotorTitanPulling(String id) {
        super(id, "Titan 90/100mm - 7 pas simples", MASSA_BRUTA, 0.9);
    }

    /** {@inheritDoc} */
    @Override
    public void processar() {
        setStatus(StatusRotor.USINADO);
        System.out.println("     Perfil de alta carga para diesel de tracao concluido.");
    }

    /** {@inheritDoc} */
    @Override
    public int calcularTempoProducao() { return 95; }

    /** {@inheritDoc} */
    @Override
    public TipoRotor getTipo() { return TipoRotor.PULLING; }
}
