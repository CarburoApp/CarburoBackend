package app.carburo.backend.seo.services;

import org.springframework.stereotype.Service;

@Service
public class RobotsService {

    private final SitemapService sitemap;

    public RobotsService(SitemapService sitemap) {
        this.sitemap = sitemap;
    }

    public String generateRobots() {
        // Se permite rastrear los recursos necesarios para renderizar las páginas.
        // La autenticación y las decisiones de noindex se gestionan por separado.
        return "User-agent: *\nAllow: /\n\nSitemap: " + sitemap.getSitemapUrl() + "\n";
    }
}
