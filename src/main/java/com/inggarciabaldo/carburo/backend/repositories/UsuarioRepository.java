package com.inggarciabaldo.carburo.backend.repositories;

import com.inggarciabaldo.carburo.backend.entities.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

/**
 * Repositorio para la entidad Usuario.
 * Proporciona métodos de acceso y comprobación de existencia de usuarios.
 */
public interface UsuarioRepository extends CrudRepository<Usuario, UUID> {

    /**
     * Comprueba si existe un usuario con el correo electrónico indicado.
     *
     * @param email correo electrónico del usuario
     * @return true si existe un usuario con ese correo, false en caso contrario
     */
    boolean existsUsuarioByEmail(String email);
}
