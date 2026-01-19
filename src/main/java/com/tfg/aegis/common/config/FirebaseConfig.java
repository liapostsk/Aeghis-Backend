package com.tfg.aegis.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws Exception {
        String b64 = System.getenv("FIREBASE_ADMINSDK_B64");
        if (b64 == null || b64.isBlank()) {
            throw new IllegalStateException("Falta FIREBASE_ADMINSDK_B64 en Heroku Config Vars");
        }

        byte[] jsonBytes = Base64.getDecoder().decode(b64);

        try (ByteArrayInputStream in = new ByteArrayInputStream(jsonBytes)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
    }
}
