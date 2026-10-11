/**
 * Contrato de componentes que podem ser auditados pela federação
 * (máquinas e rotores), permitindo relatórios polimórficos.
 *
 * @author Lucas Migliaccio
 */
public interface Auditavel {

    /**
     * Gera uma linha de diagnóstico do componente.
     *
     * @return texto formatado com o estado atual do componente
     */
    String gerarRelatorioDiagnostico();

    /**
     * Indica se o componente exige intervenção (reparo ou descarte).
     *
     * @return {@code true} se o componente precisa de manutenção
     */
    boolean precisaManutencao();
}