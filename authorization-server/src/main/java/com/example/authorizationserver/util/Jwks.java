package com.example.authorizationserver.util;

import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/*
* for generating JSON Web Keys (JWKs) that can be used in cryptographic operations, particularly for
* securing web applications. JWKs are a JSON representation of cryptographic keys used in web-based
* security protocols like OAuth 2.0 and OpenID Connect.
* */
public final class Jwks {
    private Jwks() {
    }

    // generates an RSA key pair
    // public key, private key, and a randomly generated key ID (UUID) are set in the builder
    public static RSAKey generateRsa() {
        KeyPair keyPair = KeyGenerationUtils.generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    //  generates a secret key
    public static OctetSequenceKey generateSecret() {
        SecretKey secretKey = KeyGenerationUtils.generateSecretKey();
        return new OctetSequenceKey.Builder(secretKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }
}
