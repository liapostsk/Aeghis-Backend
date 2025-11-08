package com.tfg.aegis.notification.expo.model;

import java.util.Map;
import lombok.Data;

@Data
public class ExpoTicket {
    private String status;   // "ok" | "error"
    private String id;       // ticket id si ok
    private String message;  // mensaje si error
    private Map<String,Object> details; // p.ej. { "error": "DeviceNotRegistered" }
}
