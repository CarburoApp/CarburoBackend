package app.carburo.backend.seo;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato para aportar páginas públicas indexables al sitemap.
 */
public interface SitemapUrlProvider {

    default SitemapGroup getSitemapGroup() {
        return SitemapGroup.BASICO;
    }

    /**
     * Rutas canónicas relativas al origen, comenzando por '/', sin duplicados de filtros.
     */
    List<String> getSitemapPaths();

    /**
     * Fecha real de modificación del contenido; null si todavía no se conoce.
     */
    default LocalDate getSitemapLastModified(String path) {
        return null;
    }
}
