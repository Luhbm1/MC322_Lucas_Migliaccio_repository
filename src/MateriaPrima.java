public class MateriaPrima {

    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double quantidadeMinima;

    public MateriaPrima(String id, String nome, double quantidade,
                        String unidade, double quantidadeMinima) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
    }

    // So consome se houver estoque. Devolve false para o Main poder avisar o operador caso nao haja material suficiente
    public boolean consumir(double demanda) {
        if (!verificarDisponibilidade(demanda)) {
            return false;
        }
        this.quantidade -= demanda;
        return true;
    }

    public void adicionarEstoque(double valor) {
        if (valor > 0) {
            this.quantidade += valor;
        }
    }

    public boolean verificarDisponibilidade(double demanda) {
        return demanda > 0 && demanda <= this.quantidade;
    }

    // Alerta de reposicao do lote de liga
    public boolean estoqueBaixo() {
        return this.quantidade <= this.quantidadeMinima;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public double getQuantidade() { return quantidade; }
    public String getUnidade() { return unidade; }
    public double getQuantidadeMinima() { return quantidadeMinima; }
}
