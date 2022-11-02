package com.example.herokudemo;

import javax.xml.ws.RespectBinding;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Tokenize {


    @GetMapping("/tokenizer")
    public String index() {
        return "Hello there! I'm running.";
    }
}
