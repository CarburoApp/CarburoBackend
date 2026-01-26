package com.inggarciabaldo.carburo.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "usuario_email_key", columnNames = "email"),
                @UniqueConstraint(name = "usuario_uuid_unique", columnNames = "uuid")
        })
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    // PK
    @Id
    @Column(nullable = false, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

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
            name = "provincia_favorita",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "usuario_provincia_favorita_fkey"),
            nullable = false
    )
    private Provincia provinciaFavorita;

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


    public Usuario(UUID uuid, String nombre, String email,
                   OffsetDateTime fechaRegistro,
                   OffsetDateTime fechaActualizacion,
                   Provincia provinciaFavorita) {
        setUuid(uuid);
        setNombre(nombre);
        setEmail(email);
        setFechaRegistro(fechaRegistro);
        setFechaActualizacion(fechaActualizacion);
        setProvinciaFavorita(provinciaFavorita);
    }

    public Usuario(UUID uuid, String nombre, String email,
                   Provincia provinciaFavorita) {
        setUuid(uuid);
        setNombre(nombre);
        setEmail(email);
        setFechaRegistro(OffsetDateTime.now());
        setFechaActualizacion(OffsetDateTime.now());
        setProvinciaFavorita(provinciaFavorita);
    }

}
