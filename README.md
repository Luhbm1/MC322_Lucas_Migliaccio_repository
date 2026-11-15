# LM Racing Compressor Wheels v6.0

Simulador de terminal de uma planta que fabrica **rotores billet em Ti-6Al-4V** para turbocompressores de competição. O usuário cuida dos pedidos das equipes, da linha de produção, das compras de liga de titânio, das auditorias e da oficina, tudo com budget limitado. O estado da planta pode ser **salvo e carregado em XML**.

> Autor: **Lucas Migliaccio**

---

## Sumário

1. [Funcionalidades](#funcionalidades)
2. [Arquitetura e pacotes](#arquitetura-e-pacotes)
3. [Regras de negócio](#regras-de-negócio)
4. [Formato do save XML](#formato-do-save-xml)
5. [Escopo e Limitações](#escopo-e-limitações)
   - [Dados Persistidos vs. Não Persistidos](#dados-persistidos-vs-não-persistidos)
   - [Explicação da Suíte de Testes](#explicação-da-suíte-de-testes)
   - [Limitações conhecidas](#limitações-conhecidas)

---

## Funcionalidades

| Menu | Opções |
|---|---|
| **1 – Pedidos das equipes** | Registrar, atualizar quantidade, cancelar e listar pedidos |
| **2 – Linha de produção** | Produzir o próximo pedido (pela estratégia ativa) ou um pedido específico |
| **3 – Consultas** | Armazém (rotores acabados), almoxarifado (liga) e estado da linha |
| **4 – Comprar liga** | Compra em gramas, com débito no budget |
| **5 – Estratégia de produção** | Troca da estratégia em tempo de execução |
| **6 – Auditoria e oficina** | Relatórios polimórficos, auditoria estrutural e reparo de máquinas |
| **7 – Salvar/Carregar** | Persistência em `dados/planta.xml` |

### Cenários da temporada

| Cenário | Budget | Estoque de liga | Preço da liga | Falhas | Desgaste máx./ciclo | Risco inicial do rotor |
|---|---|---|---|---|---|---|
| `IDEAL` | R$ 100.000 | 20.000 g | R$ 0,60/g | ×0,5 | 1,0% | 0% |
| `APOCALIPTICO` | R$ 8.000 | 4.000 g | R$ 0,90/g | ×2,5 | 6,0% | 15% |

### Estratégias de produção

| Estratégia | Critério |
|---|---|
| `EstrategiaFilaDoPaddock` | FIFO: atende o pedido elegível mais antigo |
| `EstrategiaGridLotado` | Escolhe o pedido com mais rotores restantes, sem olhar o budget |
| `EstrategiaOrcamentoDeEquipe` | Entre os pedidos que cabem no budget, escolhe o que entrega mais rotores. No empate, o de menor custo |

---

## Arquitetura e pacotes

```text
src/main/java/br/unicamp/ic/mc322/
├── Main.java            # Menu de terminal; único ponto que captura exceções
├── model/               # Domínio: máquinas, componentes, rotores, pedidos, enums
├── service/             # GerenciadorProducao: orquestra pedidos, produção, compras e auditoria
├── strategy/            # EstrategiaProducao e suas 3 implementações
├── persistence/         # PersistenciaPlanta: salvar/carregar XML (DOM)
├── exception/           # Hierarquia de PlantaException
└── util/                # Cores
src/test/java/br/unicamp/ic/mc322/
├── Fixtures.java
├── model/               # DemandaTest, MaquinaTest, MateriaPrimaTest
├── persistence/         # PersistenciaPlantaTest
└── service/             # GerenciadorProducaoTest
```

---

## Regras de negócio

### Ciclo do pedido

`PENDENTE → EM_PRODUCAO → CONCLUIDA`

- O pedido pode ser cancelado a qualquer momento, exceto depois de concluído.
- Se uma máquina quebra durante a produção, o pedido é **pausado** e volta a ficar pendente.
- Atualizar a quantidade reabre o pedido como pendente. A nova quantidade precisa ser maior que a já entregue.
- Toda transição inválida lança `DemandaInvalidaException` sem alterar o estado.

### Validação antes de produzir

Nada é consumido se alguma destas verificações falhar:

- o pedido está pendente;
- nenhuma máquina está quebrada;
- o bloco cabe na esteira e nas máquinas;
- há liga para o lote;
- há budget para o lote.

### Produção unidade a unidade

- Cada tentativa consome a liga do rotor e o custo de cada máquina por onde ele passou: CNC R$ 350, Cabine R$ 40, Bancada R$ 120, ou seja, R$ 510 por ciclo completo.
- Rotores reprovados viram sucata.
- Se a sucata esgotar a liga ou o budget no meio do lote, o pedido é **cancelado** e os rotores já entregues são mantidos.

### Máquinas

- **Desgaste e falhas:** cada ciclo reduz a saúde aleatoriamente. A probabilidade de falha cresce conforme a saúde cai, com teto de 95%.
- **Estados pela saúde:** abaixo de 30% (limiar padrão), **desgastada**; até 5%, **quebrada**.
- **Reparo:** custa `(100 − saúde) × custoOperação × 0,2` e revisa todos os componentes.

### Componentes

Cada componente com defeito soma risco ao rotor processado:

| Componente | Defeito | Risco extra |
|---|---|---|
| Motor | Acima de 85 °C | +8% |
| Garra | Pressão abaixo de 60% (cada uma) | +4% |
| Sensor | Descalibrado | Penalidade do próprio sensor |

### Rotores e estoque

- O rotor fica "em risco" na auditoria se o risco acumulado chegar a 30% ou se ele for reprovado.
- O estoque de liga fica baixo ao chegar ao mínimo de 2.500 g, o sistema emite um alerta.

---

## Formato do save XML

Arquivo: `dados/planta.xml` (versão de formato `2`).

```xml
<?xml version="1.0" encoding="UTF-8"?>
<planta versao="2" cenario="IDEAL">
  <budget>98980.0</budget>
  <estoque>19000.0</estoque>
  <demandas>
    <demanda numero="1" tipo="RALLY" quantidade="2" entregues="2" status="CONCLUIDA"/>
  </demandas>
  <maquinas>
    <maquina tipo="PROCESSAMENTO" nome="Centro de Usinagem CNC 5 eixos &quot;Ciclone&quot;"
             saude="97.4" usos="2" falhas="0" limiar="30.0">
      <motor temperatura="36.1"/>
      <sensor leitura="0.02"/>
      <garra posicao="1" pressao="98.2"/>
      <garra posicao="2" pressao="97.5"/>
      <garra posicao="3" pressao="99.0"/>
      <garra posicao="4" pressao="96.8"/>
    </maquina>
  </maquinas>
</planta>
```

### Garantias da persistência

- **Gravação atômica:** o XML é escrito em `planta.xml.tmp` e depois movido para o destino (`ATOMIC_MOVE`, com alternativa simples se o sistema de arquivos não suportar). Uma falha no meio da escrita não corrompe o save anterior.
- **Carregamento em duas fases:**
  1. Todo o arquivo é validado sem tocar na planta.
  2. O estado é aplicado de uma só vez por `GerenciadorProducao.restaurarEstado`.

  Qualquer erro lança `PersistenciaException` e **a planta não muda**.
- **Segurança:** o parser bloqueia `DOCTYPE` e entidades externas (proteção contra **XXE**) e ativa `FEATURE_SECURE_PROCESSING`.
- **Validações:**
  - raiz, versão e cenário compatível com a sessão;
  - números finitos e não negativos;
  - enums existentes;
  - números de pedido sem repetição;
  - consistência de cada pedido (`Demanda.restaurar`);
  - faixas de valores das máquinas (`RegistroMaquina`);
  - mesma quantidade e mesmo tipo de máquinas da linha atual, com 4 garras cada.

---

## Escopo e Limitações

### Dados Persistidos vs. Não Persistidos

#### Dados persistidos (XML)

**Obrigatórios: estado mínimo para retomar a partida**

| Dado | Onde no XML | Por que persistir |
|---|---|---|
| **Cenário** | atributo `cenario` da raiz | Define preço da liga, multiplicador de falhas, desgaste e risco inicial. É salvo para **impedir** que um save de um cenário seja carregado em outro, o que geraria estados incoerentes. |
| **Budget** | `<budget>` | É o principal recurso limitante. Sem ele, a partida recomeçaria do zero a cada execução. |
| **Estoque de liga** | `<estoque>` | Junto com o budget, decide se a produção é possível. |
| **Pedidos** (`numero`, `tipo`, `quantidade`, `entregues`, `status`) | `<demandas>` | Representam o trabalho em aberto e o histórico de entregas. O **número** é salvo para manter a identidade que o usuário usa nos menus. |

**Opcionais: escolhidos para dar mais fidelidade ao save**

| Dado | Onde no XML | Por que persistir |
|---|---|---|
| **Estado das máquinas** (`tipo`, `nome`, `saude`, `usos`, `falhas`, `limiar`) | `<maquina>` | Sem isso, carregar um save "repararia" todas as máquinas de graça, o que quebraria a economia do jogo (o reparo custa budget). O `tipo` é usado para checar a compatibilidade com a linha. |
| **Componentes** (temperatura do motor, leitura do sensor, pressão das 4 garras) | `<motor>`, `<sensor>`, `<garra>` | Componentes com defeito aumentam o risco dos rotores. Se não fossem salvos, o desgaste acumulado sumiria entre as sessões. |

> O formato passou para a **versão 2** quando o bloco de máquinas foi adicionado, ao mesmo tempo exercendo um teste da funcionalidade. Saves de outra versão são recusados.

#### Dados mantidos apenas em memória

| Dado | Justificativa |
|---|---|
| **Armazém e sucata** (`produtosFabricados`, `rotoresSucateados`) | São um histórico de consulta e auditoria da sessão e não afetam decisões futuras. O resultado relevante (rotores entregues) já está em `entregues` de cada pedido. Salvar cada rotor (id, lote, risco, status, qualidade) aumentaria muito o arquivo sem significar muito para as regras de negócio. |
| **Contador global de rotores** (`Produto.totalProdutosFabricados`) | Só gera IDs sequenciais. Como o armazém não é salvo, não há IDs antigos com que colidir. |
| **Estratégia ativa** | É uma preferência de operação, trocável a qualquer momento no menu 5. Toda sessão começa com a Fila do Paddock. |
| **Semente / estado do `Random`** | A semente é pedida a cada execução. Salvar o estado interno de `java.util.Random` não é simples, e a reprodutibilidade já é garantida pela semente. |
| **Máquina ligada/desligada** | Estado transitório: a linha só fica ligada durante a produção. `restaurarEstado` sempre deixa a máquina **desligada**. |
| **Status `EM_PRODUCAO`** | Estado transitório: a produção é síncrona e sempre termina em concluído, pendente (pausado) ou cancelado. `Demanda.restaurar` **rejeita** esse status para que nenhum pedido fique preso em produção. |
| **Custo estimado por unidade dos pedidos** | É derivado: recalculado a partir das máquinas da linha (`atualizarCustosDemandas`). |
| **Parâmetros fixos da liga** (id, nome, preço, mínimo) e **do cenário** | Vêm do código e do cenário salvo. Persistir esses valores permitiria editar o XML para mudar o preço. |
| **Estatísticas da esteira** (`totalTransportado`, `bloqueios`) e `produtosInspecionados` da bancada | Métricas informativas, sem impacto nas regras de negócio. |
| **Configuração estrutural da linha** (quais máquinas existem, capacidades, custos) | Definida em `Main`. O save guarda só o *estado* das máquinas e é validado contra a linha existente. |

---

### Explicação da Suíte de Testes

#### Estratégia geral

- **JUnit 5:** `@Test`, `@ParameterizedTest` com `@ValueSource`, `@BeforeEach` e `@TempDir`.
- **Organização espelhada:** os pacotes de teste seguem os do código (`model`, `service`, `persistence`).
- **Três eixos de cobertura:**
  1. **regras de negócio**, no caminho feliz;
  2. **casos de exceção**: cada violação lança a exceção correta;
  3. **preservação de estado**: depois de uma exceção, estoque, budget, pedidos e máquinas continuam **inalterados**. Esse é o princípio central do sistema (*validar antes de alterar*).
- **Determinismo:** `Maquina.definirSemente(...)` torna reproduzíveis os testes que envolvem sorteio. Quando o resultado é aleatório, os testes verificam **invariantes** (ex.: contabilidade coerente), não valores exatos.
- **Isolamento:** a classe `Fixtures` cria plantas e ligas padronizadas, e a persistência usa diretórios temporários (`@TempDir`), sem tocar em `dados/`.

#### `Fixtures` (apoio)

Fábrica de objetos de teste:

- `liga(gramas)`: estoque de Ti-6Al-4V;
- `planta(cenario[, liga])`: planta completa com CNC, Cabine e Bancada, usando a Fila do Paddock;
- `CUSTO_CICLO = 510.0`: custo de um ciclo completo da linha.

#### `model.DemandaTest`: ciclo de vida do pedido

| Grupo | O que avalia |
|---|---|
| Criação | O pedido nasce `PENDENTE` com a liga necessária correta. Tipo nulo e quantidades `0`, `-1` e `-50` são rejeitados (teste parametrizado). Um pedido inválido **não consome** número do contador. |
| Transições válidas | Ciclo completo `PENDENTE → EM_PRODUCAO → CONCLUIDA`. A pausa só afeta pedidos em produção. Atualizar a quantidade reabre um pedido cancelado. |
| Transições inválidas | Iniciar um pedido que não está pendente, registrar rotor fora de produção, concluir com rotores restantes, alterar em produção, nova quantidade ≤ entregues, cancelar duas vezes ou depois de concluir. Todos lançam `DemandaInvalidaException` **e mantêm o status e as quantidades**. |
| Custo | `calcularCustoEstimado` e `ehViavel`, incluindo o limite exato (R$ 1.530,00 é viável; R$ 1.529,99 não). |
| Restauração | `Demanda.restaurar` recria um pedido consistente e rejeita: número < 1, entregues > quantidade, `CONCLUIDA` com rotores restantes, `EM_PRODUCAO` e tipo nulo. |

#### `model.MaquinaTest`: máquina e componentes

| Grupo | O que avalia |
|---|---|
| Estado inicial | Uma máquina nova está `OPERANTE`, sem garras folgadas. |
| Exportar/restaurar | `restaurarEstado` aplica saúde, temperatura do motor, garras e histórico de falhas. O sensor descalibrado é restaurado e depois zerado pelo reparo. |
| Validação do `RegistroMaquina` | Saúde > 100, `NaN`, pressão de garra > 100 e desvio de sensor negativo lançam `IllegalArgumentException`. |
| Compatibilidade | Um registro de outro tipo de máquina ou com outro número de garras é recusado **sem alterar** a máquina. |
| Reparo | Restaura saúde, motor e garras e devolve a máquina a `OPERANTE`. |
| Indisponibilidade | Máquina quebrada (saúde ≤ 5%) ou desligada lança `MaquinaIndisponivelException` ao processar, sem sofrer desgaste. |

#### `model.MateriaPrimaTest`: estoque de liga

| Grupo | O que avalia |
|---|---|
| Consumo | Um consumo válido reduz o estoque. Consumir tudo zera o estoque. Consumir acima do disponível lança `RecursoInsuficienteException` e **preserva** o estoque. Valores ≤ 0 são rejeitados (teste parametrizado). |
| Reposição | Uma reposição válida aumenta o estoque. Valores `0`, negativos e `NaN` são rejeitados. |
| Consultas | `verificarDisponibilidade`, incluindo o limite exato e a quantidade zero. Alerta de `estoqueBaixo` ao atingir o mínimo. |
| Restauração | `definirQuantidade` rejeita valores negativos, `NaN` e `Infinity`. |

#### `service.GerenciadorProducaoTest`: orquestração da planta

| Grupo | O que avalia |
|---|---|
| Pedidos | Registro como pendente. Um pedido inválido não entra na lista. A lista exposta é **somente leitura**. Operações sobre um número inexistente falham. Um pedido cancelado não é produzido e não gasta recursos. |
| Compras | A compra debita o budget e aumenta o estoque. Uma compra acima do budget preserva os dois. Quantidade ≤ 0 ou `NaN` é rejeitada. |
| Falhas preservam o estado | Produção sem liga e produção sem budget (lote de 20 rotores RALLY a R$ 510 = R$ 10.200, acima dos R$ 8.000 do `APOCALIPTICO`) mantêm estoque, budget e status `PENDENTE`. Sem pedido elegível, a produção lança exceção. Reparo com índice inexistente e estratégia nula são rejeitados. |
| Produção com sucesso | Com semente fixa, o pedido é concluído e os rotores vão para o armazém. **Contabilidade coerente:** cada tentativa (aprovada ou sucata) consome exatamente 500 g e R$ 510. |

#### `persistence.PersistenciaPlantaTest`: save XML

| Grupo | O que avalia |
|---|---|
| Caminho feliz (*round-trip*) | Salvar e carregar preserva budget, estoque, pedidos e status. Funciona com a lista de pedidos vazia. Não sobra `planta.xml.tmp` (gravação atômica). |
| Máquinas | Depois de uma produção que desgasta máquinas e componentes, o estado exportado é **idêntico** após recarregar. Falham: bloco `<maquinas>` ausente, quantidade de máquinas diferente, saúde fora da faixa, tipo incompatível e garra ausente. |
| Arquivo e estrutura | Arquivo inexistente, vazio, XML malformado, raiz errada, versão não suportada, cenário diferente do atual ou inexistente. |
| Segurança | Um `DOCTYPE` com entidade externa (`file:///etc/passwd`) é bloqueado, o que protege contra **XXE**. |
| Valores numéricos | Budget não numérico, estoque negativo e estoque `NaN`. |
| Pedidos | Tipo de rotor ou status inexistente, quantidade ≤ 0, entregues > quantidade, número duplicado e atributo ausente. |
| **Atomicidade** | Um arquivo com erro em qualquer ponto (pedido ou máquina) lança `PersistenciaException` **sem alterar** budget, estoque, pedidos nem máquinas. |

Todos os casos de falha usam o utilitário `assertFalhaAoCarregar()`, que exige uma `PersistenciaException`. As exceções internas (`DemandaInvalidaException`, `IllegalArgumentException`) são encapsuladas com a causa preservada.

---