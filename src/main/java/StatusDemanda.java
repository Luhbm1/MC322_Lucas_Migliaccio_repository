/**
 * Ciclo de vida de um pedido: {@code PENDENTE -> EM_PRODUCAO -> CONCLUIDA},
 * podendo ir para {@code CANCELADA} ou voltar a {@code PENDENTE}.
 *
 * @author Lucas Migliaccio
 */
public enum StatusDemanda {
    /** Aguardando entrada na linha. */
    PENDENTE("PENDENTE    - no paddock, aguardando vaga na linha"),
    /** Rotores sendo fabricados. */
    EM_PRODUCAO("EM PRODUCAO - rotores na linha de usinagem"),
    /** Todos os rotores entregues. */
    CONCLUIDA("CONCLUIDA   - pedido entregue a empresa"),
    /** Cancelado pelo usuário ou por falta de recursos durante a produção. */
    CANCELADA("CANCELADA   - sem budget ou sem liga");

    private final String descricao;

    StatusDemanda(String descricao) { this.descricao = descricao; }

    /** @return descrição legível do status */
    public String getDescricao() { return descricao; }
}