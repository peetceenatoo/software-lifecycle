package com.polimi.PPP.CodeKataBattle.Utilities;

import com.polimi.PPP.CodeKataBattle.DTOs.MessageDTO;

public interface NotificationProvider {
    void sendNotification(MessageDTO messageDTO, String destination);
}
