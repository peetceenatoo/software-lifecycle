package com.polimi.PPP.CodeKataBattle.TaskScheduling;

import java.time.LocalDateTime;

public interface DeadlineHandler extends Runnable{
    void handleDeadline();
}
