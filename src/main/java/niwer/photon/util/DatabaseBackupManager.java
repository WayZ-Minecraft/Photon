package niwer.photon.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;

public final class DatabaseBackupManager {

    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS");
    private static volatile ScheduledExecutorService scheduler;
    
    // Variables to track the state of the database at the time of the last backup
    private static long lastDbSize = -1;
    private static String lastDbHash = null;

    private DatabaseBackupManager() {}

    public static synchronized void start() {
        if(!Directories.BACKUPS_DIR.exists()) Directories.BACKUPS_DIR.mkdirs(); // Create the backups directory if it doesn't exist yet

        /* Stop and try to delete old backups */
        stop();
        deletedOldBackups();

        final var config = Directories.getConfig();
        if (config == null || !config.dbBackupEnabled()) {
            Console.log("Database backups are disabled in configuration").type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
            return;
        }

        if (Boolean.TRUE.equals(config.dbBackupOnStartup())) {
            createBackup();
        }

        final long intervalMinutes = config.dbBackupIntervalMinutes();
        if (intervalMinutes <= 0L) {
            Console.log("Database backup schedule is disabled because the interval is not positive").type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
        scheduler.scheduleAtFixedRate(DatabaseBackupManager::createBackup, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
        Console.log("Database backups scheduled every " + intervalMinutes + " minute(s)").type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
    }

    public static synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    public static void createBackup() {
        final var config = Directories.getConfig();
        if (config == null || !config.dbBackupEnabled()) return;

        final Path databaseFile = Directories.DATA_BASE_FILE.toPath();
        if (!Files.exists(databaseFile)) {
            Console.log("Skipping database backup because the database file does not exist yet").type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
            return;
        }

        final Path backupDirectory = Path.of(Directories.BACKUPS_DIR.getPath());
        final String backupFileName = config.dbBackupFilePrefix() + "-" + LocalDateTime.now().format(BACKUP_TIMESTAMP) + ".db";
        final Path backupFile = backupDirectory.resolve(backupFileName).toAbsolutePath();

        try {
            long currentSize = Files.size(databaseFile);
            String currentHash = null;
            boolean shouldSkipBackup = false;

            /* Check metadata (size) before hashing to save compute time */
            if (currentSize == lastDbSize && lastDbHash != null) {
                currentHash = HashUtils.hashFile(databaseFile);
                if (currentHash.equals(lastDbHash)) shouldSkipBackup = true;
            }

            if (shouldSkipBackup) {
                final String TIME = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                Console.log("Backup skipped at " + TIME + ". The previous is the same.").type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
                return;
            }

            Files.createDirectories(backupDirectory);
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.toAbsolutePath()); Statement statement = connection.createStatement()) {
                statement.executeUpdate("VACUUM INTO '" + escapeSqlLiteral(backupFile.toString()) + "'");
            }
            
            /* Update tracking variables after a successful backup */
            lastDbSize = currentSize;
            if (currentHash == null) lastDbHash = HashUtils.hashFile(databaseFile);
            else lastDbHash = currentHash;

            Console.log("Database backup created at " + backupFile).type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
        } catch (IOException | SQLException e) {
            Console.log("Failed to create database backup: " + e.getMessage()).error().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
        }
    }

    private static void deletedOldBackups() {
        if(Directories.getConfig() == null || Directories.getConfig().dbBackupRetentionDays() <= 0) return;
        if(Directories.BACKUPS_DIR == null || !Directories.BACKUPS_DIR.exists() || !Directories.BACKUPS_DIR.isDirectory()) throw new IllegalStateException("Backups directory is not properly configured or accessible");

        final var STREAM = Stream.of(Directories.BACKUPS_DIR.listFiles()).parallel().filter(file -> {
            try {
                if (file == null || !file.isFile()) return false; // Skip folders and nulls
    
                final String FILME_NAME = file.getName();
                final FileTime CREATION_TIME = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                return FILME_NAME.startsWith(Directories.getConfig().dbBackupFilePrefix()) && CREATION_TIME.toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(Directories.getConfig().dbBackupRetentionDays());
            } catch (Exception e) {
                Console.log("Failed to delete old database backups: " + e.getMessage()).error().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
                return false;
            }
        });
        STREAM.forEach(file -> {
            if (file != null) file.delete();
        });
    }
 
    private static String escapeSqlLiteral(String value) {
        return value.replace("'", "''");
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            final Thread thread = new Thread(runnable, "photon-database-backup");
            thread.setDaemon(true);
            return thread;
        }
    }
}