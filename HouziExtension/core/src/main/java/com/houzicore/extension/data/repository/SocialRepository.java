package com.houzicore.extension.data.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

import com.houzicore.extension.data.database.dao.TimeDAO;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.PlayTime;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Repository for managing social interactions in HouziExtension.
 * Handles ignore relationships and mail messages between players.
 *
 * @author HouziCore Development
 * @since 0.8.1
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialRepository {


    private final TimeDAO timeDAO;


    /**
     * Saves a player's time session when they join the server.
     *
     * @param fPlayer the player whose session is being saved
     */
    public void saveJoinSession(FPlayer fPlayer) {
        timeDAO.saveJoin(fPlayer);
    }

    /**
     * Saves a player's time session when they join the server.
     *
     * @param playTime session to save
     */
    public void saveJoinSession(PlayTime playTime) {
        timeDAO.saveSession(playTime);
    }

    /**
     * Saves a player's last seen timestamp when they quit the server.
     *
     * @param fPlayer the player whose last seen time is being saved
     */
    public void saveLastSeen(FPlayer fPlayer) {
        timeDAO.saveQuit(fPlayer);
    }

    /**
     * Gets the play time statistics for a specific player.
     *
     * @param fPlayer the player to get play time for
     * @return the player's play time statistics, or null if not found
     */
    public @Nullable PlayTime getPlayTime(FPlayer fPlayer) {
        return timeDAO.getByPlayer(fPlayer).orElse(null);
    }

    /**
     * Gets the total count of all play time records in the database.
     *
     * @return the total number of play time records
     */
    public int getPlayTimesCount() {
        return timeDAO.getTotalCount();
    }

    /**
     * Gets a paginated list of all play time records.
     *
     * @param limit the maximum number of records to retrieve
     * @param offset the number of records to skip before starting to return results
     * @return list of play time records within the specified range
     */
    public List<PlayTime> getAllPlayTimes(int limit, int offset) {
        return timeDAO.getAllPlayTimes(limit, offset);
    }

}
