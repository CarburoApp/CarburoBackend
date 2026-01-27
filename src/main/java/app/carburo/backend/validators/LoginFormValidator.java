package app.carburo.backend.validators;

import app.carburo.backend.dto.UsuarioDto;
import app.carburo.backend.services.AuthService;
import app.carburo.backend.services.UsuarioService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.io.IOException;
import java.util.regex.Pattern;

import static app.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CONTRASENA;
import static app.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CORREO;

/**
 * Validador del formulario de inicio de sesión de usuarios.
 * <p>
 * Se encarga de validar:
 * <ul>
 *     <li>Campos obligatorios</li>
 *     <li>Formato y existencia del correo electrónico.</li>
 * </ul>
 */
@Component
public class LoginFormValidator implements Validator {

    /**
     * Expresión regular básica para correo electrónico
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // Inyección de dependencias
    private final UsuarioService usuarioService;
    private final AuthService authService;

    public LoginFormValidator(UsuarioService usuarioService,
                              AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
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
        // Validación de campos vacíos

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, USUARIO_DTO_FIELD_CORREO,
                "Error.auth.login.correo.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, USUARIO_DTO_FIELD_CONTRASENA,
                "Error.auth.login.contrasena.empty");

        // Errores de campos obligatorios, no seguimos
        if (errors.hasErrors()) {
            return;
        }

        String correo = usuario.getCorreo();

        /* ===========================
         *  Validación del correo
         * =========================== */

        // Formato del correo
        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            errors.rejectValue(USUARIO_DTO_FIELD_CORREO,
                               "Error.auth.login.correo.formato");
        }
        // Unicidad del correo
        else {
            try {
                if (!usuarioService.containsCorreo(correo) || !authService.containsEmail(correo)) {
                    errors.rejectValue(USUARIO_DTO_FIELD_CORREO,
                                       "Error.auth.login.correo.no.existente");
                }
            } catch (IOException e) {
                errors.rejectValue(USUARIO_DTO_FIELD_CORREO, "Error.auth.login.fallo");
            }
        }
    }
}
