package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when the title is blank
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class TitleIsBlankException extends ProperExceptionForTheUser {
    public TitleIsBlankException() {
        super(HttpStatus.BAD_REQUEST, Cons.Note.Validations.TITLE_IS_BLANK_MSG);
    }
}
