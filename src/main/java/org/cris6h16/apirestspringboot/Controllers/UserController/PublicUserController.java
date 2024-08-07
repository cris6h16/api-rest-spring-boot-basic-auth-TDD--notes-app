package org.cris6h16.apirestspringboot.Controllers.UserController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ErrorResponse;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Services.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Controller of the public endpoints to work with {@link UserServiceImpl}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestController
@RequestMapping(PublicUserController.path)
public class PublicUserController {

    public static final String path = Cons.User.Controller.Path.USER_PATH;


    UserServiceImpl userService;

    public PublicUserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Operation(
            tags = {"Public User Endpoints"},
            operationId = "create",
            summary = "create a new user",
            description = "Create a new user with the data provided in the request body. The user will have the role USER by default",
            method = "POST",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User created successfully",
                            headers = {
                                    @Header(
                                            schema = @Schema(type = "string"),
                                            name = "Location",
                                            description = "URI of the created user",
                                            example = Cons.User.Controller.Path.USER_PATH + "/1"
                                    )
                            },
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "username or email already exists",
                            content = {
                                    @Content(
                                            schema = @Schema(implementation = ErrorResponse.class),
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            examples = {
                                                    @ExampleObject(
                                                            name = "Email already exists",
                                                            value = """
                                                                    {
                                                                        "message": "Email already exists",
                                                                        "status": "409 CONFLICT",
                                                                        "instant": "2024-07-18T01:30:59.089795723Z"
                                                                    }
                                                                    """,
                                                            description = "I tried to create a user with an email that already exists",
                                                            summary = "Email already exists"
                                                    ),
                                                    @ExampleObject(
                                                            name = "Username already exists",
                                                            value = """
                                                                    {
                                                                        "message": "Username already exists",
                                                                        "status": "409 CONFLICT",
                                                                        "instant": "2024-07-18T01:32:29.379178830Z"
                                                                    }
                                                                    """,
                                                            description = "I tried to create a user with a username that already exists",
                                                            summary = "Username already exists"
                                                    )
                                            }
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation errors; invalid email, username blank, password too short, etc.",
                            content = {
                                    @Content(
                                            schema = @Schema(implementation = ErrorResponse.class),
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            examples = {
                                                    @ExampleObject(
                                                            name = "Email is invalid",
                                                            value = """
                                                                    {
                                                                        "message": "Email is invalid",
                                                                        "status": "400 BAD_REQUEST",
                                                                        "instant": "2024-07-18T01:42:17.299493658Z"
                                                                    }
                                                                    """,
                                                            description = "I tried to create a user with an invalid email",
                                                            summary = "Invalid email"
                                                    ),
                                                    @ExampleObject(
                                                            name = "Username length fail",
                                                            value = """
                                                                    {
                                                                        "message": "Username must be between 4 and 20 characters",
                                                                        "status": "400 BAD_REQUEST",
                                                                        "instant": "2024-07-18T01:43:06.792938867Z"
                                                                    }
                                                                    """,
                                                            description = "I tried to create a user with a username.length < 4 Or username.length > 20",
                                                            summary = "Username length fail"
                                                    ),
                                                    @ExampleObject(
                                                            name = "Password length fail",
                                                            value = """
                                                                    {
                                                                        "message": "Password must be between 8 and 50 characters",
                                                                        "status": "400 BAD_REQUEST",
                                                                        "instant": "2024-07-18T01:45:09.883892238Z"
                                                                    }
                                                                    """,
                                                            description = "I tried to create a user with a password.length < 8 Or password.length > 50",
                                                            summary = "Password length fail"
                                                    )
                                            }
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( request body wasn't passed, was bad formatted, database error, etc. )",
                            content = @Content
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateUserDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@RequestBody(required = true) CreateUserDTO user) {
        Long id = userService.create(user, ERole.ROLE_USER);
        URI uri = URI.create(path + "/" + id);
        return ResponseEntity.created(uri).build();
    }
}
