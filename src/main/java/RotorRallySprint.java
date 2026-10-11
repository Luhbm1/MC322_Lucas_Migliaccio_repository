/**
 * Rotor Sprint 50/58 mm para rally/time attack: 500 g, qualidade 0,5, 40 min.
 *
 * @author Lucas Migliaccio
 */
public class RotorRallySprint extends Produto {

    /** Massa de liga consumida por unidade, em gramas. */
    public static final double MASSA_BRUTA = 500.0;

    /** @param id identificador único do rotor */
    public RotorRallySprint(String id) {
        super(id, "Sprint 50/58mm - 7 pas simples", MASSA_BRUTA, 0.5);
    }

    /** {@inheritDoc} */
    @Override
    public void processar() {
        setStatus(StatusRotor.USINADO);
        System.out.println("     Rotor leve usinado: spool imediato na saida da curva.");
    }

    /** {@inheritDoc} */
    @Override
    public int calcularTempoProducao() { return 40; }

    /** {@inheritDoc} */
    @Override
    public TipoRotor getTipo() { return TipoRotor.RALLY; }
}