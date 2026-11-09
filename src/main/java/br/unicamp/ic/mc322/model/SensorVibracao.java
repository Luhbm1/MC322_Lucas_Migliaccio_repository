package main.java.br.unicamp.ic.mc322.model;

import main.java.br.unicamp.ic.mc322.util.Cores;

/**
 * Acelerômetro de vibração (Parte 1:1 de {@link Maquina}).
 * <p>
 * A cada ciclo há 15% de chance de perder 10% de calibração. Com desvio de
 * 30% ou mais fica descalibrado, deixa passar vibrações e adiciona 5% de
 * risco a cada rotor processado.
 *
 * @author Lucas Migliaccio
 */
public class SensorVibracao implements Auditavel {

    private static final double CHANCE_DESVIO = 0.15;
    private static final double PASSO_DESVIO = 0.10;
    private static final double LIMITE_DESVIO = 0.30;
    private static final double PENALIDADE = 0.05;

    private double desvioCalibracao;

    /** Cria um sensor recém-calibrado. */
    public SensorVibracao() { this.desvioCalibracao = 0.0; }

    /**
     * Executa um ciclo: há 15% de chance de o sensor perder calibração.
     *
     * @param sorteio valor aleatório entre 0 e 1
     */
    public void operar(double sorteio) {
        if (sorteio < CHANCE_DESVIO) desvioCalibracao += PASSO_DESVIO;
    }

    /** Revisão: recalibra o sensor. */
    public void revisar() { desvioCalibracao = 0.0; }

    /** @return {@code true} se o desvio atingiu o limite de 30% */
    public boolean estaDescalibrado() { return desvioCalibracao >= LIMITE_DESVIO - 1e-9; }

    /** @return risco extra (0 a 1) que o sensor impõe ao rotor */
    public double calcularPenalidade() { return estaDescalibrado() ? PENALIDADE : 0.0; }

    /** {@inheritDoc} */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("Sensor de vibracao: desvio %.0f%% %s", desvioCalibracao * 100,
                estaDescalibrado() ? Cores.pintar("DESCALIBRADO", Cores.VERMELHO) : "OK");
    }

    /** {@inheritDoc} Verdadeiro quando o sensor está descalibrado. */
    @Override
    public boolean precisaManutencao() { return estaDescalibrado(); }
}
