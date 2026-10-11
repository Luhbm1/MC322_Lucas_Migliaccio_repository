/**
 * Estoque de liga de titânio do almoxarifado.
 *
 * @author Lucas Migliaccio
 */
public class MateriaPrima {

    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double custoPorUnidade;
    private double quantidadeMinima;

    /**
     * @param id               código do lote
     * @param nome             nome da liga
     * @param quantidade       estoque inicial
     * @param unidade          unidade de medida
     * @param custoPorUnidade  preço em R$ por unidade
     * @param quantidadeMinima estoque mínimo antes de alertar
     */
    public MateriaPrima(String id, String nome, double quantidade, String unidade,
                        double custoPorUnidade, double quantidadeMinima) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.custoPorUnidade = custoPorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }

    /**
     * Retira liga do estoque. Nada é consumido se a validação falhar.
     *
     * @param demanda quantidade a consumir
     * @throws IllegalArgumentException     se a quantidade não for positiva
     * @throws RecursoInsuficienteException se não houver estoque suficiente
     */
    public void consumir(double demanda) {
        if (demanda <= 0) throw new IllegalArgumentException("Quantidade a consumir deve ser positiva.");
        if (demanda > quantidade) {
            throw new RecursoInsuficienteException(String.format(
                    "Liga insuficiente: necessario %.1f %s, disponivel %.1f %s.", demanda, unidade, quantidade, unidade));
        }
        quantidade -= demanda;
    }

    /**
     * Repõe o estoque.
     *
     * @param valor quantidade adicionada
     * @throws IllegalArgumentException se o valor não for positivo
     */
    public void adicionarEstoque(double valor) {
        if (!(valor > 0)) throw new IllegalArgumentException("A quantidade comprada deve ser positiva.");
        quantidade += valor;
    }

    /**
     * @param demanda quantidade desejada
     * @return {@code true} se a quantidade é positiva e há estoque
     */
    public boolean verificarDisponibilidade(double demanda) {
        return demanda > 0 && demanda <= quantidade;
    }

    /** @return {@code true} se o estoque está no mínimo ou abaixo */
    public boolean estoqueBaixo() { return quantidade <= quantidadeMinima; }

    /** @return código do lote */
    public String getId() { return id; }
    /** @return nome da liga */
    public String getNome() { return nome; }
    /** @return estoque atual */
    public double getQuantidade() { return quantidade; }
    /** @return unidade de medida */
    public String getUnidade() { return unidade; }
    /** @return preço por unidade, em R$ */
    public double getCustoPorUnidade() { return custoPorUnidade; }
    /** @return estoque mínimo */
    public double getQuantidadeMinima() { return quantidadeMinima; }
}
