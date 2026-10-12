/**
 * Pedido de uma empresa para um tipo de rotor.
 * <p>
 * Controla quantidade total, quantidade já entregue e o ciclo de vida
 * ({@link StatusDemanda}). Toda transição inválida lança
 * {@link DemandaInvalidaException} sem alterar o estado do pedido.
 *
 * @author Lucas Migliaccio
 */
public class Demanda {

    private static int proximoNumero = 1;

    private final int numero;
    private final TipoRotor tipo;
    private int quantidadeProdutos;
    private int quantidadeProduzida;
    private StatusDemanda status;
    private double custoOperacaoPorUnidade;

    /**
     * Cria um pedido pendente. O número só é consumido se os dados forem válidos.
     *
     * @param tipo               tipo de rotor solicitado
     * @param quantidadeProdutos quantidade de rotores (mínimo 1)
     * @throws DemandaInvalidaException se o tipo for nulo ou a quantidade menor que 1
     */
    public Demanda(TipoRotor tipo, int quantidadeProdutos) {
        if (tipo == null) throw new DemandaInvalidaException("Tipo de rotor nao informado.");
        if (quantidadeProdutos < 1) {
            throw new DemandaInvalidaException("O pedido precisa ter pelo menos 1 rotor.");
        }
        this.numero = proximoNumero++;
        this.tipo = tipo;
        this.quantidadeProdutos = quantidadeProdutos;
        this.status = StatusDemanda.PENDENTE;
    }

    /**
     * Altera o total do pedido e o reabre como pendente (inclusive se estava
     * concluído ou cancelado).
     *
     * @param novaQuantidade novo total, maior que a quantidade já entregue
     * @throws DemandaInvalidaException se o pedido estiver em produção ou a
     *                                  quantidade não superar o já entregue
     */
    public void atualizarQuantidade(int novaQuantidade) {
        if (status == StatusDemanda.EM_PRODUCAO) {
            throw new DemandaInvalidaException("Pedido #" + numero + " esta em producao e nao pode ser alterado.");
        }
        if (novaQuantidade <= quantidadeProduzida) {
            throw new DemandaInvalidaException("A nova quantidade deve ser maior que os "
                    + quantidadeProduzida + " rotor(es) ja entregues no pedido #" + numero + ".");
        }
        quantidadeProdutos = novaQuantidade;
        status = StatusDemanda.PENDENTE;
    }

    /**
     * Coloca o pedido na linha de produção.
     *
     * @throws DemandaInvalidaException se o pedido não estiver pendente
     */
    public void iniciarProducao() {
        if (status != StatusDemanda.PENDENTE) {
            throw new DemandaInvalidaException("Pedido #" + numero + " nao esta pendente ("
                    + status.getDescricao() + "). Apenas pedidos pendentes entram na linha.");
        }
        status = StatusDemanda.EM_PRODUCAO;
    }

    /**
     * Contabiliza um rotor aprovado no balanceamento.
     *
     * @throws DemandaInvalidaException se o pedido não estiver em produção
     */
    public void registrarRotorAprovado() {
        if (status != StatusDemanda.EM_PRODUCAO) {
            throw new DemandaInvalidaException("Pedido #" + numero + " nao esta em producao.");
        }
        quantidadeProduzida++;
    }

    /**
     * Marca o pedido como concluído.
     *
     * @throws DemandaInvalidaException se não estiver em produção ou ainda houver rotores restantes
     */
    public void concluir() {
        if (status != StatusDemanda.EM_PRODUCAO || getQuantidadeRestante() > 0) {
            throw new DemandaInvalidaException("Pedido #" + numero
                    + " so pode ser concluido em producao e sem rotores restantes.");
        }
        status = StatusDemanda.CONCLUIDA;
    }

    /**
     * Cancela o pedido.
     *
     * @throws DemandaInvalidaException se o pedido já estiver concluído ou cancelado
     */
    public void cancelar() {
        if (status == StatusDemanda.CONCLUIDA) {
            throw new DemandaInvalidaException("Pedido #" + numero + " ja foi entregue e nao pode ser cancelado.");
        }
        if (status == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException("Pedido #" + numero + " ja esta cancelado.");
        }
        status = StatusDemanda.CANCELADA;
    }

    /** Devolve à fila um pedido em produção (ex.: máquina quebrou). Sem efeito nos demais estados. */
    public void pausar() {
        if (status == StatusDemanda.EM_PRODUCAO) status = StatusDemanda.PENDENTE;
    }

    /** @return liga necessária, em gramas, para os rotores restantes (sem considerar sucata) */
    public double calcularMateriaPrimaNecessaria() { return getQuantidadeRestante() * tipo.getMassaBruta(); }

    /**
     * Define o custo de operação das máquinas por rotor, usado nas estimativas.
     *
     * @param custo custo em R$ por unidade
     */
    public void definirCustoOperacaoPorUnidade(double custo) { this.custoOperacaoPorUnidade = custo; }

    /** @return custo estimado, em R$, para produzir os rotores restantes */
    public double calcularCustoEstimado() { return getQuantidadeRestante() * custoOperacaoPorUnidade; }

    /**
     * Verifica se o custo estimado cabe no orçamento.
     *
     * @param orcamento budget disponível em R$
     * @return {@code true} se o pedido cabe no orçamento
     */
    public boolean ehViavel(double orcamento) { return calcularCustoEstimado() <= orcamento; }

    /** @return número sequencial do pedido */
    public int getNumero() { return numero; }
    /** @return tipo de rotor solicitado */
    public TipoRotor getTipo() { return tipo; }
    /** @return quantidade total solicitada */
    public int getQuantidadeProdutos() { return quantidadeProdutos; }
    /** @return quantidade já entregue (aprovada) */
    public int getQuantidadeProduzida() { return quantidadeProduzida; }
    /** @return rotores que ainda faltam entregar */
    public int getQuantidadeRestante() { return quantidadeProdutos - quantidadeProduzida; }
    /** @return status atual do pedido */
    public StatusDemanda getStatus() { return status; }

    /** @return linha formatada com os dados do pedido */
    @Override
    public String toString() {
        return String.format("#%03d | %-34s | %2d/%-2d | liga %7.1f g | custo R$ %9.2f | %s",
                numero, tipo.getNomeComercial(), quantidadeProduzida, quantidadeProdutos,
                calcularMateriaPrimaNecessaria(), calcularCustoEstimado(), status.getDescricao());
    }
}