package slari.researchers.dto.response;

import slari.researchers.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresIn;  // Token expiry in seconds
    
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String institute;
    private String location;
    private String phone;

    public static AuthResponse fromUser(slari.researchers.entity.User user, String token, long expiresIn) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .institute(user.getInstitute())
                .location(user.getLocation())
                .phone(user.getPhone())
                .build();
    }
}