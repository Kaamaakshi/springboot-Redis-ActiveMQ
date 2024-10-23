package org.example.springboot_redis_active.service;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @JmsListener(destination = "users.queue")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }
}