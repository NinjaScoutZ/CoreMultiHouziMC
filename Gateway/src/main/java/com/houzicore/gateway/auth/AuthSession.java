package com.houzicore.gateway.auth;

/**
 * Per-player authentication state held in memory.
 * Cleared when the player disconnects.
 */
public class AuthSession {

    public enum State {
        /** Default — waiting for /login or /register */
        WAITING_LOGIN,
        /** New player — waiting for /register */
        WAITING_REGISTER,
        /** New player — waiting to confirm the password */
        WAITING_REGISTER_CONFIRM,
        /** Password OK but IP is new — waiting for /2fa <pin> */
        WAITING_2FA,
        /** Fully authenticated — all restrictions lifted */
        AUTHENTICATED
    }

    private final String playerName;
    private State state;
    private int failedAttempts;
    private boolean premiumDetected;
    /** The IP address for this session */
    private final String ip;
    private String passwordDraft;

    public AuthSession(String playerName, String ip) {
        this.playerName      = playerName;
        this.ip              = ip;
        this.state           = State.WAITING_LOGIN;
        this.failedAttempts  = 0;
        this.premiumDetected = false;
        this.passwordDraft   = null;
    }

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    public State getState()             { return state; }
    public void  setState(State state)  { this.state = state; }

    public boolean isAuthenticated()    { return state == State.AUTHENTICATED; }
    public boolean isWaiting2Fa()       { return state == State.WAITING_2FA; }

    // -----------------------------------------------------------------------
    // Attempts
    // -----------------------------------------------------------------------

    public int  getFailedAttempts()     { return failedAttempts; }
    public void incrementFailed()       { failedAttempts++; }
    public void resetFailedAttempts()   { failedAttempts = 0; }

    // -----------------------------------------------------------------------
    // Premium
    // -----------------------------------------------------------------------

    public boolean isPremiumDetected()               { return premiumDetected; }
    public void    setPremiumDetected(boolean value) { this.premiumDetected = value; }

    // -----------------------------------------------------------------------
    // Registration Draft Password
    // -----------------------------------------------------------------------

    public String getPasswordDraft()            { return passwordDraft; }
    public void   setPasswordDraft(String draft) { this.passwordDraft = draft; }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public String getPlayerName() { return playerName; }
    public String getIp()         { return ip; }
}
