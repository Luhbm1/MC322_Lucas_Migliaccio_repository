/**
 * Lançada quando uma máquina ou a esteira não pode operar (desligada,
 * quebrada ou ocupada).
 *
 * @author Lucas Migliaccio
 */
public class MaquinaIndisponivelException extends PlantaException {

    /**
     * Cria a exceção de equipamento indisponível.
     *
     * @param mensagem descrição do equipamento e do motivo da indisponibilidade
     */
    public MaquinaIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
