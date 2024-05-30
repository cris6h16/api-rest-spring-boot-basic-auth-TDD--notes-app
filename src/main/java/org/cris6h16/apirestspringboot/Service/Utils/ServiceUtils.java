package org.cris6h16.apirestspringboot.Service.Utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.AbstractServiceExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserServiceTransversalException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;

@Slf4j
@Component
public class ServiceUtils {

    public AbstractServiceExceptionWithStatus createATraversalExceptionHandled(@NotNull Exception e, boolean isUserService) {
        String forClient = ""; // PD: verification based on: .isBlank(), dont add generic message here
        HttpStatus recommendedStatus = null; // also here, but with null

        try {

            // ------------ commons in both entities --------------------\\
            // data integrity violations { not blank, invalid email, max length, etc }
            if (e instanceof ConstraintViolationException && forClient.isBlank()) {
                recommendedStatus = HttpStatus.BAD_REQUEST;
                Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) e).getConstraintViolations();

                if (!violations.isEmpty()) forClient = violations.iterator().next().getMessage();
            }

            // Note Service: { user not found, invalid id }
            // UserService:  { user not found, invalid id, password too short }
            if (e instanceof AbstractServiceExceptionWithStatus && forClient.isBlank()) {
                recommendedStatus = ((AbstractServiceExceptionWithStatus) e).getRecommendedStatus();
                forClient = e.getMessage();
            }

            // pageable fails like passed a negative page, size, etc
            if (e instanceof IllegalArgumentException && forClient.isBlank()) {
                recommendedStatus = HttpStatus.BAD_REQUEST;
                boolean pageableFail = this.thisContains(e.getMessage(), "Page");
                if (pageableFail) forClient = e.getMessage();
            }

            if (e instanceof NullPointerException && forClient.isBlank()){
                boolean pageablePassedNull = this.thisContains(e.getMessage(), "because \"pageable\" is null");
                if (pageablePassedNull){
                    recommendedStatus = HttpStatus.BAD_REQUEST;
                    // forClient => empty isn't necessary more info
                }
            }


            if (e instanceof PropertyReferenceException && forClient.isBlank()) {
                recommendedStatus = HttpStatus.BAD_REQUEST;
                boolean propertyNonexistent = this.thisContains(e.getMessage(), "for type");
                if (propertyNonexistent) {
                    forClient = e.getMessage().split("for type")[0].trim(); // No property 'ttt' found for type 'UserEntity'
                }
            }


            // ------------ response regard to the entity ------------- \\
            if (isUserService) {
                // unique violations { primary key, unique constraints }
                if (e instanceof DuplicateKeyException && forClient.isBlank()) {
                    recommendedStatus = HttpStatus.CONFLICT;
                    boolean inUsername = thisContains(e.getMessage(), USERNAME_UNIQUE_NAME);
                    boolean inEmail = thisContains(e.getMessage(), EMAIL_UNIQUE_NAME);
                    boolean isHandledUniqueViolation = inUsername || inEmail;

                    if (isHandledUniqueViolation) forClient = inUsername ? USERNAME_UNIQUE_MSG : EMAIL_UNIQUE_MSG;
                }

            } else { // ------- is Note Service ------- \\
                // now for note service I don't have custom exceptions
            }



            // unhandled exceptions -> generic error
            if (forClient.isBlank()) {
                if (recommendedStatus == null) recommendedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                forClient = Cons.Response.ForClient.GENERIC_ERROR;
                logError(e);
            }

        } catch (Exception ignored) { // if it doesn't reach to the handling for generics(the last) due to some unexpected exception
            if (recommendedStatus == null) recommendedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            if (forClient.isBlank()) forClient = Cons.Response.ForClient.GENERIC_ERROR;
            logError(e);
        }


        return isUserService ?
                new UserServiceTransversalException(forClient, recommendedStatus) :
                new NoteServiceTransversalException(forClient, recommendedStatus);
    }


    public boolean thisContains(String msg, String... strings) {
        boolean contains = true;
        for (String s : strings) contains = contains && msg.contains(s);

        return contains;
    }

    private void logError(Exception e) {
        log.error("Unhandled exception: {}", e.toString());
         e.printStackTrace();
    }

    /**
     * @param id to validate
     * @throws AbstractServiceExceptionWithStatus impl if id is null or less than 1
     */
    public void validateId(Long id) {
        if (id == null || id <= 0)
            throw new InvalidIdException();
    }
}