package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaquinaTest {

    private Maquina cnc;
    private double sensorPadrao;

    @BeforeEach
    void setUp() {
        cnc = new CentroUsinagemCNC("CNC", 3000.0);
        sensorPadrao = cnc.exportarEstado().leituraSensor();
    }

    private RegistroMaquina registro(String tipo, double saude, double temp, List<Double> garras) {
        return new RegistroMaquina(tipo, "CNC", saude, 5, 1, 30.0, temp, sensorPadrao, garras);
    }

    @Test
    void maquinaNovaEstaOperante() {
        assertEquals(EstadoMaquina.OPERANTE, cnc.getEstado());
        assertEquals(0, cnc.contarGarrasFolgadas());
    }

    @Test
    void restauraEstadoDaMaquinaEComponentes() {
        cnc.restaurarEstado(registro("PROCESSAMENTO", 40.0, 50.0, List.of(100.0, 90.0, 80.0, 50.0)));
        assertEquals(40.0, cnc.getSaude(), 1e-9);
        assertEquals(50.0, cnc.lerTemperaturaMotor(), 1e-9);
        assertEquals(1, cnc.contarGarrasFolgadas());
        assertEquals(1, cnc.getHistoricoFalhas());
    }

    @Test
    void registroComValoresInvalidosERejeitado() {
        List<Double> ok = List.of(100.0, 100.0, 100.0, 100.0);
        assertThrows(IllegalArgumentException.class, () -> registro("PROCESSAMENTO", 150.0, 25.0, ok));
        assertThrows(IllegalArgumentException.class, () -> registro("PROCESSAMENTO", Double.NaN, 25.0, ok));
        assertThrows(IllegalArgumentException.class,
                () -> registro("PROCESSAMENTO", 50.0, 25.0, List.of(100.0, 120.0, 100.0, 100.0)));
    }

    @Test
    void registroIncompativelNaoAlteraMaquina() {
        RegistroMaquina outroTipo = registro("EMBALAGEM", 10.0, 25.0, List.of(100.0, 100.0, 100.0, 100.0));
        RegistroMaquina poucasGarras = registro("PROCESSAMENTO", 10.0, 25.0, List.of(100.0));
        assertThrows(IllegalArgumentException.class, () -> cnc.restaurarEstado(outroTipo));
        assertThrows(IllegalArgumentException.class, () -> cnc.restaurarEstado(poucasGarras));
        assertEquals(100.0, cnc.getSaude(), 1e-9);
    }

    @Test
    void reparoRestauraMaquinaEComponentes() {
        cnc.restaurarEstado(registro("PROCESSAMENTO", 10.0, 95.0, List.of(10.0, 10.0, 10.0, 10.0)));
        assertTrue(cnc.precisaManutencao());
        cnc.reparar();
        assertEquals(100.0, cnc.getSaude(), 1e-9);
        assertEquals(0, cnc.contarGarrasFolgadas());
        assertEquals(25.0, cnc.lerTemperaturaMotor(), 1e-9);
        assertEquals(EstadoMaquina.OPERANTE, cnc.getEstado());
    }

    @Test
    void maquinaQuebradaRecusaRotor() {
        cnc.restaurarEstado(registro("PROCESSAMENTO", 3.0, 25.0, List.of(100.0, 100.0, 100.0, 100.0)));
        cnc.ligar();
        assertTrue(cnc.estaQuebrada());
        assertThrows(MaquinaIndisponivelException.class, () -> cnc.processar(new RotorRallySprint("R-1")));
    }

    @Test
    void maquinaDesligadaRecusaRotor() {
        assertThrows(MaquinaIndisponivelException.class, () -> cnc.processar(new RotorRallySprint("R-2")));
        assertEquals(100.0, cnc.getSaude(), 1e-9);
    }

    @Test
    void restauraSensorDescalibrado() {
        cnc.restaurarEstado(new RegistroMaquina("PROCESSAMENTO", "CNC", 100.0, 0, 0, 30.0,
                25.0, 0.30, List.of(100.0, 100.0, 100.0, 100.0)));
        assertTrue(cnc.precisaManutencao());              // só o sensor está com defeito
        assertEquals(0.30, cnc.exportarEstado().leituraSensor(), 1e-9);
        cnc.reparar();
        assertEquals(0.0, cnc.exportarEstado().leituraSensor(), 1e-9);
        assertFalse(cnc.precisaManutencao());
    }

    @Test
    void registroComDesvioNegativoERejeitado() {
        assertThrows(IllegalArgumentException.class, () -> new RegistroMaquina("PROCESSAMENTO", "CNC",
                100.0, 0, 0, 30.0, 25.0, -0.1, List.of(100.0, 100.0, 100.0, 100.0)));
    }

}
