package com.houzicore.gateway;

import com.houzicore.gateway.auth.AuthDatabase;
import com.houzicore.gateway.auth.AuthSessionManager;
import com.houzicore.gateway.auth.IpTrustManager;
import com.houzicore.gateway.auth.TwoFactorManager;
import com.houzicore.gateway.auth.SessionLoginManager;
import com.houzicore.gateway.auth.RateLimiter;
import com.houzicore.gateway.command.GateAdminCommand;
import com.houzicore.gateway.command.TwoFaCommand;
import com.houzicore.gateway.listener.AuthGuard;
import com.houzicore.gateway.listener.LoginListener;
import com.houzicore.gateway.command.HzGateSubmitCommand;
import com.houzicore.gateway.screen.DialogLoginUI;
import com.houzicore.gateway.screen.LoginScreenManager;
import com.houzicore.gateway.screen.WarpTransferManager;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class GatewayPlugin extends JavaPlugin {

    private static GatewayPlugin instance;

    private GateConfig config;
    private AuthDatabase database;
    private AuthSessionManager sessionManager;
    private IpTrustManager ipTrustManager;
    private TwoFactorManager twoFactorManager;
    private SessionLoginManager sessionLoginManager;
    private RateLimiter rateLimiter;
    private LoginScreenManager loginScreenManager;
    private WarpTransferManager warpTransferManager;
    private DialogLoginUI dialogLoginUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.config = new GateConfig(this);

        // Database
        this.database = new AuthDatabase(this);
        if (!database.connect()) {
            getLogger().severe("Failed to connect to database! Disabling HouziGate.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        database.createTables();

        // Managers
        this.sessionManager       = new AuthSessionManager(this);
        this.ipTrustManager       = new IpTrustManager(database);
        this.twoFactorManager     = new TwoFactorManager(database);
        this.sessionLoginManager  = new SessionLoginManager(this);
        this.rateLimiter          = new RateLimiter(this);
        this.loginScreenManager   = new LoginScreenManager(this);
        this.warpTransferManager  = new WarpTransferManager(this);
        this.dialogLoginUI        = new DialogLoginUI(this);

        // BungeeCord plugin messaging channel (required for server transfer)
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        // Register incoming channel (dummy — required by some BungeeCord setups)
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord",
                (PluginMessageListener) (channel, player, bytes) -> {});

        // Listeners
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        getServer().getPluginManager().registerEvents(new AuthGuard(this), this);

        // Commands
        getCommand("hzgate-submit").setExecutor(new HzGateSubmitCommand(this));
        getCommand("2fa").setExecutor(new TwoFaCommand(this));
        getCommand("gateadmin").setExecutor(new GateAdminCommand(this));

        getLogger().info("HouziGate enabled. Lobby server: " + config.lobbyServer());
    }

    @Override
    public void onDisable() {
        if (database != null) database.disconnect();
        if (sessionManager != null) sessionManager.clearAll();
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getLogger().info("HouziGate disabled.");
    }

    // -----------------------------------------------------------------------
    // Static accessor
    // -----------------------------------------------------------------------

    public static GatewayPlugin get() { return instance; }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public GateConfig getGateConfig()              { return config; }
    public AuthDatabase getDatabase()              { return database; }
    public AuthSessionManager getSessionManager()  { return sessionManager; }
    public IpTrustManager getIpTrustManager()      { return ipTrustManager; }
    public TwoFactorManager getTwoFactorManager()  { return twoFactorManager; }
    public SessionLoginManager getSessionLoginManager() { return sessionLoginManager; }
    public RateLimiter getRateLimiter()            { return rateLimiter; }
    public LoginScreenManager getLoginScreen()     { return loginScreenManager; }
    public WarpTransferManager getWarpTransfer()   { return warpTransferManager; }
    public DialogLoginUI getDialogLoginUI()         { return dialogLoginUI; }
}
