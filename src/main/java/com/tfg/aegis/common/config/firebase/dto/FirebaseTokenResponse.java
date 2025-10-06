package com.tfg.aegis.common.config.firebase.dto;

public class FirebaseTokenResponse {
    private String firebaseCustomToken;

    public FirebaseTokenResponse(String firebaseCustomToken) {
        this.firebaseCustomToken = firebaseCustomToken;
    }

    public String getFirebaseCustomToken() {
        return firebaseCustomToken;
    }

    public void setFirebaseCustomToken(String firebaseCustomToken) {
        this.firebaseCustomToken = firebaseCustomToken;
    }
}
