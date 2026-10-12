import java.util.ArrayList;
import java.util.List;

/**
 * Núcleo da planta: gerencia pedidos, linha de produção, oficina, compras e auditoria.
 * <p>
 * Todas as operações validam suas pré-condições antes de alterar qualquer
 * estado. Em caso de violação, lançam uma {@link PlantaException} (ou
 * subclasse) e deixam estoque, budget e pedidos intactos.
 *
 * @author Lucas Migliaccio
 */
public class GerenciadorProducao {

    private final List<Demanda> demandas = new ArrayList<>();
    private final List<Produto> produtosFabricados = new ArrayList<>();
    private final List<Produto> rotoresSucateados = new ArrayList<>();
    private final List<Maquina> maquinas = new ArrayList<>();
    private final MateriaPrima materiaPrima;
    private final Esteira esteira;
    private final Cenario cenario;
    private double budget;
    private EstrategiaProducao estrategiaAtual;

    /**
     * @param materiaPrima      estoque de liga
     * @param esteira           esteira de transferência
     * @param cenario           cenário da temporada (define o budget inicial)
     * @param estrategiaInicial estratégia de produção ativa
     */
    public GerenciadorProducao(MateriaPrima materiaPrima, Esteira esteira, Cenario cenario,
                               EstrategiaProducao estrategiaInicial) {
        this.materiaPrima = materiaPrima;
        this.esteira = esteira;
        this.cenario = cenario;
        this.budget = cenario.getBudgetInicial();
        this.estrategiaAtual = estrategiaInicial;
    }

    /**
     * Adiciona uma máquina ao final da linha, já configurada para o cenário.
     *
     * @param maquina máquina a adicionar
     */
    public void adicionarMaquina(Maquina maquina) {
        maquina.configurarCenario(cenario);
        maquinas.add(maquina);
    }

