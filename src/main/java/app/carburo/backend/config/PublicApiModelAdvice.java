package app.carburo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone a las vistas la configuración pública que utiliza el cliente web.
 */
@ControllerAdvice
public class PublicApiModelAdvice {

    private final String publicApiKey;
    private final String publicApiUrl;

    public PublicApiModelAdvice(
            @Value("${security.api.public-key}") String publicApiKey,
            @Value("${security.api.public-url}") String publicApiUrl) {
        this.publicApiKey = publicApiKey;
        this.publicApiUrl = publicApiUrl;
    }

    @ModelAttribute
    public void addPublicApiConfiguration(Model model) {
        model.addAttribute("publicApiKey", publicApiKey);
        model.addAttribute("publicApiUrl", publicApiUrl);
    }
}
