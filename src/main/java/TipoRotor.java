/**
 * Catálogo de rotores fabricados. Também atua como fábrica da subclasse
 * de {@link Produto} correspondente.
 *
 * @author Lucas Migliaccio
 */
public enum TipoRotor {
    /** Rotor pesado para diesel de tração. */
    PULLING("Titan 90/100 (Diesel Pulling)", RotorTitanPulling.MASSA_BRUTA, "TTN"),
    /** Rotor de pás duplas para arrancada. */
    ARRANCADA("DualBlade 72/78 (Arrancada)", RotorDualBladeArrancada.MASSA_BRUTA, "DBL"),
    /** Rotor leve para rally e time attack. */
    RALLY("Sprint 50/58 (Rally/Time Attack)", RotorRallySprint.MASSA_BRUTA, "SPR");

    private final String nomeComercial;
    private final double massaBruta;
    private final String prefixo;

    TipoRotor(String nomeComercial, double massaBruta, String prefixo) {
        this.nomeComercial = nomeComercial;
        this.massaBruta = massaBruta;
        this.prefixo = prefixo;
    }

    /**
     * Cria a subclasse concreta de rotor deste tipo.
     *
     * @param id identificador único do rotor
     * @return novo rotor no status {@link StatusRotor#AGUARDANDO_USINAGEM}
     */
    public Produto criarRotor(String id) {
        switch (this) {
            case PULLING:   return new RotorTitanPulling(id);
            case ARRANCADA: return new RotorDualBladeArrancada(id);
            default:        return new RotorRallySprint(id);
        }
    }

    /** @return nome comercial do rotor */
    public String getNomeComercial() { return nomeComercial; }
    /** @return massa de liga consumida por unidade, em gramas */
    public double getMassaBruta() { return massaBruta; }
    /** @return prefixo usado nos IDs dos rotores deste tipo */
    public String getPrefixo() { return prefixo; }
}