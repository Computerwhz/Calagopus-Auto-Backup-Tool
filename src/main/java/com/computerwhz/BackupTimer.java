package com.computerwhz;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BackupTimer {
    private Timer timer;

    public synchronized void runTimer(long time, boolean repeat, UUID server) {
        if (timer != null) {
            System.err.println("Timer already running; use canceltimer first");
            return;
        }
        if (time <= 0) throw new IllegalArgumentException("Time must be greater than 0");
        Timer scheduled = new Timer("calagopus-backup");
        timer = scheduled;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (server == null) Main.getBackupManager().BackUpAll();
                    else Main.getBackupManager().BackUp(server);
                } catch (Exception e) {
                    System.err.println("Scheduled backup failed: " + e.getMessage());
                    if (e instanceof com.computerwhz.calagopus.ApiException apiError
                            && (apiError.getCode() == 400 || apiError.getCode() == 415)) {
                        synchronized (BackupTimer.this) {
                            scheduled.cancel();
                            if (timer == scheduled) timer = null;
                        }
                        System.err.println("Timer stopped after a rejected request; fix the error before restarting it.");
                    }
                } finally {
                    if (!repeat) {
                        synchronized (BackupTimer.this) {
                            scheduled.cancel();
                            if (timer == scheduled) timer = null;
                        }
                    }
                }
            }
        };
        if (repeat) scheduled.schedule(task, time, time);
        else scheduled.schedule(task, time);
    }

    public synchronized void stopTimer() {
        if (timer != null) timer.cancel();
        timer = null;
    }

    public synchronized boolean isRunning() { return timer != null; }
}

