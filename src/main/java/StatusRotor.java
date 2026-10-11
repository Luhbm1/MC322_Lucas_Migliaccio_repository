/**
 * Etapas pelas quais um rotor passa na linha de produção.
 *
 * @author Lucas Migliaccio
 */
public enum StatusRotor {
    /** Bloco bruto aguardando o CNC. */
    AGUARDANDO_USINAGEM("Aguardando usinagem"),
    /** Usinado pelo CNC. */
    USINADO("Usinado"),
    /** Selado na cabine a vácuo. */
    EMBALADO("Embalado a vacuo"),
    /** Aprovado na bancada de balanceamento. */
    APROVADO("Aprovado no balanceamento"),
    /** Reprovado na bancada: vai para a sucata. */
    REPROVADO("Reprovado no balanceamento");

    private final String descricao;

    StatusRotor(String descricao) { this.descricao = descricao; }

    /** @return descrição legível da etapa */
    public String getDescricao() { return descricao; }
}
