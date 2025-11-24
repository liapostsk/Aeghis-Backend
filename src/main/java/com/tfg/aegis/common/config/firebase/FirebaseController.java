package com.tfg.aegis.common.config.firebase;

import com.google.firebase.auth.FirebaseAuthException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/firebase")
public class FirebaseController {

    private final FirebaseTokenService firebaseTokenService;

    @PostMapping("/custom-token")
    public ResponseEntity<?> issueCustomToken() throws FirebaseAuthException {
        String clerkUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (clerkUserId == null || clerkUserId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            String customToken = firebaseTokenService.generarTokenPersonalizado(clerkUserId);
            return ResponseEntity.ok(Map.of("customToken", customToken));
        }
        catch (FirebaseAuthException e) {
            System.err.println("Error al crear el token personalizado de Firebase: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
