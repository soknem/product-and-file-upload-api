package com.testing.product.feature.media;

import com.testing.product.domain.FileMetaData;
import com.testing.product.feature.filemetadata.FileMetaDataRepository;
import com.testing.product.feature.media.dto.MediaResponse;
import com.testing.product.feature.media.dto.MediaViewResponse;
import com.testing.product.feature.minio.MinioStorageService;
import com.testing.product.feature.telegrambot.TelegramBotService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MinioStorageService minioService;

    private final FileMetaDataRepository fileMetaDataRepository;

    private final MinioClient minioClient;

    private final TelegramBotService telegramBotService;

    //base uri for media
    @Value("${media.base-uri}")
    private String baseUri;

    //endpoint that handle manage medias
    @Value("${media.end-point}")
    private String mediaEndpoint;

    @Value("${minio.bucket-name}")
    String bucketName;

    @Override
    public MediaResponse uploadPortalSingle(MultipartFile file) {

        String folderName = getValidFolder(file);

        String extension = minioService.extractExtension(Objects.requireNonNull(file.getOriginalFilename()));

        String newName;
        do {
            newName = UUID.randomUUID().toString();
        } while (fileMetaDataRepository.existsByFileName(newName + "." + extension));


        String objectName = folderName + "/" + newName + "." + extension;

        try {
            minioService.uploadFile(file, objectName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        //create new object that store file metadata
        FileMetaData fileMetaData = new FileMetaData();

        //set all field
        fileMetaData.setFileName(newName + "." + extension);

        fileMetaData.setFileSize(file.getSize());

        fileMetaData.setContentType(file.getContentType());

        fileMetaData.setFolder(folderName);

        fileMetaData.setExtension(extension);

        //save file metadata to database
        fileMetaDataRepository.save(fileMetaData);

        telegramBotService.sendFileUpload(fileMetaData);

        //response to DTO
        return MediaResponse.builder()
                .name(newName + "." + extension)
                .contentType(file.getContentType())
                .extension(extension)
                .size(file.getSize())
                .uri(baseUri + mediaEndpoint + "/view/" + newName + "." + extension)
                .build();
    }

    @Override
    public MediaResponse uploadSingle(MultipartFile file) {

        String folderName = getString(file);

        String extension = minioService.extractExtension(Objects.requireNonNull(file.getOriginalFilename()));

        String newName;
        do {
            newName = UUID.randomUUID().toString();
        } while (fileMetaDataRepository.existsByFileName(newName + "." + extension));

        String objectName = folderName + "/" + newName + "." + extension;

        try {
            minioService.uploadFile(file, objectName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        //create new object that store file metadata
        FileMetaData fileMetaData = new FileMetaData();

        //set all field
        fileMetaData.setFileName(newName + "." + extension);

        fileMetaData.setFileSize(file.getSize());

        fileMetaData.setContentType(file.getContentType());

        fileMetaData.setFolder(folderName);

        fileMetaData.setExtension(extension);

        //save file metadata to database
        fileMetaDataRepository.save(fileMetaData);

        telegramBotService.sendFileUpload(fileMetaData);


        //response to DTO
        return MediaResponse.builder()
                .name(newName + "." + extension)
                .contentType(file.getContentType())
                .extension(extension)
                .size(file.getSize())
                .uri(baseUri + mediaEndpoint + "/view/" + newName + "." + extension)
                .build();
    }

    @Override
    public List<MediaResponse> uploadMultiple(List<MultipartFile> files) {

        List<MediaResponse> mediaResponses = new ArrayList<>();
        files.forEach(file -> {
            MediaResponse mediaResponse;
            try {
                mediaResponse = this.uploadSingle(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            mediaResponses.add(mediaResponse);
        });

        return mediaResponses;
    }

    @Override
    public MediaResponse loadMediaByName(String mediaName) {

        try {
            String contentType = getContentType(mediaName);

            String extension = minioService.extractExtension(mediaName);

            return MediaResponse.builder()
                    .name(mediaName)
                    .contentType(contentType)
                    .extension(extension)
                    .uri(baseUri + mediaEndpoint + "/view/" + mediaName)
                    .build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public MediaResponse deleteMediaByName(String mediaName) {

        try {
            String contentType = getContentType(mediaName);

            String folderName = contentType.split("/")[0];

            String objectName = folderName + "/" + mediaName;

            minioService.deleteFile(objectName);

            return MediaResponse.builder()
                    .name(mediaName)
                    .extension(minioService.extractExtension(mediaName))
                    .build();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public Resource downloadMediaByName(String mediaName) {

        try {
            String contentType = getContentType(mediaName);

            String folderName = contentType.split("/")[0];

            String objectName = folderName + "/" + mediaName;

            InputStream inputStream = minioService.getFile(objectName);

            Path tempFile = Files.createTempFile("minio", mediaName);

            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            return new UrlResource(tempFile.toUri());

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media has not been found!");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public static String getContentType(String fileName) {
        Path path = Paths.get(fileName);
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            return "Unknown type";
        }
    }

    @Override
    public MediaViewResponse viewByFileName(String fileName) {

        // Fetch file metadata from the repository
        FileMetaData fileMetaData = fileMetaDataRepository.findByFileName(fileName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "File has not been found!"));

        // Construct the object path in MinIO
        Path path = Path.of(fileMetaData.getFileName());
        String objectPath = fileMetaData.getFolder() + "/" + path;

        // Fetch object metadata from MinIO
        StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .build();
        StatObjectResponse imageMinioMetaData;
        try {
            imageMinioMetaData = minioClient.statObject(statObjectArgs);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching file metadata from storage", e);
        }

        // Fetch the object from MinIO
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .build();

        InputStream inputStream;
        try {
            inputStream = minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching file from storage", e);
        }

        // Wrap the InputStream in an InputStreamResource
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        // Construct and return the response
        return MediaViewResponse.builder()
                .fileName(fileMetaData.getFileName())
                .fileSize(fileMetaData.getFileSize())
                .contentType(fileMetaData.getContentType())
                .stream(inputStreamResource)
                .build();
    }


    private static String getString(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !((contentType.startsWith("image/") ||
                contentType.startsWith("video/") ||
                contentType.equals("application/pdf") ||
                contentType.equals("application/vnd.ms-powerpoint") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("text/plain") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type.");
        }

        return contentType.split("/")[0];
    }

    private static String getValidFolder(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !((contentType.startsWith("image/") || contentType.equals("application/pdf")))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type.");
        }

        return contentType.split("/")[0];
    }

    @Override
    public String getUrl(String fileName) {

        return baseUri + mediaEndpoint + "/view/" + fileName;
    }

    @Override
    public String getDownloadUrl(String fileName) {

        return baseUri + mediaEndpoint + "/download/" + fileName;
    }


}
