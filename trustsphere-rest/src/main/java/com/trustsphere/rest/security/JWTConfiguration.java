package com.trustsphere.rest.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JWTConfiguration {

    private static final Logger LOGGER = Logger.getLogger(JWTConfiguration.class.getName());

    @Inject
    private ConfigurationProvider configProvider;

    /**
     * Get JWT secret key from configuration
     */
    public String getSecretKey() {
        return configProvider.getProperty("jwt.secret.key",
                System.getenv("JWT_SECRET_KEY"));
    }

    /**
     * Get JWT public key from keystore for RSA signature verification
     */
    public PublicKey getPublicKey() throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {

        String keystorePath = configProvider.getProperty("jwt.keystore.path",
                System.getenv("JWT_KEYSTORE_PATH"));
        String keystorePassword = configProvider.getProperty("jwt.keystore.password",
                System.getenv("JWT_KEYSTORE_PASSWORD"));
        String keyAlias = configProvider.getProperty("jwt.key.alias", "jwt-key");

        if (keystorePath == null || keystorePassword == null) {
            throw new IllegalStateException("Keystore configuration missing");
        }

        try (InputStream keystoreStream = new FileInputStream(keystorePath)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keystoreStream, keystorePassword.toCharArray());

            Certificate certificate = keyStore.getCertificate(keyAlias);
            if (certificate == null) {
                throw new KeyStoreException("Certificate not found for alias: " + keyAlias);
            }

            LOGGER.info("Successfully loaded JWT public key from keystore");
            return certificate.getPublicKey();
        }
    }

    /**
     * Get JWT token expiration time in seconds
     */
    public long getTokenExpirationSeconds() {
        String expiration = configProvider.getProperty("jwt.expiration.seconds", "3600");
        return Long.parseLong(expiration);
    }

    /**
     * Get JWT issuer
     */
    public String getIssuer() {
        return configProvider.getProperty("jwt.issuer", "trustsphere");
    }

    /**
     * Check if JWT is configured for RSA signatures
     */
    public boolean isRSASignature() {
        return configProvider.getProperty("jwt.signature.algorithm", "HS256").startsWith("RS");
    }
}