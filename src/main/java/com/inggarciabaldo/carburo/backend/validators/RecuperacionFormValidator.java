package com.inggarciabaldo.carburo.backend.validators;

import com.inggarciabaldo.carburo.backend.dto.UsuarioDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

import static com.inggarciabaldo.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CORREO;

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
     * Expresión regular básica para correo electrónico
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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
        if (!EMAIL_PATTERN.matcher(usuario.getCorreo()).matches()) {
            errors.rejectValue(USUARIO_DTO_FIELD_CORREO,
                               "Error.auth.login.correo.formato");
        }
    }
}
