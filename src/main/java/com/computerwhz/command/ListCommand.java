package com.computerwhz.command;

import com.computerwhz.Command;
import com.computerwhz.Main;
import com.computerwhz.calagopus.ApiException;

public class ListCommand implements Command {
    @Override
    public void execute(String[] args) {
        try {
            var servers = Main.getBackupManager().getServers();
            System.out.println("List of available servers");
            for (var server : servers) {
                System.out.println("Name: " + server.getName() + " | UUID: " + server.getUuid());
            }
            System.out.println("Listed " + servers.size() + " available servers");
        } catch (ApiException e) {
            System.err.println("Could not list Calagopus servers (" + e.getCode() + "): " + e.getMessage());
        }
    }
}
