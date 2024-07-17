package org.cris6h16.apirestspringboot.Controllers.UserController;

import jakarta.validation.Valid;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller of the authenticated endpoints to work with {@link UserServiceImpl}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestController
@RequestMapping(Cons.User.Controller.Path.USER_PATH)
public class AuthenticatedUserController {
    UserServiceImpl userService;

    public AuthenticatedUserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PublicUserDTO> getById(@PathVariable(required = true) Long id) {
        PublicUserDTO u = userService.getById(id);
        return ResponseEntity.ok(u);
    }


    @PatchMapping(
            value = Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME + "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> patchUsernameById(@PathVariable(required = true) Long id,
                                                  @RequestBody(required = true) @Valid PatchUsernameUserDTO dto) {
        userService.patchUsernameById(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(
            value = Cons.User.Controller.Path.COMPLEMENT_PATCH_EMAIL + "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> patchEmailById(@PathVariable(required = true) Long id,
                                               @RequestBody(required = true) @Valid PatchEmailUserDTO dto) {
        userService.patchEmailById(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(
            value = Cons.User.Controller.Path.COMPLEMENT_PATCH_PASSWORD + "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> patchPasswordById(@PathVariable(required = true) Long id,
                                                  @RequestBody(required = true) @Valid PatchPasswordUserDTO dto) {
        userService.patchPasswordById(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable(required = true) Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
