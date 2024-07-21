package com.example.authorizationserver.service.impl;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
import com.auth0.net.SignUpRequest;
import com.example.authorizationserver.config.Auth0Config;
import com.example.authorizationserver.dto.RegisterRequestDTO;
import com.example.authorizationserver.service.Auth0Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/*
* Will be of no use since auth0 acts as authorization server.
* Might turn this into admin service in future
*/
@Service
public class Auth0ServiceImpl implements Auth0Service {

    //used for authentication and authorization.
    private final AuthAPI authAPI;
    private final Auth0Config auth0Config;

    @Autowired
    public Auth0ServiceImpl(Auth0Config auth0Config) {
        this.authAPI = new AuthAPI(auth0Config.getDomain(), auth0Config.getClientId(), auth0Config.getClientSecret());
        this.auth0Config = auth0Config;
    }

    @Override
    public String login(String authorizationCode) throws Auth0Exception {
        AuthRequest request = authAPI.exchangeCode(authorizationCode,auth0Config.getRedirectUri());
        TokenHolder holder = request.execute();
        return holder.getAccessToken();
    }

    @Override
    public String register(RegisterRequestDTO registerRequestDTO) throws Auth0Exception {
        Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("name", registerRequestDTO.name());

        SignUpRequest request = authAPI.signUp(registerRequestDTO.email(), registerRequestDTO.password().toCharArray(),
                "Username-Password-Authentication").setCustomFields(userMetadata);
        request.execute();
        return "Request processed successfully";
    }

    @Override
    public String forgotPassword(String email) throws Auth0Exception {
        authAPI.resetPassword(email, "Username-Password-Authentication").execute();
        return "Email has been sent to the registered email";
    }
}

