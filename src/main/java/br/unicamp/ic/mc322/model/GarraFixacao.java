package main.java.br.unicamp.ic.mc322.model;

import main.java.br.unicamp.ic.mc322.util.Cores;

/**
 * Garra hidráulica que prende o bloco de titânio (Parte 1:N de {@link Maquina}).
 * <p>
 * Perde entre 0 e 3% de pressão a cada ciclo. Abaixo de 60% fica folgada,
 * o bloco trepida e cada garra folgada adiciona 4% de risco ao rotor.
 *
 * @author Lucas Migliaccio
 */
public class GarraFixacao implements Auditavel {

    private static final double PRESSAO_MINIMA = 60.0;
    private static final double PENALIDADE = 0.04;

    private final int posicao;
    private double pressao;

    /**
     * Cria uma garra com pressão máxima (100%).
     *
     * @param posicao posição da garra na mesa (1, 2, ...)
     */
    public GarraFixacao(int posicao) {
        this.posicao = posicao;
        this.pressao = 100.0;
    }

    /**
     * Executa um ciclo: perde entre 0 e 3% de pressão (mínimo 0%).
     *
     * @param sorteio valor aleatório entre 0 e 1
     */
    public void operar(double sorteio) { pressao = Math.max(0.0, pressao - sorteio * 3.0); }

    /** Revisão: troca as vedações e restaura a pressão a 100%. */
    public void revisar() { pressao = 100.0; }

    /** @return {@code true} se a pressão está abaixo do mínimo de 60% */
    public boolean estaFolgada() { return pressao < PRESSAO_MINIMA; }

    /** @return risco extra (0 a 1) que a garra impõe ao rotor */
    public double calcularPenalidade() { return estaFolgada() ? PENALIDADE : 0.0; }

    /** {@inheritDoc} */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("Garra %d: pressao %.0f%% %s", posicao, pressao,
                estaFolgada() ? Cores.pintar("FOLGADA", Cores.VERMELHO) : "OK");
    }

    /** {@inheritDoc} Verdadeiro quando a garra está folgada. */
    @Override
    public boolean precisaManutencao() { return estaFolgada(); }
}
