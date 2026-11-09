package main.java.br.unicamp.ic.mc322.model;

import main.java.br.unicamp.ic.mc322.exception.PlantaException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Linha de produção que agrupa máquinas (agregação 1..*).
 * <p>
 * As máquinas são criadas fora da linha e podem existir sem ela. A linha só
 * define a ordem de passagem dos rotores, opera as máquinas em conjunto e
 * propaga a auditoria em cascata (Linha &gt; Máquina &gt; Componentes).
 * A coleção interna nunca é exposta para modificação.
 *
 * @author Lucas Migliaccio
 */
public class LinhaDeProducao implements Auditavel {

    private final String nome;
    private final List<Maquina> maquinas = new ArrayList<>();

    /**
     * Cria uma linha vazia.
     *
     * @param nome nome da linha
     */
    public LinhaDeProducao(String nome) { this.nome = nome; }

    /**
     * Adiciona uma máquina ao final da linha.
     *
     * @param maquina máquina a adicionar
     * @throws IllegalArgumentException se a máquina for nula
     * @throws PlantaException          se a máquina já estiver na linha
     */
    public void adicionarMaquina(Maquina maquina) {
        if (maquina == null) throw new IllegalArgumentException("Maquina nao informada.");
        if (maquinas.contains(maquina)) {
            throw new PlantaException(maquina.getNome() + " ja faz parte da " + nome + ".");
        }
        maquinas.add(maquina);
    }

    /** @return visão somente leitura das máquinas, na ordem da linha */
    public List<Maquina> getMaquinas() { return Collections.unmodifiableList(maquinas); }

    /** @return quantidade de máquinas na linha */
    public int getQuantidadeMaquinas() { return maquinas.size(); }

    /** @return nome da linha */
    public String getNome() { return nome; }

    /** @return soma dos custos de operação de um ciclo completo, em R$ */
    public double calcularCustoCiclo() {
        double total = 0;
        for (Maquina m : maquinas) total += m.getCustoOperacao();
        return total;
    }

    /** @return primeira máquina quebrada da linha ou {@code null} se todas operam */
    public Maquina buscarMaquinaQuebrada() {
        for (Maquina m : maquinas) {
            if (m.estaQuebrada()) return m;
        }
        return null;
    }

    /** Liga todas as máquinas da linha. */
    public void ligar() { for (Maquina m : maquinas) m.ligar(); }

    /** Desliga todas as máquinas da linha. */
    public void desligar() { for (Maquina m : maquinas) m.desligar(); }

    /**
     * Monta um resumo operacional da linha. Os dados dos componentes são lidos
     * por métodos repassadores da máquina (Lei de Deméter).
     *
     * @return texto com saúde, temperatura do motor e garras folgadas de cada máquina
     */
    public String gerarResumo() {
        StringBuilder sb = new StringBuilder(nome + " | " + maquinas.size() + " maquina(s) | custo/ciclo R$ "
                + String.format("%.2f", calcularCustoCiclo()));
        for (int i = 0; i < maquinas.size(); i++) {
            Maquina m = maquinas.get(i);
            sb.append(String.format("%n  %d. %-42s saude %5.1f%% | motor %5.1f C | garras folgadas %d/%d",
                    i + 1, m.getNome(), m.getSaude(), m.lerTemperaturaMotor(),
                    m.contarGarrasFolgadas(), m.getQuantidadeGarras()));
        }
        return sb.toString();
    }

    /** {@inheritDoc} Relatório hierárquico e indentado: Linha &gt; Máquina &gt; Componentes. */
    @Override
    public String gerarRelatorioDiagnostico() {
        StringBuilder sb = new StringBuilder(nome + (precisaManutencao() ? " [ATENCAO]" : " [OK]"));
        for (Maquina m : maquinas) {
            sb.append("\n  > ").append(m.gerarRelatorioDiagnostico().replace("\n", "\n    "));
        }
        if (maquinas.isEmpty()) sb.append("\n  (linha vazia)");
        return sb.toString();
    }

    /** {@inheritDoc} Verdadeiro se qualquer máquina da linha precisar de manutenção. */
    @Override
    public boolean precisaManutencao() {
        for (Maquina m : maquinas) {
            if (m.precisaManutencao()) return true;
        }
        return false;
    }
}
