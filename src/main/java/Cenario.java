/**
 * Cenários de operação da temporada. Cada cenário define os recursos iniciais
 * e a agressividade de falhas e desgaste das máquinas.
 *
 * @author Lucas Migliaccio
 */
public enum Cenario {
    /** Recursos fartos, desgaste e falhas reduzidos. */
    IDEAL("Ideal - Temporada de Testes", 100000.0, 20000.0, 0.60, 0.5, 1.0, 0.00),
    /** Recursos escassos, falhas ampliadas e rotores já nascem com risco. */
    APOCALIPTICO("Apocaliptico - Final de Campeonato", 8000.0, 4000.0, 0.90, 2.5, 6.0, 0.15);

    private final String descricao;
    private final double budgetInicial;
    private final double estoqueInicialLiga;
    private final double precoLiga;
    private final double multiplicadorFalha;
    private final double desgasteMaximo;
    private final double riscoInicialRotor;

    Cenario(String descricao, double budgetInicial, double estoqueInicialLiga, double precoLiga,
            double multiplicadorFalha, double desgasteMaximo, double riscoInicialRotor) {
        this.descricao = descricao;
        this.budgetInicial = budgetInicial;
        this.estoqueInicialLiga = estoqueInicialLiga;
        this.precoLiga = precoLiga;
        this.multiplicadorFalha = multiplicadorFalha;
        this.desgasteMaximo = desgasteMaximo;
        this.riscoInicialRotor = riscoInicialRotor;
    }

    /** @return nome do cenário exibido no menu */
    public String getDescricao() { return descricao; }
    /** @return budget inicial em R$ */
    public double getBudgetInicial() { return budgetInicial; }
    /** @return estoque inicial de liga, em gramas */
    public double getEstoqueInicialLiga() { return estoqueInicialLiga; }
    /** @return preço da liga em R$ por grama */
    public double getPrecoLiga() { return precoLiga; }
    /** @return fator aplicado à probabilidade de falha das máquinas */
    public double getMultiplicadorFalha() { return multiplicadorFalha; }
    /** @return desgaste máximo de saúde (%) por ciclo de máquina */
    public double getDesgasteMaximo() { return desgasteMaximo; }
    /** @return risco de falha (0 a 1) com que cada rotor já nasce */
    public double getRiscoInicialRotor() { return riscoInicialRotor; }
}