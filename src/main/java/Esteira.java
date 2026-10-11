/**
 * Esteira de transferência entre máquinas: transporta uma peça por vez,
 * respeitando um limite de peso, e só opera em movimento.
 *
 * @author Lucas Migliaccio
 */
public class Esteira {

    private String nome;
    private Object item;
    private boolean emMovimento;
    private double capacidadeMaxima;
    private int totalTransportado;
    private int bloqueios;

    /**
     * Cria uma esteira parada e vazia.
     *
     * @param nome             nome da esteira
     * @param capacidadeMaxima peso máximo por peça, em gramas
     */
    public Esteira(String nome, double capacidadeMaxima) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.emMovimento = false;
        this.item = null;
    }

    /** Coloca a esteira em movimento. */
    public void ligar() {
        this.emMovimento = true;
        System.out.println("[OK] " + nome + " em movimento.");
    }

    /** Para a esteira. */
    public void desligar() {
        this.emMovimento = false;
        System.out.println("[OK] " + nome + " parada.");
    }

    /**
     * @param peso peso da peça, em gramas
     * @return {@code true} se a peça cabe na esteira
     */
    public boolean verificarCapacidade(double peso) {
        return peso <= capacidadeMaxima;
    }

    /**
     * Coloca uma peça na esteira.
     *
     * @param novoItem peça transportada (liga ou rotor)
     * @param peso     peso da peça, em gramas
     * @throws MaquinaIndisponivelException se a esteira estiver parada ou ocupada
     * @throws PlantaException              se o peso exceder o limite
     */
    public void adicionarItem(Object novoItem, double peso) {
        if (!emMovimento) {
            bloqueios++;
            throw new MaquinaIndisponivelException(nome + " esta parada e nao transporta nada.");
        }
        if (this.item != null) {
            bloqueios++;
            throw new MaquinaIndisponivelException(nome + " ja esta ocupada. Uma peca por vez.");
        }
        if (!verificarCapacidade(peso)) {
            bloqueios++;
            throw new PlantaException("Carga de " + peso + " g excede o limite de "
                    + capacidadeMaxima + " g da " + nome + ".");
        }
        this.item = novoItem;
    }

    /**
     * Entrega a peça ao destino e libera a esteira.
     *
     * @return peça transportada ou {@code null} se a esteira estava vazia
     * @throws MaquinaIndisponivelException se a esteira estiver parada
     */
    public Object removerItem() {
        if (!emMovimento) {
            throw new MaquinaIndisponivelException(nome + " parada: a peca nao chega ao destino.");
        }
        Object transportado = this.item;
        if (transportado != null) totalTransportado++;
        this.item = null;
        return transportado;
    }

    /** @return nome da esteira */
    public String getNome() { return nome; }
    /** @return {@code true} se em movimento */
    public boolean estaEmMovimento() { return emMovimento; }
    /** @return {@code true} se não há peça na esteira */
    public boolean estaVazia() { return item == null; }
    /** @return limite de peso, em gramas */
    public double getCapacidadeMaxima() { return capacidadeMaxima; }
    /** @return total de peças entregues */
    public int getTotalTransportado() { return totalTransportado; }
    /** @return total de tentativas bloqueadas */
    public int getBloqueios() { return bloqueios; }

    /** @return resumo do estado da esteira */
    @Override
    public String toString() {
        return String.format("Esteira %-10s | %s | %s | limite: %.1f g | transportados: %d | bloqueios: %d",
                nome,
                emMovimento ? "em movimento" : "parada",
                estaVazia() ? "livre" : "ocupada",
                capacidadeMaxima, totalTransportado, bloqueios);
    }
}
