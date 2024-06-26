package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller for {@link NoteServiceImpl}}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestController
@RequestMapping(path = NoteController.path)
@PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'USER')")
public class NoteController {

    public static final String path = "/api/notes";
    private final NoteServiceImpl noteService;

    public NoteController(NoteServiceImpl noteService) {
        this.noteService = noteService;
    }


    /**
     * Create a {@link NoteEntity}<br>
     * make it through: {@link NoteServiceImpl#create(CreateNoteDTO, Long)}
     *
     * @param note        {@link CreateNoteDTO} with the data of the new note
     * @param principalId injected, the id of the principal; the user that is creating the note
     * @return {@link ResponseEntity#created(URI)} with the location of the created note
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE // if is successful else the defined on Advice
    )
    public ResponseEntity<Void> create(@RequestBody CreateNoteDTO note, @MyId Long principalId) {
        Long id = noteService.create(note, principalId);
        URI uri = URI.create(path + "/" + id);

        return ResponseEntity.created(uri).build();
    }

    /**
     * Get a page of {@link NoteEntity}<br>
     * make it through: {@link NoteServiceImpl#getPage(Pageable, Long)}
     *
     * @param pageable    the page request
     * @param principalId injected, the id of the principal; the user that is getting the notes
     * @return {@link ResponseEntity} with the page of notes
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<PublicNoteDTO>> getPage(Pageable pageable, @MyId Long principalId) {
        List<PublicNoteDTO> list = noteService.getPage(pageable, principalId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get a {@link NoteEntity} by id<br>
     * make it through: {@link NoteServiceImpl#get(Long, Long)}
     *
     * @param noteId      of the note to get
     * @param principalId injected, the id of the principal; the user that is getting the note
     * @return {@link ResponseEntity} with the note data
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @GetMapping(
            value = "/{noteId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PublicNoteDTO> get(@PathVariable Long noteId, @MyId Long principalId) {
        PublicNoteDTO en = noteService.get(noteId, principalId);
        return ResponseEntity.ok(en);
    }

    /**
     * Update a {@link NoteEntity}<br>
     * make it through: {@link NoteServiceImpl#put(Long, CreateNoteDTO, Long)}
     *
     * @param noteId      of the note to update
     * @param note        to be PUT
     * @param principalId the principal id; the user that is updating the note
     * @return {@link ResponseEntity#noContent()} if successful
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @PutMapping(
            value = "/{noteId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> update(@PathVariable Long noteId,
                                       @RequestBody CreateNoteDTO note,
                                       @MyId Long principalId) {
        noteService.put(noteId, note, principalId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a {@link NoteEntity}<br>
     * make it through: {@link NoteServiceImpl#delete(Long, Long)}
     *
     * @param noteId      of the note to delete
     * @param principalId the principal id; the user that is deleting the note
     * @return {@link ResponseEntity#noContent()} if successful
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @DeleteMapping(value = "/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable Long noteId, @MyId Long principalId) {
        noteService.delete(noteId, principalId);
        return ResponseEntity.noContent().build();
    }
}
