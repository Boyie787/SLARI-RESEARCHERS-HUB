package slari.researchers.dto.response;

import slari.researchers.entity.User;
import slari.researchers.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String displayName;  // For UI display
    private String email;
    private Role role;
    private String roleDisplay;  // Human-readable role
    private String location;
    private String institute;
    private String phone;
    private boolean active;
    private String statusDisplay; // "Active" or "Inactive"
    private LocalDateTime createdAt;
    private String createdAtDisplay; // Formatted date

    public static UserResponse fromEntity(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .displayName(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .roleDisplay(u.getRole().name().charAt(0) + 
                             u.getRole().name().substring(1).toLowerCase()
                             .replace("_", " "))
                .location(u.getLocation())
                .institute(u.getInstitute())
                .phone(u.getPhone())
                .active(u.isActive())
                .statusDisplay(u.isActive() ? "Active" : "Inactive")
                .createdAt(u.getCreatedAt())
                .createdAtDisplay(u.getCreatedAt() != null ? 
                        u.getCreatedAt().toString() : null)
                .build();
    }
}