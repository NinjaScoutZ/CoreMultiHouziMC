package com.houzicore.lobby.hub.tutorial.types;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.modules.TextManager;
import com.houzicore.lobby.hub.tutorial.Tutorial;
import com.houzicore.lobby.hub.tutorial.TutorialPhase;

public class WelcomeTutorial extends Tutorial
{
	public WelcomeTutorial(HubManager manager, TextManager text) 
	{
		super(manager, "Welcome Tutorial", 5000, "Hub_JoinTutorial");

		double y = -manager.GetSpawn().getY();

		//Welcome
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(-40, y+85, 0), 
				manager.GetSpawn(),
				"Welcome to " + com.houzicore.shared.core.common.BrandConfig.mainServerName(),
				"ยินดีต้อนรับสู่ " + com.houzicore.shared.core.common.BrandConfig.mainServerName(),
				new String[] 
						{
					"§eWelcome!",
					"",
					"§7This is a short tutorial to help you get started.",
					"§7Really, it only takes 30 seconds!!!",
					"",
					"§7" + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " has many minigames to choose from.",
					"§7We will show you around and how to play!"
						},
				new String[] 
						{
					"§eยินดีต้อนรับ!",
					"",
					"§7นี่คือบทเรียนสั้นๆ ที่จะช่วยให้คุณเริ่มต้น",
					"§7จริงๆ นะ, มันใช้เวลาแค่ 30 วินาที!!!",
					"",
					"§7" + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " มีมินิเกมมากมายให้เลือกเล่น",
					"§7เราจะพาคุณไปดู และบอกวิธีเข้าเล่น!"
						}
				));

		//Arcade
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(0, -3, 13), 
				text.locArcade,
				"Arcade",
				"อาร์เคด",
				new String[] 
						{
					"§7This is " + _elem + "Arcade" + _main + " mode.",
					"",
					"§7The server cycles through games continuously,",
					"§7so there is no need to leave after a game ends!",
					"",
					"§7They are fast-paced and fun 16-player games!"
						},
				new String[] 
						{
					"§7นี่คือโหมด " + _elem + "Arcade",
					"",
					"§7เซิร์ฟเวอร์จะสลับเกมไปเรื่อยๆ",
					"§7ดังนั้นไม่จำเป็นต้องออกหลังจบเกม!",
					"",
					"§7มันเป็นเกม 16 คนที่รวดเร็วและสนุก!"
						}
				));

		//Bridges
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(-13, -3, 0), 
				text.locSurvival,
				"Survival Games",
				"เอาชีวิตรอด",
				new String[] 
						{
					"§7This is " + _elem + "Survival" + _main + " mode.",
					"",
					_elem + "The Bridges" + _main + " §7gives you 10 minutes to prepare",
					"§7before the bridges fall and combat begins!",
					"",
					_elem + "Survival Primal Game" + _main + " §7throws you into a deadly arena",
					"§7to survive against 23 other players!"
						},
				new String[] 
						{
					"§7นี่คือโหมด " + _elem + "Survival",
					"",
					_elem + "The Bridges" + _main + " §7คุณมีเวลา 10 นาทีเพื่อเตรียมตัว",
					"§7จากนั้นสะพานจะตกลงมา และการต่อสู้จะเริ่มขึ้น!",
					"",
					_elem + "Survival Primal Game" + _main + " §7พาคุณเข้าสู่สนามอันตราย",
					"§7เพื่อเอาชีวิตรอดจากผู้เล่นอีก 23 คน!"
						}
				));

		//Pig
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(-9, y+73, 53), 
				manager.GetSpawn().add(-11, y+72.5, 57),
				"???",
				"???",
				new String[] 
						{
					"",
					"",
					"§7This is a pig standing on a log."
						},
				new String[] 
						{
					"",
					"",
					"§7นี่คือหมูที่ยืนอยู่บนท่อนไม้"
						}
				));

		//Classics
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(0, -3, -13), 
				text.locClassics,
				"Classics",
				"คลาสสิก",
				new String[] 
						{
					"§7Here you can play " + _elem + "Classics" + _main + " mode.",
					"",
					"§7In " + _elem + "Super Smash Mobs" + _main + " §7you become a monster",
					"§7and fight other players with fun abilities!",
					"",					
					_elem + "Draw My Thing" + _main + " §7is a drawing game where players",
					"§7take turns drawing and guessing words!"
						},
				new String[] 
						{
					"§7ที่นี่ คุณสามารถเล่นโหมด " + _elem + "Classics",
					"",
					"§7ใน " + _elem + "Super Smash Mobs" + _main + " §7คุณจะกลายเป็นมอนสเตอร์",
					"§7และต่อสู้กับผู้เล่นคนอื่นด้วยสกิลสนุกๆ!",
					"",					
					_elem + "Draw My Thing" + _main + " §7เป็นเกมวาดรูปที่ผู้เล่น",
					"§7ผลัดกันวาดและทายคำศัพท์"
						}
				));

		//Comp
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(13, -3, 0), 
				text.locComp,
				"Champions",
				"แชมเปี้ยน",
				new String[] 
						{
					"§7Lastly, here is " + _elem + "Champions" + _main + " mode.",
					"§7This is a highly competitive and skill-based game",
					"",
					"§7Each class can be customized with unlockable skills.",
					"",
					"§7Fight other players across 3 different modes!"
						},
				new String[] 
						{
					"§7สุดท้ายนี่คือโหมด " + _elem + "Champions",
					"§7เป็นเกมที่ต้องใช้ทักษะและการแข่งขันสูงมาก",
					"",
					"§7แต่ละคลาสสามารถปรับแต่งด้วยสกิลที่ปลดล็อกได้",
					"",
					"§7ต่อสู้กับคนอื่นใน 3 โหมดที่แตกต่างกัน!"
						}
				));

		//JOIN
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(0, -3, 19), 
				manager.GetSpawn().add(0, -3.1, 23), 
				"Joining Games",
				"การเข้าเกม",
				new String[] 
						{
					"§7You can join a game in 2 ways.",
					"",
					"§7The easiest way is to walk through the portal.",
					"§7It will automatically connect you to the best server!",
					"",
					"§7Click the game's " + _elem + "NPC" + _main + " to open the " + _elem + "Server Menu",
					"§7Here you can select which server to join yourself!"
						},
				new String[] 
						{
					"§7คุณสามารถเข้าเกมได้ 2 วิธี",
					"",
					"§7วิธีที่ง่ายที่สุดคือเดินทะลุประตูมิติไปเลย",
					"§7มันจะเข้าเซิร์ฟเวอร์ที่ดีที่สุดให้อัตโนมัติ!",
					"",
					"§7คลิกที่ " + _elem + "NPC ของเกม" + _main + " เพื่อเปิด " + _elem + "Server Menu",
					"§7ที่นี่คุณสามารถเลือกเซิร์ฟเวอร์ที่จะเข้าได้เอง!"
						}
				));

		//END
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(0, -2, 0), 
				manager.GetSpawn().add(0, -2.1, 5), 
				"End",
				"จบ",
				new String[] 
						{
					"",
					"§eSimple right?",
					"",
					"§7Thanks for watching! Have fun!",
					"",
						},
				new String[] 
						{
					"",
					"§eง่ายใช่ไหมล่ะ?",
					"",
					"§7ขอบคุณที่รับฟัง! ขอให้สนุกนะ!",
					"",
						}
				));
	}
}
