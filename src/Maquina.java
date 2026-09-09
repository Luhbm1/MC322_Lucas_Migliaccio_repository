public class Maquina {

    private String nome;
    private boolean ligada;
    private double capacidadeMaxima;   // gramas de liga por ciclo

    public Maquina(String nome, double capacidadeMaxima) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ligada = false;
    }

    public void ligar() {
        this.ligada = true;
        System.out.println("[OK] " + nome + " ligado. Fuso em rotacao nominal.");
    }

    public void desligar() {
        this.ligada = false;
        System.out.println("[OK] " + nome + " desligado.");
    }

    // Usinagem do rotor: valida estado, capacidade e estoque antes de consumir

    public boolean processar(MateriaPrima materiaPrima, Produto produto, double demanda) {
        if (!ligada) {
            System.out.println("[ERRO] " + nome + " esta desligado. Impossivel iniciar a usinagem.");
            return false;
        }
        if (demanda > capacidadeMaxima) {
            System.out.println("[ERRO] Demanda de " + demanda + " g excede a capacidade de "
                               + capacidadeMaxima + " g por ciclo do " + nome + ".");
            return false;
        }
        double receita = produto.getDemandaMateriaPrima();

        if (demanda < receita) {
            System.out.println("[ERRO] " + produto.getNome() + " exige " + receita
                               + " g. Alocacao de " + demanda
                               + " g resultaria em rotor fora de especificacao.");
            return false;
        }
        
        // fabrica apenas 1 rotor por vez independente do valor especificado
        if (demanda > receita) {
            System.out.println("[!] Alocados " + demanda + " g, receita pede " + receita
                               + " g. Excedente de " + (demanda - receita)
                               + " g devolvido ao almoxarifado.");
        }

        if (!materiaPrima.consumir(receita)) {
            System.out.println("[ERRO] Liga insuficiente no estoque para este ciclo.");
            return false;
        }
        System.out.println("[OK] " + nome + " usinando " + receita + " g de "
                           + materiaPrima.getNome() + "...");
        produto.processar(materiaPrima.getId());
        System.out.println("[OK] Rotor " + produto.getId() + " - " + produto.getNome() + " usinado.");
        return true;
    }

    public String getNome() { return nome; }
    public boolean estaLigada() { return ligada; }
    public double getCapacidadeMaxima() { return capacidadeMaxima; }
}
