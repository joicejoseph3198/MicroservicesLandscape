package com.example.authorizationserver.controller;

import com.auth0.exception.Auth0Exception;
import com.example.authorizationserver.dto.LoginRequestDTO;
import com.example.authorizationserver.dto.RegisterRequestDTO;
import com.example.authorizationserver.service.Auth0Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class Auth0Controller {
    private final Auth0Service auth0Service;

    @Autowired
    public Auth0Controller(Auth0Service auth0Service) {
        this.auth0Service = auth0Service;
    }

    @PostMapping(value = "/public/register")
    public String signUp(@RequestBody RegisterRequestDTO registerRequestDTO){
        try{
            return auth0Service.register(registerRequestDTO);
        } catch (Auth0Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/public/login")
    public String signIn(@RequestParam("code") String code ){
        try{
            return auth0Service.login(code);
        } catch (Auth0Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/public/forgot-password")
    public String forgotPassword(@RequestBody String email){
        try{
            return auth0Service.forgotPassword(email);
        } catch (Auth0Exception e) {
            throw new RuntimeException(e);
        }
    }
}
