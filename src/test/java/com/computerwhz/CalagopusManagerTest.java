package com.computerwhz;

import com.computerwhz.calagopus.api.ApiClientApi;
import com.computerwhz.calagopus.model.PostApiClientServersServerBackupsRequest;
import org.junit.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.assertEquals;

public class CalagopusManagerTest {
    @Test public void everyBackupOperationSendsAuthorization() throws Exception {
        var client = new CalagopusManager("https://panel.example.com/", "test-key").getClient();
        var api = new ApiClientApi(client);
        var id = UUID.randomUUID();
        var requests = List.of(
                api.getApiClientServersCall(1L, 100L, false, null, null),
                api.getApiClientServersServerCall(id, null),
                api.getApiClientServersServerBackupsCall(id, 1L, 100L, true, null, null, null, null, null),
                api.postApiClientServersServerBackupsCall(id,
                        new PostApiClientServersServerBackupsRequest().name("test").ignoredFiles(List.of()), null),
                api.deleteApiClientServersServerBackupsBackupCall(id, UUID.randomUUID(), null));
        for (var call : requests) {
            assertEquals(List.of("Bearer test-key"), call.request().headers("Authorization"));
            assertEquals("panel.example.com", call.request().url().host());
        }
    }
}
