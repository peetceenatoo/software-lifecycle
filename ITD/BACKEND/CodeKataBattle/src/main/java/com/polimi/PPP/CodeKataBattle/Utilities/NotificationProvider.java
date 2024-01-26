package com.polimi.PPP.CodeKataBattle.Utilities;

import com.polimi.PPP.CodeKataBattle.DTOs.Message;

public interface NotificationProvider {
    void sendNotification(Message message, String destination);
}
