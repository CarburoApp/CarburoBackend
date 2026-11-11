package app.carburo.backend.seo;

import lombok.Getter;

@Getter
public enum SitemapGroup {

    BASICO("basic"),
    UBICACIONES("gas_stations_territories"),
    ESTACIONES("gas_stations"),;

    private final String slug;

    SitemapGroup(String slug) {
        this.slug = slug;
    }

    public String getPath() {
        return "/sitemap/sitemap_" + slug + ".xml";
    }
}
