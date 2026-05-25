package com.houzicore.shared.serverdata.commands;


public class AnnouncementCommand extends ServerCommand
{
	private boolean _displayTitle;
	private String _message;
	private long _durationMillis;
	
	public boolean getDisplayTitle() { return _displayTitle; }
	public String getMessage() { return _message; }
	public long getDurationMillis() { return _durationMillis; }
	
	public AnnouncementCommand(boolean displayTitle, String message, long durationMillis)
	{
		_displayTitle = displayTitle;
		_message = message;
		_durationMillis = durationMillis;
	}
	
	@Override
	public void run() 
	{
		// Utilitizes a callback functionality to seperate dependencies
	}
}
