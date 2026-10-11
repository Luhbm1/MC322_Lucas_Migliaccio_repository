/**
 * Bancada de balanceamento dinâmico: aprova ou reprova o rotor.
 * <p>
 * Chance de rejeição = {@code qualidade * 0.25 + risco acumulado} (máx. 95%).
 * Uma falha da bancada inverte o laudo (sensor descalibrado).
 * Custo de R$ 120 por ciclo, falha base de 8%.
 *
 * @author Lucas Migliaccio
 */
public class BancadaBalanceamento extends Maquina {

    private static final double PESO_QUALIDADE = 0.25;
    private int produtosInspecionados;

    /**
     * @param nome             nome da máquina
     * @param capacidadeMaxima maior bloco aceito, em gramas
     */
    public BancadaBalanceamento(String nome, double capacidadeMaxima) {
        super(nome, capacidadeMaxima, 0.08, 120.0);
    }

    /** {@inheritDoc} */
    @Override
    public boolean processar(Produto produto) {
        validarOperacao(produto);
        produtosInspecionados++;

        boolean aprovado = sortear() >= calcularChanceRejeicao(produto);
        if (verificarFalha()) {
            aprovado = !aprovado;
            System.out.println(Cores.pintar("[!] Sensor descalibrado: laudo incorreto emitido!", Cores.AMARELO));
        }

        produto.setStatus(aprovado ? StatusRotor.APROVADO : StatusRotor.REPROVADO);
        System.out.println(aprovado
                ? Cores.pintar("[OK] " + produto.getId() + " girou liso a 150.000 rpm. Aprovado!", Cores.VERDE)
                : Cores.pintar("[X] " + produto.getId() + " vibrou na bancada. Sucata!", Cores.VERMELHO));
        return aprovado;
    }

    private double calcularChanceRejeicao(Produto produto) {
        return Math.min(0.95, produto.getQualidade() * PESO_QUALIDADE + produto.getProbabilidadeFalhaAcumulada());
    }

    /** @return total de rotores inspecionados */
    public int getTotalInspecionados() { return produtosInspecionados; }

    /** {@inheritDoc} */
    @Override
    public String getTipo() { return "INSPECAO"; }
}
