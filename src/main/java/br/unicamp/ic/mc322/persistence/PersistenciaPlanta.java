package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.TipoRotor;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.model.RegistroMaquina;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Salva e carrega o estado da planta (cenário, budget, estoque de liga, pedidos, máquinas) 
 * em XML.
 * <p>
 * A gravação é atômica: escreve em um arquivo temporário e depois o move para o destino.
 * A leitura valida o arquivo inteiro antes de alterar a planta. Se algo estiver errado,
 * lança {@link br.unicamp.ic.mc322.exception.PersistenciaException} e a planta não é modificada.
 *
 * @author Lucas Migliaccio
 */
public final class PersistenciaPlanta {

    private static final String RAIZ = "planta";
    private static final String VERSAO = "2";

    private PersistenciaPlanta() { }

    // ---------- Salvar ----------

    /**
     * Grava o estado da planta em XML.
     *
     * @param gp      gerenciador cuja planta será salva
     * @param arquivo arquivo de destino
     * @throws PersistenciaException se a gravação falhar
     */
    public static void salvar(GerenciadorProducao gp, Path arquivo) {
        Path tmp = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
        try {
            Path pai = arquivo.toAbsolutePath().getParent();
            if (pai != null) Files.createDirectories(pai);
            Document doc = novoBuilder().newDocument();
            Element raiz = doc.createElement(RAIZ);
            raiz.setAttribute("versao", VERSAO);
            raiz.setAttribute("cenario", gp.getCenario().name());
            doc.appendChild(raiz);

            adicionarTexto(doc, raiz, "budget", Double.toString(gp.getBudget()));
            adicionarTexto(doc, raiz, "estoque", Double.toString(gp.getEstoqueLiga()));

            Element demandas = doc.createElement("demandas");
            raiz.appendChild(demandas);
            for (Demanda d : gp.getDemandas()) {
                Element e = doc.createElement("demanda");
                e.setAttribute("numero", Integer.toString(d.getNumero()));
                e.setAttribute("tipo", d.getTipo().name());
                e.setAttribute("quantidade", Integer.toString(d.getQuantidadeProdutos()));
                e.setAttribute("entregues", Integer.toString(d.getQuantidadeProduzida()));
                e.setAttribute("status", d.getStatus().name());
                demandas.appendChild(e);
            }

            Element maquinas = doc.createElement("maquinas");
            raiz.appendChild(maquinas);
            for (RegistroMaquina r : gp.exportarMaquinas()) {
                Element m = doc.createElement("maquina");
                m.setAttribute("tipo", r.tipo());
                m.setAttribute("nome", r.nome());
                m.setAttribute("saude", Double.toString(r.saude()));
                m.setAttribute("usos", Integer.toString(r.usos()));
                m.setAttribute("falhas", Integer.toString(r.falhas()));
                m.setAttribute("limiar", Double.toString(r.limiar()));
                Element motor = doc.createElement("motor");
                motor.setAttribute("temperatura", Double.toString(r.temperaturaMotor()));
                m.appendChild(motor);
                Element sensor = doc.createElement("sensor");
                sensor.setAttribute("leitura", Double.toString(r.leituraSensor()));
                m.appendChild(sensor);
                for (int i = 0; i < r.pressoesGarras().size(); i++) {
                    Element g = doc.createElement("garra");
                    g.setAttribute("posicao", Integer.toString(i + 1));
                    g.setAttribute("pressao", Double.toString(r.pressoesGarras().get(i)));
                    m.appendChild(g);
                }
                maquinas.appendChild(m);
            }

            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            try (OutputStream out = Files.newOutputStream(tmp)) {
                t.transform(new DOMSource(doc), new StreamResult(out));
            }

            moverAtomico(tmp, arquivo);
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            apagarSilencioso(tmp);
            throw new PersistenciaException("Falha ao salvar: " + e.getMessage(), e);
        }
    }

    // ---------- Carregar ----------

