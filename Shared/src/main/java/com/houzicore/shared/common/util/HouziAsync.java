package com.houzicore.shared.common.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Utility for running asynchronous tasks using Java 25 Virtual Threads.
 */
public class HouziAsync
{
	private static final ExecutorService IO_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

	public static <T> CompletableFuture<T> supplyAsync(Supplier<T> task)
	{
		return CompletableFuture.supplyAsync(task, IO_EXECUTOR);
	}

	public static CompletableFuture<Void> runAsync(Runnable task)
	{
		return CompletableFuture.runAsync(task, IO_EXECUTOR);
	}

	public static ExecutorService getExecutor()
	{
		return IO_EXECUTOR;
	}

	public static void shutdown()
	{
		IO_EXECUTOR.shutdown();
	}
}
