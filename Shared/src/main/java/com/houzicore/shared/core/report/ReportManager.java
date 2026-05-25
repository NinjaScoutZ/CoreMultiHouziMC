package com.houzicore.shared.core.report;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.command.CommandCenter;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.core.report.command.ReportNotification;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.Utility;
import com.houzicore.shared.serverdata.data.DataRepository;
import com.houzicore.shared.serverdata.redis.RedisDataRepository;

import org.bukkit.entity.Player;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * ReportManager hooks into a synchronized network-wide report system with
 * methods for updating/fetching/closing reports in real time.
 * 
 * @author Ty
 *
 */
public class ReportManager {

	private static ReportManager instance;

	/**
	 * @return the singleton instance of {@link ReportManager}.
	 */
	public static ReportManager getInstance() {
		if (instance == null) {
			instance = new ReportManager();
		}

		return instance;
	}

	// Holds active/open reports in a synchronized database.
	private final DataRepository<Report> reportRepository;

	private final DataRepository<ReportProfile> reportProfiles;

	// Stores/logs closed tickets, and various reporter/staff actions.
	private ReportRepository reportSqlRepository = null;

	// A mapping of PlayerName(String) to the ReportId(Integer) for all active
	// reports on this server.
	private final Map<String, Integer> activeReports;

	// Current server name, read from JVM startup arg (set by HCSM).
	private final String _serverName;

	/**
	 * Private constructor to prevent non-singleton instances.
	 */
	private ReportManager() {
		reportRepository = new RedisDataRepository<>(Region.ALL, Report.class, "reports");
		reportProfiles = new RedisDataRepository<>(Region.ALL, ReportProfile.class, "reportprofiles");
		activeReports = new HashMap<>();
		// Read server name from JVM arg injected by HCSM (e.g. -Dserverstatus.name=MIN-1)
		_serverName = System.getProperty("serverstatus.name", "UNKNOWN");
	}

	public void setupSql(org.bukkit.plugin.java.JavaPlugin plugin) {
		if (reportSqlRepository == null) {
			reportSqlRepository = new ReportRepository(plugin);
			reportSqlRepository.initialize();
		}
	}

	public void closeReport(int reportId, Player reportCloser, String reason) {
		retrieveReportResult(reportId, reportCloser, reason);
	}

	public void closeReport(int reportId, Player reportCloser, String reason, ReportResult result) {
		if (isActiveReport(reportId)) {
			final Report report = getReport(reportId);
			reportRepository.removeElement(String.valueOf(reportId)); // Remove report from redis database
			removeActiveReport(reportId);

			// Use -1 as closerId sentinel if the closer is null (e.g. player disconnect)
			final int closerId = (reportCloser != null) ? getPlayerAccount(reportCloser).getAccountId() : -1;
			final String playerName = getReport(reportId) != null ? getReport(reportId).getPlayerName() : "UNKNOWN";
			final int playerId = playerName.equals("UNKNOWN") ? -1 : getPlayerAccount(playerName).getAccountId();
			if (reportSqlRepository != null) reportSqlRepository.logReport(reportId, playerId, _serverName, closerId, result, reason);

			// Update the reputation/profiles of all reporters on this closing report.
			for (final String reporterName : report.getReporters()) {
				final CoreClient reporterAccount = getPlayerAccount(reporterName);
				final ReportProfile reportProfile = getReportProfile(String.valueOf(reporterAccount.getAccountId()));
				reportProfile.onReportClose(result);
				reportProfiles.addElement(reportProfile);
			}

			if (reportCloser != null) {
				// Notify staff that the report was closed.
				sendReportNotification(String.format("[Report %d] %s closed this report. (%s).", reportId,
						reportCloser.getName(), result.toDisplayMessage()));
			}
		}

	}

