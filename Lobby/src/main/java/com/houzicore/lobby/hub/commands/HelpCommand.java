package com.houzicore.lobby.hub.commands;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.common.BrandConfig;
import com.houzicore.lobby.hub.HubManager;

/**
 * Product-ready /help command.
 * Displays rank-filtered, bilingual command reference and a rank hierarchy guide.
 */
public class HelpCommand extends CommandBase<HubManager> {

	private static final String BAR = C.cDGray + C.Strike + "                                                     ";
	private static final String BAR_THIN = C.cDGray + C.Strike + "                                           ";

	public HelpCommand(HubManager plugin) {
		super(plugin, Rank.ALL, "help", "h", "?");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		CoreClient client = Plugin.GetClients().Get(caller);
		if (client == null) return;

		Rank rank = client.GetRank();
		String lang = Plugin.getPreferences().Get(caller).Language;
		if (lang == null) lang = "THA";
		boolean th = lang.equalsIgnoreCase("THAI") || lang.equalsIgnoreCase("THA") || lang.equalsIgnoreCase("TH");

		if (args != null && args.length > 0 && args[0].equalsIgnoreCase("ranks")) {
			sendRankGuide(caller, th);
			return;
		}

		sendHelpMenu(caller, rank, th);
	}

	// ─────────────────────────────────────────────────────────────────
	//  MAIN HELP MENU
	// ─────────────────────────────────────────────────────────────────
	private void sendHelpMenu(Player p, Rank rank, boolean th) {
		p.sendMessage("");
		p.sendMessage(BAR);
		p.sendMessage(center(C.cGold + C.Bold + "✦ " + BrandConfig.mainServerName().toUpperCase(java.util.Locale.ROOT) + " " + C.cGray + "— " + C.cWhite + (th ? "เมนูช่วยเหลือ" : "Help Menu")));
		p.sendMessage(BAR);
		p.sendMessage("");

		// ── Player Commands ──
		p.sendMessage(C.cGreen + C.Bold + "⬥ " + (th ? "คำสั่งผู้เล่น" : "PLAYER COMMANDS"));
		cmd(p, "/prefs",      th ? "ตั้งค่าและภาษา"         : "Preferences & Language");
		cmd(p, "/party",      th ? "สร้าง/เข้าร่วมปาร์ตี้"   : "Create or join parties");
		cmd(p, "/stats",      th ? "ดูสถิติของคุณ"           : "View your statistics");
		cmd(p, "/msg <player>", th ? "ส่งข้อความส่วนตัว"     : "Private message");
		cmd(p, "/r",           th ? "ตอบข้อความล่าสุด"       : "Reply to last message");
		cmd(p, "/report",      th ? "รายงานผู้เล่น"          : "Report a player");
		cmd(p, "/server",      th ? "เปลี่ยนเซิร์ฟเวอร์"     : "Change server");
		cmd(p, "/poll",        th ? "ดูโพลที่เปิดอยู่"        : "View active polls");
		p.sendMessage("");

		// ── Premium Commands ──
		if (rank.Has(Rank.WARRIOR) || rank == Rank.YOUTUBE || rank == Rank.TWITCH) {
			p.sendMessage(" ");
			if (rank.Has(Rank.DIVINE)) {
				cmd(p, "/hostserver", th ? "เปิดเซิร์ฟเวอร์ส่วนตัว" : "Host a private server", Rank.DIVINE);
			}
			p.sendMessage("");
		}

		// ── Staff Commands ──
		if (rank.Has(Rank.HELPER)) {
			p.sendMessage(C.cDPurple + C.Bold + "⬥ " + (th ? "คำสั่งสตาฟ" : "STAFF COMMANDS"));
			if (rank.Has(Rank.HELPER)) {
				cmd(p, "/punish <player>", th ? "ลงโทษผู้เล่น"     : "Punish a player",      Rank.HELPER);
			}
			if (rank.Has(Rank.MODERATOR)) {
				cmd(p, "/tp <player>",  th ? "เทเลพอร์ตไปหาผู้เล่น" : "Teleport to player",   Rank.MODERATOR);
				cmd(p, "/locate",       th ? "ค้นหาเซิร์ฟเวอร์ผู้เล่น" : "Find player's server", Rank.MODERATOR);
				cmd(p, "/s <message>",  th ? "ประกาศข้อความ"        : "Broadcast message",    Rank.MODERATOR);
			}
			if (rank.Has(Rank.SNR_MODERATOR)) {
				cmd(p, "/chatslow",     th ? "เปิดแชทช้า"            : "Slow down chat",       Rank.SNR_MODERATOR);
				cmd(p, "/am <message>", th ? "ข้อความถึงแอดมิน"      : "Admin message",        Rank.SNR_MODERATOR);
			}
			if (rank.Has(Rank.ADMIN) || rank == Rank.JNR_DEV) {
				cmd(p, "/gm",           th ? "เปลี่ยนเกมโหมด"        : "Change gamemode",      Rank.ADMIN);
				cmd(p, "/coin <player>", th ? "ให้เหรียญ"            : "Give coins",           Rank.ADMIN);
				cmd(p, "/gem <player>",  th ? "ให้อัญมณี"            : "Give gems",            Rank.ADMIN);
				cmd(p, "/give",          th ? "ให้ไอเท็ม"            : "Give items",           Rank.ADMIN);
				cmd(p, "/giveitem",      th ? "ให้ไอเท็มอินเวนทอรี"  : "Give inventory item",  Rank.ADMIN);
				cmd(p, "/silence",       th ? "ปิดแชททั้งเซิร์ฟ"     : "Silence server chat",  Rank.ADMIN);
				cmd(p, "/announce",      th ? "ประกาศทั่วเซิร์ฟ"     : "Server announcement",  Rank.ADMIN);
				cmd(p, "/news",          th ? "จัดการข่าว"            : "Manage news system",   Rank.ADMIN);
			}
			if (rank.Has(Rank.OWNER)) {
				cmd(p, "/givestat",    th ? "ให้สถิติ"              : "Give statistics",     Rank.OWNER);
			}
			p.sendMessage("");
		}

		// ── Footer ──
		p.sendMessage(BAR_THIN);
		p.sendMessage(C.cGray + "  " + (th ? "ยศ: " : "Rank: ")
				+ rank.GetColor() + rank.Name
				+ C.cDGray + "  |  "
				+ C.cGray + (th ? "พิมพ์ " : "Type ") + C.cYellow + "/help ranks "
				+ C.cGray + (th ? "ดูยศทั้งหมด" : "for rank guide"));
		p.sendMessage(BAR_THIN);
		p.sendMessage("");
	}

