import java.util.List;

/**
 * Estratégia (padrão Strategy) que decide qual pedido entra na linha de produção.
 *
 * @author Lucas Migliaccio
 */
public interface EstrategiaProducao {

    /**
     * Seleciona o próximo pedido a ser produzido.
     *
     * @param demandas            pedidos registrados na planta
     * @param orcamentoDisponivel budget atual em R$
     * @return pedido escolhido ou {@code null} se nenhum for elegível
     */
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    /** @return nome da estratégia exibido no menu */
    String getNomeEstrategia();

    /**
     * Filtro comum a todas as estratégias: o pedido deve estar pendente e
     * ter rotores restantes.
     *
     * @param d pedido avaliado
     * @return {@code true} se o pedido pode entrar na linha
     */
    default boolean ehElegivel(Demanda d) {
        return d != null && d.getStatus() == StatusDemanda.PENDENTE && d.getQuantidadeRestante() > 0;
    }
}