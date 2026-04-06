package org.autoservicio.backendcontratoservicio.util;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
public class GoogleDriveOAuthUtil {
    private static final String APPLICATION_NAME = "SISTEMACONTRATO";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT,
                                            String tokensDirectoryPath) throws Exception {
        InputStream in = GoogleDriveOAuthUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) throw new FileNotFoundException("No se encontró client_secret.json en resources");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    private static final String TOKENS_DIRECTORY_PATH_ALL = "tokens-all";
    private static final String CREDENTIALS_FILE_PATH_ALL = "/credenciales.json";

    public static Credential getCredentialsAll(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        // 1. Cargar credenciales
        InputStream in = GoogleDriveOAuthUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH_ALL);
        if (in == null) throw new FileNotFoundException("Credenciales no encontradas");

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // 2. Configurar flujo OAuth
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH_ALL)))
                .setAccessType("offline")
                .build();

        // 3. Usar puerto explícito que coincida con redirect_uris
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(49180)  // Usa el mismo puerto que en tu redirect_uri
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }



    private static GoogleAuthorizationCodeFlow buildFlow(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        InputStream in = GoogleDriveOAuthUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) throw new RuntimeException("No se encontró client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        String tokensPath = System.getProperty("google.drive.tokens.path", "/opt/contrato_servicio/tokens");
        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensPath)))
                .setAccessType("offline")
                .build();
    }

    public static Credential exchangeCodeForCredential(String code) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        GoogleAuthorizationCodeFlow flow = buildFlow(HTTP_TRANSPORT);

        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri("https://agoisp.pro/contrato_servicio/Callback")
                .execute();

        // Convierte la respuesta en Credential
        return flow.createAndStoreCredential(tokenResponse, "user");
    }


    public static void saveCredential(Credential credential, String tokensDirectoryPath) throws Exception {
        File tokenFile = new File(tokensDirectoryPath, "StoredCredential.json");
        try (FileOutputStream fos = new FileOutputStream(tokenFile)) {
            JSON_FACTORY.createJsonGenerator(fos, null).serialize(credential);
        }
    }
}