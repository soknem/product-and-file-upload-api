package com.testing.product.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaUtil {

    private static String mediaEndpoint;
    private static String baseUri;

    @Value("${media.end-point}")
    public void setMediaEndpoint(String mediaEndpoint) {
        MediaUtil.mediaEndpoint = mediaEndpoint;
    }

    @Value("${media.base-uri}")
    public void setBaseUri(String baseUri) {
        MediaUtil.baseUri = baseUri;
    }

    public static String getUrl(String fileName) {

        return baseUri + mediaEndpoint + "/view/" + fileName;
    }

    public static String getDownloadUrl(String fileName) {

        return baseUri + mediaEndpoint + "/download/" + fileName;
    }
}
