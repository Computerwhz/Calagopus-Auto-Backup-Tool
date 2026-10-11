package com.computerwhz;

import com.computerwhz.calagopus.api.ApiClientApi;
import com.computerwhz.command.*;

public class Main {

    private final static CommandManager cm = new CommandManager();
    private static String panelUrl;
    private static String apiKey;
    private static CalagopusManager calagopusMgr;
    private static ApiClientApi api;
    private static BackupManager backupManager;
    private static BackupTimer timer;

    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--panelurl") && i + 1 < args.length){
                panelUrl = args[i + 1];
            }
            if (args[i].equals("--apikey") && i + 1 < args.length) {
                apiKey = args[i + 1];
            }
        }

        if (panelUrl == null || panelUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            System.err.println("Usage: java -jar Calagopus-Auto-Backup-Tool-3.0.0.jar --panelurl <url> --apikey <key>");
            return;
        }

        try{
            calagopusMgr = new CalagopusManager(panelUrl, apiKey);
            api = new ApiClientApi(calagopusMgr.getClient());
            System.out.println("Calagopus client configured");
        } catch (Exception e) {
            System.err.println("Panel Connection failed " + e.getMessage());
            e.printStackTrace();
            Exit(1);
        }

        backupManager = new BackupManager(calagopusMgr.getClient());
        timer = new BackupTimer();
        cm.Register("list", new ListCommand());
        cm.Register("backup", new BackupCommand());
        cm.Register("backupall", new BackupAllCommand());
        cm.Register("exit", new ExitCommand());
        cm.Register("help", new HelpCommand());
        cm.Register("settimer", new SetTimerCommand());
        cm.Register("canceltimer", new CancelTimerCommand());
        cm.Run();
    }

    public static void Exit(int exitCode){
        if (timer != null) timer.stopTimer();
        System.out.println("Exiting Auto Backup tool...");
        cm.UnRegisterAll();
        cm.Stop();
        System.exit(exitCode);
    }

    public static BackupTimer getTimer() { return timer; }

    public static BackupManager getBackupManager(){
        return backupManager;
    }

    public static ApiClientApi getApi() {
        return api;
    }
}
