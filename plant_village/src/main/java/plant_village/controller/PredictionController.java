package plant_village.controller;

import plant_village.model.Prediction;
import plant_village.model.User;
import plant_village.service.PredictionService;
import plant_village.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;
    private final UserService userService;

    @Autowired
    public PredictionController(PredictionService predictionService, UserService userService) {
        this.predictionService = predictionService;
        this.userService = userService;
    }

    /**
     * Yeni bir tahmin kaydı oluşturur (FastAPI'dan gelen POST isteği)
     * POST /api/predictions
     * @param prediction Tahmin verisi (ML çıktısı ve kullanıcı ID'si dahil)
     * @return Yeni tahmin nesnesi ve HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<Prediction> createPrediction(@RequestBody Prediction prediction) {
        // Not: Gerçekte, isteğin başlığından (header) gelen JWT ile kullanıcı kimlik doğrulaması yapılır.
        // Şimdilik sadece kullanıcı nesnesinin Prediction içinde geldiğini varsayıyoruz.
        
        Prediction newPrediction = predictionService.createPrediction(prediction);
        return new ResponseEntity<>(newPrediction, HttpStatus.CREATED);
    }

    /**
     * Bir kullanıcıya ait tüm tahmin geçmişini getirir.
     * GET /api/predictions/history/{userId}
     * @param userId Yolda belirtilen kullanıcı ID'si
     * @return Tahmin listesi ve HTTP 200 OK
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Prediction>> getPredictionHistory(@PathVariable Integer userId) {
        List<Prediction> history = predictionService.getPredictionHistory(userId);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    /**
     * Yöneticiler için tahmin düzeltme uç noktası.
     * PUT /api/predictions/{predictionId}
     * @param predictionId Düzeltilecek tahminin ID'si
     * @param updatedPrediction Güncel veri (isValid, confidence vb.)
     * @param adminId Düzeltmeyi yapan yöneticinin ID'si (Normalde JWT'den alınır)
     * @return Güncellenmiş tahmin nesnesi
     */
    @PutMapping("/{predictionId}")
    public ResponseEntity<?> updatePrediction(
            @PathVariable Integer predictionId,
            @RequestBody Prediction updatedPrediction,
            @RequestParam Integer adminId) {
        
        // İş Mantığı: Yönetici var mı?
        User adminUser = userService.findById(adminId)
                                    .orElseThrow(() -> new plant_village.exception.ResourceNotFoundException("Yönetici kullanıcı bulunamadı."));
        
        // İş Mantığı: Yönetici rolüne sahip mi? (Gerçek projede yapılır)
        // if (!adminUser.getRole().equals("ADMIN")) { ... }
        
        try {
            Prediction result = predictionService.updatePrediction(predictionId, updatedPrediction, adminUser);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
