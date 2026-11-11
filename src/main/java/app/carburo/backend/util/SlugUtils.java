package app.carburo.backend.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Genera el segmento de ruta a partir de la denominación, sin catálogos de alias.
 */
public final class SlugUtils {

    private SlugUtils() { /**/ }

    public static String getSlug(String denominacion) {
        if (denominacion == null || denominacion.isBlank())
            throw new IllegalArgumentException("La denominación no puede estar vacía.");

        // NFD separa también la ñ: se recompone antes de eliminar el resto de marcas.
        String slug = Normalizer.normalize(denominacion.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replace("n\u0303", "ñ")
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-z0-9ñ]+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isEmpty()) throw new IllegalArgumentException("La denominación no genera un segmento de ruta válido.");

        return slug;
    }
}
