package app.carburo.backend.validators;

import app.carburo.backend.dto.UsuarioDto;
import app.carburo.backend.services.ProvinciaService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static app.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_PROVINCIA;

/**
 * Validador del formulario del cambio de la provincia del usuario.
 * <p>
 * Se encarga de validar:
 * <ul>
 *     <li>Campos obligatorios</li>
 *     <li>Selección válida de provincia</li>
 * </ul>
 */
@Component
public class PerfilCambioDeProvinciaFavoritaFormValidator implements Validator {

    // Inyección de dependencias
    private final ProvinciaService provinciaService;

    public PerfilCambioDeProvinciaFavoritaFormValidator(ProvinciaService provinciaService) {
        this.provinciaService = provinciaService;
    }

    /**
     * Indica qué clase soporta este validador.
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return UsuarioDto.class.equals(clazz);
    }

    /**
     * Lógica principal de validación.
     */
    @Override
    public void validate(Object target, Errors errors) {
        UsuarioDto usuario = (UsuarioDto) target;

        //  Validación de provincia
        int provinciaId = usuario.getProvinciaFavorita();
        long totalProvincias = provinciaService.getTotalProvincias();

        // Provincia negativa o fuera de rango
        if (provinciaId < 0 || provinciaId > totalProvincias) {
            errors.rejectValue(USUARIO_DTO_FIELD_PROVINCIA,
                    "Error.auth.login.provincia.generico");
        }
        // No ha seleccionado provincia (opción por defecto)
        else if (provinciaId == 0) {
            errors.rejectValue(USUARIO_DTO_FIELD_PROVINCIA,
                    "Error.auth.login.provincia.noSeleccionada");
        }
    }
}
