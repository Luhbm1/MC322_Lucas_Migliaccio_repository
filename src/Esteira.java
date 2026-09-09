public class Esteira {

    private String nome;
    private Object item;          // pode carregar liga bruta ou rotor usinado
    private boolean emMovimento;
    private double capacidadeMaxima;

    public Esteira(String nome, double capacidadeMaxima) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.emMovimento = false;
        this.item = null;
    }

    public void ligar() {
        this.emMovimento = true;
        System.out.println("[OK] " + nome + " em movimento.");
    }

    public void desligar() {
        this.emMovimento = false;
        System.out.println("[OK] " + nome + " parada.");
    }

    public boolean verificarCapacidade(double peso) {
        return peso <= capacidadeMaxima;
    }

    public boolean adicionarItem(Object novoItem, double peso) {
        if (!emMovimento) {
            System.out.println("[ERRO] " + nome + " esta parada e nao transporta nada.");
            return false;
        }
        if (this.item != null) {
            System.out.println("[ERRO] " + nome + " ja esta ocupada. Uma peca por vez.");
            return false;
        }
        if (!verificarCapacidade(peso)) {
            System.out.println("[ERRO] Carga de " + peso + " g excede o limite de "
                               + capacidadeMaxima + " g da " + nome + ".");
            return false;
        }
        this.item = novoItem;
        return true;
    }

    public Object removerItem() {
        if (!emMovimento) {
            System.out.println("[ERRO] " + nome + " parada: a peca nao chega ao destino.");
            return null;
        }
        Object transportado = this.item;
        this.item = null;
        return transportado;
    }

    public String getNome() { return nome; }
    public boolean estaEmMovimento() { return emMovimento; }
    public boolean estaVazia() { return item == null; }
}
