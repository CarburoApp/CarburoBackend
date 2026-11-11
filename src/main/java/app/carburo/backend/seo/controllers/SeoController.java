package app.carburo.backend.seo.controllers;

import app.carburo.backend.seo.services.RobotsService;
import app.carburo.backend.seo.services.SitemapService;
import app.carburo.backend.seo.SitemapGroup;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SeoController {

    private final SitemapService sitemap;
    private final RobotsService robots;

    public SeoController(SitemapService sitemap, RobotsService robots) {
        this.sitemap = sitemap;
        this.robots = robots;
    }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public String sitemap() {
        return sitemap.generateSitemap();
    }

    @GetMapping(value = "/sitemap/sitemap_{grupo}.xml", produces = "application/xml;charset=UTF-8")
    public String sitemapGrupo(@PathVariable String grupo) {
        for (SitemapGroup value : SitemapGroup.values())
            if (value.getSlug().equals(grupo))
                return sitemap.generateSitemap(value);

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
    public String robots() {
        return robots.generateRobots();
    }
}
