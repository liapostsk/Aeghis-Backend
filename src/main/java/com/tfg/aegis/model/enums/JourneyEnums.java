package com.tfg.aegis.model.enums;

public class JourneyEnums {
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
