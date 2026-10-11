import java.util.List;

/**
 * Estratégia FIFO: atende primeiro o pedido elegível mais antigo.
 *
 * @author Lucas Migliaccio
 */
public class EstrategiaFilaDoPaddock implements EstrategiaProducao {

    /** Cria a estratégia de ordem de chegada. */
    public EstrategiaFilaDoPaddock() { }

    /** {@inheritDoc} */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        for (Demanda d : demandas) {
            if (ehElegivel(d)) return d;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getNomeEstrategia() { return "Fila do Paddock (ordem de chegada)"; }
}