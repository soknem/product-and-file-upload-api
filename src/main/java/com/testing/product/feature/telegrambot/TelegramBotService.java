package com.testing.product.feature.telegrambot;


import com.testing.product.domain.FileMetaData;
import com.testing.product.domain.Product;

public interface TelegramBotService {
    void sendProductResponse(Product product);

    void sendFileUpload(FileMetaData file);
}
