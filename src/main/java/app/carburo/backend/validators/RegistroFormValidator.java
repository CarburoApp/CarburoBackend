package app.carburo.backend.validators;

import app.carburo.backend.dto.UsuarioDto;
import app.carburo.backend.services.AuthService;
import app.carburo.backend.services.ProvinciaService;
import app.carburo.backend.services.UsuarioService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Validador del formulario de registro de usuario.
 * <p>
 * Se encarga de validar:
 * <ul>
 *     <li>Campos obligatorios</li>
 *     <li>Formato y unicidad del correo electrónico</li>
 *     <li>Fortaleza y confirmación de la contraseña</li>
 *     <li>Selección válida de provincia</li>
 * </ul>
 */
@Component
public class RegistroFormValidator implements Validator {

    // Constantes para literales del UsuarioDto
    public static final String USUARIO_DTO_FIELD_NOMBRE = "nombre";
    public static final String USUARIO_DTO_FIELD_CORREO = "correo";
    public static final String USUARIO_DTO_FIELD_CONTRASENA = "contrasena";
    public static final String USUARIO_DTO_FIELD_CONTRASENA_CONFIRMACION = "confirmacionContrasena";
    public static final String USUARIO_DTO_FIELD_PROVINCIA = "provinciaFavorita";
    
    /* ===========================
     *  Constantes de validación
     * =========================== */

    /**
     * Longitud mínima de la contraseña
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * Longitud máxima del nombre
     */
    public static final int LONGITUD_MAX_NOMBRE = 50;

    /**
     * Expresiones regulares para la contraseña
     */
    public static final Pattern PASSWORD_UPPERCASE = Pattern.compile(".*[A-Z].*");
    public static final Pattern PASSWORD_LOWERCASE = Pattern.compile(".*[a-z].*");
    public static final Pattern PASSWORD_DIGIT = Pattern.compile(".*\\d.*");
    public static final Pattern PASSWORD_SPECIAL = Pattern.compile(".*[^a-zA-Z0-9].*");

    /**
     * Expresión regular básica para correo electrónico
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // Inyección de dependencias
    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final ProvinciaService provinciaService;

    public RegistroFormValidator(UsuarioService usuarioService,
                                 AuthService authService,
                                 ProvinciaService provinciaService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
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

        /* ===========================
         *  Validación de campos vacíos
         * =========================== */

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, USUARIO_DTO_FIELD_NOMBRE,
                "Error.auth.login.nombre.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, USUARIO_DTO_FIELD_CORREO,
                "Error.auth.login.correo.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, USUARIO_DTO_FIELD_CONTRASENA,
                "Error.auth.login.contrasena.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,
                                                  USUARIO_DTO_FIELD_CONTRASENA_CONFIRMACION,
                                                  "Error.auth.login.confirmacion.contrasena.empty");

        // Si ya hay errores de campos obligatorios, no seguimos
        if (errors.hasErrors()) {
            return;
        }

        String nombre = usuario.getNombre();
        String correo = usuario.getCorreo();
        String contrasena = usuario.getContrasena();
        String confirmacionContrasena = usuario.getConfirmacionContrasena();
        int provinciaId = usuario.getProvinciaFavorita();

        /* ===========================
         *  Validación del nombre
         * =========================== */

        if (nombre.trim().isEmpty()) {
            errors.rejectValue(USUARIO_DTO_FIELD_NOMBRE, "Error.auth.login.nombre.empty");
        }

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
                if (usuarioService.containsCorreo(correo) || authService.containsEmail(correo)) {
                    errors.rejectValue(USUARIO_DTO_FIELD_CORREO,
                                       "Error.auth.login.correo.existente");
                }
            } catch (IOException e) {
                errors.rejectValue(USUARIO_DTO_FIELD_CORREO,
                                   "Error.auth.login.registro.fallo");
            }
        }

        /* ===========================
         *  Validación de contraseña
         * =========================== */

        if (isValidPassword(contrasena, errors) &&
                !contrasena.equals(confirmacionContrasena)) {
            errors.rejectValue(USUARIO_DTO_FIELD_CONTRASENA_CONFIRMACION,
                    "Error.auth.login.contrasena.confirmacion");
        }

        /* ===========================
         *  Validación de provincia
         * =========================== */

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


        //  Validación del Nombre
        if (nombre.length() > LONGITUD_MAX_NOMBRE) {
            errors.rejectValue(USUARIO_DTO_FIELD_NOMBRE, "Error.auth.login.nombre.empty");
        }
    }

    /**
     * Se encarga de válidar la fortaleza de la contraseña.
     *
     * @param contrasena La contraseña a validar
     * @param errors     El objeto de errores donde registrar los errores de validación
     * @return true si la contraseña es válida, false en caso contrario
     */
    public static boolean isValidPassword(String contrasena, Errors errors) {
        if (contrasena.length() < PASSWORD_MIN_LENGTH) {
            errors.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                               "Error.auth.login.contrasena.length");
        } else if (!PASSWORD_UPPERCASE.matcher(contrasena).matches()) {
            errors.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                               "Error.auth.login.contrasena.mayuscula");
        } else if (!PASSWORD_LOWERCASE.matcher(contrasena).matches()) {
            errors.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                               "Error.auth.login.contrasena.minuscula");
        } else if (!PASSWORD_DIGIT.matcher(contrasena).matches()) {
            errors.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                               "Error.auth.login.contrasena.numero");
        } else if (!PASSWORD_SPECIAL.matcher(contrasena).matches()) errors.rejectValue(
                USUARIO_DTO_FIELD_CONTRASENA, "Error.auth.login.contrasena.simbolo");
        else return true;
        return false;
    }
}
