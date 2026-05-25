package com.houzicore.shared.core.shop.confirmation;

public interface ConfirmationCallback {
	void resolve(String message);
	void reject(String message);
}
