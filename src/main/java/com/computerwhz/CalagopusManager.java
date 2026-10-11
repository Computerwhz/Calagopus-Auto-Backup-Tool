package com.computerwhz;

import com.computerwhz.calagopus.ApiClient;
import com.computerwhz.calagopus.auth.ApiKeyAuth;

public class CalagopusManager {

    private final ApiClient client;

    public CalagopusManager(String panelUrl, String apiKey) {
        client = new ApiClient() {
            @Override
            public okhttp3.RequestBody serialize(Object object, String contentType)
                    throws com.computerwhz.calagopus.ApiException {
                if ("application/json".equals(contentType)) {
                    // The String overload adds charset=utf-8. Calagopus versions
                    // with strict content-type matching require application/json.
                    byte[] json = com.computerwhz.calagopus.JSON.serialize(object)
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    return okhttp3.RequestBody.create(json, okhttp3.MediaType.parse(contentType));
                }
                return super.serialize(object, contentType);
            }
        };

        client.setBasePath(panelUrl.replaceAll("/+$", ""));

        ApiKeyAuth auth = (ApiKeyAuth)
                client.getAuthentication("api_key");

        auth.setApiKey(apiKey);
        auth.setApiKeyPrefix("Bearer");
        client.addDefaultHeader("Authorization", "Bearer " + apiKey);
    }

    public ApiClient getClient() {
        return client;
    }
}

