package com.houzicore.shared.server.remotecall;

import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.HouziAsync;

public class AsyncJsonWebCall extends JsonWebCall {
	public AsyncJsonWebCall(String url) {
		super(url);
	}

	@Override
	public void Execute() {
		HouziAsync.runAsync(() -> {
			AsyncJsonWebCall.super.Execute();
		});
	}

	@Override
	public <T> void Execute(final Class<T> callbackClass, final Callback<T> callback) {
		HouziAsync.runAsync(() -> {
			AsyncJsonWebCall.super.Execute(callbackClass, callback);
		});
	}

	@Override
	public <T> void Execute(final Class<T> callbackClass, final Callback<T> callback, final Object argument) {
		HouziAsync.runAsync(() -> {
			AsyncJsonWebCall.super.Execute(callbackClass, callback, argument);
		});
	}

	@Override
	public void Execute(final Object argument) {
		HouziAsync.runAsync(() -> {
			AsyncJsonWebCall.super.Execute(argument);
		});
	}
}
