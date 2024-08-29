package com.testing.product.feature.telegrambot;

import com.testing.product.domain.FileMetaData;
import com.testing.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TelegramBotServiceImpl implements TelegramBotService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.id}")
    private String chatId;

    private final RestTemplate restTemplate;

    @Override
    public void sendProductResponse(Product product) {


        //get field that need to send to bot and format as string
        String sendMessage = String.format("""
                        New Product Created..!
                        _______________________________
                                                
                        Id          : %s
                        Name        : %s
                        Price       : %s
                        Image       : %s
                        Description : %s
                        _______________________________
                        """,
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImage(),
                product.getDescription());

        sendMessage(sendMessage);

    }

    @Override
    public void sendFileUpload(FileMetaData file) {


        //get field that need to send to bot and format as string
        String sendMessage = String.format("""
                        New File Upload..!
                        *********************************
                                                
                        Name     : %s
                        Content Type: %s
                        Folder      : %s
                        *********************************
                        """,
                file.getFileName(),
                file.getContentType(),
                file.getFolder());

        sendMessage(sendMessage);
    }

    private void sendMessage(String sendMessage) {

        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String requestJson = String.format("{\"chat_id\":\"%s\",\"text\":\"%s\"}", chatId, sendMessage);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send message to Telegram");
        }
    }
}

