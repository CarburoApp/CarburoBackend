package app.carburo.backend.services;

import app.carburo.backend.entities.EstacionDeServicio;
import app.carburo.backend.entities.Provincia;
import app.carburo.backend.entities.Usuario;
import app.carburo.backend.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
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
     * Elimina un usuario en el sistema.
     */
    public void delete(UUID uuidUsuario) {
        if (uuidUsuario == null ) {
            throw new IllegalArgumentException("El UUID del usuario no puede ser nulo.");
        }
        usuarioRepository.deleteById(uuidUsuario);
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

    /**
     * Comprueba si una estación de servicio está marcada como favorita por un usuario.
     *
     * @param usuario            Usuario a comprobar
     * @param estacionDeServicio Estación de servicio
     * @return true si la estación es favorita del usuario, false en caso contrario
     * @throws IllegalArgumentException si alguno de los parámetros es null
     */
    public boolean isEstacionDeServicioFavorita(Usuario usuario,
                                                EstacionDeServicio estacionDeServicio) {
        if (usuario == null || estacionDeServicio == null)
            throw new IllegalArgumentException(
                    "Parámetros inválidos para la comprobación de estación de servicio favorita para el usuario.");
        return usuario.getEessFavoritas().contains(estacionDeServicio);
    }

    /**
     * Elimina una estación de servicio de la lista de favoritas del usuario.
     *
     * @param usuario            Usuario propietario de las favoritas
     * @param estacionDeServicio Estación a eliminar
     * @throws IllegalArgumentException si alguno de los parámetros es null
     */
    @Transactional
    public void removeEstacionDeServicioFavorita(Usuario usuario,
                                                 EstacionDeServicio estacionDeServicio) {
        if (!isEstacionDeServicioFavorita(usuario, estacionDeServicio)) return;
        if (usuario == null || estacionDeServicio == null)
            throw new IllegalArgumentException(
                    "Parámetros inválidos para la comprobación de estación de servicio favorita para el usuario.");
        usuario.getEessFavoritas().remove(estacionDeServicio);
        usuarioRepository.save(usuario);
    }

    /**
     * Añade una estación de servicio a la lista de favoritas del usuario.
     *
     * @param usuario            Usuario propietario de las favoritas
     * @param estacionDeServicio Estación a añadir
     * @throws IllegalArgumentException si alguno de los parámetros es null
     */
    @Transactional
    public void addEstacionDeServicioFavorita(Usuario usuario,
                                              EstacionDeServicio estacionDeServicio) {
        if (isEstacionDeServicioFavorita(usuario, estacionDeServicio)) return;
        if (usuario == null || estacionDeServicio == null)
            throw new IllegalArgumentException(
                    "Parámetros inválidos para la comprobación de estación de servicio favorita para el usuario.");
        usuario.getEessFavoritas().add(estacionDeServicio);
        usuarioRepository.save(usuario);
    }
}