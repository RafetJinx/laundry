package com.laundry.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserRequestDto {

    @JsonView(Views.Create.class)
    @NotNull(
            groups = ValidationGroups.Create.class,
            message = "Username is required (Create)"
    )
    @NotBlank(
            groups = ValidationGroups.Create.class,
            message = "Username is required (Create)"
    )
    @Size(
            max = 255,
            message = "Username must be less than 255 characters"
    )
    String username;

    // 2) DISPLAY NAME (opsiyonel alan)
    // Tüm senaryolarda (Create, Update, Patch) gönderilebilir;
    // ancak zorunluluk eklemiyoruz, o yüzden "NotNull" yok.
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 255,
            message = "DisplayName must be less than 255 characters"
    )
    String displayName;

    // 3) PASSWORD
    // Create ve Update sırasında zorunlu, Patch için opsiyonel (örnek).
    // "Patch" grubunu da ekleyebilir ya da eklemeyebilirsiniz. Tercih size kalmış.
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Password is required"
    )
    @NotBlank(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Password cannot be blank"
    )
    @Size(
            max = 255,
            message = "Password must be less than 255 characters"
    )
    @Size(
            min = 8,
            message = "Password must be at least 8 characters"
    )
    String password;

    // 4) EMAIL
    // Create ve Update’te zorunlu. Patch’te opsiyonel.
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @NotNull(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Email is required"
    )
    @NotBlank(
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
            message = "Email cannot be blank"
    )
    @Size(
            max = 255,
            message = "Email must be less than 255 characters"
    )
    @Email(
            message = "Email should be valid",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}
    )
    String email;

    // 5) PHONE (opsiyonel)
    // İsterseniz Create, Update için zorunluluk ekleyebilirsiniz.
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 20,
            message = "Phone must be less than 20 characters"
    )
    String phone;

    // 6) ADDRESS (opsiyonel)
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 255,
            message = "Address must be less than 255 characters"
    )
    String address;

    // 7) ROLE
    // Admin, Customer vb. (Bu alanı sadece adminler değiştirebilsin diyorsanız
    // Service/Controller katmanında kontrol edebilirsiniz.)
    @JsonView({Views.Create.class, Views.Update.class, Views.Patch.class})
    @Size(
            max = 50,
            message = "Role must be less than 50 characters"
    )
    String role;
}
