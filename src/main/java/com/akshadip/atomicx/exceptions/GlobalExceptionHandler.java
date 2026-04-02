package com.akshadip.atomicx.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Object> handeInsufficientFunds(InsufficientFundsException ex){
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message",ex.getMessage());
        body.put("code","ERR_INSUFFICIENT_FUNDS");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserDoesnotExist.class)
    public ResponseEntity<Object> handeleUserDoesNotExist(UserDoesnotExist ex){
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp",Instant.now());
        body.put("message",ex.getMessage());
        body.put("code","USER_DOESNOT_EXIST");
        return new ResponseEntity<>(body,HttpStatus.NOT_FOUND);
    }
}
