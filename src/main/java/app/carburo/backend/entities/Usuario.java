package app.carburo.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    // PK
    @Id
    @Column(nullable = false, updatable = false)
    private UUID uuid;

    @Transient
    private String nombre;

    @Transient
    private String email;

    @Transient
    private String avatarUrl;

    // Contraseñas temporales, no se guardan en BD
    @Transient
    private String contrasena;

    @Transient
    private String confirmacionContrasena;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    // FK a provincia, unidireccional
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_provincia_favorita",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "usuario_provincia_favorita_fkey"),
            nullable = false
    )
    private Provincia provinciaFavorita;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "combustible_favorito",
            joinColumns = @JoinColumn(
                    name = "uuid_usuario",
                    referencedColumnName = "uuid"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "id_combustible",
                    referencedColumnName = "id"
            )
    )
    private Set<Combustible> combustiblesFavoritos = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "eess_favoritas",
            joinColumns = @JoinColumn(
                    name = "uuid_usuario",
                    referencedColumnName = "uuid"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "id_eess",
                    referencedColumnName = "id"
            )
    )
    private Set<EstacionDeServicio> eessFavoritas = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = OffsetDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = OffsetDateTime.now();
    }


    public Usuario(UUID uuid, OffsetDateTime fechaRegistro,
                   OffsetDateTime fechaActualizacion,
                   Provincia provinciaFavorita) {
        setUuid(uuid);
        setFechaRegistro(fechaRegistro);
        setFechaActualizacion(fechaActualizacion);
        setProvinciaFavorita(provinciaFavorita);
    }

    public Usuario(UUID uuid,
                   Provincia provinciaFavorita) {
        setUuid(uuid);
        setFechaRegistro(OffsetDateTime.now());
        setFechaActualizacion(OffsetDateTime.now());
        setProvinciaFavorita(provinciaFavorita);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(uuid, usuario.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
