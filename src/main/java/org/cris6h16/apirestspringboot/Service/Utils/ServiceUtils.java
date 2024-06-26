package org.cris6h16.apirestspringboot.Service.Utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.cris6h16.apirestspringboot.Utils.FilesSyncUtils;
import org.cris6h16.apirestspringboot.Utils.SychFor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;

/**
 * Utility class for process the exceptions in the service layer methods,
 * this class is used to pass from an exception ( sometimes with critical information )
 * to an extended exception of {@link AbstractExceptionWithStatus} with
 * a proper message and status ready to pass through the layers
 * even ready to be sent directly to the client.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @implNote The existent methods are concurrently, There isn't synchronization
 * in the methods, I'm using local variables for sure the immutability
 * then all are thread-safe.
 * @since 1.0
 */
@Slf4j
@Component
public class ServiceUtils {

    private final FilesSyncUtils filesUtils;

    public ServiceUtils(FilesSyncUtils filesUtils) {
        this.filesUtils = filesUtils;
    }

    /**
     * Create a new exception (Extended of {@link AbstractExceptionWithStatus})
     * with a proper message and status ready to be sent directly to the client.
     *
     * @param e             the exception to handle
     * @param isUserService if true return a {@link UserServiceTransversalException} else a {@link NoteServiceTransversalException}
     * @return a new TransversalException with the proper message and status ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public AbstractExceptionWithStatus createATraversalExceptionHandled(@NotNull Exception e, boolean isUserService) {
        String forClient = ""; // PD: verification based on: .isBlank(), dont add generic message here
        HttpStatus recommendedStatus = null; // also here, but with null

        // ------------ commons in both entities --------------------\\
        if (e instanceof ConstraintViolationException && forClient.isBlank()) { //{ not blank, invalid email, max length, etc }
            recommendedStatus = HttpStatus.BAD_REQUEST;
            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) e).getConstraintViolations();

            if (!violations.isEmpty()) forClient = violations.iterator().next().getMessage();
        }

        if (e instanceof AbstractExceptionWithStatus && forClient.isBlank()) { // { user not found, invalid id, password too short }
            recommendedStatus = ((AbstractExceptionWithStatus) e).getRecommendedStatus();
            forClient = e.getMessage();
        }

        if (e instanceof NullPointerException && forClient.isBlank()) {
            boolean pageablePassedNull = this.thisContains(e.getMessage(), "because \"pageable\" is null");
            if (pageablePassedNull) recommendedStatus = HttpStatus.BAD_REQUEST;
        }

        if (e instanceof PropertyReferenceException && forClient.isBlank()) { // { No property '<ttt>' found for type '<User>Entity' }
            recommendedStatus = HttpStatus.BAD_REQUEST;
            boolean propertyNonexistent = this.thisContains(e.getMessage(), "for type");
            if (propertyNonexistent) forClient = e.getMessage().split("for type")[0].trim();
        }

        // ------------ response regarding the entity ------------- \\
        if (isUserService) {
            if (e instanceof DataIntegrityViolationException && forClient.isBlank()) { //{ primary key, unique constraints }
                recommendedStatus = HttpStatus.CONFLICT;

                boolean inUsername = thisContains(e.getMessage(), USERNAME_UNIQUE_NAME);
                boolean inEmail = thisContains(e.getMessage(), EMAIL_UNIQUE_NAME);
                boolean isHandledUniqueViolation = inUsername || inEmail;
                if (isHandledUniqueViolation) forClient = inUsername ? USERNAME_UNIQUE_MSG : EMAIL_UNIQUE_MSG;
            }

        } else {
            /*
            now for note service I don't have custom exceptions, here
            can be added exceptions like DataIntegrityViolationException
            if we have unique constraints
             */
        }

        // -------------- default handling ( UNHANDLED ) ----------------- \\
        {
            boolean statusIsNull = recommendedStatus == null;
            boolean forClientIsBlank = forClient.isBlank();
            if (statusIsNull) recommendedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            if (forClientIsBlank) forClient = Cons.Response.ForClient.GENERIC_ERROR;
            if (statusIsNull) logUnhandledException(e);
            else logDebug(e);
        }

        return isUserService ?
                new UserServiceTransversalException(forClient, recommendedStatus) :
                new NoteServiceTransversalException(forClient, recommendedStatus);
    }


    /**
     * Verify if the {@code msg.toLowerCase().trim()} contains all the
     * {@code strings.toLowerCase().trim()}
     *
     * @param msg     that contains the {@code strings}
     * @param strings to verify if are in the {@code msg}
     * @return true if all the {@code strings} are in the {@code msg}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public boolean thisContains(String msg, String... strings) {
        if (msg == null || strings == null || strings.length == 0) return false;

        msg = msg.toLowerCase().trim();
        boolean contains = true;
        for (String s : strings) {
            s = s.toLowerCase().trim();
            contains = contains && msg.contains(s);
        }
        return contains;
    }

    /**
     * save the {@code UnauthenticatedException} in a file, also log an {@code ERROR}
     * if the exception is an exception threw in production.
     *
     * @param e the exception to log
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public void logUnhandledException(@NotNull Exception e) {
        boolean isTesting = this.thisContains(e.getMessage(), Cons.TESTING.UNHANDLED_ERROR_WITH_TESTING_PURPOSES);
        if (!isTesting)
            log.error("Unhandled exception: {}", (e.toString())); // print ERRORs in testing can be confusing
        saveUnhandledException(e);
    }

    /**
     * Save the unhandled exception in the file: {@link Cons.Logs#UNHANDLED_EXCEPTIONS_FILE}
     *
     * @param e the exception to save in the file
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void saveUnhandledException(Exception e) {
        Path path = Path.of(Cons.Logs.UNHANDLED_EXCEPTIONS_FILE);
        filesUtils.appendToFile(
                path,
                new Date().toString() + "::" + e.toString() + "::" + Arrays.toString(e.getStackTrace()),
                SychFor.UNHANDLED_EXCEPTIONS
        );
    }

    /**
     * Log the exception with the level {@link org.slf4j.event.Level#DEBUG}, this
     * should be used when the exception is handled.
     *
     * @param e the exception to log
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void logDebug(@NotNull Exception e) {
        log.debug("Handled exception: {}", e.toString());
    }


    /**
     * @param id to validate
     * @throws InvalidIdException if {@code id} is null or less than 1
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public void validateId(Long id) {
        if (id == null || id <= 0) throw new InvalidIdException();
    }
}
