package com.tfg.aegis.invitation;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CryptoService {

    // Clave de 256 bits en Base64 desde application.properties
    @Value("${invitation.aes.key}")
    private String base64Key;
    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] raw = java.util.Base64.getDecoder().decode(base64Key);
        this.key = new SecretKeySpec(raw, "AES");
    }

    public byte[] encrypt(byte[] plaintext, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        c.init(Cipher.ENCRYPT_MODE, key, spec);
        return c.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        c.init(Cipher.DECRYPT_MODE, key, spec);
        return c.doFinal(ciphertext);
    }
}
