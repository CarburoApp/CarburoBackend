package app.carburo.backend.validators;

import app.carburo.backend.dto.UsuarioDto;
import app.carburo.backend.util.EmailValidation;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


import static app.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CORREO;

/**
 * Validador del formulario de recuperación de contraseña.
 * <p>
 * Se encarga de validar:
 * <ul>
 *     <li>Campos obligatorios</li>
 *     <li>Formato del correo electrónico.</li>
 * </ul>
 */
@Component
public class RecuperacionFormValidator implements Validator {

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
        // Validación de campos vacíos
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, USUARIO_DTO_FIELD_CORREO,
                "Error.auth.login.correo.empty");

        // Errores de campos obligatorios, no seguimos
        if (errors.hasErrors()) return;

        //  Validación del correo
        if (!EmailValidation.isValid(usuario.getCorreo())) {
            errors.rejectValue(USUARIO_DTO_FIELD_CORREO,
                               "Error.auth.login.correo.formato");
        }
    }
}
