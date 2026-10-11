package com.computerwhz;

import com.computerwhz.calagopus.ApiException;
import com.computerwhz.calagopus.api.ApiClientApi;
import com.computerwhz.calagopus.model.*;
import org.junit.Test;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.Assert.*;

public class BackupManagerTest {
    static class FakeApi extends ApiClientApi {
        final UUID id = UUID.randomUUID();
        final List<ApiServerBackup> backups = new ArrayList<>();
        final List<UUID> deleted = new ArrayList<>();
        final List<Long> pages = new ArrayList<>();
        boolean suspended;
        int limit = 2;
        int created;
        boolean fail;

        @Override public GetApiClientServers200Response getApiClientServers(Long page, Long size, Boolean other, String search) {
            pages.add(page);
            return new GetApiClientServers200Response().servers(new GetApiClientServers200ResponseServers()
                    .total(2L).data(List.of(new Server().uuid(id).name("test"))));
        }
        @Override public GetApiClientServersServer200Response getApiClientServersServer(UUID server) {
            return new GetApiClientServersServer200Response().server(new ApiServer().uuid(server).name("test")
                    .isSuspended(suspended).isTransferring(false)
                    .featureLimits(new ApiServerFeatureLimits().backups(limit)));
        }
        @Override public GetApiClientServersServerBackups200Response getApiClientServersServerBackups(
                UUID server, Long page, Long size, Boolean ungrouped, String search,
                ServerBackupKind kind, UUID database, DatabaseAgentType type) {
            return new GetApiClientServersServerBackups200Response().backups(
                    new GetApiClientServersServerBackups200ResponseBackups().total((long) backups.size()).data(backups));
        }
        @Override public Object deleteApiClientServersServerBackupsBackup(UUID server, UUID backup) throws ApiException {
            if (fail) throw new ApiException(403, "denied");
            deleted.add(backup); return null;
        }
        @Override public GetApiAdminNodesNodeBackupsBackup200Response postApiClientServersServerBackups(
                UUID server, PostApiClientServersServerBackupsRequest request) {
            assertNotNull(request.getName());
            assertTrue(request.getIgnoredFiles().isEmpty());
            created++;
            return new GetApiAdminNodesNodeBackupsBackup200Response();
        }
        ApiServerBackup backup(boolean locked, int days) {
            var b = new ApiServerBackup().uuid(UUID.randomUUID()).name("backup").isLocked(locked)
                    .created(OffsetDateTime.parse("2026-01-01T00:00:00Z").plusDays(days));
            backups.add(b);
            return b;
        }
    }

    @Test public void listsEveryPage() throws Exception {
        var api = new FakeApi();
        assertEquals(2, new BackupManager(api).getServers().size());
        assertEquals(List.of(1L, 2L), api.pages);
    }
    @Test public void deletesOldestUnlockedAndMakesRoomAfterLimitReduction() throws Exception {
        var api = new FakeApi();
        api.backup(true, 0);
        var newer = api.backup(false, 2);
        var older = api.backup(false, 1);
        new BackupManager(api).BackUp(api.id);
        assertEquals(List.of(older.getUuid(), newer.getUuid()), api.deleted);
        assertEquals(1, api.created);
    }
    @Test public void lockedBackupsPreventDeletionAndCreation() throws Exception {
        var api = new FakeApi();
        api.backup(true, 0); api.backup(true, 1);
        new BackupManager(api).BackUp(api.id);
        assertTrue(api.deleted.isEmpty()); assertEquals(0, api.created);
    }
    @Test public void suspendedAndDisabledServersAreSkipped() throws Exception {
        var api = new FakeApi();
        api.suspended = true;
        new BackupManager(api).BackUp(api.id);
        api.suspended = false; api.limit = 0;
        new BackupManager(api).BackUp(api.id);
        assertEquals(0, api.created);
    }
    @Test public void failedDeletionDoesNotCreateBackup() throws Exception {
        var api = new FakeApi();
        api.backup(false, 0); api.backup(false, 1); api.fail = true;
        try { new BackupManager(api).BackUp(api.id); fail("Expected API failure"); }
        catch (ApiException expected) { assertEquals(403, expected.getCode()); }
        assertEquals(0, api.created);
    }
    @Test public void timerCanBeCancelledAndRestarted() {
        var timer = new BackupTimer();
        try {
            timer.runTimer(60000, true, null); assertTrue(timer.isRunning());
            timer.stopTimer(); assertFalse(timer.isRunning());
            timer.runTimer(60000, false, null); assertTrue(timer.isRunning());
        } finally { timer.stopTimer(); }
    }
}

