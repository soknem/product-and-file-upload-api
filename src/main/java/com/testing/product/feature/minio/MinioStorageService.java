package com.testing.product.feature.minio;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Business logic interface which contains to manage minio
 *
 * @author Pov soknem
 * @since 1.0 (2024)
 */
public interface MinioStorageService {

    /**
     * Uploads a file to the Minio storage.
     *
     * @param file       the file to upload
     * @param objectName the name of the object in the storage
     * @throws Exception if an error occurs during the upload
     */
    void uploadFile(MultipartFile file, String objectName) throws Exception;

    /**
     * Retrieves a file from the Minio storage as an InputStream.
     *
     * @param objectName the name of the object in the storage
     * @return {@link InputStream}
     * @throws Exception if an error occurs during the retrieval
     * @author Pov soknem
     * @since 1.0 (2024)
     */
    InputStream getFile(String objectName) throws Exception;

    /**
     * Deletes a file from the Minio storage.
     *
     * @param objectName the name of the object in the storage
     * @throws Exception if an error occurs during the deletion
     * @author Pov soknem
     * @since 1.0 (2024)
     */
    void deleteFile(String objectName) throws Exception;

    /**
     *
     * @param mediaName is the name of object
     * @return {@link String}
     */

    String extractExtension(String mediaName);
}
