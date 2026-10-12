/**
 * Exceção base para violações das regras de negócio da planta.
 * <p>
 * Permite que a camada de interação diferencie erros de domínio (pedidos,
 * recursos, máquinas) de erros de entrada do usuário e de falhas inesperadas.
 *
 * @author Lucas Migliaccio
 */
public class PlantaException extends RuntimeException {

    /**
     * Cria a exceção com uma mensagem descritiva para o usuário.
     *
     * @param mensagem explicação do problema e, se possível, de como corrigi-lo
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }
}