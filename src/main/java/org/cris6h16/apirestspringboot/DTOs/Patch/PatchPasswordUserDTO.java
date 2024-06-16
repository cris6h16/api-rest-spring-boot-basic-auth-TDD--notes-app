package org.cris6h16.apirestspringboot.DTOs.Patch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cris6h16.apirestspringboot.Constants.Cons;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatchPasswordUserDTO {

    @Size(message = Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG) //--> null is valid
    @NotBlank(message = Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
    private String password; // pass is passed encrypted, then always is > 8

}
