package com.tfg.aegis.companionrequest.model;

public class Enums {
    public enum RequestStatus {
        CREATED, // Creator has published the request, no companion assigned yet
        PENDING, // Companion has requested to join, waiting for creator's approval
        MATCHED, // Creator has accepted a companion for the request
        IN_PROGRESS, // The journey with the companion is currently ongoing
        FINISHED, // The journey with the companion has been completed
        CANCELLED,  // Creator has cancelled the request
        EXPIRED    // The request has expired without being matched
    }
}
