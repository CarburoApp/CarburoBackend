package com.inggarciabaldo.carburo.backend.validators;

import com.inggarciabaldo.carburo.backend.dto.UsuarioDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.io.IOException;

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
public class PerfilCambioDeNombreFormValidator implements Validator {

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

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nombre",
                "Error.auth.login.nombre.empty");

        // Errores de campos obligatorios, no seguimos
        if (errors.hasErrors()) {
            return;
        }

        String nombre = usuario.getNombre();

        //  Validación del Nombre
        if (nombre == null || nombre.length() > RegistroFormValidator.LONGITUD_MAX_NOMBRE) {
            errors.rejectValue("nombre", "Error.auth.login.nombre.empty");
        }
    }
}
