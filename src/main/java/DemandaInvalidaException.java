/**
 * Lançada quando um pedido não existe, possui dados inválidos ou não aceita
 * a operação solicitada no seu estado atual.
 *
 * @author Lucas Migliaccio
 */
public class DemandaInvalidaException extends PlantaException {

    /**
     * Cria a exceção de demanda inválida.
     *
     * @param mensagem descrição do problema com o pedido
     */
    public DemandaInvalidaException(String mensagem) {
        super(mensagem);
    }
}