package com.example.authorizationserver.service;

import com.auth0.exception.Auth0Exception;
import com.example.authorizationserver.dto.LoginRequestDTO;
import com.example.authorizationserver.dto.RegisterRequestDTO;

public interface Auth0Service {

    String login(String code) throws Auth0Exception;

    String register(RegisterRequestDTO registerRequestDTO) throws Auth0Exception;

    String forgotPassword(String email) throws Auth0Exception;
}

