/**
 * Rotor de compressor fabricado pela planta.
 * <p>
 * Acumula a probabilidade de falha gerada por defeitos nas máquinas; acima de
 * 30% (ou se reprovado) é considerado em risco na auditoria. Um contador
 * estático registra todos os blocos usinados.
 *
 * @author Lucas Migliaccio
 */
public abstract class Produto implements Auditavel {

    private static final double LIMIAR_RISCO = 0.30;
    private static int totalProdutosFabricados = 0;

    private String id;
    private String nome;
    private StatusRotor status;
    private double quantidadeMateriaPrimaPorUnidade;
    private double qualidade;
    private double probabilidadeFalhaAcumulada;
    private String lote;

    /**
     * Cria um rotor aguardando usinagem e incrementa o contador global.
     *
     * @param id                               identificador único
     * @param nome                             descrição técnica
     * @param quantidadeMateriaPrimaPorUnidade liga consumida, em gramas
     * @param qualidade                        nível de qualidade (0 a 1); maior = inspeção mais rigorosa
     */
    public Produto(String id, String nome, double quantidadeMateriaPrimaPorUnidade, double qualidade) {
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
        this.status = StatusRotor.AGUARDANDO_USINAGEM;
        this.lote = "-";
        totalProdutosFabricados++;
    }

    /** Executa a usinagem específica do modelo e marca o rotor como usinado. */
    public abstract void processar();

    /** @return tempo estimado de produção, em minutos */
    public abstract int calcularTempoProducao();

    /** @return tipo do rotor */
    public abstract TipoRotor getTipo();

    /**
     * Soma risco de falha ao rotor, limitado a 100%. Valores não positivos são ignorados.
     *
     * @param incremento risco adicional (0 a 1)
     */
    public void aumentarProbabilidadeFalha(double incremento) {
        if (incremento > 0) {
            probabilidadeFalhaAcumulada = Math.min(1.0, probabilidadeFalhaAcumulada + incremento);
        }
    }

    /** {@inheritDoc} Verdadeiro se o risco for de pelo menos 30% ou o rotor estiver reprovado. */
    @Override
    public boolean precisaManutencao() {
        return probabilidadeFalhaAcumulada >= LIMIAR_RISCO || status == StatusRotor.REPROVADO;
    }

    /** {@inheritDoc} */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("[ROTOR] %-9s %-34s qualidade %.1f | risco %3.0f%% | lote %-12s | %s",
                id, getTipo().getNomeComercial(), qualidade, probabilidadeFalhaAcumulada * 100, lote,
                precisaManutencao() ? Cores.pintar("RISCO ALTO", Cores.VERMELHO) : Cores.pintar("OK", Cores.VERDE));
    }

    /**
     * Vincula o rotor ao lote de liga e ao pedido de origem.
     *
     * @param lote código do lote
     */
    public void registrarLote(String lote) { this.lote = lote; }

    /** @return identificador do rotor */
    public String getId() { return id; }
    /** @return descrição técnica */
    public String getNome() { return nome; }
    /** @return etapa atual na linha */
    public StatusRotor getStatus() { return status; }
    /** @param status nova etapa do rotor */
    public void setStatus(StatusRotor status) { this.status = status; }
    /** @return liga consumida por unidade, em gramas */
    public double getQuantidadeMateriaPrimaPorUnidade() { return quantidadeMateriaPrimaPorUnidade; }
    /** @return nível de qualidade (0 a 1) */
    public double getQualidade() { return qualidade; }
    /** @return risco de falha acumulado (0 a 1) */
    public double getProbabilidadeFalhaAcumulada() { return probabilidadeFalhaAcumulada; }
    /** @return código do lote */
    public String getLote() { return lote; }

    /** @return total de rotores criados desde o início da execução */
    public static int getTotalProdutosFabricados() { return totalProdutosFabricados; }
}