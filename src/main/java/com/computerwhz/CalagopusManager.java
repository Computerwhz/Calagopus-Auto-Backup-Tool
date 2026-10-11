package com.computerwhz;

import com.computerwhz.calagopus.ApiClient;
import com.computerwhz.calagopus.auth.ApiKeyAuth;

public class CalagopusManager {

    private final ApiClient client;

    public CalagopusManager(String panelUrl, String apiKey) {
        client = new ApiClient();

        client.setBasePath(panelUrl.replaceAll("/+$", ""));

        ApiKeyAuth auth = (ApiKeyAuth)
                client.getAuthentication("api_key");

        auth.setApiKey(apiKey);
        auth.setApiKeyPrefix("Bearer");
    }

    public ApiClient getClient() {
        return client;
    }
}
