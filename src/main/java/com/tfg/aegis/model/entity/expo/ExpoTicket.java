package com.tfg.aegis.model.entity.expo;

import java.util.Map;
import lombok.Data;

@Data
public class ExpoTicket {
    private String status;
    private String id;
    private String message;
    private Map<String,Object> details;
}
