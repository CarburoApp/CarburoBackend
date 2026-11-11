package app.carburo.backend.util;

/**
 * Contrato común para objetos cuya denominación genera un segmento de URL.
 */
public interface ConSlug {

    String getDenominacion();

    /**
     * Valor derivado: no necesita almacenarse ni mantenerse como otro dato.
     */
    default String getSlug() {
        return SlugUtils.getSlug(getDenominacion());
    }
}
