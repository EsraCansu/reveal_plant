package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Integer status;
    private String message;
    private T data;
    private Boolean success;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .status(200)
            .message("Success")
            .data(data)
            .success(true)
            .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .status(200)
            .message(message)
            .data(data)
            .success(true)
            .build();
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
            .status(201)
            .message(message)
            .data(data)
            .success(true)
            .build();
    }
    
    public static <T> ApiResponse<T> error(Integer status, String message) {
        return ApiResponse.<T>builder()
            .status(status)
            .message(message)
            .success(false)
            .build();
    }
}
