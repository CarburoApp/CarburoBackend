package app.carburo.backend.seo.services;

import app.carburo.backend.seo.SitemapGroup;
import app.carburo.backend.seo.SitemapUrlProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

@Service
public class SitemapService {

    public static final String SITEMAP_URL = "/sitemap.xml";

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final String ROOT_PATH = "/";
    private static final String TRAILING_SLASHES_PATTERN = "/+$";

    private static final String XML_ENCODING = "UTF-8";
    private static final String XML_VERSION = "1.0";
    private static final String SITEMAP_NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9";
    private static final String XSI_PREFIX = "xsi";
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String SCHEMA_LOCATION_ATTRIBUTE = "schemaLocation";
    private static final String INDEX_SCHEMA_LOCATION = SITEMAP_NAMESPACE + " "
            + SITEMAP_NAMESPACE + "/siteindex.xsd";
    private static final String SITEMAP_SCHEMA_LOCATION = SITEMAP_NAMESPACE + " "
            + SITEMAP_NAMESPACE + "/sitemap.xsd";

    private static final String TAG_SITEMAP_INDEX = "sitemapindex";
    private static final String TAG_URL_SET = "urlset";
    private static final String TAG_SITEMAP = "sitemap";
    private static final String TAG_URL = "url";
    private static final String TAG_LOCATION = "loc";
    private static final String TAG_LAST_MODIFIED = "lastmod";

    private final String publicUrl;
    private final List<SitemapUrlProvider> providers;

    public SitemapService(
            @Value("${app.url}") String publicUrl,
            List<SitemapUrlProvider> providers
    ) {
        URI uri = URI.create(publicUrl);

        if (uri.getScheme() == null || !ALLOWED_SCHEMES.contains(uri.getScheme()) || uri.getHost() == null
                || uri.getRawQuery() != null || uri.getRawFragment() != null
                || uri.getUserInfo() != null || !(uri.getPath().isEmpty() || uri.getPath().equals(ROOT_PATH))) {
            throw new IllegalArgumentException("app.url debe ser el origen público HTTP(S), sin ruta.");
        }

        this.publicUrl = publicUrl.replaceAll(TRAILING_SLASHES_PATTERN, "");
        this.providers = List.copyOf(providers);
    }

    public String getSitemapUrl() {
        return publicUrl + SITEMAP_URL;
    }

    public String generateSitemap() {
        return generateXML(true, Arrays.stream(SitemapGroup.values())
                .map(SitemapGroup::getPath)
                .toList(), Map.of());
    }

    public String generateSitemap(SitemapGroup grupo) {
        Set<String> rutas = new LinkedHashSet<>();
        Map<String, java.time.LocalDate> fechas = new HashMap<>();

        for (SitemapUrlProvider provider : providers) {
            if (provider.getSitemapGroup() != grupo) continue;
            for (String ruta : provider.getSitemapPaths()) {
                rutas.add(ruta);
                java.time.LocalDate fecha = provider.getSitemapLastModified(ruta);
                if (fecha != null) fechas.merge(ruta, fecha, (a, b) -> a.isAfter(b) ? a : b);
            }
        }

        return generateXML(false, rutas, fechas);
    }

    private String generateXML(boolean indice, Collection<String> rutas,
                               Map<String, java.time.LocalDate> fechas) {
        try {
            StringWriter output = new StringWriter();
            XMLStreamWriter xml = XMLOutputFactory.newFactory().createXMLStreamWriter(output);
            xml.writeStartDocument(XML_ENCODING, XML_VERSION);
            xml.writeStartElement(indice ? TAG_SITEMAP_INDEX : TAG_URL_SET);
            xml.writeDefaultNamespace(SITEMAP_NAMESPACE);
            xml.writeNamespace(XSI_PREFIX, XSI_NAMESPACE);
            xml.writeAttribute(XSI_PREFIX, XSI_NAMESPACE, SCHEMA_LOCATION_ATTRIBUTE,
                    indice ? INDEX_SCHEMA_LOCATION : SITEMAP_SCHEMA_LOCATION);
            for (String ruta : rutas) {
                xml.writeStartElement(indice ? TAG_SITEMAP : TAG_URL);
                xml.writeStartElement(TAG_LOCATION);
                xml.writeCharacters(publicUrl + ruta);
                xml.writeEndElement();
                if (fechas.containsKey(ruta)) {
                    xml.writeStartElement(TAG_LAST_MODIFIED);
                    xml.writeCharacters(fechas.get(ruta).toString());
                    xml.writeEndElement();
                }
                xml.writeEndElement();
            }
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
            return output.toString();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("No se pudo generar el sitemap", e);
        }
    }

}
