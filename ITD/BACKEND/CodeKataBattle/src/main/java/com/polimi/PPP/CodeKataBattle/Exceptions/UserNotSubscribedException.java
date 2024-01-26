package com.polimi.PPP.CodeKataBattle.Exceptions;

public class UserNotSubscribedException extends RuntimeException {
    public UserNotSubscribedException(String message) {
        super(message);
    }
}