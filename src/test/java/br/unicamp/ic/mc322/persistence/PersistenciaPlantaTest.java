package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.Fixtures;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.TipoRotor;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaPlantaTest {

    private static final Cenario CENARIO = Cenario.values()[0];
    private static final String TIPO_VALIDO = TipoRotor.values()[0].name();
    private String maquinasValidas;

    @TempDir
    Path dir;

    private GerenciadorProducao gp;
    private Path arquivo;

    @BeforeEach
    void setUp() throws IOException {
        gp = Fixtures.planta(CENARIO);
        arquivo = dir.resolve("planta.xml");
        Path base = dir.resolve("base.xml");
        PersistenciaPlanta.salvar(Fixtures.planta(CENARIO), base);
        String s = Files.readString(base);
        maquinasValidas = s.substring(s.indexOf("<maquinas"), s.indexOf("</maquinas>") + "</maquinas>".length());
    }

    private String xml(String cenario, String budget, String estoque, String demandas) {
        return xmlCom(cenario, budget, estoque, demandas, maquinasValidas);
    }

    private String xmlCom(String cenario, String budget, String estoque, String demandas, String maquinas) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <planta versao="2" cenario="%s">
              <budget>%s</budget>
              <estoque>%s</estoque>
              <demandas>%s</demandas>
              %s
            </planta>
            """.formatted(cenario, budget, estoque, demandas, maquinas);
    }

    // ---------- utilitários ----------

    private String xmlValido(String demandas) {
        return xml(CENARIO.name(), "1000", "500", demandas);
    }

    private String demanda(String numero, String tipo, String qtd, String entregues, String status) {
        return "<demanda numero=\"%s\" tipo=\"%s\" quantidade=\"%s\" entregues=\"%s\" status=\"%s\"/>"
                .formatted(numero, tipo, qtd, entregues, status);
    }

    private void escrever(String conteudo) throws IOException {
        Files.writeString(arquivo, conteudo);
    }

    private void assertFalhaAoCarregar() {
        assertThrows(PersistenciaException.class,
                () -> PersistenciaPlanta.carregar(arquivo, gp));
    }

    // ---------- caminho feliz ----------

    @Test
    void salvarECarregarPreservaEstado() throws Exception {
        gp.registrarDemanda(TipoRotor.values()[0], 3);
        double budget = gp.getBudget();
        double estoque = gp.getEstoqueLiga();

        PersistenciaPlanta.salvar(gp, arquivo);

        GerenciadorProducao outro = Fixtures.planta(CENARIO);
        PersistenciaPlanta.carregar(arquivo, outro);

        assertEquals(budget, outro.getBudget(), 1e-9);
        assertEquals(estoque, outro.getEstoqueLiga(), 1e-9);
        assertEquals(1, outro.getDemandas().size());
        assertEquals(3, outro.getDemandas().get(0).getQuantidadeProdutos());
        assertEquals(gp.getDemandas().get(0).getStatus(), outro.getDemandas().get(0).getStatus());
    }

    @Test
    void salvarSemDemandasECarregar() throws Exception {
        PersistenciaPlanta.salvar(gp, arquivo);
        GerenciadorProducao outro = Fixtures.planta(CENARIO);
        PersistenciaPlanta.carregar(arquivo, outro);
        assertTrue(outro.getDemandas().isEmpty());
    }

    @Test
    void salvarNaoDeixaArquivoTemporario() throws Exception {
        PersistenciaPlanta.salvar(gp, arquivo);
        assertTrue(Files.exists(arquivo));
        assertFalse(Files.exists(dir.resolve("planta.xml.tmp")));
    }

    // ---------- máquinas ----------

    @Test
    void salvarECarregarPreservaMaquinasEComponentes() {
        Maquina.definirSemente(7);
        gp.registrarDemanda(TipoRotor.RALLY, 2);
        gp.executarProximaProducao();          // desgasta máquinas e componentes
        PersistenciaPlanta.salvar(gp, arquivo);

        GerenciadorProducao outro = Fixtures.planta(CENARIO);
        PersistenciaPlanta.carregar(arquivo, outro);

        assertEquals(gp.exportarMaquinas(), outro.exportarMaquinas());
        assertEquals(StatusDemanda.CONCLUIDA, outro.getDemandas().get(0).getStatus());
    }

    @Test
    void blocoDeMaquinasAusente() throws IOException {
        escrever(xmlCom(CENARIO.name(), "1000", "500", "", ""));
        assertFalhaAoCarregar();
    }

    @Test
    void quantidadeDeMaquinasIncompativel() throws IOException {
        escrever(xmlCom(CENARIO.name(), "1000", "500", "", "<maquinas/>"));
        assertFalhaAoCarregar();
    }

    @Test
    void saudeForaDaFaixa() throws IOException {
        escrever(xml(CENARIO.name(), "1000", "500", "").replaceFirst("saude=\"[^\"]*\"", "saude=\"150\""));
        assertFalhaAoCarregar();
    }

    @Test
    void tipoDeMaquinaIncompativel() throws IOException {
        escrever(xml(CENARIO.name(), "1000", "500", "").replaceFirst("tipo=\"PROCESSAMENTO\"", "tipo=\"INSPECAO\""));
        assertFalhaAoCarregar();
    }

    @Test
    void garraAusente() throws IOException {
        escrever(xml(CENARIO.name(), "1000", "500", "").replaceFirst("<garra[^>]*/>", ""));
        assertFalhaAoCarregar();
    }

    @Test
    void falhaEmMaquinaNaoAlteraPlanta() throws IOException {
        var maquinasAntes = gp.exportarMaquinas();
        double budgetAntes = gp.getBudget();
        escrever(xml(CENARIO.name(), "1", "1", "").replaceFirst("saude=\"[^\"]*\"", "saude=\"-3\""));
        assertFalhaAoCarregar();
        assertEquals(maquinasAntes, gp.exportarMaquinas());
        assertEquals(budgetAntes, gp.getBudget(), 1e-9);
    }


    // ---------- arquivo / estrutura ----------

    @Test
    void arquivoInexistente() {
        assertFalhaAoCarregar();
    }

    @Test
    void arquivoVazio() throws IOException {
        escrever("");
        assertFalhaAoCarregar();
    }

    @Test
    void xmlMalformado() throws IOException {
        escrever("<planta versao=\"1\"><budget>10");
        assertFalhaAoCarregar();
    }

    @Test
    void raizErrada() throws IOException {
        escrever("<outra versao=\"1\"/>");
        assertFalhaAoCarregar();
    }

    @Test
    void versaoNaoSuportada() throws IOException {
        escrever(xmlValido("").replace("versao=\"2\"", "versao=\"9\""));
        assertFalhaAoCarregar();
    }

    @Test
    void cenarioDiferenteDaPlanta() throws IOException {
        if (Cenario.values().length < 2) return;
        escrever(xml(Cenario.values()[1].name(), "1000", "500", ""));
        assertFalhaAoCarregar();
    }

    @Test
    void cenarioInexistente() throws IOException {
        escrever(xml("MARTE", "1000", "500", ""));
        assertFalhaAoCarregar();
    }

    @Test
    void doctypeBloqueadoContraXXE() throws IOException {
        escrever("<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]>"
               + "<planta versao=\"1\"><budget>&e;</budget></planta>");
        assertFalhaAoCarregar();
    }

    // ---------- valores numéricos ----------

    @Test
    void budgetNaoNumerico() throws IOException {
        escrever(xml(CENARIO.name(), "abc", "500", ""));
        assertFalhaAoCarregar();
    }

    @Test
    void estoqueNegativo() throws IOException {
        escrever(xml(CENARIO.name(), "1000", "-1", ""));
        assertFalhaAoCarregar();
    }

    @Test
    void estoqueNaN() throws IOException {
        escrever(xml(CENARIO.name(), "1000", "NaN", ""));
        assertFalhaAoCarregar();
    }

    // ---------- demandas ----------

    @Test
    void tipoDeRotorInvalido() throws IOException {
        escrever(xmlValido(demanda("1", "TURBO", "2", "0", "PENDENTE")));
        assertFalhaAoCarregar();
    }

    @Test
    void statusInvalido() throws IOException {
        escrever(xmlValido(demanda("1", TIPO_VALIDO, "2", "0", "XYZ")));
        assertFalhaAoCarregar();
    }

    @Test
    void quantidadeNaoPositiva() throws IOException {
        escrever(xmlValido(demanda("1", TIPO_VALIDO, "0", "0", "PENDENTE")));
        assertFalhaAoCarregar();
    }

    @Test
    void entreguesMaiorQueQuantidade() throws IOException {
        escrever(xmlValido(demanda("1", TIPO_VALIDO, "2", "5", "PENDENTE")));
        assertFalhaAoCarregar();
    }

    @Test
    void numeroDeDemandaDuplicado() throws IOException {
        escrever(xmlValido(
                demanda("1", TIPO_VALIDO, "2", "0", "PENDENTE")
              + demanda("1", TIPO_VALIDO, "3", "0", "PENDENTE")));
        assertFalhaAoCarregar();
    }

    @Test
    void atributoAusente() throws IOException {
        escrever(xmlValido("<demanda numero=\"1\" quantidade=\"2\"/>"));
        assertFalhaAoCarregar();
    }

    // ---------- atomicidade ----------

    @Test
    void falhaNoCarregamentoNaoAlteraPlanta() throws IOException {
        double budgetAntes = gp.getBudget();
        double estoqueAntes = gp.getEstoqueLiga();
        int demandasAntes = gp.getDemandas().size();

        escrever(xml(CENARIO.name(), "999", "1",
                demanda("1", "TURBO", "2", "0", "PENDENTE")));
        assertFalhaAoCarregar();

        assertEquals(budgetAntes, gp.getBudget(), 1e-9);
        assertEquals(estoqueAntes, gp.getEstoqueLiga(), 1e-9);
        assertEquals(demandasAntes, gp.getDemandas().size());
    }
}
