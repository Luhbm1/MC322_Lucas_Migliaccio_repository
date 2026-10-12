import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Ponto de entrada e de interação com o usuário (menu de terminal).
 * <p>
 * É o único ponto que captura exceções: erros de domínio
 * ({@link PlantaException}), de entrada ({@link IllegalArgumentException}) e
 * falhas inesperadas são exibidos em uma tela de erro e o usuário volta ao menu.
 *
 * @author Lucas Migliaccio
 */
public class Main {

    private static final Scanner entrada = new Scanner(System.in);
    private static final String LINHA = "==============================================================";
    private static final String SEPARADOR = "--------------------------------------------------------------";

    private Main() { }

    /**
     * Inicia a planta e executa o menu principal até o usuário encerrar o turno.
     *
     * @param args não utilizados
     */
    public static void main(String[] args) {
        try {
            exibirIntroducao();
            Cenario cenario = escolherCenario();
            int semente = lerInteiro("Semente do sorteio (0 = aleatoria)", 0, 999999);
            if (semente != 0) Maquina.definirSemente(semente);

            EstrategiaProducao[] estrategias = {
                new EstrategiaFilaDoPaddock(),
                new EstrategiaGridLotado(),
                new EstrategiaOrcamentoDeEquipe()
            };

            MateriaPrima titanio = new MateriaPrima("MP001", "Liga de Titanio Ti-6Al-4V",
                    cenario.getEstoqueInicialLiga(), "g", cenario.getPrecoLiga(), 2500.0);
            Esteira esteira = new Esteira("Esteira de Transferencia", 3000.0);

            GerenciadorProducao gp = new GerenciadorProducao(titanio, esteira, cenario, estrategias[0]);
            gp.adicionarMaquina(new CentroUsinagemCNC("Centro de Usinagem CNC 5 eixos \"Ciclone\"", 3000.0));
            gp.adicionarMaquina(new CabineEmbalagemVacuo("Cabine de Selagem a Vacuo", 3000.0));
            gp.adicionarMaquina(new BancadaBalanceamento("Bancada de Balanceamento Dinamico", 3000.0));

            boolean executando = true;
            while (executando) {
                executando = executarComTratamento(gp, titanio, estrategias);
            }
            System.out.println("\nTurno encerrado. " + gp.getTamanhoArmazem()
                    + " rotor(es) prontos para entrega. Ate o proximo turno!");
        } catch (NoSuchElementException e) {
            System.out.println("\n[!] Entrada do teclado encerrada. Turno finalizado.");
        } finally {
            entrada.close();
        }
    }

    // Executa uma opcao do menu, qualquer erro recuperavel volta ao menu
    private static boolean executarComTratamento(GerenciadorProducao gp, MateriaPrima titanio,
                                                 EstrategiaProducao[] estrategias) {
        try {
            return executarMenuPrincipal(gp, titanio, estrategias);
        } catch (RecursoInsuficienteException e) {
            exibirErro("RECURSO INSUFICIENTE", e.getMessage());
        } catch (MaquinaIndisponivelException e) {
            exibirErro("MAQUINA INDISPONIVEL", e.getMessage());
        } catch (DemandaInvalidaException e) {
            exibirErro("PEDIDO INVALIDO", e.getMessage());
        } catch (PlantaException e) {
            exibirErro("OPERACAO NAO PERMITIDA", e.getMessage());
        } catch (IllegalArgumentException e) {
            exibirErro("ENTRADA INVALIDA", e.getMessage());
        } catch (NoSuchElementException e) {
            throw e;   // fim da entrada: tratado em main
        } catch (RuntimeException e) {
            exibirErro("FALHA INESPERADA", "Erro interno (" + e.getClass().getSimpleName() + ": "
                    + e.getMessage() + "). A operacao foi abortada; o sistema continua disponivel.");
        }
        return true;
    }

    private static boolean executarMenuPrincipal(GerenciadorProducao gp, MateriaPrima titanio,
                                                 EstrategiaProducao[] estrategias) {
        exibirCabecalho(gp, titanio);
        opcoes("1 - Pedidos das equipes", "2 - Linha de producao", "3 - Consultar armazem/almoxarifado",
               "4 - Comprar liga de titanio", "5 - Estrategia de producao", "6 - Auditoria e oficina",
               "0 - Encerrar turno");
        switch (lerInteiro("ESCOLHA", 0, 6)) {
            case 1: menuPedidos(gp); break;
            case 2: menuFabricacao(gp); break;
            case 3: menuConsultas(gp); break;
            case 4: {
            gp.exibirPrecoLiga();
            double gramas = lerDouble("Gramas de liga a comprar (0 = voltar)", 0.0, 50000.0);
            if (gramas == 0) {
            System.out.println("Compra cancelada. Voltando ao menu...");
            break;
            }
            gp.comprarMateriaPrima(gramas);
            break;
            }
            case 5: menuEstrategia(gp, estrategias); break;
            case 6: menuAuditoria(gp); break;
            default: return false;
        }
        return true;
    }

