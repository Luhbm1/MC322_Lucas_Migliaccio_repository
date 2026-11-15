package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DemandaTest {

    @Test
    void criaPedidoPendente() {
        Demanda d = new Demanda(TipoRotor.RALLY, 3);
        assertEquals(StatusDemanda.PENDENTE, d.getStatus());
        assertEquals(3, d.getQuantidadeRestante());
        assertEquals(1500.0, d.calcularMateriaPrimaNecessaria(), 1e-9);
    }

    @Test
    void rejeitaTipoNulo() {
        assertThrows(DemandaInvalidaException.class, () -> new Demanda(null, 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50})
    void rejeitaQuantidadeMenorQueUm(int qtd) {
        assertThrows(DemandaInvalidaException.class, () -> new Demanda(TipoRotor.RALLY, qtd));
    }

    @Test
    void numeroNaoEConsumidoPorPedidoInvalido() {
        Demanda a = new Demanda(TipoRotor.RALLY, 1);
        assertThrows(DemandaInvalidaException.class, () -> new Demanda(TipoRotor.RALLY, 0));
        Demanda b = new Demanda(TipoRotor.RALLY, 1);
        assertEquals(a.getNumero() + 1, b.getNumero());
    }

    @Test
    void cicloCompletoAteConcluir() {
        Demanda d = new Demanda(TipoRotor.PULLING, 2);
        d.iniciarProducao();
        d.registrarRotorAprovado();
        d.registrarRotorAprovado();
        d.concluir();
        assertEquals(StatusDemanda.CONCLUIDA, d.getStatus());
        assertEquals(0, d.getQuantidadeRestante());
    }

    @Test
    void naoIniciaPedidoQueNaoEstaPendente() {
        Demanda d = new Demanda(TipoRotor.RALLY, 1);
        d.iniciarProducao();
        assertThrows(DemandaInvalidaException.class, d::iniciarProducao);
        assertEquals(StatusDemanda.EM_PRODUCAO, d.getStatus());
    }

    @Test
    void naoRegistraRotorForaDeProducao() {
        Demanda d = new Demanda(TipoRotor.RALLY, 1);
        assertThrows(DemandaInvalidaException.class, d::registrarRotorAprovado);
        assertEquals(0, d.getQuantidadeProduzida());
    }

    @Test
    void naoConcluiComRotoresRestantes() {
        Demanda d = new Demanda(TipoRotor.RALLY, 2);
        d.iniciarProducao();
        d.registrarRotorAprovado();
        assertThrows(DemandaInvalidaException.class, d::concluir);
        assertEquals(StatusDemanda.EM_PRODUCAO, d.getStatus());
    }

    @Test
    void naoAlteraQuantidadeEmProducao() {
        Demanda d = new Demanda(TipoRotor.RALLY, 2);
        d.iniciarProducao();
        assertThrows(DemandaInvalidaException.class, () -> d.atualizarQuantidade(5));
        assertEquals(2, d.getQuantidadeProdutos());
    }

    @Test
    void novaQuantidadeDeveSuperarEntregues() {
        Demanda d = new Demanda(TipoRotor.RALLY, 3);
        d.iniciarProducao();
        d.registrarRotorAprovado();
        d.registrarRotorAprovado();
        d.pausar();
        assertThrows(DemandaInvalidaException.class, () -> d.atualizarQuantidade(2));
        assertEquals(3, d.getQuantidadeProdutos());
        d.atualizarQuantidade(4);
        assertEquals(2, d.getQuantidadeRestante());
    }

    @Test
    void atualizarReabrePedidoCancelado() {
        Demanda d = new Demanda(TipoRotor.RALLY, 1);
        d.cancelar();
        d.atualizarQuantidade(5);
        assertEquals(StatusDemanda.PENDENTE, d.getStatus());
    }

    @Test
    void naoCancelaDuasVezesNemConcluido() {
        Demanda c = new Demanda(TipoRotor.RALLY, 1);
        c.cancelar();
        assertThrows(DemandaInvalidaException.class, c::cancelar);

        Demanda e = new Demanda(TipoRotor.RALLY, 1);
        e.iniciarProducao();
        e.registrarRotorAprovado();
        e.concluir();
        assertThrows(DemandaInvalidaException.class, e::cancelar);
        assertEquals(StatusDemanda.CONCLUIDA, e.getStatus());
    }

    @Test
    void pausarSoAfetaPedidoEmProducao() {
        Demanda d = new Demanda(TipoRotor.RALLY, 1);
        d.pausar();
        assertEquals(StatusDemanda.PENDENTE, d.getStatus());
        d.iniciarProducao();
        d.pausar();
        assertEquals(StatusDemanda.PENDENTE, d.getStatus());
        d.cancelar();
        d.pausar();
        assertEquals(StatusDemanda.CANCELADA, d.getStatus());
    }

    @Test
    void custoEstimadoEViabilidade() {
        Demanda d = new Demanda(TipoRotor.RALLY, 3);
        d.definirCustoOperacaoPorUnidade(510.0);
        assertEquals(1530.0, d.calcularCustoEstimado(), 1e-9);
        assertTrue(d.ehViavel(1530.0));
        assertFalse(d.ehViavel(1529.99));
    }

    // ---------- Restauração ----------

    @Test
    void restauraPedidoConsistente() {
        Demanda d = Demanda.restaurar(77, TipoRotor.ARRANCADA, 4, 1, StatusDemanda.CANCELADA);
        assertEquals(77, d.getNumero());
        assertEquals(3, d.getQuantidadeRestante());
        assertEquals(StatusDemanda.CANCELADA, d.getStatus());
    }

    @Test
    void restauracaoRejeitaDadosInconsistentes() {
        assertThrows(DemandaInvalidaException.class,
                () -> Demanda.restaurar(0, TipoRotor.RALLY, 1, 0, StatusDemanda.PENDENTE));
        assertThrows(DemandaInvalidaException.class,
                () -> Demanda.restaurar(1, TipoRotor.RALLY, 2, 3, StatusDemanda.PENDENTE));
        assertThrows(DemandaInvalidaException.class,
                () -> Demanda.restaurar(1, TipoRotor.RALLY, 2, 1, StatusDemanda.CONCLUIDA));
        assertThrows(DemandaInvalidaException.class,
                () -> Demanda.restaurar(1, TipoRotor.RALLY, 2, 0, StatusDemanda.EM_PRODUCAO));
        assertThrows(DemandaInvalidaException.class,
                () -> Demanda.restaurar(1, null, 2, 0, StatusDemanda.PENDENTE));
    }
}
