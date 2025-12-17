package com.tfg.aegis.model.entity.expo;

import java.util.Map;
import lombok.Data;

@Data
public class ExpoReceipt {
    private String status;
    private String message;
    private Map<String,Object> details;
}