    /**
     * Lê o save e substitui o estado da planta. Nada é alterado se o arquivo for inválido.
     *
     * @param arquivo arquivo de origem
     * @param gp      gerenciador que receberá o estado
     * @throws PersistenciaException se o arquivo estiver ausente, corrompido ou incompatível
     */
    public static void carregar(Path arquivo, GerenciadorProducao gp) {
        if (!Files.isRegularFile(arquivo)) {
            throw new PersistenciaException("Arquivo de save nao encontrado: " + arquivo);
        }

        Document doc;
        try {
            doc = novoBuilder().parse(arquivo.toFile());
        } catch (SAXException e) {
            throw new PersistenciaException("Arquivo corrompido (XML invalido): " + e.getMessage(), e);
        } catch (IOException | ParserConfigurationException e) {
            throw new PersistenciaException("Falha ao ler o save: " + e.getMessage(), e);
        }

        // Fase 1: validar tudo sem tocar na planta
        Element raiz = doc.getDocumentElement();
        if (!RAIZ.equals(raiz.getTagName())) {
            throw new PersistenciaException("Arquivo corrompido: raiz <" + raiz.getTagName() + "> inesperada.");
        }
        if (!VERSAO.equals(raiz.getAttribute("versao"))) {
            throw new PersistenciaException("Versao de save nao suportada: \"" + raiz.getAttribute("versao") + "\".");
        }
        Cenario cenario = lerEnum(Cenario.class, raiz.getAttribute("cenario"), "cenario");
        if (cenario != gp.getCenario()) {
            throw new PersistenciaException("Save de cenario incompativel (" + cenario
                    + "); cenario atual: " + gp.getCenario() + ".");
        }

        double budget = lerDouble(textoFilho(raiz, "budget"), "budget");
        if (budget < 0) throw new PersistenciaException("Arquivo corrompido: budget negativo.");
        double estoque = lerDouble(textoFilho(raiz, "estoque"), "estoque");
        if (estoque < 0) throw new PersistenciaException("Arquivo corrompido: estoque negativo.");

        List<Demanda> demandas = new ArrayList<>();
        Set<Integer> numeros = new HashSet<>();
        NodeList nos = raiz.getElementsByTagName("demanda");
        for (int i = 0; i < nos.getLength(); i++) {
            Element e = (Element) nos.item(i);
            String ctx = "demanda #" + (i + 1);
            int numero = lerInt(e.getAttribute("numero"), ctx + " numero");
            TipoRotor tipo = lerEnum(TipoRotor.class, e.getAttribute("tipo"), ctx + " tipo");
            int qtd = lerInt(e.getAttribute("quantidade"), ctx + " quantidade");
            int entregues = lerInt(e.getAttribute("entregues"), ctx + " entregues");
            StatusDemanda status = lerEnum(StatusDemanda.class, e.getAttribute("status"), ctx + " status");

            if (!numeros.add(numero)) {
                throw new PersistenciaException("Arquivo corrompido: numero de pedido repetido (" + numero + ").");
            }
            try {
                demandas.add(Demanda.restaurar(numero, tipo, qtd, entregues, status));
            } catch (DemandaInvalidaException ex) {
                throw new PersistenciaException("Arquivo corrompido em " + ctx + ": " + ex.getMessage(), ex);
            }
        }

        List<RegistroMaquina> maquinas = lerMaquinas(raiz);

        // Fase 2: aplicar de uma vez
        try {
            gp.restaurarEstado(budget, estoque, demandas, maquinas);
        } catch (IllegalArgumentException ex) {
            throw new PersistenciaException("Arquivo corrompido: " + ex.getMessage(), ex);
        }
    }

    // ---------- Auxiliares ----------

    // Parser protegido contra XXE (DTD e entidades externas desabilitadas)
    private static DocumentBuilder novoBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setExpandEntityReferences(false);
        return f.newDocumentBuilder();
    }

    private static void adicionarTexto(Document doc, Element pai, String tag, String valor) {
        Element e = doc.createElement(tag);
        e.setTextContent(valor);
        pai.appendChild(e);
    }

    private static String textoFilho(Element pai, String tag) {
        NodeList l = pai.getElementsByTagName(tag);
        if (l.getLength() != 1) {
            throw new PersistenciaException("Arquivo corrompido: esperado exatamente um <" + tag + ">.");
        }
        return l.item(0).getTextContent().trim();
    }

    private static double lerDouble(String s, String campo) {
        try {
            double v = Double.parseDouble(s);
            if (Double.isNaN(v) || Double.isInfinite(v)) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            throw new PersistenciaException("Arquivo corrompido: " + campo + " invalido (\"" + s + "\").");
        }
    }

    private static int lerInt(String s, String campo) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new PersistenciaException("Arquivo corrompido: " + campo + " invalido (\"" + s + "\").");
        }
    }

    private static <E extends Enum<E>> E lerEnum(Class<E> tipo, String s, String campo) {
        try {
            return Enum.valueOf(tipo, s);
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Arquivo corrompido: " + campo + " desconhecido (\"" + s + "\").");
        }
    }

    private static void moverAtomico(Path origem, Path destino) throws IOException {
        try {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void apagarSilencioso(Path p) {
        try { Files.deleteIfExists(p); } catch (IOException ignored) { }
    }

    private static List<RegistroMaquina> lerMaquinas(Element raiz) {
        Element bloco = filhoUnico(raiz, "maquinas", "planta");
        NodeList nos = bloco.getElementsByTagName("maquina");
        List<RegistroMaquina> lista = new ArrayList<>();
        for (int i = 0; i < nos.getLength(); i++) {
            Element e = (Element) nos.item(i);
            String ctx = "maquina #" + (i + 1);
            Element motor = filhoUnico(e, "motor", ctx);
            Element sensor = filhoUnico(e, "sensor", ctx);
            NodeList gs = e.getElementsByTagName("garra");
            List<Double> pressoes = new ArrayList<>();
            for (int j = 0; j < gs.getLength(); j++) {
                pressoes.add(lerDouble(((Element) gs.item(j)).getAttribute("pressao"), ctx + " garra " + (j + 1)));
            }
            try {
                lista.add(new RegistroMaquina(e.getAttribute("tipo"), e.getAttribute("nome"),
                        lerDouble(e.getAttribute("saude"), ctx + " saude"),
                        lerInt(e.getAttribute("usos"), ctx + " usos"),
                        lerInt(e.getAttribute("falhas"), ctx + " falhas"),
                        lerDouble(e.getAttribute("limiar"), ctx + " limiar"),
                        lerDouble(motor.getAttribute("temperatura"), ctx + " motor"),
                        lerDouble(sensor.getAttribute("leitura"), ctx + " sensor"),
                        pressoes));
            } catch (IllegalArgumentException ex) {
                throw new PersistenciaException("Arquivo corrompido em " + ctx + ": " + ex.getMessage(), ex);
            }
        }
        return lista;
    }

    private static Element filhoUnico(Element pai, String tag, String ctx) {
        NodeList l = pai.getElementsByTagName(tag);
        if (l.getLength() != 1) {
            throw new PersistenciaException("Arquivo corrompido em " + ctx + ": esperado exatamente um <" + tag + ">.");
        }
        return (Element) l.item(0);
    }

}
