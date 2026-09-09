import java.util.Scanner;

public class Main {

    private static final Scanner entrada = new Scanner(System.in); // utilizacao da biblioteca/classe Scanner para interpretar inputs de usuario

    public static void main(String[] args) {    // bloco principal

        // Declaracao de produtos comeca aqui

        MateriaPrima titanio = new MateriaPrima("MP001", "Liga de Titanio Ti-6Al-4Av", 10000.0, "g", 2500.0);

        // Liga metalica composta de 90% titanio, 6%  aluminio e 4% vanadio, utilizada em compressores de competicao (nao comum no mercado de carros de rua)

        Produto[] catalogo = {
            new Produto("S5058", "Rotor 50/58mm - 7 pas simples", 500.0),       // aplicacao: motores otto de baixa/media cilindrada (Rally, Time Attack)
            new Produto("D7278", "Rotor 72/78mm - 6 pas dual-blade", 1500.0),   // aplicacao: motores otto de media/alta cilindrada (Arrancada)
            new Produto("S90100", "Rotor 90/100mm - 7 pas simples", 2500.0)     // aplicacao: motores diesel de alta cilindrada (Arrancada, Pulling)

            // O valor em gramas indica o peso bruto do bloco de Ti-6Al-4Av a ser usinado para fabricar o rotor
            // Cerca de 60~80% do material de um bloco metalico acaba desperdicado na usinagem de um rotor billet de turbocompressor e,
            // por conta disso, a massa bruta fica quase que exponencialmente maior conforme o rotor aumenta de tamanho
        };

        Maquina centroUsinagem = new Maquina("Centro de Usinagem CNC 5 eixos \"Ciclone\"", 3000.0);
        Esteira esteira = new Esteira("Esteira de Transferencia", 3000.0);
        EstacaoInspecao inspecao = new EstacaoInspecao("Bancada de Balanceamento Dinamico");

        // Declaracao de produtos termina aqui

        // Fluxo de funcionamento inicial do terminal comeca aqui

        exibirIntroducao();

        boolean executando = true;
        while (executando) {
            exibirMenu(titanio);
            int opcao = lerInteiro("Escolha", 1, 4);

            if (opcao == 1) {
                iniciarProducao(titanio, catalogo, centroUsinagem, esteira, inspecao);
            } else if (opcao == 2) {
                consultarEstoque(titanio, inspecao);
            } else if (opcao == 3) {
                double lote = lerDouble("Quantos gramas de liga entram no galpao", 1.0, 50000.0);
                titanio.adicionarEstoque(lote);
                System.out.println("[OK] Estoque atualizado: " + titanio.getQuantidade() + " g.");
            } else {
                executando = false;
                System.out.println("\nTurno encerrado. " + inspecao.getTotalInspecionados()
                                   + " rotor(es) balanceado(s) hoje. Ate a proxima!");
            }
        }
        entrada.close();
    }

    // Fluxo de producao

