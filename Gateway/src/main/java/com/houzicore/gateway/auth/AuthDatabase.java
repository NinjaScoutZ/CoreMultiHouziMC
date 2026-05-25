package com.houzicore.gateway.auth;

import com.houzicore.gateway.GatewayPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MySQL persistence layer for HouziGate using HikariCP.
 *
 * Tables:
 *   gate_accounts    — credentials + premium flag
 *   gate_trusted_ips — per-player trusted IP list
 *   gate_2fa         — optional secondary PIN
 *   gate_login_log   — login history audit trail
 */
public class AuthDatabase {

    private final GatewayPlugin plugin;
    private final Logger log;
    private HikariDataSource dataSource;

    public AuthDatabase(GatewayPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    // -----------------------------------------------------------------------
    // Connection Pool Setup
    // -----------------------------------------------------------------------

    public boolean connect() {
        try {
            HikariConfig config = new HikariConfig();
            
            String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
                plugin.getGateConfig().dbHost(),
                plugin.getGateConfig().dbPort(),
                plugin.getGateConfig().dbName()
            );
            
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(plugin.getGateConfig().dbUser());
            config.setPassword(plugin.getGateConfig().dbPassword());
            
            // Connection pool tuning
            config.setMaximumPoolSize(plugin.getGateConfig().dbPoolSize());
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000); // 5 mins
            config.setMaxLifetime(1800000); // 30 mins
            config.setConnectionTimeout(5000); // 5s timeout
            
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            log.info("Initialized database connection pool (HikariCP).");
            return true;
        } catch (Exception e) {
            log.severe("Failed to initialize database pool: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Database connection pool closed.");
        }
    }

    private Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database source is closed or uninitialized.");
        }
        return dataSource.getConnection();
    }

    // -----------------------------------------------------------------------
    // Schema Initialization
    // -----------------------------------------------------------------------

    public void createTables() {
        String accounts = """
            CREATE TABLE IF NOT EXISTS gate_accounts (
                name          VARCHAR(16)  NOT NULL,
                password_hash VARCHAR(60)  NOT NULL,
                is_premium    TINYINT(1)   NOT NULL DEFAULT 0,
                registered_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                last_login    TIMESTAMP    NULL,
                last_ip       VARCHAR(45)  NULL,
                PRIMARY KEY (name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

        String trustedIps = """
            CREATE TABLE IF NOT EXISTS gate_trusted_ips (
                name     VARCHAR(16) NOT NULL,
                ip       VARCHAR(45) NOT NULL,
                added_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (name, ip),
                INDEX idx_name (name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

        String twoFa = """
            CREATE TABLE IF NOT EXISTS gate_2fa (
                name     VARCHAR(16) NOT NULL,
                pin_hash VARCHAR(60) NOT NULL,
                enabled  TINYINT(1)  NOT NULL DEFAULT 1,
                PRIMARY KEY (name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

        String auditLog = """
            CREATE TABLE IF NOT EXISTS gate_login_log (
                id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                name       VARCHAR(16) NOT NULL,
                ip         VARCHAR(45) NOT NULL,
                action     VARCHAR(20) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_name (name),
                INDEX idx_ip (ip)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute(accounts);
            st.execute(trustedIps);
            st.execute(twoFa);
            st.execute(auditLog);
            log.info("Database tables verified.");
        } catch (SQLException e) {
            log.severe("Failed to create tables: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Account Operations
    // -----------------------------------------------------------------------

    public boolean accountExists(String name) {
        String sql = "SELECT 1 FROM gate_accounts WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.warning("accountExists error: " + e.getMessage());
            return false;
        }
    }

    public void createAccount(String name, String hashedPassword, boolean isPremium) {
        String sql = "INSERT INTO gate_accounts (name, password_hash, is_premium) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.setString(2, hashedPassword);
            ps.setInt(3, isPremium ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.severe("createAccount error: " + e.getMessage());
        }
    }

    public String getPasswordHash(String name) {
        String sql = "SELECT password_hash FROM gate_accounts WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            log.warning("getPasswordHash error: " + e.getMessage());
        }
        return null;
    }

    public boolean isPremium(String name) {
        String sql = "SELECT is_premium FROM gate_accounts WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("is_premium") == 1;
            }
        } catch (SQLException e) {
            log.warning("isPremium error: " + e.getMessage());
        }
        return false;
    }

    public void updateLastLogin(String name, String ip) {
        String sql = "UPDATE gate_accounts SET last_login = CURRENT_TIMESTAMP, last_ip = ? WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setString(2, name.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("updateLastLogin error: " + e.getMessage());
        }
    }

    public void resetPassword(String name) {
        String sql = "DELETE FROM gate_accounts WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("resetPassword error: " + e.getMessage());
        }
    }

    public void setPremiumFlag(String name, boolean premium) {
        String sql = "UPDATE gate_accounts SET is_premium = ? WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, premium ? 1 : 0);
            ps.setString(2, name.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("setPremiumFlag error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Trusted IPs
    // -----------------------------------------------------------------------

    public boolean isTrustedIp(String name, String ip) {
        String sql = "SELECT 1 FROM gate_trusted_ips WHERE name = ? AND ip = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.setString(2, ip);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.warning("isTrustedIp error: " + e.getMessage());
            return false;
        }
    }

    public void addTrustedIp(String name, String ip) {
        int max = plugin.getGateConfig().maxTrustedIps();
        if (max > 0) pruneTrustedIps(name, max - 1);

        String sql = "INSERT IGNORE INTO gate_trusted_ips (name, ip) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.setString(2, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("addTrustedIp error: " + e.getMessage());
        }
    }

    private void pruneTrustedIps(String name, int keepCount) {
        String sql = """
            DELETE FROM gate_trusted_ips
            WHERE name = ?
              AND ip NOT IN (
                SELECT ip FROM (
                  SELECT ip FROM gate_trusted_ips
                  WHERE name = ?
                  ORDER BY added_at DESC
                  LIMIT ?
                ) AS sub
              )
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.setString(2, name.toLowerCase());
            ps.setInt(3, keepCount);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("pruneTrustedIps error: " + e.getMessage());
        }
    }

    public void clearTrustedIps(String name) {
        String sql = "DELETE FROM gate_trusted_ips WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("clearTrustedIps error: " + e.getMessage());
        }
    }

    public List<String> getTrustedIps(String name) {
        List<String> ips = new ArrayList<>();
        String sql = "SELECT ip FROM gate_trusted_ips WHERE name = ? ORDER BY added_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ips.add(rs.getString("ip"));
            }
        } catch (SQLException e) {
            log.warning("getTrustedIps error: " + e.getMessage());
        }
        return ips;
    }

    // -----------------------------------------------------------------------
    // 2FA PIN
    // -----------------------------------------------------------------------

    public String getPinHash(String name) {
        String sql = "SELECT pin_hash FROM gate_2fa WHERE name = ? AND enabled = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("pin_hash");
            }
        } catch (SQLException e) {
            log.warning("getPinHash error: " + e.getMessage());
        }
        return null;
    }

    public void setPin(String name, String pinHash) {
        String sql = "INSERT INTO gate_2fa (name, pin_hash, enabled) VALUES (?, ?, 1) " +
                     "ON DUPLICATE KEY UPDATE pin_hash = ?, enabled = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.setString(2, pinHash);
            ps.setString(3, pinHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.severe("setPin error: " + e.getMessage());
        }
    }

    public boolean hasPin(String name) {
        return getPinHash(name) != null;
    }

    // -----------------------------------------------------------------------
    // Audit Logging
    // -----------------------------------------------------------------------

    public void logLogin(String name, String ip, String action) {
        String sql = "INSERT INTO gate_login_log (name, ip, action) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.setString(2, ip);
            ps.setString(3, action);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("logLogin error: " + e.getMessage());
        }
    }
}
