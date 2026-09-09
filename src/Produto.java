public class Produto {

    private String id;
    private String nome;
    private String status;
    private double quantidadeMateriaPrimaNecessaria;
    private String idMateriaPrimaUsada;   // rastreabilidade do lote

    public Produto(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
        this.status = "AGUARDANDO USINAGEM";
        this.idMateriaPrimaUsada = "-";
    }

    public void processar(String idMateriaPrima) {
        this.status = "USINADO";
        this.idMateriaPrimaUsada = idMateriaPrima;
    }

    public void aprovar() {
        this.status = "APROVADO NO BALANCEAMENTO";
    }

    public void reprovar() {
        this.status = "BALANCEAMENTO REPROVADO";
    }

    public void definirDemandaMateriaPrima(double demanda) {
        if (demanda > 0) {
            this.quantidadeMateriaPrimaNecessaria = demanda;
        }
    }

    public double getDemandaMateriaPrima() { return quantidadeMateriaPrimaNecessaria; }
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getStatus() { return status; }
    public String getIdMateriaPrimaUsada() { return idMateriaPrimaUsada; }
}
