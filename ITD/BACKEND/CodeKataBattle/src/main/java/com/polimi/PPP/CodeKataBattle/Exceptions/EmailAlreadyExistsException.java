package com.polimi.PPP.CodeKataBattle.Exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