    // ---------- Submenus ----------

    private static void menuPedidos(GerenciadorProducao gp) { // ao selecionar opção 1
        secao("PEDIDOS DAS EQUIPES");
        opcoes("1 - Registrar novo pedido", "2 - Atualizar quantidade", "3 - Cancelar pedido",
               "4 - Listar pedidos", "0 - Voltar");
        switch (lerInteiro("ESCOLHA", 0, 4)) {
            case 1: gp.registrarDemanda(escolherTipo(), lerInteiro("Quantidade de rotores", 1, 50)); break;
            case 2:
                gp.listarDemandas();
                gp.atualizarDemanda(lerInteiro("Numero do pedido", 1, 9999), lerInteiro("Nova quantidade total", 1, 50));
                break;
            case 3:
                gp.listarDemandas();
                gp.cancelarDemanda(lerInteiro("Numero do pedido", 1, 9999));
                break;
            case 4: gp.listarDemandas(); break;
            default: break;
        }
    }

    private static void menuFabricacao(GerenciadorProducao gp) { // ao selecionar opção 2
        secao("LINHA DE PRODUCAO");
        opcoes("1 - Produzir proximo pedido (estrategia ativa)", "2 - Produzir pedido especifico", "0 - Voltar");
        switch (lerInteiro("ESCOLHA", 0, 2)) {
            case 1: gp.executarProximaProducao(); break;
            case 2:
                gp.listarDemandas();
                gp.fabricarDemanda(lerInteiro("Numero do pedido", 1, 9999));
                break;
            default: break;
        }
    }

    private static void menuConsultas(GerenciadorProducao gp) { // ao selecionar opção 3
        secao("CONSULTAR");
        opcoes("1 - Armazem (rotores acabados)", "2 - Almoxarifado (liga bruta)", "0 - Voltar");
        switch (lerInteiro("ESCOLHA", 0, 2)) {
            case 1: gp.exibirArmazem(); break;
            case 2: gp.exibirEstoque(); break;
            default: break;
        }
    }

    private static void menuEstrategia(GerenciadorProducao gp, EstrategiaProducao[] estrategias) { // ao selecionar opção 5
        secao("ESTRATEGIA DE PRODUCAO");
        for (int i = 0; i < estrategias.length; i++) {
            String ativa = estrategias[i] == gp.getEstrategiaAtual() ? Cores.pintar("  [ATIVA]", Cores.VERDE) : "";
            System.out.println((i + 1) + " - " + estrategias[i].getNomeEstrategia() + ativa);
        }
        System.out.println("0 - Voltar");
        int op = lerInteiro("ESCOLHA", 0, estrategias.length);
        if (op > 0) gp.setEstrategia(estrategias[op - 1]);
    }

    private static void menuAuditoria(GerenciadorProducao gp) { // ao selecionar opção 6
        secao("AUDITORIA E OFICINA");
        opcoes("1 - Relatorio geral", "2 - Detalhar maquinas", "3 - Detalhar rotores",
               "4 - Reparar maquina", "0 - Voltar");
        switch (lerInteiro("ESCOLHA", 0, 4)) {
            case 1: gp.gerarAuditoriaGeral(); break;
            case 2: gp.detalharMaquinas(); break;
            case 3: gp.detalharProdutos(); break;
            case 4:
                gp.listarMaquinas();
                gp.repararMaquina(lerInteiro("Maquina", 1, gp.getQuantidadeMaquinas()));
                break;
            default: break;
        }
    }

    // ---------- Telas ----------

    private static void exibirIntroducao() {
        System.out.println(Cores.CIANO + LINHA + Cores.RESET);
        System.out.println(Cores.NEGRITO + "        LM RACING COMPRESSOR WHEELS  v4.0" + Cores.RESET);
        System.out.println("        Potencia e durabilidade maximas");
        System.out.println(Cores.CIANO + LINHA + Cores.RESET);
        System.out.println("Rotores billet em Ti-6Al-4V para turbos de competicao.");
        System.out.println("A temporada esquentou: varias equipes, pedidos concorrentes");
        System.out.println("e auditorias da federacao. Escolha sua estrategia!");
        System.out.println();
        System.out.println("Desenvolvido por Lucas Migliaccio");
        System.out.println(Cores.CIANO + LINHA + Cores.RESET);
    }

