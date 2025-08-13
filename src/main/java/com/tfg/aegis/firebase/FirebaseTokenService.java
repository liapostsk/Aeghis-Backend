package com.tfg.aegis.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.stereotype.Service;

@Service
public class FirebaseTokenService {

    public String generarTokenPersonalizado(String uidClerk) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().createCustomToken(uidClerk);
    }
}
