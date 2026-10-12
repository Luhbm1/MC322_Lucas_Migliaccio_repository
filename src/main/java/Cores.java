/**
 * Utilitário com códigos ANSI para colorir a saída do terminal.
 * Não pode ser instanciada.
 *
 * @author Lucas Migliaccio
 */
public final class Cores {
    /** Restaura a cor padrão do terminal. */
    public static final String RESET = "\u001B[0m";
    /** Vermelho: erros e falhas graves. */
    public static final String VERMELHO = "\u001B[31m";
    /** Verde: operações bem-sucedidas. */
    public static final String VERDE = "\u001B[32m";
    /** Amarelo: alertas e títulos de seção. */
    public static final String AMARELO = "\u001B[33m";
    /** Ciano: molduras do cabeçalho. */
    public static final String CIANO = "\u001B[36m";
    /** Coloca o texto em negrito. */
    public static final String NEGRITO = "\u001B[1m";

    private Cores() { }

    /**
     * Envolve o texto com a cor indicada e reseta a cor ao final.
     *
     * @param texto texto a ser colorido
     * @param cor   código ANSI (fixo)
     * @return texto colorido pronto para impressão
     */
    public static String pintar(String texto, String cor) { return cor + texto + RESET; }
}
