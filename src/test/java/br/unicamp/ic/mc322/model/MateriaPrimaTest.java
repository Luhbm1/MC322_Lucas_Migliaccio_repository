package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.Fixtures;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MateriaPrimaTest {

    private MateriaPrima liga;

    @BeforeEach
    void setUp() { liga = Fixtures.liga(3000.0); }

    @Test
    void consumoValidoReduzEstoque() {
        liga.consumir(500.0);
        assertEquals(2500.0, liga.getQuantidade(), 1e-9);
    }

    @Test
    void consumirTudoZeraEstoque() {
        liga.consumir(3000.0);
        assertEquals(0.0, liga.getQuantidade(), 1e-9);
    }

    @Test
    void consumoAcimaDoEstoquePreservaEstoque() {
        assertThrows(RecursoInsuficienteException.class, () -> liga.consumir(3000.1));
        assertEquals(3000.0, liga.getQuantidade(), 1e-9);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void consumoNaoPositivoERejeitado(double qtd) {
        assertThrows(IllegalArgumentException.class, () -> liga.consumir(qtd));
        assertEquals(3000.0, liga.getQuantidade(), 1e-9);
    }

    @Test
    void reposicaoAumentaEstoque() {
        liga.adicionarEstoque(1000.0);
        assertEquals(4000.0, liga.getQuantidade(), 1e-9);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -5.0, Double.NaN})
    void reposicaoInvalidaERejeitada(double qtd) {
        assertThrows(IllegalArgumentException.class, () -> liga.adicionarEstoque(qtd));
        assertEquals(3000.0, liga.getQuantidade(), 1e-9);
    }

    @Test
    void verificaDisponibilidade() {
        assertTrue(liga.verificarDisponibilidade(3000.0));
        assertFalse(liga.verificarDisponibilidade(3000.1));
        assertFalse(liga.verificarDisponibilidade(0.0));
    }

    @Test
    void alertaDeEstoqueBaixo() {
        assertFalse(liga.estoqueBaixo());
        liga.consumir(500.0);              // fica em 2500 = mínimo
        assertTrue(liga.estoqueBaixo());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, Double.NaN, Double.POSITIVE_INFINITY})
    void definirQuantidadeInvalidaPreservaEstoque(double v) {
        assertThrows(IllegalArgumentException.class, () -> liga.definirQuantidade(v));
        assertEquals(3000.0, liga.getQuantidade(), 1e-9);
    }
}
