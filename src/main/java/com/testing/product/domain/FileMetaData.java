package com.testing.product.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "file_meta_data")
public class FileMetaData {
    @Id
    String id;

    String fileName;

    String contentType;

    String folder;

    Long fileSize;

    String extension;

}
