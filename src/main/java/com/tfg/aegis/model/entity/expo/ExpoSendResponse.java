package com.tfg.aegis.model.entity.expo;

import java.util.List;
import lombok.Data;

@Data
public class ExpoSendResponse {
    private List<ExpoTicket> data;
    private Object errors;
}
