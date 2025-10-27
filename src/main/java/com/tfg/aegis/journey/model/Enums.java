package com.tfg.aegis.journey.model;

public class Enums {
    public enum JourneyState {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    public enum JourneyType {
        INDIVIDUAL,
        COMMON_DESTINATION,
        PERSONALIZED
    }
}
