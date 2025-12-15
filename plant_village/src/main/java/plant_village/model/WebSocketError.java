package plant_village.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketError {
    private String errorCode;
    private String message;
    private String details;
    private LocalDateTime timestamp;
}
