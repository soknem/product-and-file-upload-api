package com.testing.product.feature.media;

import com.testing.product.feature.media.dto.MediaResponse;
import com.testing.product.feature.media.dto.MediaViewResponse;
import io.minio.errors.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


/**
 * Business logic interface which contains to manage media
 *
 * @author Pov soknem
 * @since 1.0 (2024)
 */
public interface MediaService {

    MediaResponse uploadPortalSingle(MultipartFile file);

    /**
     * Uploads a single media file.
     *
     * @param file the media file to upload
     *             file type can be video, image, document
     * @return {@link MediaResponse}
     * @throws Exception if an error occurs during the upload
     * @author Pov soknem
     * @since 1.0 (2024)
     */
    MediaResponse uploadSingle(MultipartFile file) throws Exception;

    /**
     * Uploads multiple media files.
     *
     * @param files the list of media files to upload
     * @return {@link List<MediaResponse>}
     * @author Pov soknem
     * @since 1.0 (2024)
     */
    List<MediaResponse> uploadMultiple(List<MultipartFile> files);

    /**
     * Loads media details by its name.
     *
     * @param mediaName the name with extension of the media
     * @return {@link  MediaResponse}
     * @author Pov soknem
     * @since 1.0 (2024)
     */
    MediaResponse loadMediaByName(String mediaName);

    /**
     * Deletes media by its name.
     *
     * @param mediaName the name with extension of the media to delete
     * @return {@link MediaResponse}
     * @author Pov soknem
     * @since 1.0 (2024)
     */
    MediaResponse deleteMediaByName(String mediaName);

    /**
     * Downloads media by its name.
     *
     * @param mediaName the name with extension of the media to download
     * @return {@link Resource}
     * @author Pov soknem
     * @since 1.0 (2024)
     */
    Resource downloadMediaByName(String mediaName);


    /**
     * Checks if an object with the specified name exists in the Minio bucket.
     *
     * @param fileName the name of the file to check for existence
     * @return {@link MediaViewResponse}
     * @throws InsufficientDataException       if not enough data is available
     * @throws ErrorResponseException          if an error response is received from the server
     * @throws IOException                     if an I/O error occurs
     * @throws NoSuchAlgorithmException        if the specified algorithm is not available
     * @throws InvalidKeyException             if the key is invalid
     * @throws InvalidResponseException        if the response from the server is invalid
     * @throws XmlParserException              if an error occurs while parsing XML
     * @throws InternalException               if an internal error occurs
     * @throws io.minio.errors.ServerException if a server-side error occurs
     * @author Pov soknem
     * @since 1.0 (2024)
     */

    MediaViewResponse viewByFileName(String fileName) throws InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, io.minio.errors.ServerException;

    /**
     * get preview url by file name
     *
     * @param fileName the file name to generate url to preview
     * @return {@link String}
     * @author Pov soknem
     * @since 1.0 (2024)
     */
    String getUrl(String fileName);

    /**
     * get download url by file name
     *
     * @param fileName the file name to generate url to preview
     * @return {@link String}
     * @author Pov soknem
     * @since 1.0 (2024)
     */

    String getDownloadUrl(String fileName);
}
