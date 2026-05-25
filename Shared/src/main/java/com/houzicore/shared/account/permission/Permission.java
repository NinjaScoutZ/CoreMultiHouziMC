package com.houzicore.shared.account.permission;

/**
 * Empty interface utilized to enforce compile-time safety on Enum permissions.
 * Managers implement this interface on their internal enums and assign them using Rank.XX.setPermission().
 */
public interface Permission {
}
