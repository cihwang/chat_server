package com.example.chatserver.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<?> k8sHealthCheck(){
        return ResponseEntity.ok().build();
    }
}
