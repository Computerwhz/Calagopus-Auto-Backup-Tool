package com.computerwhz;

import com.computerwhz.calagopus.ApiClient;
import com.computerwhz.calagopus.ApiException;
import com.computerwhz.calagopus.api.ApiClientApi;
import com.computerwhz.calagopus.model.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BackupManager {
    private static final long PAGE_SIZE = 100L;
    private final ApiClientApi api;

    public BackupManager(ApiClient client) {
        this(new ApiClientApi(client));
    }

    BackupManager(ApiClientApi api) {
        this.api = api;
    }

    public List<Server> getServers() throws ApiException {
        List<Server> result = new ArrayList<>();
        for (long page = 1; ; page++) {
            var servers = api.getApiClientServers(page, PAGE_SIZE, false, null).getServers();
            result.addAll(servers.getData());
            if (servers.getData().isEmpty() || result.size() >= servers.getTotal()) return result;
        }
    }

    public void BackUpAll() throws ApiException {
        for (Server server : getServers()) {
            try {
                BackUp(server.getUuid());
            } catch (ApiException e) {
                if (e.getCode() == 400 || e.getCode() == 415) {
                    System.err.println("Stopping backupall after a rejected request on " + server.getName());
                    throw e;
                }
                System.err.println("Failed to back up " + server.getName() + ": " + e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("Failed to back up " + server.getName() + ": " + e.getMessage());
            }
        }
    }

    public synchronized void BackUp(UUID serverId) throws ApiException {
        // Refresh state for every run, including repeating timers.
        ApiServer server = api.getApiClientServersServer(serverId).getServer();
        String name = server.getName();
        if (Boolean.TRUE.equals(server.getIsSuspended()) || Boolean.TRUE.equals(server.getIsTransferring())) {
            System.out.println("Server suspended or transferring: " + name);
            return;
        }
        int limit = server.getFeatureLimits().getBackups();
        if (limit <= 0) {
            System.out.println("Backups disabled: " + name);
            return;
        }
        List<ApiServerBackup> backups = getBackups(serverId);
        backups.sort(Comparator.comparing(ApiServerBackup::getCreated));
        int toDelete = Math.max(0, backups.size() - limit + 1);
        var unlocked = backups.stream().filter(b -> Boolean.FALSE.equals(b.getIsLocked())).toList();
        if (unlocked.size() < toDelete) {
            System.out.println("Not enough unlocked backups to make room. Skipping " + name);
            return;
        }
        for (int i = 0; i < toDelete; i++) {
            var backup = unlocked.get(i);
            api.deleteApiClientServersServerBackupsBackup(serverId, backup.getUuid());
            System.out.println("Deleted oldest unlocked backup: " + backup.getName() + " on " + name);
        }
        String backupName = "Auto Backup tool at " + System.currentTimeMillis();
        api.postApiClientServersServerBackups(serverId,
                new PostApiClientServersServerBackupsRequest().name(backupName).ignoredFiles(List.of()));
        System.out.println("Backup request accepted: " + backupName + " on " + name + " (" + serverId + ")");
    }

    private List<ApiServerBackup> getBackups(UUID serverId) throws ApiException {
        List<ApiServerBackup> result = new ArrayList<>();
        for (long page = 1; ; page++) {
            var backups = api.getApiClientServersServerBackups(
                    serverId, page, PAGE_SIZE, true, null, null, null, null).getBackups();
            result.addAll(backups.getData());
            if (backups.getData().isEmpty() || result.size() >= backups.getTotal()) return result;
        }
    }
}