	/**
	 * @return a uniquely generated report id.
	 */
	public int generateReportId() {
		final JedisPool pool = Utility.getPool(true);
		Jedis jedis = pool.getResource();
		long uniqueReportId = -1;

		try {
			uniqueReportId = jedis.incr("reports.unique-id");
		} catch (final JedisConnectionException exception) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, exception.getMessage(), exception);
			pool.returnBrokenResource(jedis);
			jedis = null;
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}

		return (int) uniqueReportId;
	}

	private int getAccountId(String playerName) {
		return getPlayerAccount(playerName).getAccountId();
	}

	/**
	 * @param playerName
	 *            - the name of the player whose active report id is being fetched
	 * @return the report id for the active report corresponding with playerName, if
	 *         one currently exists, -1 otherwise.
	 */
	public int getActiveReport(String playerName) {
		if (activeReports.containsKey(playerName.toLowerCase()))
			return activeReports.get(playerName.toLowerCase());

		return -1;
	}

	private CoreClient getPlayerAccount(Player player) {
		return getPlayerAccount(player.getName());
	}

	private CoreClient getPlayerAccount(String playerName) {
		return CommandCenter.Instance.GetClientManager().Get(playerName);
	}

	public Report getReport(int reportId) {
		return reportRepository.getElement(String.valueOf(reportId));
	}

	public ReportProfile getReportProfile(String playerName) {
		ReportProfile profile = reportProfiles.getElement(playerName);

		if (profile == null) {
			profile = new ReportProfile(playerName, getAccountId(playerName));
			saveReportProfile(profile);
		}

		return profile;
	}

	public void handleReport(int reportId, Player reportHandler) {
		if (reportRepository.elementExists(String.valueOf(reportId))) {
			final Report report = getReport(reportId);
			Portal.transferPlayer(reportHandler.getName(), report.getServerName());
			final String handlerName = reportHandler.getName();
			sendReportNotification(String.format("[Report %d] %s is handling this report.", reportId, handlerName));

			// Publish a notification to the target server so the handler sees report info on arrival.
			final String targetServer = report.getServerName();
			if (targetServer != null) {
				// Send the report summary as a notification to the target server.
				// The ReportNotification will broadcast to all staff on that server.
				final ReportNotification arrivalNote = new ReportNotification(
						String.format("[Report %d] §eTarget: §f%s §7— Reason: §f%s §7— Reporters: §f%d",
								reportId, report.getPlayerName(),
								report.getReporters().isEmpty() ? "Unknown" : String.join(", ", report.getReporters()),
								report.getReporters().size()));
				arrivalNote.setTargetServers(targetServer);
				arrivalNote.publish();
			}

			final int handlerId = getPlayerAccount(reportHandler).getAccountId();
			if (reportSqlRepository != null) reportSqlRepository.logReportHandling(reportId, handlerId); // Log handling into sql database
		}
	}

	public boolean hasActiveReport(Player player) {
		return getActiveReport(player.getName()) != -1;
	}

	/**
	 * @param player
	 *            - the player whose report notification settings are to be checked
	 * @return true, if the player should receive report notifications, false
	 *         otherwise.
	 */
	public boolean hasReportNotifications(Player player) {
		// Return true only for staff members (MOD rank and above)
		final CoreClient client = getPlayerAccount(player);
		return client != null && client.GetRank().Has(Rank.MODERATOR);
	}

	public boolean isActiveReport(int reportId) {
		for (final Entry<String, Integer> activeReport : activeReports.entrySet()) {
			if (activeReport.getValue() == reportId)
				return true;
		}

		return false;
	}

	public void onPlayerQuit(Player player) {
		if (hasActiveReport(player)) {
			final int reportId = getActiveReport(player.getName());
			// Pass null as closer — closeReport guards against null closerId
			this.closeReport(reportId, null, "Player Quit", ReportResult.UNDETERMINED);
			sendReportNotification(String.format("[Report %d] %s has left the game.", reportId, player.getName()));
		}
	}

	public boolean removeActiveReport(int reportId) {
		for (final Entry<String, Integer> activeReport : activeReports.entrySet()) {
			if (activeReport.getValue() == reportId) {
				activeReports.remove(activeReport.getKey());
				return true;
			}
		}

		return false;
	}

	public void reportPlayer(Player reporter, Player reportedPlayer, String reason) {
		final int reporterId = getPlayerAccount(reporter).getAccountId();
		final ReportProfile reportProfile = getReportProfile(String.valueOf(reporterId));

		if (reportProfile.canReport()) {
			Report report = null;

			if (hasActiveReport(reportedPlayer)) {
				final int reportId = getActiveReport(reportedPlayer.getName());
				report = getReport(reportId);
				report.addReporter(reporter.getName());
			} else {
				final int reportId = generateReportId();
				report = new Report(reportId, reportedPlayer.getName(), _serverName);
				report.addReporter(reporter.getName());
				activeReports.put(reportedPlayer.getName().toLowerCase(), report.getReportId());
				reportRepository.addElement(report);
			}

			if (report != null) {
				// [Report 42] [MrTwiggy +7] [Cheater102 - 5 - Speed hacking]
				final String message = String.format("[Report %d] [%s %d] [%s - %d - %s]", report.getReportId(),
						reporter.getName(), reportProfile.getReputation(), reportedPlayer.getName(),
						report.getReporters().size(), reason);
				sendReportNotification(message);
				if (reportSqlRepository != null) reportSqlRepository.logReportSending(report.getReportId(), reporterId, reason);
			}
		}
	}

	public void retrieveReportResult(int reportId, Player reportCloser, String reason) {
		// Prompt the report closer with a menu of options to determine the result
		// of the report. When confirmation is received, THEN close report.
	}

	private void saveReportProfile(ReportProfile profile) {
		reportProfiles.addElement(profile);
	}

	/**
	 * Send a network-wide {@link ReportNotification} to all online staff.
	 * 
	 * @param message
	 *            - the report notification message to send.
	 */
	public void sendReportNotification(String message) {
		final ReportNotification reportNotification = new ReportNotification(message);
		reportNotification.publish();
	}
}
