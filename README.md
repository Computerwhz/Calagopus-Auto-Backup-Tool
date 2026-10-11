# Calagopus Auto Backup Tool

Interactive Java CLI for creating and scheduling backups through Calagopus.
Requires Java 17 or newer and a Calagopus client API key with access to the
servers and permission to list, create, and delete backups.

Build and test with `mvn clean package`. The Calagopus client dependency
`com.computerwhz:calagopus-client:1.0.0` must be available from the configured
Maven repositories or installed in your local Maven repository.

Run:

```sh
java -jar target/Calagopus-Auto-Backup-Tool-3.0.0.jar --panelurl https://panel.example.com --apikey YOUR_KEY
```

Use the panel base URL, without `/api/client`. The tool sends the key as a
Bearer token. Client configuration at startup does not verify connectivity;
commands report API errors when requests are made.

Commands:

- `list`: list available servers and their full UUIDs, including all pages.
- `backup <server-uuid|all>`: request a backup for one server or all servers.
- `backupall`: alias for `backup all`.
- `settimer <minutes> <server-uuid|all> [true|false]`: schedule a backup; the
  optional repeat flag defaults to false.
- `canceltimer`: cancel future timer runs. A backup already executing may finish.
- `help`: show command usage.
- `exit`: cancel the timer and exit.

Use full UUIDs from `list`; old Pterodactyl short identifiers are not accepted.
Timers run only while this process remains open. Each run refreshes server
state, skips suspended/transferring servers and servers with backups disabled,
and removes the oldest unlocked ungrouped backups when capacity is full.
Locked backups are preserved; if insufficient unlocked backups exist, the
server is skipped. Backup creation reports request acceptance, not completion.
Repeating timers continue after an API failure.
