package com.inggarciabaldo.carburo.backend.services;

import com.inggarciabaldo.carburo.backend.entities.Provincia;
import com.inggarciabaldo.carburo.backend.entities.Usuario;
import com.inggarciabaldo.carburo.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Servicio encargado de la gestión de usuarios del sistema.
 * <p>
 * Proporciona operaciones relacionadas con la persistencia y actualización
 * de la entidad {@link Usuario}, desacoplando la lógica de negocio del acceso
 * directo al repositorio.
 * </p>
 *
 * <p>
 * Este servicio se utiliza principalmente como apoyo al sistema de autenticación
 * (Supabase + Spring Security).:
 * </p>
 */
@Service
public class UsuarioService {

    /**
     * Repositorio de acceso a datos de usuarios.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param usuarioRepository repositorio de usuarios
     */
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Comprueba si un correo electrónico ya está registrado en el sistema.
     *
     * @param correo correo electrónico a comprobar
     * @return {@code true} si el correo ya existe, {@code false} en caso contrario
     * @throws IllegalArgumentException si el correo es {@code null} o está vacío
     */
    public boolean containsCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo no puede ser nulo ni vacío.");
        }
        return usuarioRepository.existsUsuarioByEmail(correo);
    }

    /**
     * Guarda un usuario en el sistema.
     *
     * @param usuario entidad {@link Usuario} a persistir
     * @throws IllegalArgumentException si el usuario o su UUID son {@code null}
     */
    public void save(Usuario usuario) {
        if (usuario == null || usuario.getUuid() == null) {
            throw new IllegalArgumentException("El usuario y su UUID no pueden ser nulos.");
        }
        usuarioRepository.save(usuario);
    }

    /**
     * Actualiza el nombre de un usuario existente.
     *
     * @param id                   identificador UUID del usuario
     * @param nuevoNombreDeUsuario nuevo nombre a asignar
     * @throws IllegalArgumentException si alguno de los parámetros es inválido
     */
    public void updateNombreDeUsuario(UUID id, String nuevoNombreDeUsuario) {
        if (id == null || nuevoNombreDeUsuario == null || nuevoNombreDeUsuario.isBlank()) {
            throw new IllegalArgumentException("Parámetros inválidos para la actualización del nombre.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setNombre(nuevoNombreDeUsuario);
            usuarioRepository.save(usuario);
        }
    }

    /**
     * Actualiza la provincia favorita de un usuario.
     *
     * @param id        identificador UUID del usuario
     * @param provincia nueva provincia favorita
     * @throws IllegalArgumentException si alguno de los parámetros es {@code null}
     */
    public void updateProvinciaFavoritaUsuario(UUID id, Provincia provincia) {
        if (id == null || provincia == null) {
            throw new IllegalArgumentException("Parámetros inválidos para la actualización de provincia.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setProvinciaFavorita(provincia);
            usuarioRepository.save(usuario);
        }
    }

    /**
     * Busca un usuario por su identificador UUID.
     *
     * @param uuid identificador del usuario
     * @return {@link Optional} con el usuario si existe, o {@link Optional#empty()} en caso contrario
     * @throws IllegalArgumentException si el UUID es {@code null}
     */
    public Optional<Usuario> findByUUID(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("El UUID no puede ser nulo.");
        }
        return usuarioRepository.findById(uuid);
    }
}