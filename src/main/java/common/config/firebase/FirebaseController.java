package common.config.firebase;
;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/firebase")
public class FirebaseController {

    private final FirebaseTokenService firebaseTokenService;

    public FirebaseController(FirebaseTokenService firebaseTokenService) {
        this.firebaseTokenService = firebaseTokenService;
    }

    @PostMapping("/custom-token")
    public ResponseEntity<String> obtenerTokenFirebase(@RequestBody TokenRequest request) {
        try {
            String token = firebaseTokenService.generarTokenPersonalizado(request.getUidClerk());
            return ResponseEntity.ok(token);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(500).body("Error generando token: " + e.getMessage());
        }
    }

    public static class TokenRequest {
        private String uidClerk;

        public String getUidClerk() {
            return uidClerk;
        }

        public void setUidClerk(String uidClerk) {
            this.uidClerk = uidClerk;
        }
    }
}