    private static Cenario escolherCenario() {
        secao("ESCOLHA O CENARIO DA TEMPORADA");
        Cenario[] cenarios = Cenario.values();
        for (int i = 0; i < cenarios.length; i++) {
            System.out.printf("%d - %-36s | budget R$ %.2f%n", i + 1, cenarios[i].getDescricao(),
                    cenarios[i].getBudgetInicial());
        }
        return cenarios[lerInteiro("Cenario", 1, cenarios.length) - 1];
    }

    private static TipoRotor escolherTipo() {
        TipoRotor[] tipos = TipoRotor.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.printf("%d - %-34s (%.0f g de liga)%n", i + 1, tipos[i].getNomeComercial(), tipos[i].getMassaBruta());
        }
        return tipos[lerInteiro("Rotor", 1, tipos.length) - 1];
    }

    private static void exibirCabecalho(GerenciadorProducao gp, MateriaPrima titanio) {
        String corBudget = gp.getBudget() < 2000 ? Cores.VERMELHO : Cores.VERDE; // cor vermelha quando budget está baixo
        System.out.println("\n" + Cores.CIANO + LINHA + Cores.RESET);
        System.out.println(Cores.NEGRITO + "  [LM RACING COMPRESSOR WHEELS]" + Cores.RESET);
        System.out.println("  ESTRATEGIA ATUAL : " + gp.getEstrategiaAtual().getNomeEstrategia());
        System.out.println("  CENARIO ATIVO    : " + gp.getCenario().getDescricao());
        System.out.println("  BUDGET ATUAL     : " + Cores.pintar(String.format("R$ %.2f", gp.getBudget()), corBudget));
        System.out.printf("  LIGA EM ESTOQUE  : %.1f g%n", titanio.getQuantidade());
        System.out.println(Cores.CIANO + LINHA + Cores.RESET);
    }

    private static void exibirErro(String titulo, String mensagem) {
        System.out.println("\n" + Cores.pintar(LINHA, Cores.VERMELHO));
        System.out.println(Cores.pintar("ERRO - " + titulo, Cores.VERMELHO + Cores.NEGRITO));
        System.out.println(Cores.pintar(LINHA, Cores.VERMELHO));
        System.out.println(mensagem);
        System.out.print("\nPressione ENTER para continuar...");
        if (entrada.hasNextLine()) entrada.nextLine();
    }

    private static void secao(String titulo) {
        System.out.println("\n" + SEPARADOR);
        System.out.println(Cores.pintar("[" + titulo + "]", Cores.AMARELO));
        System.out.println(SEPARADOR);
    }

    private static void opcoes(String... itens) {
        for (String item : itens) System.out.println(" " + item);
    }

    // ---------- Leitura validada ----------

    // Repete a pergunta ate receber um inteiro dentro da faixa
    private static int lerInteiro(String rotulo, int minimo, int maximo) {
        while (true) {
            System.out.print(rotulo + " (" + minimo + "-" + maximo + "): ");
            String linha = entrada.nextLine().trim();
            try {
                int valor = Integer.parseInt(linha);
                if (valor >= minimo && valor <= maximo) return valor;
                System.out.println("[ERRO] Opcao " + valor + " inexistente. Escolha entre "
                        + minimo + " e " + maximo + ".");
            } catch (NumberFormatException e) {
                System.out.println("[ERRO] \"" + linha + "\" nao e um numero inteiro. Tente novamente.");
            }
        }
    }

    // Repete a pergunta ate receber um decimal dentro da faixa (aceita virgula)
    private static double lerDouble(String rotulo, double minimo, double maximo) {
        while (true) {
            System.out.print(rotulo + " [" + minimo + " a " + maximo + "]: ");
            String linha = entrada.nextLine().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(linha);
                if (valor >= minimo && valor <= maximo) return valor;
                System.out.println("[ERRO] Valor fora da faixa aceita (" + minimo + " a " + maximo + ").");
            } catch (NumberFormatException e) {
                System.out.println("[ERRO] \"" + linha + "\" nao e um numero valido (ex: 1500).");
            }
        }
    }
}