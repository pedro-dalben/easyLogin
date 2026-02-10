package com.easylogin.data;

import com.easylogin.EasyLoginConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Persistent storage for player accounts using JSON.
 * Thread-safe with atomic writes and automatic backups.
 */
public class PlayerDataStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type ACCOUNTS_TYPE = new TypeToken<Map<String, PlayerAccount>>() {
    }.getType();

    private Path dataFile;
    private Path tempFile;
    private Path backupFile;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, PlayerAccount> accounts = new ConcurrentHashMap<>();

    public PlayerDataStore(Path storageDir) {
        setStoragePath(storageDir);
    }

    /**
     * Updates the storage path to a new directory. Useful for moving to
     * world-specific storage.
     */
    public void setStoragePath(Path storageDir) {
        lock.writeLock().lock();
        try {
            Path modDir = storageDir.resolve("easylogin");
            this.dataFile = modDir.resolve("players.json");
            this.tempFile = modDir.resolve("players.json.tmp");
            this.backupFile = modDir.resolve("players.json.bak");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Migrates data from the old config directory to the new world directory if
     * necessary.
     */
    public void migrateIfNeeded(Path oldConfigDir) {
        Path oldModDir = oldConfigDir.resolve("easylogin");
        Path oldFile = oldModDir.resolve("players.json");
        Path oldBackup = oldModDir.resolve("players.json.bak");

        if (Files.exists(oldFile) && !Files.exists(dataFile)) {
            try {
                Files.createDirectories(dataFile.getParent());
                Files.move(oldFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
                if (Files.exists(oldBackup)) {
                    Files.move(oldBackup, backupFile, StandardCopyOption.REPLACE_EXISTING);
                }
                EasyLoginConstants.LOGGER.info("Successfully migrated player data from {} to {}", oldFile, dataFile);
            } catch (IOException e) {
                EasyLoginConstants.LOGGER.error("Failed to migrate player data", e);
            }
        }
    }

    /**
     * Load accounts from disk. Should be called once on server start.
     */
    public void load() {
        lock.writeLock().lock();
        try {
            if (Files.exists(dataFile)) {
                try (Reader reader = Files.newBufferedReader(dataFile)) {
                    Map<String, PlayerAccount> loaded = GSON.fromJson(reader, ACCOUNTS_TYPE);
                    if (loaded != null) {
                        accounts.clear();
                        accounts.putAll(loaded);
                    }
                }
                EasyLoginConstants.LOGGER.info("Loaded {} player accounts", accounts.size());
            } else {
                EasyLoginConstants.LOGGER.info("No player data file found, starting fresh");
            }
        } catch (IOException e) {
            EasyLoginConstants.LOGGER.error("Failed to load player data", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Save all accounts to disk with atomic write and backup.
     */
    public void save() {
        lock.readLock().lock();
        try {
            Files.createDirectories(dataFile.getParent());

            // Write to temp file first
            try (Writer writer = Files.newBufferedWriter(tempFile)) {
                GSON.toJson(accounts, ACCOUNTS_TYPE, writer);
            }

            // Backup existing file
            if (Files.exists(dataFile)) {
                Files.copy(dataFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Atomic rename
            Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            EasyLoginConstants.LOGGER.error("Failed to save player data", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get a player account by UUID.
     */
    public Optional<PlayerAccount> getAccount(UUID uuid) {
        return Optional.ofNullable(accounts.get(uuid.toString()));
    }

    /**
     * Check if a player has a registered account.
     */
    public boolean isRegistered(UUID uuid) {
        return accounts.containsKey(uuid.toString());
    }

    /**
     * Register a new account. Returns false if already registered.
     */
    public boolean register(PlayerAccount account) {
        String key = account.getUuid().toString();
        if (accounts.containsKey(key)) {
            return false;
        }
        accounts.put(key, account);
        save();
        return true;
    }

    /**
     * Update an existing account.
     */
    public void updateAccount(PlayerAccount account) {
        accounts.put(account.getUuid().toString(), account);
        save();
    }

    /**
     * Remove a player account (purge).
     */
    public boolean removeAccount(UUID uuid) {
        PlayerAccount removed = accounts.remove(uuid.toString());
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    /**
     * Get all registered accounts (for admin).
     */
    public Collection<PlayerAccount> getAllAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    public int getAccountCount() {
        return accounts.size();
    }
}
