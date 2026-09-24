package slari.researchers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * LoginRequest — DTO for POST /api/auth/login
 *
 * DTO = Data Transfer Object.
 * This is NOT an entity — it's never saved to the database.
 * It represents exactly what the frontend sends in the request body.
 *
 * @Data (Lombok) generates: getters, setters, toString,
 *       equals, hashCode, and a required-args constructor.
 *
 * Bean Validation annotations (@NotBlank, @Email) cause Spring
 * to reject the request with 400 Bad Request before it hits
 * the service layer, when the frontend sends invalid data.
 *
 * Frontend sends:
 *   POST /api/auth/login
 *   { "email": "amara@slari.gov.sl", "password": "demo1234" }
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
