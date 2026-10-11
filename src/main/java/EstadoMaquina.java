/**
 * Estado operacional de uma máquina, derivado da sua saúde.
 *
 * @author Lucas Migliaccio
 */
public enum EstadoMaquina {
    /** Saúde acima do limiar de manutenção. */
    OPERANTE("Operante"),
    /** Saúde abaixo do limiar de manutenção, mas ainda funcional. */
    DESGASTADA("Desgastada - agendar manutencao"),
    /** Saúde crítica: não aceita novos rotores até ser reparada. */
    QUEBRADA("QUEBRADA - fora de operacao");

    private final String descricao;

    EstadoMaquina(String descricao) { this.descricao = descricao; }

    /** @return descrição legível do estado */
    public String getDescricao() { return descricao; }
}