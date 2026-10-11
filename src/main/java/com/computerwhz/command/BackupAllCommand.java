package com.computerwhz.command;

import com.computerwhz.Command;

public class BackupAllCommand implements Command {
    @Override
    public void execute(String[] args) {
        new BackupCommand().execute(new String[]{"all"});
    }
}
