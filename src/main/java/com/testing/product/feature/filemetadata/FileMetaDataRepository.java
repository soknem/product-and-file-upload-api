package com.testing.product.feature.filemetadata;

import com.testing.product.domain.FileMetaData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FileMetaDataRepository extends MongoRepository<FileMetaData, Long> {

    Optional<FileMetaData> findByFileName(String fileName);

    boolean existsByFileName(String fileName);
}
