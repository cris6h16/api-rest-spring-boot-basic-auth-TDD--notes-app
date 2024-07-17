package org.cris6h16.apirestspringboot.Controllers.UserController;

import jakarta.validation.Valid;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Services.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@RequestBody(required = true) @Valid CreateUserDTO user) {
        Long id = userService.create(user, ERole.ROLE_USER);
        URI uri = URI.create(path + "/" + id);
        return ResponseEntity.created(uri).build();
    }
}
