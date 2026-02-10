package com.easylogin.data;

import java.util.UUID;

/**
 * Represents a registered player account.
 */
public class PlayerAccount {

    private UUID uuid;
    private String username;
    private String passwordHash;
    private long createdAt;
    private long lastLogin;
    private String lastIp;
    private int loginCount;

    public PlayerAccount() {
    }

    public PlayerAccount(UUID uuid, String username, String passwordHash, String ip) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = System.currentTimeMillis();
        this.lastLogin = this.createdAt;
        this.lastIp = ip;
        this.loginCount = 1;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    /**
     * Updates fields on a successful login.
     */
    public void recordLogin(String ip) {
        this.lastLogin = System.currentTimeMillis();
        this.lastIp = ip;
        this.loginCount++;
    }
}
