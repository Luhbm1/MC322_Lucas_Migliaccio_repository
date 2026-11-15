package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando o save não pode ser gravado ou lido (ausente, vazio,
 * corrompido ou incompatível).
 *
 * @author Lucas Migliaccio
 */
public class PersistenciaException extends PlantaException {

    /** @param mensagem descrição do problema */
    public PersistenciaException(String mensagem) { super(mensagem); }

    /**
     * @param mensagem descrição do problema
     * @param causa    exceção de origem
     */
    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}