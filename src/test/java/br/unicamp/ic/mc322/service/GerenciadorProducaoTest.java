package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.Fixtures;
import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GerenciadorProducaoTest {

    private MateriaPrima liga;
    private GerenciadorProducao gp;

    @BeforeEach
    void setUp() {
        liga = Fixtures.liga(20000.0);
        gp = Fixtures.planta(Cenario.IDEAL, liga);
    }

    private Demanda unica() { return gp.getDemandas().get(0); }

    // ---------- Pedidos ----------

    @Test
    void registraPedidoPendente() {
        gp.registrarDemanda(TipoRotor.RALLY, 2);
        assertEquals(1, gp.getDemandas().size());
        assertEquals(StatusDemanda.PENDENTE, unica().getStatus());
    }

    @Test
    void pedidoInvalidoNaoEntraNaLista() {
        assertThrows(DemandaInvalidaException.class, () -> gp.registrarDemanda(TipoRotor.RALLY, 0));
        assertThrows(DemandaInvalidaException.class, () -> gp.registrarDemanda(null, 1));
        assertTrue(gp.getDemandas().isEmpty());
    }

    @Test
    void listaDePedidosESomenteLeitura() {
        gp.registrarDemanda(TipoRotor.RALLY, 1);
        assertThrows(UnsupportedOperationException.class, () -> gp.getDemandas().clear());
    }

    @Test
    void operacoesEmPedidoInexistenteFalham() {
        assertThrows(DemandaInvalidaException.class, () -> gp.atualizarDemanda(99999, 3));
        assertThrows(DemandaInvalidaException.class, () -> gp.cancelarDemanda(99999));
        assertThrows(DemandaInvalidaException.class, () -> gp.fabricarDemanda(99999));
    }

    @Test
    void pedidoCanceladoNaoEProduzido() {
        gp.registrarDemanda(TipoRotor.RALLY, 1);
        gp.cancelarDemanda(unica().getNumero());
        double budget = gp.getBudget();
        assertThrows(DemandaInvalidaException.class, () -> gp.fabricarDemanda(unica().getNumero()));
        assertEquals(budget, gp.getBudget());
        assertEquals(20000.0, liga.getQuantidade(), 1e-9);
    }

    // ---------- Compras ----------

    @Test
    void compraDebitaBudgetEAumentaEstoque() {
        gp.comprarMateriaPrima(1000.0);
        assertEquals(100000.0 - 600.0, gp.getBudget(), 1e-9);
        assertEquals(21000.0, liga.getQuantidade(), 1e-9);
    }

    @Test
    void compraAcimaDoBudgetPreservaEstado() {
        assertThrows(RecursoInsuficienteException.class, () -> gp.comprarMateriaPrima(1_000_000.0));
        assertEquals(100000.0, gp.getBudget(), 1e-9);
        assertEquals(20000.0, liga.getQuantidade(), 1e-9);
    }

    @Test
    void compraNaoPositivaERejeitada() {
        assertThrows(IllegalArgumentException.class, () -> gp.comprarMateriaPrima(0));
        assertThrows(IllegalArgumentException.class, () -> gp.comprarMateriaPrima(Double.NaN));
    }

    // ---------- Produção: falhas preservam estado ----------

    @Test
    void producaoSemLigaPreservaEstado() {
        MateriaPrima pouca = Fixtures.liga(100.0);
        GerenciadorProducao g = Fixtures.planta(Cenario.IDEAL, pouca);
        g.registrarDemanda(TipoRotor.PULLING, 1);
        assertThrows(RecursoInsuficienteException.class, g::executarProximaProducao);
        assertEquals(100.0, pouca.getQuantidade(), 1e-9);
        assertEquals(100000.0, g.getBudget(), 1e-9);
        assertEquals(StatusDemanda.PENDENTE, g.getDemandas().get(0).getStatus());
    }

    @Test
    void producaoSemBudgetPreservaEstado() {
        MateriaPrima muita = Fixtures.liga(1_000_000.0);
        GerenciadorProducao g = Fixtures.planta(Cenario.APOCALIPTICO, muita);
        g.registrarDemanda(TipoRotor.RALLY, 20);            // 20 x 510 = 10200 > 8000
        int numero = g.getDemandas().get(0).getNumero();
        assertThrows(RecursoInsuficienteException.class, () -> g.fabricarDemanda(numero));
        assertEquals(8000.0, g.getBudget(), 1e-9);
        assertEquals(1_000_000.0, muita.getQuantidade(), 1e-9);
        assertEquals(StatusDemanda.PENDENTE, g.getDemandas().get(0).getStatus());
    }

    @Test
    void semPedidoElegivelLancaExcecao() {
        assertThrows(DemandaInvalidaException.class, gp::executarProximaProducao);
    }

    @Test
    void reparoComIndiceInexistenteERejeitado() {
        assertThrows(IllegalArgumentException.class, () -> gp.repararMaquina(0));
        assertThrows(IllegalArgumentException.class, () -> gp.repararMaquina(4));
        assertEquals(100000.0, gp.getBudget(), 1e-9);
    }

    @Test
    void estrategiaNulaERejeitada() {
        assertThrows(IllegalArgumentException.class, () -> gp.setEstrategia(null));
        assertNotNull(gp.getEstrategiaAtual());
    }

    // ---------- Produção: sucesso ----------

    @Test
    void producaoConcluiPedidoComContabilidadeCoerente() {
        Maquina.definirSemente(42);
        gp.registrarDemanda(TipoRotor.RALLY, 2);
        gp.executarProximaProducao();

        assertEquals(StatusDemanda.CONCLUIDA, unica().getStatus());
        assertEquals(2, unica().getQuantidadeProduzida());
        assertEquals(2, gp.getTamanhoArmazem());

        // cada tentativa (aprovada ou sucata) consome 500 g e R$ 510
        double tentativas = (20000.0 - liga.getQuantidade()) / 500.0;
        assertTrue(tentativas >= 2);
        assertEquals(Math.rint(tentativas), tentativas, 1e-9);
        assertEquals(100000.0 - Fixtures.CUSTO_CICLO * tentativas, gp.getBudget(), 1e-6);
    }
}
