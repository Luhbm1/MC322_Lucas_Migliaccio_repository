import java.util.List;

/**
 * Estratégia de máximo de produtos: entre os pedidos que cabem no budget,
 * escolhe o que entrega mais rotores; em empate, o de menor custo.
 *
 * @author Lucas Migliaccio
 */
public class EstrategiaOrcamentoDeEquipe implements EstrategiaProducao {

    /** Cria a estratégia orientada ao budget. */
    public EstrategiaOrcamentoDeEquipe() { }

    /** {@inheritDoc} */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda escolhida = null;
        for (Demanda d : demandas) {
            if (!ehElegivel(d) || !d.ehViavel(orcamentoDisponivel)) continue;
            if (escolhida == null
                    || d.getQuantidadeRestante() > escolhida.getQuantidadeRestante()
                    || (d.getQuantidadeRestante() == escolhida.getQuantidadeRestante()
                        && d.calcularCustoEstimado() < escolhida.calcularCustoEstimado())) {
                escolhida = d;
            }
        }
        return escolhida;
    }

    /** {@inheritDoc} */
    @Override
    public String getNomeEstrategia() { return "Orcamento de Equipe (max. rotores no budget)"; }
}
