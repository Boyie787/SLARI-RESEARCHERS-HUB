package slari.researchers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ForgotPasswordRequest — DTO for POST /api/auth/forgot-password
 *
 * User provides their registered email address.
 * The system looks it up and sends a reset token.
 * In production this token is emailed to the user.
 * In this implementation it is returned in the response
 * (for demo/testing — replace with email sending in production).
 */
@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;
}
