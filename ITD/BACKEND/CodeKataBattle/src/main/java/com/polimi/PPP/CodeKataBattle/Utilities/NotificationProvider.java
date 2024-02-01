package com.polimi.PPP.CodeKataBattle.Utilities;

import com.polimi.PPP.CodeKataBattle.DTOs.MessageDTO;

import java.util.List;

public interface NotificationProvider {
    void sendNotification(MessageDTO messageDTO, String destination);
    void sendNotification(MessageDTO messageDTO, List<String> destinations);
}
