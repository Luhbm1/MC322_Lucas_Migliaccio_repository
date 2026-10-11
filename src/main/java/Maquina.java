import java.util.Random;

/**
 * Máquina da linha de produção.
 * <p>
 * Regras principais:
 * <ul>
 *   <li>cada ciclo reduz a saúde aleatoriamente até o desgaste máximo do cenário;</li>
 *   <li>a probabilidade de falha é inversamente proporcional à saúde (máx. 95%);</li>
 *   <li>saúde abaixo do limiar de manutenção (padrão 30%) = desgastada;</li>
 *   <li>saúde de até 5% = quebrada: recusa rotores até ser reparada;</li>
 *   <li>o reparo custa {@code (100 - saude) * custoOperacao * 0.2}.</li>
 * </ul>
 *
 * @author Lucas Migliaccio
 */
public abstract class Maquina implements Auditavel {

    private static final Random GERADOR = new Random();
    private static final double LIMIAR_QUEBRA = 5.0;

    private String nome;
    private boolean ligada;
    private double capacidadeMaxima;
    private double probabilidadeFalhaBase;
    private double custoOperacao;
    private double saude = 100.0;
    private double limiarManutencao = 30.0;
    private double multiplicadorFalha = 1.0;
    private double desgasteMaximo = 3.0;
    private int historicoFalhas;
    private int usos;