	// ─────────────────────────────────────────────────────────────────
	//  RANK GUIDE (/help ranks)
	// ─────────────────────────────────────────────────────────────────
	private void sendRankGuide(Player p, boolean th) {
		p.sendMessage("");
		p.sendMessage(BAR);
		p.sendMessage(center(C.cGold + C.Bold + "✦ " + (th ? "คู่มือยศ" : "RANK GUIDE")));
		p.sendMessage(BAR);
		p.sendMessage("");

		// Player Ranks
		p.sendMessage(C.cWhite + C.Bold + "» " + (th ? "ยศผู้เล่น" : "PLAYER RANKS"));
		rankLine(p, Rank.ALL,    th ? "ยศเริ่มต้นสำหรับผู้เล่นทุกคน"               : "Default rank for all players");
		rankLine(p, Rank.WARRIOR,  th ? "สล็อตสำรอง, เครื่องสำอาง"                    : "Reserved slot, cosmetics access");
		rankLine(p, Rank.SOVEREIGN,   th ? "เครื่องสำอาง Hero, สัตว์เลี้ยง, อนุภาค"     : "Hero cosmetics, pets, particles");
		rankLine(p, Rank.DIVINE, th ? "เปิดเซิร์ฟส่วนตัว, สิทธิ์ Hero ทั้งหมด"     : "Host servers, all Hero perks");
		p.sendMessage("");

		// Media Ranks
		p.sendMessage(C.cWhite + C.Bold + "» " + (th ? "ยศสื่อ" : "MEDIA RANKS"));
		rankLine(p, Rank.YOUTUBE, th ? "ล่องหน, Forcefield, สิทธิ์พิเศษ"            : "Invisibility, forcefield, perks");
		rankLine(p, Rank.TWITCH,  th ? "ล่องหน, Forcefield, สิทธิ์พิเศษ"            : "Invisibility, forcefield, perks");
		p.sendMessage("");

		// Staff Ranks
		p.sendMessage(C.cWhite + C.Bold + "» " + (th ? "ยศสตาฟ" : "STAFF RANKS"));
		rankLine(p, Rank.HELPER,        th ? "รายงาน, การดูแลเบื้องต้น"             : "Reports, basic moderation");
		rankLine(p, Rank.MODERATOR,      th ? "การดูแลเต็มรูปแบบ, เทเลพอร์ต"        : "Full moderation, teleport");
		rankLine(p, Rank.SNR_MODERATOR,  th ? "แชทช้า, ข้อความแอดมิน"                : "Chat slow, admin messages");
		rankLine(p, Rank.ADMIN,          th ? "จัดการเซิร์ฟ, สกุลเงิน, สปอน"        : "Server management, currency");
		rankLine(p, Rank.JNR_DEV,        th ? "พัฒนา, NPC, เครื่องมือ"               : "Development, NPC tools");
		rankLine(p, Rank.DEVELOPER,      th ? "การจัดการ NPC, เครื่องมือพัฒนา"       : "NPC management, dev tools");
		rankLine(p, Rank.OWNER,          th ? "ควบคุมเซิร์ฟเวอร์ทั้งหมด"            : "Full server control");
		p.sendMessage("");

		p.sendMessage(BAR);
		p.sendMessage("");
	}

	// ─────────────────────────────────────────────────────────────────
	//  UTILITY HELPERS
	// ─────────────────────────────────────────────────────────────────

	/** Standard command line without rank tag (player commands) */
	private void cmd(Player p, String command, String description) {
		p.sendMessage(C.cGray + "  " + command + " " + C.cDGray + "— " + C.cWhite + description);
	}

	/** Command line with a rank badge for staff commands */
	private void cmd(Player p, String command, String description, Rank minRank) {
		p.sendMessage(C.cGray + "  " + command + " " + C.cDGray + "— " + C.cWhite + description
				+ "  " + C.cDGray + "[" + minRank.GetColor() + minRank.Name + C.cDGray + "]");
	}

	/** Rank line for the rank guide */
	private void rankLine(Player p, Rank rank, String description) {
		String tag;
		if (rank == Rank.ALL) {
			tag = C.cGray + "ALL";
		} else {
			tag = rank.GetTag(false, true);
		}
		// Pad the tag to align descriptions
		p.sendMessage(C.cGray + "  " + tag + " " + C.cDGray + "— " + C.cGray + description);
	}

	/** Center text (rough approximation for chat) */
	private String center(String text) {
		return "        " + text;
	}
}
