package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.model.*;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDoPaddock;

/** Fábrica de objetos para os testes. */
public final class Fixtures {

    /** Custo de um ciclo completo da linha (CNC + cabine + bancada). */
    public static final double CUSTO_CICLO = 510.0;

    private Fixtures() { }

    public static MateriaPrima liga(double gramas) {
        return new MateriaPrima("MP001", "Liga Ti-6Al-4V", gramas, "g", 0.60, 2500.0);
    }

    public static GerenciadorProducao planta(Cenario cenario, MateriaPrima liga) {
        GerenciadorProducao gp = new GerenciadorProducao(liga, new Esteira("Esteira", 3000.0),
                cenario, new EstrategiaFilaDoPaddock());
        gp.adicionarMaquina(new CentroUsinagemCNC("CNC", 3000.0));
        gp.adicionarMaquina(new CabineEmbalagemVacuo("Cabine", 3000.0));
        gp.adicionarMaquina(new BancadaBalanceamento("Bancada", 3000.0));
        return gp;
    }

    public static GerenciadorProducao planta(Cenario cenario) {
        return planta(cenario, liga(cenario.getEstoqueInicialLiga()));
    }
}
