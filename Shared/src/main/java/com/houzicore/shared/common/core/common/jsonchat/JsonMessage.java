package com.houzicore.shared.common.jsonchat;

import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;

/**
 * JSON Message wrapper for Spigot 1.21.11 using Adventure API
 */
public class JsonMessage
{
	protected StringBuilder Builder;

	public JsonMessage(String text)
	{
		this(new StringBuilder(), text);
	}
	
	public JsonMessage(StringBuilder builder, String text)
	{	
		Builder = builder;		
		Builder.append("{\"text\":\"" + text + "\"");
	}
	
	public JsonMessage color(String color)
	{
		Builder.append(", color:" + color);
		return this;
	}
	
	public JsonMessage bold()
	{
		Builder.append(", bold:true");
		return this;
	}

	public JsonMessage italic()
	{
		Builder.append(", italic:true");
		return this;
	}

	public JsonMessage underlined()
	{
		Builder.append(", underlined:true");
		return this;
	}

	public JsonMessage strikethrough()
	{
		Builder.append(", strikethrough:true");

		return this;
	}

	public JsonMessage obfuscated()
	{
		Builder.append(", obfuscated:true");

		return this;
	}
	
	public ChildJsonMessage extra(String text)
	{
		Builder.append(", \"extra\":[");
		return new ChildJsonMessage(this, Builder, text);
	}
	
	public JsonMessage click(String action, String value)
	{
		Builder.append(", \"clickEvent\":{\"action\":\"" + action + "\",\"value\":\"" + value + "\"}");
		
		return this;
	}

	public JsonMessage hover(String action, String value)
	{
		Builder.append(", \"hoverEvent\":{\"action\":\"" + action + "\",\"value\":\"" + value + "\"}");
		
		return this;
	}

	public JsonMessage click(ClickEvent event, String value)
	{
		return click(event.toString(), value);
	}

	public JsonMessage hover(HoverEvent event, String value)
	{
		return hover(event.toString(), value);
	}

	public JsonMessage color(Color color)
	{
		return color(color.toString());
	}
	
	public String toString()
	{
		Builder.append("}");
		return Builder.toString();
	}
	
	public void sendToPlayer(Player player)
	{
		// Use Adventure API to deserialize JSON and send as component
		try
		{
			Component component = GsonComponentSerializer.gson().deserialize(toString());
			player.sendMessage(component);
		}
		catch (Exception e)
		{
			// Fallback: send as plain legacy text
			player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
				toString().replaceAll("\\{\"text\":\"", "").replaceAll("\"\\}", "")));
		}
	}

	/**
	 * Send a message to players using Adventure API
	 *
	 * @param messageType Message type to send
	 * @param players Players to send to
	 */
	public void send(MessageType messageType, Player... players)
	{
		send(messageType, false, players);
	}

	/**
	 * Send a message to players using Adventure API
	 * Properly handles ActionBar vs Chat messages
	 */
	public void send(MessageType messageType, boolean defaultToChat, Player... players)
	{
		for (Player player : players)
		{
			try
			{
				Component component = GsonComponentSerializer.gson().deserialize(toString());
				
				if (messageType == MessageType.ABOVE_HOTBAR)
				{
					ActionBarService.display(player, ActionBarChannel.GAME_EVENT, component);
				}
				else
				{
					player.sendMessage(component);
				}
			}
			catch (Exception e)
			{
				// Fallback for malformed JSON - send raw text
				if (messageType == MessageType.ABOVE_HOTBAR)
				{
					ActionBarService.display(player, ActionBarChannel.GAME_EVENT, Component.text(toString()));
				}
				else
				{
					player.sendMessage(Component.text(toString()));
				}
			}
		}
	}

	public static enum MessageType
	{
		CHAT_BOX((byte) 0),
		SYSTEM_MESSAGE((byte) 1),
		ABOVE_HOTBAR((byte) 2);

		private byte _id;

		MessageType(byte id)
		{
			_id = id;
		}

		public byte getId()
		{
			return _id;
		}
	}
}
