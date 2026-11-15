package br.unicamp.ic.mc322.model;

import java.util.List;

/**
 * Retrato persistível do estado de uma máquina e de seus componentes.
 *
 * @param tipo             categoria da máquina ({@link br.unicamp.ic.mc322.model.Maquina#getTipo()})
 * @param nome             nome da máquina
 * @param saude            saúde de 0 a 100
 * @param usos             ciclos executados (>= 0)
 * @param falhas           falhas registradas (>= 0)
 * @param limiar           limiar de manutenção (entre 5 e 100, exclusivos)
 * @param temperaturaMotor temperatura do motor spindle, em °C (>= 0)
 * @param leituraSensor    desvio de calibração do sensor de vibração (>= 0)
 * @param pressoesGarras   pressão de cada garra (0 a 100)
 * @author Lucas Migliaccio
 */
public record RegistroMaquina(String tipo, String nome, double saude, int usos, int falhas,
                              double limiar, double temperaturaMotor, double leituraSensor,
                              List<Double> pressoesGarras) {

    /** @throws IllegalArgumentException se algum valor violar as regras do domínio */
    public RegistroMaquina {
        if (tipo == null || tipo.isBlank()) throw new IllegalArgumentException("Tipo de maquina ausente.");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome de maquina ausente.");
        if (!Double.isFinite(saude) || saude < 0 || saude > 100)
            throw new IllegalArgumentException("Saude fora da faixa 0..100: " + saude);
        if (usos < 0 || falhas < 0) throw new IllegalArgumentException("Usos/falhas negativos.");
        if (!Double.isFinite(limiar) || limiar <= 5 || limiar >= 100)
            throw new IllegalArgumentException("Limiar de manutencao invalido: " + limiar);
        if (!Double.isFinite(temperaturaMotor) || temperaturaMotor < 0)
            throw new IllegalArgumentException("Temperatura do motor invalida: " + temperaturaMotor);
        if (!Double.isFinite(leituraSensor) || leituraSensor < 0)
            throw new IllegalArgumentException("Leitura do sensor invalida: " + leituraSensor);
                if (pressoesGarras == null)
            throw new IllegalArgumentException("Garras ausentes.");
        for (Double p : pressoesGarras) {
            if (p == null || !Double.isFinite(p) || p < 0 || p > 100)
                throw new IllegalArgumentException("Pressao de garra fora da faixa 0..100: " + p);
        }
        pressoesGarras = List.copyOf(pressoesGarras);
        for (double p : pressoesGarras) {
            if (!Double.isFinite(p) || p < 0 || p > 100)
                throw new IllegalArgumentException("Pressao de garra fora da faixa 0..100: " + p);
        }
        pressoesGarras = List.copyOf(pressoesGarras);
    }
}
