package com.desenvolvimento.sistemas;

public final class AppConfig {
    // Construtor privado para evitar instanciação
    private AppConfig() {
        throw new UnsupportedOperationException("Esta é uma classe de constantes e não pode ser instanciada");
    }

    // Constantes públicas, estáticas e finais
    public static final String DB_PREFIX       = "";
    public static final String TABLE_PREFIX    = "table_";
    // Importante, pois aponta para o pacote onde as classes de entidade estão:
    public static final String ENTITIES_PATH    = "com.desenvolvimento.sistemas.model.domain.entities";
    public static final int    LIMITE_USUARIOS = 100;
    public static final double VERSAO          = 1.0;

}
