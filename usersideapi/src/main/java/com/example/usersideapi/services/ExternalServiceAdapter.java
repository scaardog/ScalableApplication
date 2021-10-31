package com.example.usersideapi.services;

import org.springframework.stereotype.Service;

@Service
public class ExternalServiceAdapter {

    public String doSomething(String message) {
        StringBuilder sb = new StringBuilder(message);
        sb.reverse();
        return sb.toString();
    }
}
