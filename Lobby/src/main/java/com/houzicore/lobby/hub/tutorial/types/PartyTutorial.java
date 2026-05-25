package com.houzicore.lobby.hub.tutorial.types;

import org.bukkit.ChatColor;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.tutorial.Tutorial;
import com.houzicore.lobby.hub.tutorial.TutorialPhase;

public class PartyTutorial extends Tutorial
{
	public PartyTutorial(HubManager manager) 
	{
		super(manager, "Party Tutorial", 1000, "Hub_PartyTutorial");

		double y = -manager.GetSpawn().getY();
		
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(84, y+69, 10), 
				manager.GetSpawn().add(81, y+68.5, 10),
				"Parties",
				"ปาร์ตี้",
				new String[] 
						{
					"§7Hi there!",
					"",
					"§7This tutorial will teach you about Parties.",
					"",
					"§7Parties are used to group with other players",
					"§7in order to easily play the same game together."
						},
				new String[]
						{
					"§7สวัสดี!",
					"",
					"§7บทเรียนนี้จะสอนคุณเกี่ยวกับระบบปาร์ตี้",
					"",
					"§7ปาร์ตี้ใช้เพื่อรวมกลุ่มกับผู้เล่นคนอื่น",
					"§7เพื่อให้สามารถเล่นเกมเดียวกันด้วยกันได้อย่างง่ายดาย"
						}
				));
		
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(84, y+69, 9), 
				manager.GetSpawn().add(81, y+68.5, 9),
				"Creating a Party",
				"การสร้างปาร์ตี้",
				new String[] 
						{
					"§7To create a Party with someone;",
					"",
					"§7Type " + F.link("/party <Player>"),
					"",
					"§7This will create a party, and invite them to it!",
					"§7They will receive a notification on how to join."
						},
				new String[] 
						{
					"§7การสร้างปาร์ตี้กับคนอื่น;",
					"",
					"§7พิมพ์ " + F.link("/party <Player>"),
					"",
					"§7วิธีนี้จะสร้างปาร์ตี้และเชิญพวกเขาเข้าร่วม!",
					"§7พวกเขาจะได้รับการแจ้งเตือนเกี่ยวกับวิธีเข้าร่วม"
						}
				));
		
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(84, y+69, 9), 
				manager.GetSpawn().add(81, y+68.5, 9),
				"Inviting and Suggesting Players",
				"การเชิญและแนะนำผู้เล่น",
				new String[] 
						{
					"§7To invite/suggest more players to a Party;",
					"",
					"§7Type " + F.link("/party <Player>"),
					"",
					"§7Invitations last for 60 seconds."
						},
				new String[] 
						{
					"§7เพื่อเชิญหรือแนะนำผู้เล่นเข้าปาร์ตี้เพิ่ม;",
					"",
					"§7พิมพ์ " + F.link("/party <Player>"),
					"",
					"§7คำเชิญมีอายุ 60 วินาที"
						}
				));
		
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(84, y+69, 9), 
				manager.GetSpawn().add(81, y+68.5, 9),
				"Leaving Parties",
				"การออกจากปาร์ตี้",
				new String[] 
						{
					"§7To leave your current Party;",
					"",
					"§7Type " + F.link("/party leave"),
						},
				new String[] 
						{
					"§7การออกจากปาร์ตี้ปัจจุบันของคุณ;",
					"",
					"§7พิมพ์ " + F.link("/party leave"),
						}
				));
		
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(84, y+69, 9), 
				manager.GetSpawn().add(81, y+68.5, 9),
				"Kicking Players from Party",
				"การเตะผู้เล่นออกจากปาร์ตี้",
				new String[] 
						{
					"§7To kick players from your current Party;",
					"",
					"§7Type " + F.link("/party kick <Player>"),
					"",
					"§7Only the Party Leader can do this."
						},
				new String[] 
						{
					"§7การเตะผู้เล่นออกจากปาร์ตี้ปัจจุบันของคุณ;",
					"",
					"§7พิมพ์ " + F.link("/party kick <Player>"),
					"",
					"§7เฉพาะหัวหน้าปาร์ตี้เท่านั้นที่ทำได้"
						}
				));
		
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(84, y+69, 9), 
				manager.GetSpawn().add(81, y+68.5, 9),
				"Joining Games Together",
				"การเข้าเกมด้วยกัน",
				new String[] 
						{
					"§7Only the Party Leader can join games.",
					"",
					"§7The game must have enough slots for",
					"§7all Party Members to fit.",
					"",
					"§7All members will be connected to the game."
						},
				new String[] 
						{
					"§7เฉพาะหัวหน้าปาร์ตี้เท่านั้นที่สามารถเข้าเกมได้",
					"",
					"§7เกมต้องมีจำนวนผู้เล่นที่ว่างเพียงพอ",
					"§7สำหรับสมาชิกปาร์ตี้ทุกคน",
					"",
					"§7สมาชิกทุกคนจะเชื่อมต่อไปยังเกมพร้อมกัน"
						}
				));
		
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(84, y+69, 9), 
				manager.GetSpawn().add(81, y+68.5, 9),
				"Party Chat",
				"แชทปาร์ตี้",
				new String[] 
						{
					"§7To send a message to your Party;",
					"",
					"§7Type " + F.link("@Hey guys, how are you?"),
					"",
					"§7They will see; ",
					C.cDPurple + C.Bold + "Party " + C.cWhite + C.Bold + "YourName " + ChatColor.RESET + C.cPurple + "Hey guys, how are you?"
						},
				new String[] 
						{
					"§7การส่งข้อความไปยังปาร์ตี้ของคุณ;",
					"",
					"§7พิมพ์ " + F.link("@ไง สบายดีไหม?"),
					"",
					"§7พวกเขาจะเห็น: ",
					C.cDPurple + C.Bold + "Party " + C.cWhite + C.Bold + "YourName " + ChatColor.RESET + C.cPurple + "ไง สบายดีไหม?"
						}
				));
		
		_phases.add(new TutorialPhase(
				manager.GetSpawn().add(0, -2, 0), 
				manager.GetSpawn().add(0, -2.1, 5), 
				"End",
				"จบ",
				new String[] 
						{
					"",
					"",
					"§7Thanks for doing the party tutorial!",
					"",
					"",
						},
				new String[] 
						{
					"",
					"",
					"§7ขอบคุณที่รับฟังบทเรียนปาร์ตี้นะ!",
					"",
					"",
						}
				));


	}
}
