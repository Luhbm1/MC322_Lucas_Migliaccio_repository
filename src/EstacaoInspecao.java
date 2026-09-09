public class EstacaoInspecao {

    private String nome;
    private boolean ativa;
    private int produtosInspecionados;

    public EstacaoInspecao(String nome) {
        this.nome = nome;
        this.ativa = false;
        this.produtosInspecionados = 0;
    }

    public void ativar() {
        this.ativa = true;
        System.out.println("[OK] " + nome + " ativada.");
    }

    public void desativar() {
        this.ativa = false;
        System.out.println("[OK] " + nome + " desativada.");
    }

    // Inspecao simplificada: neste momento, todo rotor usinado passa no balanceamento
    // pode ser adicionada uma etapa de escolha do resultado do balanceamento futuramente

    public boolean inspecionar(Produto produto) {
        if (!ativa) {
            System.out.println("[ERRO] " + nome + " desativada. Rotor sem laudo nao pode sair da fabrica.");
            return false;
        }
        produto.aprovar();
        produtosInspecionados++;
        System.out.println("[OK] Rotor " + produto.getId() + " aprovado no balanceamento dinamico.");
        return true;
    }

    public String getNome() { return nome; }
    public boolean estaAtiva() { return ativa; }
    public int getTotalInspecionados() { return produtosInspecionados; }
}
