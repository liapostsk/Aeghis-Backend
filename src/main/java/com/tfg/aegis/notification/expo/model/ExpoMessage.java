// com/tfg/aegis/notification/expo/ExpoMessage.java
package com.tfg.aegis.notification.expo.model;

import java.util.Map;
import lombok.Data;

@Data
public class ExpoMessage {
    private String to;           // ExponentPushToken[...]
    private String sound = "default";
    private String title;
    private String body;
    private Map<String,Object> data;
    private String channelId;    // Android: debe existir en la app
}
