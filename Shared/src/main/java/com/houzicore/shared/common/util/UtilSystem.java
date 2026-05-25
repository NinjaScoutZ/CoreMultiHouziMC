package com.houzicore.shared.common.util;

public class UtilSystem
{
	public static void printStackTrace()
	{
		for (StackTraceElement trace : Thread.currentThread().getStackTrace())
		{	
		}	
	}
	
	public static void printStackTrace(StackTraceElement[] stackTrace)
	{
		for (StackTraceElement trace : stackTrace)
		{	
		}	
	}
}
