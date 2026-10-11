/**
 * Centro de usinagem CNC: usina o rotor. Uma falha adiciona 15% de risco ao rotor.
 * Custo de R$ 350 por ciclo, falha base de 20%.
 *
 * @author Lucas Migliaccio
 */
public class CentroUsinagemCNC extends Maquina {

    private static final double AUMENTO_RISCO = 0.15;

    /**
     * @param nome             nome da máquina
     * @param capacidadeMaxima maior bloco aceito, em gramas
     */
    public CentroUsinagemCNC(String nome, double capacidadeMaxima) {
        super(nome, capacidadeMaxima, 0.20, 350.0);
    }

    /** {@inheritDoc} Sempre retorna {@code true}: defeitos viram risco, não reprovação. */
    @Override
    public boolean processar(Produto produto) {
        validarOperacao(produto);
        System.out.println("[..] " + getNome() + " usinando " + produto.getId() + "...");
        produto.processar();
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(AUMENTO_RISCO);
            System.out.println(Cores.pintar("[!] Vibracao no fuso: micro-rebarbas nas pas!", Cores.AMARELO));
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getTipo() { return "PROCESSAMENTO"; }
}