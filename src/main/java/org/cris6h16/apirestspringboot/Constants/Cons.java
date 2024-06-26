package org.cris6h16.apirestspringboot.Constants;

import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;

/**
 * This class contains all the constants used in the project
 * used to avoid hardcoding strings and centralize it.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class Cons {
    public class FrontEnd {
        public static final String BASE_URL = "http://localhost:3000";
    }


    public class User {
        public class Constrains {
            public static final String EMAIL_UNIQUE_NAME = "email_unique";
            public static final String EMAIL_UNIQUE_MSG = "Email already exists";
            public static final String USERNAME_UNIQUE_MSG = "Username already exists";
            public static final String USERNAME_UNIQUE_NAME = "username_unique";
        }

        public class Validations {
            public static final String EMAIL_IS_BLANK_MSG = "Email is required";
            public static final String EMAIL_INVALID_MSG = "Email is invalid";
            public static final String USERNAME_MAX_LENGTH_MSG = "Username must be less than 20 characters";
            public static final String USERNAME_IS_BLANK_MSG = "Username mustn't be blank";
            public static final byte MAX_USERNAME_LENGTH = 20;
            public static final String PASS_IS_BLANK_MSG = "Password mustn't be blank";

            public class InService {
                public static final String PASS_IS_TOO_SHORT_MSG = "Password must be at least 8 characters";
            }
        }

        public static class Fails {
            public static final String NOT_FOUND = "User not found";
        }

        /**
         * {@link CreateUpdateUserDTO}
         */
        public static class DTO {
            public static final String NULL = "User to update/create cannot be null";
        }
    }

    public class Note {
        public class Validations {
            public static final String TITLE_MAX_LENGTH_MSG = "Title must be less than 255 characters";
            public static final String TITLE_IS_BLANK_MSG = "Title is required";
            public static final short MAX_TITLE_LENGTH = 255;
        }

        public class Fails {
            public static final String NOT_FOUND = "Note not found";
        }

        /**
         * {@link CreateNoteDTO}
         */
        public static class DTO {
            public static final String NULL = "Note to update/create cannot be null";
        }

    }

    public class Role {
        public class Validations {
            public static final String NAME_IS_BLANK = "Name mustn't be blank";
        }
    }

    public class CommonInEntity {
        public static final String ID_INVALID = "Invalid id";
    }

    public class Auth {
        public class Fails {
            public static final String ACCESS_DENIED = "Access denied";
            public static final String UNAUTHORIZED = "Unauthorized";
        }
    }

    public class Response {
        public static class ForClient {
            /**
             * Generic error message for the client, used when we don't want to provide a specific message about the failure
             *
             * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
             * @since 1.0
             */
            public static final String GENERIC_ERROR = "An error occurred, please try again later or contact the us for support";
            public static final String NO_RESOURCE_FOUND = "No resource found";
            public static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported media type";

        }
    }


    public static class TESTING {
        /**
         * Constant for testing purposes
         * <p>
         * the {@link TESTING#UNHANDLED_ERROR_WITH_TESTING_PURPOSES} is the pattern
         * used for identify if an unhandled exceptions was thrown for testing purposes
         *
         * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
         * @since 1.0
         */
        public static final String UNHANDLED_ERROR_WITH_TESTING_PURPOSES = "cris6h16's";
    }

    public static class Logs {
        /**
         * Used to store the logs when an unhandled exception is thrown, used in
         * {@link ServiceUtils#logUnhandledException(Exception)}
         *
         * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
         * @since 1.0
         */
        public static final String UNHANDLED_EXCEPTIONS_FILE = "logs/exceptions/unhandled.log";
        public static final String SUCCESS_AUTHENTICATION_FILE = "logs/auth/success.log";
        public static final String FAIL_AUTHENTICATION_FILE = "logs/auth/failures.log";
    }
}