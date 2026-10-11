package com.computerwhz.command;

import com.computerwhz.Command;
import com.computerwhz.Main;
import com.computerwhz.calagopus.ApiException;
import java.util.UUID;

public class BackupCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: backup <all|server-uuid>");
            return;
        }
        try {
            if (args[0].equalsIgnoreCase("all")) Main.getBackupManager().BackUpAll();
            else Main.getBackupManager().BackUp(UUID.fromString(args[0]));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid server UUID: " + args[0]);
        } catch (ApiException e) {
            System.err.println("Calagopus API error (" + e.getCode() + "): " + e.getMessage());
        }
    }
}