    private static void iniciarProducao(MateriaPrima titanio, Produto[] catalogo,
                                        Maquina maquina, Esteira esteira,
                                        EstacaoInspecao inspecao) {

        System.out.println("\n--- SELECAO DE ROTOR ---");
        for (int i = 0; i < catalogo.length; i++) {
            System.out.println((i + 1) + " - " + catalogo[i].getNome()
                               + " (demanda padrao: " + catalogo[i].getDemandaMateriaPrima() + " g)");
        }
        Produto produto = catalogo[lerInteiro("Selecione o rotor", 1, catalogo.length) - 1];

        double demanda = lerDouble("Informe a demanda de liga (g)", 1.0, 3000.0);

        System.out.println("\n[..] Verificando disponibilidade de " + titanio.getNome() + "...");
        if (!titanio.verificarDisponibilidade(demanda)) {
            System.out.println("[ERRO] Estoque de apenas " + titanio.getQuantidade()
                               + " g. Nao da para forjar este rotor. Reabasteca no menu (opcao 3).");
            return;
        }
        System.out.println("[OK] Demanda de " + demanda + " g pode ser atendida.");

        // 1) Ligar equipamentos
        esteira.ligar();
        maquina.ligar();

        // 2) Transporte da liga bruta ate a maquina
        if (!esteira.adicionarItem(titanio, demanda)) {
            return;
        }
        System.out.println("[OK] Lote " + titanio.getId() + " colocado na esteira.");
        if (esteira.removerItem() == null) {
            return;
        }
        System.out.println("[OK] Liga transportada ate o centro de usinagem.");

        // 3) Usinagem (consome o estoque)
        if (!maquina.processar(titanio, produto, demanda)) {
            return;
        }

        // 4) Transporte do rotor ate a inspecao
        if (!esteira.adicionarItem(produto, demanda)) {
            return;
        }
        if (esteira.removerItem() == null) {
            return;
        }
        System.out.println("[OK] Rotor " + produto.getId() + " transportado para inspecao.");

        // 5) Inspecao
        inspecao.ativar();
        inspecao.inspecionar(produto);

        // 6) Produto final
        maquina.desligar();
        esteira.desligar();

        System.out.println("\n========================================");
        System.out.println("   PRODUCAO CONCLUIDA COM SUCESSO");
        System.out.println("========================================");
        System.out.println("Rotor.......: " + produto.getId() + " - " + produto.getNome());
        System.out.println("Status......: " + produto.getStatus());
        System.out.println("Origem da liga: " + produto.getIdMateriaPrimaUsada());
        System.out.println("Estoque restante: " + titanio.getQuantidade() + " g");
        if (titanio.estoqueBaixo()) {
            System.out.println("[!] Estoque abaixo do minimo de "
                               + titanio.getQuantidadeMinima() + " g. Chame o comprador.");
        }
    }

    private static void consultarEstoque(MateriaPrima titanio, EstacaoInspecao inspecao) {
        System.out.println("\n--- ESTOQUE / ALMOXARIFADO ---");
        System.out.println("Lote.......: " + titanio.getId() + " - " + titanio.getNome());
        System.out.println("Disponivel.: " + titanio.getQuantidade() + " " + titanio.getUnidade());
        System.out.println("Minimo.....: " + titanio.getQuantidadeMinima() + " " + titanio.getUnidade());
        System.out.println("Rotores aprovados ate agora: " + inspecao.getTotalInspecionados());
    }

    // Telas do terminal

    private static void exibirIntroducao() {
        System.out.println("========================================");
        System.out.println("   LM RACING COMPRESSOR WHEELS");
        System.out.println("  Potencia e durabilidade maximas");
        System.out.println("========================================");
        System.out.println("Fabricamos rotores frios (impellers) para");
        System.out.println("turbocompressores automotivos de competicaoo");
        System.out.println("com Liga de Titanio Ti-6Al-4Av.");
        System.out.println();
        System.out.println("Desenvolvido por Lucas Migliaccio");
        System.out.println("========================================");
    }

    private static void exibirMenu(MateriaPrima titanio) {
        System.out.println("\n========================================");
        System.out.println("   MENU PRINCIPAL   [estoque: "
                           + titanio.getQuantidade() + " " + titanio.getUnidade() + "]");
        System.out.println("========================================");
        System.out.println("1 - Iniciar producao");
        System.out.println("2 - Consultar estoque");
        System.out.println("3 - Receber lote de liga");
        System.out.println("4 - Sair");
    }

    // Validacao de leitura de comandos inputs (deteccao de erros e entradas invalidas)

    private static int lerInteiro(String rotulo, int minimo, int maximo) {
        while (true) {
            System.out.print(rotulo + " (" + minimo + "-" + maximo + "): ");
            String linha = entrada.nextLine().trim(); // rRecebe input do usuario
            try {
                int valor = Integer.parseInt(linha);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                System.out.println("[ERRO] Fora da faixa permitida.");
            } catch (NumberFormatException e) {
                System.out.println("[ERRO] O painel da fabrica so atende numeros inteiros.");
            }
        }
    }

    private static double lerDouble(String rotulo, double minimo, double maximo) {
        while (true) {
            System.out.print(rotulo + " [" + minimo + " a " + maximo + "]: ");
            String linha = entrada.nextLine().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(linha);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                System.out.println("[ERRO] Valor fora da faixa aceita pela celula de usinagem.");
            } catch (NumberFormatException e) {
                System.out.println("[ERRO] Digite apenas numeros inteiros (ex: 1500).");
            }
        }
    }
}
