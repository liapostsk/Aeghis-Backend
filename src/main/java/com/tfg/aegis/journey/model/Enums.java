package com.tfg.aegis.journey.model;

public class Enums {
    public enum JourneyState {
        PENDING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    public enum JourneyType {
        INDIVIDUAL,
        COMMON_DESTINATION,
        PERSONALIZED
    }
}
