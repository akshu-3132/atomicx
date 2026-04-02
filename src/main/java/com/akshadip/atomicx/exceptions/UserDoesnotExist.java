package com.akshadip.atomicx.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserDoesnotExist extends RuntimeException{
    public UserDoesnotExist(String message){
        super(message);
    }
}