    /**
     * Cria uma máquina desligada com saúde 100%.
     *
     * @param nome                   nome exibido nos relatórios
     * @param capacidadeMaxima       maior bloco aceito, em gramas
     * @param probabilidadeFalhaBase chance de falha com saúde 100% (0 a 1)
     * @param custoOperacao          custo em R$ por ciclo
     */
    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalhaBase, double custoOperacao) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalhaBase = probabilidadeFalhaBase;
        this.custoOperacao = custoOperacao;
    }

    /**
     * Processa o rotor nesta etapa da linha.
     *
     * @param produto rotor a processar
     * @return {@code true} se o rotor segue na linha; {@code false} se foi reprovado
     * @throws MaquinaIndisponivelException se a máquina estiver desligada ou quebrada
     * @throws PlantaException              se o bloco exceder a capacidade da máquina
     */
    public abstract boolean processar(Produto produto);

    /** @return categoria da máquina (processamento, embalagem, inspeção) */
    public abstract String getTipo();

    /**
     * Fixa a semente do sorteio para tornar a simulação reproduzível.
     *
     * @param semente semente do gerador aleatório
     */
    public static void definirSemente(long semente) { GERADOR.setSeed(semente); }

    /**
     * Aplica os parâmetros de falha e desgaste do cenário.
     *
     * @param cenario cenário ativo
     */
    public void configurarCenario(Cenario cenario) {
        this.multiplicadorFalha = cenario.getMultiplicadorFalha();
        this.desgasteMaximo = cenario.getDesgasteMaximo();
    }

    /** Liga a máquina. */
    public void ligar() {
        ligada = true;
        System.out.println("[OK] " + nome + " ligado(a).");
    }

    /** Desliga a máquina. */
    public void desligar() {
        ligada = false;
        System.out.println("[OK] " + nome + " desligado(a).");
    }

    /** @return probabilidade de falha atual (0 a 0,95), considerando saúde e cenário */
    public double getProbabilidadeFalhaEfetiva() {
        return Math.min(0.95, probabilidadeFalhaBase * multiplicadorFalha * 100.0 / Math.max(saude, 1.0));
    }

    /**
     * Sorteia se ocorreu falha neste ciclo e registra no histórico.
     *
     * @return {@code true} se houve falha
     */
    protected boolean verificarFalha() {
        boolean falhou = GERADOR.nextDouble() < getProbabilidadeFalhaEfetiva();
        if (falhou) historicoFalhas++;
        return falhou;
    }

    /** @return número aleatório entre 0 (inclusivo) e 1 (exclusivo) */
    protected static double sortear() { return GERADOR.nextDouble(); }

    /**
     * Verifica se o bloco cabe na máquina.
     *
     * @param peso massa do bloco, em gramas
     * @return {@code true} se o peso não excede a capacidade
     */
    public boolean suporta(double peso) { return peso <= capacidadeMaxima; }

    /**
     * Valida o uso da máquina e, só então, aplica o desgaste do ciclo.
     *
     * @param produto rotor que será processado
     * @throws MaquinaIndisponivelException se a máquina estiver desligada ou quebrada
     * @throws PlantaException              se o bloco exceder a capacidade
     */
    protected void validarOperacao(Produto produto) {
        if (!ligada) {
            throw new MaquinaIndisponivelException(nome + " esta desligado(a); o rotor "
                    + produto.getId() + " nao pode ser processado.");
        }
        if (estaQuebrada()) {
            throw new MaquinaIndisponivelException(nome
                    + " esta quebrado(a). Repare-o(a) no menu 'Auditoria e oficina'.");
        }
        if (!suporta(produto.getQuantidadeMateriaPrimaPorUnidade())) {
            throw new PlantaException("Bloco de " + produto.getQuantidadeMateriaPrimaPorUnidade()
                    + " g excede a capacidade de " + capacidadeMaxima + " g do(a) " + nome + ".");
        }
        aplicarDesgaste();
    }

    private void aplicarDesgaste() {
        usos++;
        saude = Math.max(0.0, saude - GERADOR.nextDouble() * desgasteMaximo);
        if (estaQuebrada()) {
            System.out.println(Cores.pintar("[X] " + nome + " soltou fumaca e parou! Saude critica.", Cores.VERMELHO));
        }
    }

    /** @return estado operacional derivado da saúde */
    public EstadoMaquina getEstado() {
        if (saude <= LIMIAR_QUEBRA) return EstadoMaquina.QUEBRADA;
        if (precisaManutencao()) return EstadoMaquina.DESGASTADA;
        return EstadoMaquina.OPERANTE;
    }

    /** @return {@code true} se a máquina está quebrada */
    public boolean estaQuebrada() { return getEstado() == EstadoMaquina.QUEBRADA; }

    /** @return custo do reparo completo, em R$ */
    public double calcularCustoReparo() { return (100.0 - saude) * custoOperacao * 0.2; }

    /** Restaura a saúde para 100%. A cobrança é feita pelo {@link GerenciadorProducao}. */
    public void reparar() {
        saude = 100.0;
        System.out.println("[OK] " + nome + " revisado(a) pela oficina. Saude 100%.");
    }

    /** {@inheritDoc} Verdadeiro se a saúde estiver abaixo do limiar de manutenção. */
    @Override
    public boolean precisaManutencao() { return saude < limiarManutencao; }

    /** {@inheritDoc} */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("[%-13s] %-42s saude %5.1f%% | falha %4.1f%% | usos %3d | falhas %2d | %s",
                getTipo(), nome, saude, getProbabilidadeFalhaEfetiva() * 100, usos, historicoFalhas,
                getEstado().getDescricao());
    }

    /**
     * Define o limiar de saúde abaixo do qual a máquina pede manutenção.
     *
     * @param limiar valor entre 5 e 100 (exclusivos)
     * @throws IllegalArgumentException se o limiar estiver fora da faixa
     */
    public void setLimiarManutencao(double limiar) {
        if (limiar <= LIMIAR_QUEBRA || limiar >= 100) {
            throw new IllegalArgumentException("Limiar de manutencao deve estar entre "
                    + LIMIAR_QUEBRA + " e 100.");
        }
        this.limiarManutencao = limiar;
    }

    /** @return {@code true} se a máquina está ligada */
    public boolean estaLigada() { return ligada; }
    /** @return nome da máquina */
    public String getNome() { return nome; }
    /** @return custo por ciclo, em R$ */
    public double getCustoOperacao() { return custoOperacao; }
    /** @return capacidade máxima, em gramas */
    public double getCapacidadeMaxima() { return capacidadeMaxima; }
    /** @return saúde atual (0 a 100) */
    public double getSaude() { return saude; }
    /** @return número de falhas ocorridas */
    public int getHistoricoFalhas() { return historicoFalhas; }
}
