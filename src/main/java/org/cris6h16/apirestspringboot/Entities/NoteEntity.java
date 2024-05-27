package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

// import static Validation responses
import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Validations.*;

@Entity
@Table(name = "notes")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class NoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_note_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    @Column(name = "title", length = MAX_TITLE_LENGTH)
    @NotBlank(message = TITLE_IS_BLANK_MSG) // not  null/only spaces
    @Length(max = 255, message = TITLE_MAX_LENGTH_MSG)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "updated_at")
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = {},
            optional = true,
            targetEntity = UserEntity.class)
    private UserEntity user;

}
