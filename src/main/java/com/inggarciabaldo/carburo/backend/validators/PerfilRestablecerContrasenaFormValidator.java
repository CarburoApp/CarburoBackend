package com.inggarciabaldo.carburo.backend.validators;

import com.inggarciabaldo.carburo.backend.dto.UsuarioDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import static com.inggarciabaldo.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CONTRASENA;
import static com.inggarciabaldo.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CONTRASENA_CONFIRMACION;

/**
 * Validador del formulario de restablecimineto de contraseña para usuarios.
 * <p>
 * Se encarga de validar:
 * <ul>
 *     <li>Campos obligatorios</li>
 *     <li>Fortaleza y confirmación de la contraseña</li>
 * </ul>
 */
@Component
public class PerfilRestablecerContrasenaFormValidator implements Validator {

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

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, USUARIO_DTO_FIELD_CONTRASENA,
												  "Error.auth.restablecer.contrasena.nueva.empty");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors,
												  USUARIO_DTO_FIELD_CONTRASENA_CONFIRMACION,
												  "Error.auth.restablecer.contrasena.confirmacion.empty");

		// Si ya hay errores de campos obligatorios, no seguimos
		if (errors.hasErrors()) {
			return;
		}

		String contrasena = usuario.getContrasena();
		String confirmacionContrasena = usuario.getConfirmacionContrasena();

		/* ===========================
		 *  Validación de contraseña
		 * =========================== */

		if (RegistroFormValidator.isValidPassword(contrasena, errors) &&
				!contrasena.equals(confirmacionContrasena)) {
			errors.rejectValue(USUARIO_DTO_FIELD_CONTRASENA_CONFIRMACION,
							   "Error.auth.restablecer.contrasena.confirmacion.incorrecta");
		}
	}
}