    /**
     * Troca a estratégia de produção.
     *
     * @param novaEstrategia nova estratégia
     * @throws IllegalArgumentException se a estratégia for nula
     */
    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        if (novaEstrategia == null) throw new IllegalArgumentException("Estrategia nao informada.");
        estrategiaAtual = novaEstrategia;
        System.out.println("[OK] Estrategia alterada para: " + novaEstrategia.getNomeEstrategia());
    }

    // ---------- Pedidos ----------

    /**
     * Registra um novo pedido pendente.
     *
     * @param tipo       tipo de rotor
     * @param quantidade quantidade de rotores
     * @throws DemandaInvalidaException se os dados do pedido forem inválidos
     */
    public void registrarDemanda(TipoRotor tipo, int quantidade) {
        Demanda d = new Demanda(tipo, quantidade);
        demandas.add(d);
        atualizarCustosDemandas();
        System.out.println("[OK] Pedido registrado: " + d);
    }

    /**
     * Altera a quantidade total de um pedido.
     *
     * @param numero         número do pedido
     * @param novaQuantidade novo total
     * @throws DemandaInvalidaException se o pedido não existir ou não aceitar a alteração
     */
    public void atualizarDemanda(int numero, int novaQuantidade) {
        Demanda d = buscarDemanda(numero);
        d.atualizarQuantidade(novaQuantidade);
        atualizarCustosDemandas();
        System.out.println("[OK] Pedido atualizado: " + d);
    }

    /**
     * Cancela um pedido.
     *
     * @param numero número do pedido
     * @throws DemandaInvalidaException se o pedido não existir, já estiver concluído ou cancelado
     */
    public void cancelarDemanda(int numero) {
        buscarDemanda(numero).cancelar();
        System.out.println("[OK] Pedido #" + numero + " cancelado.");
    }

    /** Lista todos os pedidos com custos atualizados. */
    public void listarDemandas() {
        atualizarCustosDemandas();
        System.out.println("\n--- PEDIDOS DAS EMPRESAS ---");
        if (demandas.isEmpty()) System.out.println("Nenhum pedido no paddock.");
        for (Demanda d : demandas) System.out.println(d);
    }

    // ---------- Fabricacao ----------

    /**
     * Produz o pedido escolhido pela estratégia ativa.
     *
     * @throws DemandaInvalidaException     se a estratégia não encontrar pedido elegível
     * @throws RecursoInsuficienteException se faltar liga ou budget para o lote
     * @throws MaquinaIndisponivelException se alguma máquina estiver quebrada
     */
    public void executarProximaProducao() {
        atualizarCustosDemandas();
        Demanda escolhida = estrategiaAtual.selecionarDemanda(demandas, budget);
        if (escolhida == null) {
            throw new DemandaInvalidaException("A estrategia \"" + estrategiaAtual.getNomeEstrategia()
                    + "\" nao encontrou pedido elegivel. Registre um pedido pendente"
                    + " ou verifique se ha budget suficiente.");
        }
        System.out.println("[>>] Estrategia escolheu o pedido #" + escolhida.getNumero());
        fabricarDemanda(escolhida);
    }

    /**
     * Produz um pedido específico.
     *
     * @param numero número do pedido
     * @throws DemandaInvalidaException     se o pedido não existir ou não estiver pendente
     * @throws RecursoInsuficienteException se faltar liga ou budget para o lote
     * @throws MaquinaIndisponivelException se alguma máquina estiver quebrada
     */
    public void fabricarDemanda(int numero) {
        fabricarDemanda(buscarDemanda(numero));
    }

    /*
     * Fluxo: (1) valida tudo sem alterar estado; (2) produz unidade a unidade.
     * Se a sucata consumir os recursos no meio do lote, o pedido é cancelado
     * (regra da Tarefa 3) mantendo os rotores ja entregues.
     */
    private void fabricarDemanda(Demanda d) {
        atualizarCustosDemandas();
        validarInicioProducao(d);
        d.iniciarProducao();

        double custoUnidade = calcularCustoProducao();
        double massa = d.getTipo().getMassaBruta();
        int aprovados = 0;
        int rejeitados = 0;

        ligarLinha();
        try {
            while (d.getQuantidadeRestante() > 0) {
                Maquina quebrada = buscarMaquinaQuebrada();
                if (quebrada != null) {
                    System.out.println("[X] " + quebrada.getNome() + " fora de operacao. Pedido volta para a fila.");
                    d.pausar();
                    break;
                }
                if (!materiaPrima.verificarDisponibilidade(massa)) {
                    System.out.println("[X] A sucata consumiu a liga de titanio! Pedido cancelado.");
                    d.cancelar();
                    break;
                }
                if (budget < custoUnidade) {
                    System.out.println("[X] O budget secou antes do final. Pedido cancelado.");
                    d.cancelar();
                    break;
                }

                Produto rotor = d.getTipo().criarRotor(gerarIdRotor(d.getTipo()));
                rotor.aumentarProbabilidadeFalha(cenario.getRiscoInicialRotor());

                if (fabricarUnidade(rotor, d)) {
                    produtosFabricados.add(rotor);
                    d.registrarRotorAprovado();
                    aprovados++;
                } else {
                    rotoresSucateados.add(rotor);
                    rejeitados++;
                }
            }
            if (d.getQuantidadeRestante() == 0) d.concluir();
        } catch (PlantaException e) {
            d.pausar();   // pedido nao fica preso em EM_PRODUCAO
            throw e;
        } finally {
            desligarLinha();
        }

        System.out.println("\n================ RESUMO DO PEDIDO #" + d.getNumero() + " ================");
        System.out.printf("Aprovados: %d | Sucata: %d | Restantes: %d%n", aprovados, rejeitados, d.getQuantidadeRestante());
        System.out.println("Status: " + d.getStatus().getDescricao());
        exibirBudget();
        if (materiaPrima.estoqueBaixo()) {
            System.out.println("[!] Estoque de liga abaixo do minimo de " + materiaPrima.getQuantidadeMinima() + " g.");
        }
    }

    // Valida o lote inteiro antes de qualquer consumo de liga ou budget
    private void validarInicioProducao(Demanda d) {
        if (d.getStatus() != StatusDemanda.PENDENTE) {
            throw new DemandaInvalidaException("Pedido #" + d.getNumero() + " nao pode ser produzido: "
                    + d.getStatus().getDescricao() + ". Apenas pedidos pendentes entram na linha.");
        }
        Maquina quebrada = buscarMaquinaQuebrada();
        if (quebrada != null) {
            throw new MaquinaIndisponivelException(quebrada.getNome()
                    + " esta quebrado(a). Repare-o(a) no menu 'Auditoria e oficina' antes de produzir.");
        }
        double massa = d.getTipo().getMassaBruta();
        if (!esteira.verificarCapacidade(massa)) {
            throw new PlantaException("O bloco de " + massa + " g excede o limite da " + esteira.getNome() + ".");
        }
        for (Maquina m : maquinas) {
            if (!m.suporta(massa)) {
                throw new PlantaException("O bloco de " + massa + " g excede a capacidade do(a) " + m.getNome() + ".");
            }
        }
        double ligaNecessaria = d.calcularMateriaPrimaNecessaria();
        if (materiaPrima.getQuantidade() < ligaNecessaria) {
            throw new RecursoInsuficienteException(String.format(
                    "Nao foi possivel iniciar a producao: materia-prima insuficiente para fabricar o lote"
                    + " solicitado (necessario %.1f g, disponivel %.1f g). Compre liga no menu 4."
                    + " Nenhuma alteracao foi realizada no estoque.",
                    ligaNecessaria, materiaPrima.getQuantidade()));
        }
        double custo = d.calcularCustoEstimado();
        if (budget < custo) {
            throw new RecursoInsuficienteException(String.format(
                    "Nao foi possivel iniciar a producao: budget insuficiente (necessario R$ %.2f,"
                    + " disponivel R$ %.2f). Reduza o pedido no menu 1. Nenhuma alteracao foi realizada.",
                    custo, budget));
        }
    }

    // O custo de cada maquina so e cobrado depois que ela efetivamente processou o rotor
    private boolean fabricarUnidade(Produto rotor, Demanda d) {
        materiaPrima.consumir(rotor.getQuantidadeMateriaPrimaPorUnidade());
        rotor.registrarLote(materiaPrima.getId() + "/PED" + d.getNumero());
        System.out.println("\n>> " + rotor.getId() + " (tempo estimado: " + rotor.calcularTempoProducao() + " min)");

        for (Maquina m : maquinas) {
            transportar(rotor);
            boolean seguiu = m.processar(rotor);
            budget -= m.getCustoOperacao();
            if (!seguiu) return false;
        }
        return true;
    }

    private void transportar(Produto rotor) {
        esteira.adicionarItem(rotor, rotor.getQuantidadeMateriaPrimaPorUnidade());
        esteira.removerItem();
    }

    private double calcularCustoProducao() {
        double total = 0;
        for (Maquina m : maquinas) total += m.getCustoOperacao();
        return total;
    }

    private void atualizarCustosDemandas() {
        double custo = calcularCustoProducao();
        for (Demanda d : demandas) d.definirCustoOperacaoPorUnidade(custo);
    }

    private Maquina buscarMaquinaQuebrada() {
        for (Maquina m : maquinas) {
            if (m.estaQuebrada()) return m;
        }
        return null;
    }

    private void ligarLinha() {
        esteira.ligar();
        for (Maquina m : maquinas) m.ligar();
    }

    private void desligarLinha() {
        for (Maquina m : maquinas) m.desligar();
        esteira.desligar();
    }

    private String gerarIdRotor(TipoRotor tipo) {
        return String.format("%s-%04d", tipo.getPrefixo(), Produto.getTotalProdutosFabricados() + 1);
    }

    private Demanda buscarDemanda(int numero) {
        for (Demanda d : demandas) {
            if (d.getNumero() == numero) return d;
        }
        throw new DemandaInvalidaException("Pedido #" + numero
                + " nao encontrado. Consulte os numeros validos em 'Listar pedidos'.");
    }

    // ---------- Oficina ----------

    /** Lista as máquinas com saúde e custo de reparo. */
    public void listarMaquinas() {
        for (int i = 0; i < maquinas.size(); i++) {
            Maquina m = maquinas.get(i);
            System.out.printf("%d - %-42s saude %5.1f%% | reparo R$ %.2f%n",
                    i + 1, m.getNome(), m.getSaude(), m.calcularCustoReparo());
        }
    }

    /**
     * Repara uma máquina e debita o custo do budget.
     *
     * @param indice posição da máquina (a partir de 1)
     * @throws IllegalArgumentException     se o índice não existir
     * @throws RecursoInsuficienteException se o budget não cobrir o reparo
     */
    public void repararMaquina(int indice) {
        if (indice < 1 || indice > maquinas.size()) {
            throw new IllegalArgumentException("Maquina " + indice + " inexistente. Escolha entre 1 e "
                    + maquinas.size() + ".");
        }
        Maquina m = maquinas.get(indice - 1);
        double custo = m.calcularCustoReparo();
        if (custo > budget) {
            throw new RecursoInsuficienteException(String.format(
                    "O reparo custa R$ %.2f e o budget e R$ %.2f. Nenhuma alteracao foi realizada.", custo, budget));
        }
        budget -= custo;
        m.reparar();
        exibirBudget();
    }

    /** @return quantidade de máquinas na linha */
    public int getQuantidadeMaquinas() { return maquinas.size(); }

    // ---------- Compras e consultas ----------

    /**
     * Compra liga de titânio, debitando o budget.
     *
     * @param quantidade gramas a comprar
     * @throws IllegalArgumentException     se a quantidade não for positiva
     * @throws RecursoInsuficienteException se o budget não cobrir a compra
     */
    public void comprarMateriaPrima(double quantidade) {
        if (!(quantidade > 0)) throw new IllegalArgumentException("A quantidade comprada deve ser positiva.");
        double custo = quantidade * materiaPrima.getCustoPorUnidade();
        if (custo > budget) {
            throw new RecursoInsuficienteException(String.format(
                    "O lote custa R$ %.2f e o budget e R$ %.2f. Compre uma quantidade menor."
                    + " Nenhuma alteracao foi realizada.", custo, budget));
        }
        materiaPrima.adicionarEstoque(quantidade);
        budget -= custo;
        System.out.printf("[OK] %.1f g de liga comprados por R$ %.2f.%n", quantidade, custo);
        exibirBudget();
    }

    /** Exibe o budget atual. */
    public void exibirBudget() { System.out.printf("Budget atual: R$ %.2f%n", budget); }

    /** Exibe o preço da liga definido pelo cenário ativo e o budget atual. */
    public void exibirPrecoLiga() {
    System.out.printf("Preco da liga: R$ %.2f/%s (cenario %s)%n",
            materiaPrima.getCustoPorUnidade(), materiaPrima.getUnidade(), cenario.name());
    System.out.printf("Maximo compravel: %.1f g%n", budget / materiaPrima.getCustoPorUnidade());
    }

    /** Exibe o resumo por tipo e o diagnóstico de cada rotor no armazém. */
    public void exibirArmazem() {
        System.out.println("\n================ ARMAZEM DE ROTORES ACABADOS ================");
        for (TipoRotor tipo : TipoRotor.values()) {
            int qtd = 0;
            int emRisco = 0;
            double qualidade = 0;
            for (Produto p : produtosFabricados) {
                if (p.getTipo() != tipo) continue;
                qtd++;
                qualidade = p.getQualidade();
                if (p.precisaManutencao()) emRisco++;
            }
            System.out.printf("%-34s | qtd %3d | qualidade %.1f | em risco %d%n",
                    tipo.getNomeComercial(), qtd, qualidade, emRisco);
        }
        System.out.println("--------------------------------------------------------------");
        if (produtosFabricados.isEmpty()) System.out.println("Prateleiras vazias.");
        for (Produto p : produtosFabricados) System.out.println(p.gerarRelatorioDiagnostico());
        System.out.println("Total no armazem: " + produtosFabricados.size()
                + " | Sucata: " + rotoresSucateados.size()
                + " | Blocos usinados: " + Produto.getTotalProdutosFabricados());
    }

    /** Exibe o estoque de matéria-prima. */
    public void exibirEstoque() {
        System.out.println("\n--- ALMOXARIFADO (MATERIA-PRIMA) ---");
        System.out.println("Lote.......: " + materiaPrima.getId() + " - " + materiaPrima.getNome());
        System.out.println("Disponivel.: " + materiaPrima.getQuantidade() + " " + materiaPrima.getUnidade());
        System.out.println("Minimo.....: " + materiaPrima.getQuantidadeMinima() + " " + materiaPrima.getUnidade());
        System.out.printf("Preco......: R$ %.2f/%s%n", materiaPrima.getCustoPorUnidade(), materiaPrima.getUnidade());
    }

    // ---------- Auditoria ----------

    /** Audita máquinas, rotores do armazém e sucata. */
    public void gerarAuditoriaGeral() {
        List<Auditavel> componentes = new ArrayList<>();
        componentes.addAll(maquinas);
        componentes.addAll(produtosFabricados);
        componentes.addAll(rotoresSucateados);
        auditar("AUDITORIA GERAL DA PLANTA", componentes);
    }

    /** Audita apenas as máquinas. */
    public void detalharMaquinas() { auditar("AUDITORIA DE MAQUINAS", maquinas); }

    /** Audita os rotores do armazém e da sucata. */
    public void detalharProdutos() {
        List<Auditavel> rotores = new ArrayList<>(produtosFabricados);
        rotores.addAll(rotoresSucateados);
        auditar("AUDITORIA DE ROTORES (ARMAZEM + SUCATA)", rotores);
    }

    private void auditar(String titulo, List<? extends Auditavel> itens) {
        System.out.println("\n================ " + titulo + " ================");
        int alertas = 0;
        for (Auditavel a : itens) {
            System.out.println(a.gerarRelatorioDiagnostico());
            if (a.precisaManutencao()) alertas++;
        }
        if (itens.isEmpty()) System.out.println("Nada para auditar.");
        System.out.println("Itens auditados: " + itens.size() + " | Exigem intervencao: " + alertas);
    }

    /** @return budget atual, em R$ */
    public double getBudget() { return budget; }
    /** @return cenário ativo */
    public Cenario getCenario() { return cenario; }
    /** @return estratégia ativa */
    public EstrategiaProducao getEstrategiaAtual() { return estrategiaAtual; }
    /** @return quantidade de rotores aprovados no armazém */
    public int getTamanhoArmazem() { return produtosFabricados.size(); }
}