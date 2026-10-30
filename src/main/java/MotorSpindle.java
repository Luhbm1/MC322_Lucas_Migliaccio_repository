/**
 * Motor do fuso principal (Parte 1:1 de {@link Maquina}).
 * <p>
 * Aquece entre 2 e 8 °C a cada ciclo. Acima da temperatura máxima fica
 * superaquecido e adiciona 8% de risco a cada rotor processado.
 * Só existe dentro da máquina que o criou (composição estrita).
 *
 * @author Lucas Migliaccio
 */
public class MotorSpindle implements Auditavel {

    private static final double TEMP_AMBIENTE = 25.0;
    private static final double PENALIDADE = 0.08;

    private final double temperaturaMaxima;
    private double temperatura;

    /**
     * Cria um motor frio, na temperatura ambiente.
     *
     * @param temperaturaMaxima limite seguro de operação, em °C
     */
    public MotorSpindle(double temperaturaMaxima) {
        this.temperaturaMaxima = temperaturaMaxima;
        this.temperatura = TEMP_AMBIENTE;
    }

    /**
     * Executa um ciclo: o motor aquece entre 2 e 8 °C.
     *
     * @param sorteio valor aleatório entre 0 e 1
     */
    public void operar(double sorteio) { temperatura += 2.0 + sorteio * 6.0; }

    /** Revisão: troca o fluido de arrefecimento e volta à temperatura ambiente. */
    public void revisar() { temperatura = TEMP_AMBIENTE; }

    /** @return {@code true} se a temperatura passou do limite seguro */
    public boolean estaSuperaquecido() { return temperatura > temperaturaMaxima; }

    /** @return risco extra (0 a 1) que o motor impõe ao rotor */
    public double calcularPenalidade() { return estaSuperaquecido() ? PENALIDADE : 0.0; }

    /** @return temperatura atual, em °C */
    public double getTemperatura() { return temperatura; }

    /** {@inheritDoc} */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("Motor spindle: %.1f/%.0f C %s", temperatura, temperaturaMaxima,
                estaSuperaquecido() ? Cores.pintar("SUPERAQUECIDO", Cores.VERMELHO) : "OK");
    }

    /** {@inheritDoc} Verdadeiro quando o motor está superaquecido. */
    @Override
    public boolean precisaManutencao() { return estaSuperaquecido(); }
}
