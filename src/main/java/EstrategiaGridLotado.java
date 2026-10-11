import java.util.List;

/**
 * Estratégia de maior demanda: prioriza o pedido com mais rotores restantes,
 * independentemente do budget.
 *
 * @author Lucas Migliaccio
 */
public class EstrategiaGridLotado implements EstrategiaProducao {

    /** Cria a estratégia de maior pedido. */
    public EstrategiaGridLotado() { }

    /** {@inheritDoc} */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda escolhida = null;
        for (Demanda d : demandas) {
            if (ehElegivel(d) && (escolhida == null
                    || d.getQuantidadeRestante() > escolhida.getQuantidadeRestante())) {
                escolhida = d;
            }
        }
        return escolhida;
    }

    /** {@inheritDoc} */
    @Override
    public String getNomeEstrategia() { return "Grid Lotado (maior pedido)"; }
}