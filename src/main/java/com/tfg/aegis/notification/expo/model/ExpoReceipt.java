package com.tfg.aegis.notification.expo.model;

import java.util.Map;
import lombok.Data;

@Data
public class ExpoReceipt {
    private String status;  // "ok" | "error"
    private String message;
    private Map<String,Object> details; // { "error": "DeviceNotRegistered" } etc.
}
