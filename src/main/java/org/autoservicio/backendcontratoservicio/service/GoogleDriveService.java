package org.autoservicio.backendcontratoservicio.service;


import java.nio.file.Files;
import java.nio.file.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.autoservicio.backendcontratoservicio.model.DriveFileInfo;
import org.autoservicio.backendcontratoservicio.util.GoogleDriveOAuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


@Service
public class GoogleDriveService {

    private final Drive driveService;

    public GoogleDriveService(@Value("${google.drive.tokens.path}") String tokensPath) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = GoogleDriveOAuthUtil.getCredentials(HTTP_TRANSPORT, tokensPath);

        driveService = new Drive.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("SISTEMACONTRATO")
                .build();
    }

    public String uploadFile(java.io.File file, String folderId) throws IOException {
        // Paso 1: Buscar archivo existente con mismo nombre
        String query = String.format("name='%s' and '%s' in parents and trashed=false", file.getName(), folderId);

        FileList result = driveService.files().list()
                .setQ(query)
                .setFields("files(id, webViewLink)")
                .execute();

        // Detectar MIME real del archivo
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream"; // fallback genérico
        }

        if (!result.getFiles().isEmpty()) {
            // Ya existe, hacer UPDATE (reemplazo)
            File existingFile = result.getFiles().get(0);

            FileContent mediaContent = new FileContent(mimeType, file);
            File updatedFile = driveService.files()
                    .update(existingFile.getId(), null, mediaContent)
                    .setFields("id, webViewLink")
                    .execute();

            return updatedFile.getWebViewLink(); // mantiene misma URL
        }

        // No existe, hacer CREATE
        File fileMetadata = new File();
        fileMetadata.setName(file.getName());
        fileMetadata.setParents(List.of(folderId));

        FileContent mediaContent = new FileContent(mimeType, file);

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink")
                .execute();

        return uploadedFile.getWebViewLink();
    }


    public static class DriveFileInfo {
        public String id;
        public String webViewLink;

        public DriveFileInfo(String id, String webViewLink) {
            this.id = id;
            this.webViewLink = webViewLink;
        }
    }

}
