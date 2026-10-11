/**
 * Lançada quando falta liga de titânio ou budget para concluir uma operação.
 *
 * @author Lucas Migliaccio
 */
public class RecursoInsuficienteException extends PlantaException {

    /**
     * Cria a exceção de recurso insuficiente.
     *
     * @param mensagem descrição do recurso que faltou e das quantidades envolvidas
     */
    public RecursoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}