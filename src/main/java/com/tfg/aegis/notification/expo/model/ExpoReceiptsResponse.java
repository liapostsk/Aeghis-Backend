package com.tfg.aegis.notification.expo.model;

import java.util.Map;
import lombok.Data;

@Data
public class ExpoReceiptsResponse {
    private Map<String, ExpoReceipt> data;
    private Object errors;
}