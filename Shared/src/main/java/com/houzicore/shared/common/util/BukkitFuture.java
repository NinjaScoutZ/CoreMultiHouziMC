package com.houzicore.shared.common.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilities for interleaving Bukkit scheduler operations as
 * intermediate and terminal operations in a {@link CompletionStage}
 * pipeline.
 */
public class BukkitFuture {
	private static Plugin LOADING_PLUGIN;

	private static Plugin getPlugin() {
		if (LOADING_PLUGIN == null) {
			LOADING_PLUGIN = JavaPlugin.getProvidingPlugin(BukkitFuture.class);
		}
		return LOADING_PLUGIN;
	}

	private static void runBlocking(Runnable action) {
		Bukkit.getScheduler().runTask(getPlugin(), action);
	}

	public static <T> Function<T, CompletionStage<Void>> accept(Consumer<? super T> action) {
		return val -> {
			CompletableFuture<Void> future = new CompletableFuture<>();
			runBlocking(() -> {
				action.accept(val);
				future.complete(null);
			});
			return future;
		};
	}

	public static <T> Function<T, CompletionStage<Void>> run(Runnable action) {
		return val -> {
			CompletableFuture<Void> future = new CompletableFuture<>();
			runBlocking(() -> {
				action.run();
				future.complete(null);
			});
			return future;
		};
	}

	public static <T, U> Function<T, CompletionStage<U>> map(Function<? super T, ? extends U> fn) {
		return val -> {
			CompletableFuture<U> future = new CompletableFuture<>();
			runBlocking(() -> future.complete(fn.apply(val)));
			return future;
		};
	}

	public static <T> BiConsumer<? super T, ? super Throwable> complete(
			BiConsumer<? super T, ? super Throwable> action) {
		return (val, throwable) -> runBlocking(() -> action.accept(val, throwable));
	}

	public static <T> CompletionStage<T> supply(Supplier<T> supplier) {
		CompletableFuture<T> future = new CompletableFuture<>();
		runBlocking(() -> future.complete(supplier.get()));
		return future;
	}
}
