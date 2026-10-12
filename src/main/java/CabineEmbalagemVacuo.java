/**
 * Cabine de selagem a vácuo: embala o rotor. Uma falha adiciona 10% de risco.
 * Custo de R$40 por ciclo, falha base de 10%.
 *
 * @author Lucas Migliaccio
 */
public class CabineEmbalagemVacuo extends Maquina {

    private static final double AUMENTO_RISCO = 0.10;

    /**
     * @param nome             nome da máquina
     * @param capacidadeMaxima maior bloco aceito, em gramas
     */
    public CabineEmbalagemVacuo(String nome, double capacidadeMaxima) {
        super(nome, capacidadeMaxima, 0.10, 40.0);
    }

    /** {@inheritDoc} Sempre retorna {@code true}: defeitos viram risco, não reprovação. */
    @Override
    public boolean processar(Produto produto) {
        validarOperacao(produto);
        produto.setStatus(StatusRotor.EMBALADO);
        System.out.println("[OK] " + produto.getId() + " selado em embalagem antiestatica.");
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(AUMENTO_RISCO);
            System.out.println(Cores.pintar("[!] Selagem irregular. Risco de falha aumentado.", Cores.AMARELO));
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getTipo() { return "EMBALAGEM"; }
}