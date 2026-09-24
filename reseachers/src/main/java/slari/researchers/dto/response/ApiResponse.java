package slari.researchers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApiResponse<T> — Generic API response wrapper
 *
 * Every endpoint returns this wrapper so the frontend
 * always gets a consistent structure:
 *
 *   Success:
 *   {
 *     "success": true,
 *     "message": "Variety submitted for review",
 *     "data": { ...variety object... }
 *   }
 *
 *   Error:
 *   {
 *     "success": false,
 *     "message": "Email already registered",
 *     "data": null
 *   }
 *
 * The generic <T> means `data` can be any type:
 *   ApiResponse<VarietyResponse>
 *   ApiResponse<List<VarietyResponse>>
 *   ApiResponse<UserResponse>
 *   ApiResponse<Void>  (for actions with no return data)
 *
 * Static factory methods make creation clean:
 *   return ResponseEntity.ok(ApiResponse.success("Done", variety));
 *   return ResponseEntity.badRequest().body(ApiResponse.error("Not found"));
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;

    /** Shorthand for a successful response with data */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /** Shorthand for a successful response with no data (e.g. delete) */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /** Shorthand for an error response */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
