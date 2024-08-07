package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Validations.*;

/**
 * Entity to represent the {@code notes}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Entity
@Table(name = "notes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class NoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_note_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    @Column(
            name = "title",
            length = MAX_TITLE_LENGTH,
            nullable = false
    )
    private String title;

    @Column(
            name = "content",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String content;

    @Column(
            name = "updated_at",
            nullable = false
    )
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {},
            optional = true,
            targetEntity = UserEntity.class
    )
    private UserEntity user;
}
